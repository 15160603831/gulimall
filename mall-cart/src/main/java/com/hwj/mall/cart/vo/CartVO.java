package com.hwj.mall.cart.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 *
 * @author hwj
 */
public class CartVO {
    List<CartItemVO> items;
    @ApiModelProperty("商品数量")
    private Integer countNum;
    @ApiModelProperty("商品类型数量")
    private Integer countType;
    @ApiModelProperty("商品总价")
    private BigDecimal totalAmount;
    @ApiModelProperty("减免价格")
    private BigDecimal reduce = new BigDecimal("0");

    public List<CartItemVO> getItems() {
        return items;
    }

    public void setItems(List<CartItemVO> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (this.items != null && this.items.size() > 0) {
            for (CartItemVO item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public void setCountNum(Integer count) {
        this.countNum = count;
    }

    public Integer getCountType() {
        int count = 0;
        if (this.items != null && this.items.size() > 0) {
            for (CartItemVO item : items) {
                count += 1;
            }
        }
        return count;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        //计算购物项总价格
        if (this.items != null && this.items.size() > 0) {
            for (CartItemVO item : items) {
                BigDecimal totalPrice = item.getTotalPrice();
                amount = amount.add(totalPrice);
            }
        }
        //减去优惠价格
        amount.subtract(reduce);
        return amount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
