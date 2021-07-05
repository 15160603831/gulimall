package com.hwj.mall.order.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author hwj
 */
@Data
public class OrderItemVo {
    @ApiModelProperty("skuid")
    private Long skuId;
    @ApiModelProperty("商品标题")
    private String title;
    @ApiModelProperty("商品图片")
    private String image;

    /**
     * 商品套餐属性
     */
    @ApiModelProperty("套餐属性")
    private List<String> skuAttrValues;
    @ApiModelProperty("价格")
    private BigDecimal price;
    @ApiModelProperty("数量")
    private Integer count;
    @ApiModelProperty("总价格")
    private BigDecimal totalPrice;

    @ApiModelProperty("重量")
    private BigDecimal weight;
}
