package com.hwj.mall.ware.vo;

import lombok.Data;

/**
 * @author hwj
 */
@Data
public class LockStockResult {

    private Long skuId;
    private Integer num;
    private Boolean locked;
}
