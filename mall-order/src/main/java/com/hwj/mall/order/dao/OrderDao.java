package com.hwj.mall.order.dao;

import com.hwj.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hwj.mall.order.enume.OrderStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 *
 * @author hwj
 * @email huangwenjun@gmail.com
 * @date 2021-07-06 10:13:09
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    /**
     * 支付成功修改订单状态
     *
     * @param orderSn
     * @param payed
     */
    void updateOrderStatus(@Param("orderSn") String orderSn, @Param("payed") Integer payed);
}

