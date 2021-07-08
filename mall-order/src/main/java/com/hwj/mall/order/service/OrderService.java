package com.hwj.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.order.entity.OrderEntity;
import com.hwj.mall.order.vo.OrderConfirmVo;
import com.hwj.mall.order.vo.OrderSubmitVo;
import com.hwj.mall.order.vo.PayVo;
import com.hwj.mall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author hwj
 * @email huangwenjun@gmail.com
 * @date 2021-07-06 10:13:09
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页返回项
     *
     * @return
     */
    OrderConfirmVo confirmVo() throws ExecutionException, InterruptedException;

    /**
     * 创建订单
     *
     * @param orderSubmitVo
     * @return
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo);

    /**
     * 查询订单状态
     *
     * @param orderSn
     * @return
     */
    OrderEntity getOrderByOrderSn(String orderSn);


    /**
     * 未付款取消订单
     *
     * @param orderEntity
     */
    void closeOrder(OrderEntity orderEntity);

    /**
     * 根据订单号查订单信息
     *
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);
}

