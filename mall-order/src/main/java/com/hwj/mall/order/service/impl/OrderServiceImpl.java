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


    //????????????????????????
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
     * ????????????????????????
     *
     * @return
     */
    @Override
    public OrderConfirmVo confirmVo() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        //?????????????????????
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //????????????
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //??????????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberEntity.getId());
            orderConfirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> orderItemFuture = CompletableFuture.runAsync(() -> {
            //??????????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //?????????
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
        //3???????????????
        orderConfirmVo.setIntegration(memberEntity.getIntegration());
        //todo ????????????
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN + memberEntity.getId(), token, Long.parseLong(orderTokenOut), TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        //????????????????????????
        CompletableFuture.allOf(getAddressFuture, orderItemFuture).get();
        return orderConfirmVo;
    }

    /**
     * ????????????
     *
     * @param vo
     * @return
     * @GlobalTransactional ????????????????????????
     */
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        //???????????????????????????????????????
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        //1???????????????????????????
        //??????????????????????????????????????? ????????????
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long execute = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN +
                        memberEntity.getId()), vo.getOrderToken());
        //?????????????????????
        if (execute == 0L) {
            //??????
            responseVo.setCode(1);
            return responseVo;
        } else {
            //2???????????????
            OrderCreateTo order = this.createOrder(vo, memberEntity.getId());
            //??????
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //3???????????????
                this.saveOrder(order);
                //4.?????????
                WareSkuLockVo lockVo = new WareSkuLockVo();
                //???????????????
                List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSpuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                lockVo.setLocks(collect);
                //?????????
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    //??????
                    responseVo.setOrderEntity(order.getOrder());
//                    int i = 10 / 0;
                    //??????????????????????????????????????????????????????
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return responseVo;
                } else {
                    //5.1 ??????????????????
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }
            } else {
                //??????
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    /**
     * ??????????????????
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
     * ?????????????????????
     *
     * @param order
     */
    @Override
    public void closeOrder(OrderEntity order) {
        //????????????????????????
        OrderEntity orderEntity = baseMapper.selectById(order.getId());
        if (orderEntity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            LambdaUpdateWrapper<OrderEntity> wrapper = new UpdateWrapper<OrderEntity>().lambda()
                    .eq(OrderEntity::getOrderSn, orderEntity.getOrderSn())
                    .set(OrderEntity::getStatus, OrderStatusEnum.CANCLED.getCode());
            baseMapper.update(orderEntity, wrapper);
            log.info("?????????{}????????????????????????", orderEntity.getOrderSn());
            //todo ????????????????????????????????????????????????????????????????????????????????????
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

    /**
     * ??????????????????????????????
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
     * ??????????????????
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        //???????????????????????????????????????
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
     * ??????????????????????????????????????????????????? 0-???1
     *
     * @param payAsyncVo
     */
    @Override
    public void handlerPayResult(PayAsyncVo payAsyncVo) {
        //??????????????????
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

        //??????????????????????????????
        if (trade_status.equals("TRADE_SUCCESS") || trade_status.equals("TRADE_FINISHED")) {
            baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
        }
    }

    /**
     * ??????????????????
     *
     * @param orderTo
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        //1. ????????????
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
        //2. ???????????????
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
     * ????????????????????????????????????????????????
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        //???????????????
        List<OrderItemEntity> orderItems = order.getOrderItems();
        //todo s??????seata?????????????????????
        orderItemService.saveBatch(orderItems);
    }

    /**
     * ????????????
     *
     * @return
     */
    private OrderCreateTo createOrder(OrderSubmitVo vo, Long memberId) {
        OrderCreateTo createTo = new OrderCreateTo();
        //2.1??????????????????
        OrderEntity entity = this.buildOrder(vo);
        entity.setMemberId(memberId);
        //2.2?????????????????????
        List<OrderItemEntity> orderItemEntities = this.buildOrderItems(entity.getOrderSn());
        //2.3?????????
        this.computePrice(entity, orderItemEntities);
        createTo.setOrder(entity);
        createTo.setOrderItems(orderItemEntities);
        return createTo;
    }

    /**
     * ????????????????????????
     *
     * @param orderEntity
     * @param itemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        //????????????
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        //??????
        Integer integrationTotal = 0;
        Integer growthTotal = 0;
        //??????????????????
        for (OrderItemEntity itemEntity : itemEntities) {
            BigDecimal realAmount = itemEntity.getRealAmount();
            total = total.add(realAmount);
            promotion = promotion.add(itemEntity.getPromotionAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            coupon = coupon.add(itemEntity.getCouponAmount());
            integrationTotal += itemEntity.getGiftIntegration();
            growthTotal += itemEntity.getGiftGrowth();
        }
        //????????????
        orderEntity.setTotalAmount(total);
        //????????????
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //????????????=????????????+??????
        orderEntity.setPayAmount(orderEntity.getFreightAmount().add(total));

        //??????????????????(0-????????????1-?????????)
        orderEntity.setDeleteStatus(0);
    }

    /**
     * ??????????????????????????????
     *
     * @param vo
     */
    private OrderEntity buildOrder(OrderSubmitVo vo) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        //2.1????????????
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);
        //2.2?????????????????????
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
     * ???????????????
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
     * ????????????????????????
     *
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //??????spu??????
        Long skuId = item.getSkuId();
        R r = productFeginService.getSpuInfoBySkuId(skuId);
        SpuInfoTo spuInfoEntity = JSON.parseObject(JSON.toJSONString(r.get("spuInfoEntity")), new TypeReference<SpuInfoTo>() {
        });
        orderItemEntity.setSpuId(spuInfoEntity.getId());
        orderItemEntity.setSpuName(spuInfoEntity.getSpuName());
        orderItemEntity.setCategoryId(spuInfoEntity.getCatalogId());
        orderItemEntity.setSpuBrand(spuInfoEntity.getBrandId().toString());

        //??????sku??????
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttrValues(), ";"));
        orderItemEntity.setSkuQuantity(item.getCount());
        //????????????
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        //????????????
        //????????????????????????
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));

        BigDecimal orign = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal subtract = orign
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        //????????????
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }
}