package com.hwj.mall.order.vo;

import com.hwj.mall.order.entity.OrderEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hwj
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity orderEntity;

    @ApiModelProperty("错误码：")
    private Integer code;
}
