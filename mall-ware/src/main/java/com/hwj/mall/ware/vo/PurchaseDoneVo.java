package com.hwj.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author hwj
 */
@Data
public class PurchaseDoneVo {
    //采购单id
    @NotNull
    private Long id;

    private List<PurchaseItemDoneVo> items;
}
