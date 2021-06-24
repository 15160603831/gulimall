package com.hwj.mall.product.vo;

import com.hwj.mall.product.entity.PmsSkuImagesEntity;
import com.hwj.mall.product.entity.PmsSkuInfoEntity;
import com.hwj.mall.product.entity.PmsSpuInfoDescEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author hwj
 */
@Data
@ToString
@ApiModel(value = "sku基本信息")
public class SkuItemVo {

    @ApiModelProperty("sku基本信息")
    private PmsSkuInfoEntity skuInfoEntity;

    @ApiModelProperty("sku图片信息")
    private List<PmsSkuImagesEntity> skuImagesEntities;

    @ApiModelProperty("商品介绍")
    private PmsSpuInfoDescEntity desp;

    @ApiModelProperty("销售属性")
    private List<SkuItemSaleAttrVo> saleAttrs;

    @ApiModelProperty(value = "spu规格参数信息")
    private List<SpuItemAttrGroupVo> groupAttrs;

}
