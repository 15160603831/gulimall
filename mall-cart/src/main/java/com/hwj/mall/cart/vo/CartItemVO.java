package com.hwj.mall.cart.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车内容
 *
 * @author hwj
 */
@Data
public class CartItemVO {
    @ApiModelProperty("skuid")
    private Long skuId;
    @ApiModelProperty("是否选中")
    private Boolean check = true;
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

    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal("" + this.count));
    }
}
