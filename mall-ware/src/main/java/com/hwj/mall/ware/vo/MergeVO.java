package com.hwj.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author hwj
 */
@Data
public class MergeVO {

    private Long purchaseId;

    private List<Long> items;

}
