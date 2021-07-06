package com.hwj.mall.order.vo;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提交订单模型
 *
 * @author hwj
 */
@Data
public class OrderSubmitVo {

    @ApiModelProperty("地址id")
    private Long addrId;

    @ApiModelProperty("支付方式")
    private Integer payType;

    @ApiModelProperty("防重令牌")
    private String orderToken;

    @ApiModelProperty("应付价格")
    private BigDecimal payPrice;

}
