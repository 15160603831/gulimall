package com.hwj.mall.order.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author hwj
 */

public class OrderConfirmVo {

    @Setter
    @Getter
    @ApiModelProperty("用户收货地址列表")
    private List<MemberAddressVo> address;

    @Setter
    @Getter
    @ApiModelProperty("选中购物项")
    List<OrderItemVo> orderItemVos;

    @Setter
    @Getter
    @ApiModelProperty("用户积分")
    private Integer integration;

    @ApiModelProperty("订单防重令牌")
    private String orderToken;

    @ApiModelProperty("货状态")
    @Getter
    @Setter
    private Map<Long, Boolean> hasStock;


    @ApiModelProperty("订单总额")
    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (this.orderItemVos != null) {
            for (OrderItemVo itemVo : orderItemVos) {
                BigDecimal multiply = itemVo.getPrice().multiply(new BigDecimal(itemVo.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    @ApiModelProperty("应付价格")
    public BigDecimal getPayPrice() {
        return this.getTotal();
    }

    public Integer getCount() {
        Integer i = 0;
        if (this.orderItemVos != null) {
            for (OrderItemVo itemVo : orderItemVos) {
                i += itemVo.getCount();
            }
        }
        return i;
    }


}
