package com.hwj.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hwj
 */
@Data
public class FareVo {
    private MemberAddressVo address;

    private BigDecimal fare;
}
