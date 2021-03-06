package com.hwj.mall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.exception.NoStockException;
import com.hwj.common.to.SkuHasStockVO;
import com.hwj.common.to.mq.OrderTo;
import com.hwj.common.to.mq.SeckillOrderTo;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;
import com.hwj.common.utils.R;
import com.hwj.common.vo.MemberEntity;
import com.hwj.mall.order.constant.OrderConstant;
import com.hwj.mall.order.dao.OrderDao;
import com.hwj.mall.order.dao.OrderItemDao;
import com.hwj.mall.order.entity.OrderEntity;
import com.hwj.mall.order.entity.OrderItemEntity;
import com.hwj.mall.order.entity.PaymentInfoEntity;
import com.hwj.mall.order.enume.OrderStatusEnum;
import com.hwj.mall.order.feign.CartFeignService;
import com.hwj.mall.order.feign.MemberFeignService;
import com.hwj.mall.order.feign.ProductFeginService;
import com.hwj.mall.order.feign.WmsFeignService;
import com.hwj.mall.order.interceptor.LoginUserInterceptor;
import com.hwj.mall.order.service.OrderItemService;
import com.hwj.mall.order.service.OrderService;
import com.hwj.mall.order.service.PaymentInfoService;
import com.hwj.mall.order.to.OrderCreateTo;
import com.hwj.mall.order.to.SpuInfoTo;
import com.hwj.mall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private WmsFeignService wmsFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeginService productFeginService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private PaymentInfoService paymentInfoService;


    //订单令牌过期时间
    @Value("${spring.cache.timeout.order-token}")
    private String orderTokenOut;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单确认页返回项
     *
     * @return
     */
    @Override
    public OrderConfirmVo confirmVo() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        //获取之前的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //会员地址
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //每个线程都需要共享之前的请求
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberEntity.getId());
            orderConfirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> orderItemFuture = CompletableFuture.runAsync(() -> {
            //每个线程都需要共享之前的请求
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //购物车
            List<OrderItemVo> item = cartFeignService.currentUserCartItem();
            orderConfirmVo.setOrderItemVos(item);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> orderItemVos = orderConfirmVo.getOrderItemVos();
            List<Long> collect = orderItemVos.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            List<SkuHasStockVO> skuHasStock = wmsFeignService.getSkuHasStock(collect);
            if (skuHasStock != null) {
                Map<Long, Boolean> map = skuHasStock.stream().collect(Collectors.toMap(SkuHasStockVO::getSkuId, SkuHasStockVO::getHasStock));
                orderConfirmVo.setHasStock(map);
            }

        });
        //3、用户积分
        orderConfirmVo.setIntegration(memberEntity.getIntegration());
        //todo 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN + memberEntity.getId(), token, Long.parseLong(orderTokenOut), TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        //等待所有线程结束
        CompletableFuture.allOf(getAddressFuture, orderItemFuture).get();
        return orderConfirmVo;
    }

    /**
     * 创建订单
     *
     * @param vo
     * @return
     * @GlobalTransactional 不适合高并发场景
     */
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        //拦截器获取缓存中的用户信息
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        //1、验证令牌是否合法
        //验证和删除令牌要保证原子性 使用脚本
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long execute = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN +
                        memberEntity.getId()), vo.getOrderToken());
        //验证令牌合法性
        if (execute == 0L) {
            //失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            //2、创建订单
            OrderCreateTo order = this.createOrder(vo, memberEntity.getId());
            //验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //3、保存订单
                this.saveOrder(order);
                //4.锁库存
                WareSkuLockVo lockVo = new WareSkuLockVo();
                //锁库存数据
                List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSpuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                lockVo.setLocks(collect);
                //锁库存
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    //成功
                    responseVo.setOrderEntity(order.getOrder());
//                    int i = 10 / 0;
                    //发送消息到订单延迟队列，判断过期订单
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return responseVo;
                } else {
                    //5.1 锁定库存失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }
            } else {
                //失败
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    /**
     * 查询订单状态
     *
     * @param orderSn
     * @return
     */
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity orderEntity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().lambda().eq(OrderEntity::getOrderSn, orderSn));
        return orderEntity;
    }

    /**
     * 未付款取消订单
     *
     * @param order
     */
    @Override
    public void closeOrder(OrderEntity order) {
        //查询当前最新状态
        OrderEntity orderEntity = baseMapper.selectById(order.getId());
        if (orderEntity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            LambdaUpdateWrapper<OrderEntity> wrapper = new UpdateWrapper<OrderEntity>().lambda()
                    .eq(OrderEntity::getOrderSn, orderEntity.getOrderSn())
                    .set(OrderEntity::getStatus, OrderStatusEnum.CANCLED.getCode());
            baseMapper.update(orderEntity, wrapper);
            log.info("订单：{}状态更改为已取消", orderEntity.getOrderSn());
            //todo 关单后发送消息通知其他服务进行关单相关的操作，如解锁库存
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

    /**
     * 根据订单号查订单信息
     *
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {

        OrderEntity orderEntity =
                baseMapper.selectOne(new QueryWrapper<OrderEntity>().lambda().eq(OrderEntity::getOrderSn, orderSn));

        List<OrderItemEntity> list = orderItemService.list(new QueryWrapper<OrderItemEntity>().lambda().eq(OrderItemEntity::getOrderSn, orderSn));
        OrderItemEntity orderItemEntity = list.get(0);
        PayVo payVo = new PayVo();
        payVo.setBody(orderItemEntity.getSkuAttrsVals());
        payVo.setSubject(orderItemEntity.getSkuName());
        payVo.setOut_trade_no(orderEntity.getOrderSn());
        BigDecimal bigDecimal = orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(bigDecimal.toString());
        return payVo;
    }

    /**
     * 用户订单列表
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        //拦截器获取缓存中的用户信息
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().lambda()
                        .eq(OrderEntity::getMemberId, memberEntity.getId())
                        .orderByDesc(OrderEntity::getId)
        );
        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> list = orderItemService.list(
                    new QueryWrapper<OrderItemEntity>().lambda().eq(OrderItemEntity::getOrderSn, order.getOrderSn()));
            order.setItemEntityList(list);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(order_sn);

        return new PageUtils(page);
    }

    /**
     * 支付成功保存支付流水，修改订单状态 0-》1
     *
     * @param payAsyncVo
     */
    @Override
    public void handlerPayResult(PayAsyncVo payAsyncVo) {
        //保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        String orderSn = payAsyncVo.getOut_trade_no();
        infoEntity.setOrderSn(orderSn);
        infoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        infoEntity.setSubject(payAsyncVo.getSubject());
        String trade_status = payAsyncVo.getTrade_status();
        infoEntity.setPaymentStatus(trade_status);
        infoEntity.setCreateTime(new Date());
        infoEntity.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(infoEntity);

        //判断交易状态是否成功
        if (trade_status.equals("TRADE_SUCCESS") || trade_status.equals("TRADE_FINISHED")) {
            baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
        }
    }

    /**
     * 保存秒杀订单
     *
     * @param orderTo
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        //1. 创建订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setMemberId(orderTo.getMemberId());
        if (memberEntity != null) {
            orderEntity.setMemberUsername(memberEntity.getUsername());
        }
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setCreateTime(new Date());
        orderEntity.setPayAmount(orderTo.getSeckillPrice().multiply(new BigDecimal(orderTo.getNum())));
        this.save(orderEntity);
        //2. 创建订单项
        R r = productFeginService.info(orderTo.getSkuId());
        if (r.getCode() == 0) {
            SeckillSkuInfoVo skuInfo = JSON.parseObject("pmsSpuInfo", new TypeReference<SeckillSkuInfoVo>() {
            });
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrderSn(orderTo.getOrderSn());
            orderItemEntity.setSpuId(skuInfo.getSpuId());
            orderItemEntity.setCategoryId(skuInfo.getCatalogId());
            orderItemEntity.setSkuId(skuInfo.getSkuId());
            orderItemEntity.setSkuName(skuInfo.getSkuName());
            orderItemEntity.setSkuPic(skuInfo.getSkuDefaultImg());
            orderItemEntity.setSkuPrice(skuInfo.getPrice());
            orderItemEntity.setSkuQuantity(orderTo.getNum());
            orderItemService.save(orderItemEntity);
        }
    }

    /**
     * 构建数据项成功，保存订单到数据库
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        //保存数据项
        List<OrderItemEntity> orderItems = order.getOrderItems();
        //todo s使用seata循环插入会失败
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单
     *
     * @return
     */
    private OrderCreateTo createOrder(OrderSubmitVo vo, Long memberId) {
        OrderCreateTo createTo = new OrderCreateTo();
        //2.1收货地址信息
        OrderEntity entity = this.buildOrder(vo);
        entity.setMemberId(memberId);
        //2.2、所有的订单项
        List<OrderItemEntity> orderItemEntities = this.buildOrderItems(entity.getOrderSn());
        //2.3、验价
        this.computePrice(entity, orderItemEntities);
        createTo.setOrder(entity);
        createTo.setOrderItems(orderItemEntities);
        return createTo;
    }

    /**
     * 创建订单校验价格
     *
     * @param orderEntity
     * @param itemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        //优惠价格
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        //积分
        Integer integrationTotal = 0;
        Integer growthTotal = 0;
        //订单叠加总额
        for (OrderItemEntity itemEntity : itemEntities) {
            BigDecimal realAmount = itemEntity.getRealAmount();
            total = total.add(realAmount);
            promotion = promotion.add(itemEntity.getPromotionAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            coupon = coupon.add(itemEntity.getCouponAmount());
            integrationTotal += itemEntity.getGiftIntegration();
            growthTotal += itemEntity.getGiftGrowth();
        }
        //订单价格
        orderEntity.setTotalAmount(total);
        //应付金额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //付款价格=商品价格+运费
        orderEntity.setPayAmount(orderEntity.getFreightAmount().add(total));

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);
    }

    /**
     * 订单号、收货地址信息
     *
     * @param vo
     */
    private OrderEntity buildOrder(OrderSubmitVo vo) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        //2.1、订单号
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);
        //2.2、收货地址信息
        R r = wmsFeignService.getFare(vo.getAddrId());
        FareVo fareVo = JSON.parseObject(JSON.toJSONString(r.get("fare")), new TypeReference<FareVo>() {
        });
        orderEntity.setFreightAmount(fareVo.getFare());
        orderEntity.setReceiverCity(fareVo.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareVo.getAddress().getName());
        orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());
        return orderEntity;
    }

    /**
     * 构建订单项
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> orderItem = cartFeignService.currentUserCartItem();
        if (orderItem != null && orderItem.size() > 0) {
            List<OrderItemEntity> collect = orderItem.stream().map(item -> {
                OrderItemEntity orderItemEntity = this.buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 构建每一个订单项
     *
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //商品spu信息
        Long skuId = item.getSkuId();
        R r = productFeginService.getSpuInfoBySkuId(skuId);
        SpuInfoTo spuInfoEntity = JSON.parseObject(JSON.toJSONString(r.get("spuInfoEntity")), new TypeReference<SpuInfoTo>() {
        });
        orderItemEntity.setSpuId(spuInfoEntity.getId());
        orderItemEntity.setSpuName(spuInfoEntity.getSpuName());
        orderItemEntity.setCategoryId(spuInfoEntity.getCatalogId());
        orderItemEntity.setSpuBrand(spuInfoEntity.getBrandId().toString());

        //商品sku信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttrValues(), ";"));
        orderItemEntity.setSkuQuantity(item.getCount());
        //积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        //价格信息
        //商品促销分解金额
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));

        BigDecimal orign = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal subtract = orign
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        //实际金额
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }
}