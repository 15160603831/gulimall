package com.hwj.mall.cart.feign;

import com.hwj.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("mall-product")
public interface ProductFeignService {
    @RequestMapping("/product/pmsskuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/pmsskusaleattrvalue/stringList}")
    List<String> getSkuSaleAttrValue(@RequestParam("skuId") Long skuId);


    @ApiOperation("sku价格")
    @GetMapping("/product/pmsskuinfo/{skuId}/price")
    R getPrice(@PathVariable("skuId") Long skuId);

}
