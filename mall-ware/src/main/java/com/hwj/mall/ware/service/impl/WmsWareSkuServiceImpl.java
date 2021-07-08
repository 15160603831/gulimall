package com.hwj.mall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hwj.common.exception.NoStockException;
import com.hwj.common.to.mq.OrderTo;
import com.hwj.common.to.mq.StockDetailTo;
import com.hwj.common.to.mq.StockLockedTo;
import com.hwj.common.utils.R;
import com.hwj.mall.ware.entity.WmsWareOrderTaskDetailEntity;
import com.hwj.mall.ware.entity.WmsWareOrderTaskEntity;
import com.hwj.mall.ware.enume.OrderStatusEnum;
import com.hwj.mall.ware.enume.WareTaskStatusEnum;
import com.hwj.mall.ware.feign.OrderFeignService;
import com.hwj.mall.ware.feign.ProductFeignService;
import com.hwj.mall.ware.service.WmsWareOrderTaskDetailService;
import com.hwj.mall.ware.service.WmsWareOrderTaskService;
import com.hwj.mall.ware.vo.OrderItemVo;
import com.hwj.mall.ware.vo.SkuHasStockVO;
import com.hwj.mall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.ware.dao.WmsWareSkuDao;
import com.hwj.mall.ware.entity.WmsWareSkuEntity;
import com.hwj.mall.ware.service.WmsWareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("wmsWareSkuService")
public class WmsWareSkuServiceImpl extends ServiceImpl<WmsWareSkuDao, WmsWareSkuEntity> implements WmsWareSkuService {
    @Autowired
    WmsWareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private WmsWareOrderTaskDetailService wmsWareOrderTaskDetailService;
    @Autowired
    private WmsWareOrderTaskService wmsWareOrderTaskService;
    @Autowired
    private OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WmsWareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<WmsWareSkuEntity> page = this.page(
                new Query<WmsWareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WmsWareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WmsWareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WmsWareSkuEntity skuEntity = new WmsWareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }


            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    /**
     * 查询sku是否有库存
     */
    @Override
    public List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVO> collect = skuIds.stream().map(sku -> {
            SkuHasStockVO vo = new SkuHasStockVO();
            Long count = baseMapper.getSkuStock(sku);
            vo.setHasStock(count == null ? false : count > 0);
            vo.setSkuId(sku);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 锁订单库存
     * (rollbackFor = NoStockException.class)
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        //保存库存工作单的详情
        WmsWareOrderTaskEntity taskEntity = new WmsWareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wmsWareOrderTaskService.save(taskEntity);
        //
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            //skuId
            Long skuId = item.getSkuId();
            skuWareHasStock.setNum(item.getCount());
            skuWareHasStock.setSkuId(skuId);
            ////找出所有库存大于商品数的仓库
            List<Long> wareIds = wareSkuDao.listWareIdHasStock(skuId);
            skuWareHasStock.setWareIds(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        //锁定库存
        for (SkuWareHasStock hasStock : collect) {
            boolean lock = true;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareIds();
            //如果没有满足条件的仓库，抛出异常
            if (wareIds == null && wareIds.size() == 0) {
                throw new NoStockException(skuId);
            } else {
                for (Long wareId : wareIds) {
                    Long count = wareSkuDao.lockWareSku(skuId, hasStock.getNum(), wareId);
                    if (count == 0) {
                        lock = false;
                    } else {
                        ////锁定成功，保存工作单详情
                        WmsWareOrderTaskDetailEntity taskDetailEntity = new WmsWareOrderTaskDetailEntity();
                        taskDetailEntity
                                .setSkuId(skuId)
                                .setSkuNum(hasStock.getNum())
                                .setWareId(wareId)
                                .setTaskId(taskEntity.getId())
                                .setLockStatus(1);
                        wmsWareOrderTaskDetailService.save(taskDetailEntity);
                        //发送库存锁定消息至延迟队列
                        StockLockedTo stockLockedTo = new StockLockedTo();
                        stockLockedTo.setId(taskEntity.getId());
                        StockDetailTo stockDetailTo = new StockDetailTo();
                        BeanUtils.copyProperties(taskDetailEntity, stockDetailTo);
                        stockLockedTo.setDetailTo(stockDetailTo);
                        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                        lock = true;
                        break;
                    }
                }
                if (!lock) {
                    //当前商品所有仓库都没有锁住
                    throw new NoStockException(skuId);
                }
            }
        }
        //锁定成功
        return true;
    }


    /**
     * 1、没有这个订单，必须解锁库存
     * *          *          2、有这个订单，不一定解锁库存
     * *          *              订单状态：已取消：解锁库存
     * *          *                      已支付：不能解锁库存
     * * 消息队列解锁库存
     *
     * @param stockLockedTo
     */
    @Override
    public void unlock(StockLockedTo stockLockedTo) {
        System.out.println("收到解锁库存的消息");
        StockDetailTo detailTo = stockLockedTo.getDetailTo();
        Long detailId = detailTo.getId();
        WmsWareOrderTaskDetailEntity byId = wmsWareOrderTaskDetailService.getById(detailId);
        //1.如果工作单详情不为空，说明该库存锁定成功
        if (byId != null) {
            WmsWareOrderTaskEntity taskEntity = wmsWareOrderTaskService.getById(stockLockedTo.getId());
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.infoByOrderSn(orderSn);
            if (r.getCode() == 0) {
                OrderTo orderTo = JSON.parseObject(JSON.toJSONString(r.get("order")), new TypeReference<OrderTo>() {
                });
                //没有这个订单||订单状态已经取消 解锁库存
                if (orderTo == null || orderTo.getStatus().equals(OrderStatusEnum.CANCLED.getCode())) {
                    //为保证幂等性，只有当工作单详情处于被锁定的情况下才进行解锁
                    if (byId.getLockStatus().equals(WareTaskStatusEnum.Locked.getCode())) {
                        this.unLockStock(detailTo.getSkuId(), detailTo.getSkuNum(), detailTo.getWareId(), taskEntity.getId());
                    }
                }
            } else {
                //消息拒绝重新放入队列让别人继续消费
                throw new RuntimeException("远程调用订单服务失败");
            }
        } else {
            //无需解锁
        }
    }


    /**
     * 解锁库存
     *
     * @param skuId
     * @param skuNum
     * @param wareId
     * @param detailId
     */
    private void unLockStock(Long skuId, Integer skuNum, Long wareId, Long detailId) {
        //数据库中解锁库存数据
        baseMapper.unlockStock(skuId, skuNum, wareId);
        //更新库存工作单的状态
        WmsWareOrderTaskDetailEntity detailEntity = new WmsWareOrderTaskDetailEntity();
        detailEntity.setId(detailId).setLockStatus(2);
        wmsWareOrderTaskDetailService.updateById(detailEntity);
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}