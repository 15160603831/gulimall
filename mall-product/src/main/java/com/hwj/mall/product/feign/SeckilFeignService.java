package com.hwj.mall.product.feign;

import com.hwj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient("mall-seckill")
public interface SeckilFeignService {


    @ResponseBody
    @GetMapping(value = "/getSeckillSkuInfo/{skuId}")
    R getSeckillSkuInfo(@PathVariable("skuId") Long skuId);
}
