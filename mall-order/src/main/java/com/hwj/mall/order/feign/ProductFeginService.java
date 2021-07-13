package com.hwj.mall.order.feign;

import com.hwj.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hwj
 */
@FeignClient("mall-product")
public interface ProductFeginService {

    /**
     * @param skuId
     * @return
     */
    @GetMapping("/product/pmsspuinfo/skuId/{skuId}")
    @ApiOperation("根据skuId查询spu")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);

    /**
     * sku信息
     */
    @RequestMapping("/product/pmsskuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

}
