package com.hwj.mall.order.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hwj
 */
@Data
public class WareSkuLockVo {
    @ApiModelProperty("订单号")
    private String OrderSn;

    @ApiModelProperty("需要锁住的库存信息")
    private List<OrderItemVo> locks;
}
