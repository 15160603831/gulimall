package com.hwj.mall.search.service;

import com.hwj.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author hwj
 */
public interface ProductSaveService {

    /**
     * 商品上架
     *
     * @param skuEsModels
     */
    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
