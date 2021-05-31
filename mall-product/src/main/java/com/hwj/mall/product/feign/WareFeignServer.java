package com.hwj.mall.product.feign;

import com.hwj.common.utils.R;
import com.hwj.common.to.SkuHasStockVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author hwj
 */
@FeignClient(value = "mall-ware")
public interface WareFeignServer {

    /**
     * 查询sku是否有库存
     */
    @PostMapping("/ware/waresku/has-stock")
    @ApiOperation(value = "查询sku是否有库存")
    R<List<SkuHasStockVO>> getSkuHasStock(@RequestBody List<Long> skuIds);
}
