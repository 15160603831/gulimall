package com.hwj.mall.search.vo;

import com.hwj.common.to.es.SkuEsModel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author hwj
 */
@Data
public class SearchResult {

    /**
     * 商品信息
     */
    private List<SkuEsModel> products;
    /**
     * 当前页码
     */
    private Integer pageNum;
    /**
     * 总记录数
     */
    private Long total;
    /**
     * 总页码
     */
    private Integer totalPages;
    /**
     * 品牌信息 所有涉及到的品牌
     */
    private List<BrandVO> brands;
    /**
     * 当前查询到的结果，所有涉及到的所有属性
     */
    private List<AttrVo> attrs;

    /**
     * 当前查询到的结果，所有涉及到的所有分类
     */
    private List<CatalogVo> catalogs;


    //============================以上是返回给页面的所有信息==============================================

    /* 面包屑导航数据 */
    private List<NavVo> navs;

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }


    @Data
    @AllArgsConstructor
    public static class BrandVO {
        private Long brandId;

        private String brandName;

        private String brandImg;
    }

    @Data
    @AllArgsConstructor
    public static class AttrVo {

        private Long attrId;

        private String attrName;

        private List<String> attrValue;
    }

    @Data
    @AllArgsConstructor
    public static class CatalogVo {

        private Long catalogId;

        private String catalogName;

        public CatalogVo() {

        }
    }


}
