package com.hwj.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 搜索条件
 *
 * @author hwj
 */
@Data
public class SearchParamVO {

    /**
     * 检索全文匹配关键字
     */
    private String keyword;
    /**
     * 三级分类id
     */
    private Long catalog3Id;
    /**
     * 排序条件 sort=price/salecount/hotscore_desc/asc
     */
    private String sort;
    /**
     * 是否有货 （0-无库存 1-有库存）
     */
    private Integer hasStock;
    /**
     * 价格区间 1_500 || 4_700
     */
    private String skuPrice;
    /**
     * 品牌id
     */
    private List<Long> brandId;
    /**
     * 按照属性进行筛选
     */
    private List<String> attrs;
    /**
     * 页码
     */
    private Integer pageNum = 1;

}
