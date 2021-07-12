package com.hwj.mall.seckill.feign;

import com.hwj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author hwj
 */
@FeignClient("mall-product")
public interface ProductFeignService {
    @GetMapping("/product/pmsskuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

}
