package com.hwj.mall.product.vo;

import com.hwj.mall.product.entity.PmsSkuImagesEntity;
import com.hwj.mall.product.entity.PmsSkuInfoEntity;
import com.hwj.mall.product.entity.PmsSpuInfoDescEntity;
import com.hwj.mall.product.entity.PmsSpuInfoEntity;
import lombok.Data;

import java.util.List;

/**
 * @author hwj
 */
@Data
public class SkuItemVo {

    private PmsSkuInfoEntity skuInfoEntity;

    private List<PmsSkuImagesEntity> skuImagesEntities;

    private PmsSpuInfoDescEntity spuInfoDescEntity;

    private static class SkuItemSaleAttrVo {

    }

}
