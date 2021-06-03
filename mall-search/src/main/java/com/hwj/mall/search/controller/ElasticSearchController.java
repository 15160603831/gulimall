package com.hwj.mall.search.controller;

import com.hwj.common.exception.BizCodeEnum;
import com.hwj.common.to.es.SkuEsModel;
import com.hwj.common.utils.R;
import com.hwj.mall.search.service.ProductSaveService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author hwj
 */
@RestController
@RequestMapping(value = "/search")
@Slf4j
public class ElasticSearchController {

    @Autowired
    private ProductSaveService productSaveService;

    //
    @PostMapping("/product-status-up/save")
    @ApiOperation(value = "商品上架")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("es商品上架错误:{}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (b) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }

    }

}
