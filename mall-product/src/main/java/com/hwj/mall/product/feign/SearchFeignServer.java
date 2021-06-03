package com.hwj.mall.product.feign;

import com.hwj.common.to.es.SkuEsModel;
import com.hwj.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author hwj
 */
@FeignClient(value = "mall-search")
public interface SearchFeignServer {

    @PostMapping("/search/product-status-up/save")
    @ApiOperation(value = "商品上架")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
