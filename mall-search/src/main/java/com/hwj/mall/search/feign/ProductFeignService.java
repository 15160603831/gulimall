package com.hwj.mall.search.feign;

import com.hwj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author hwj
 */
@FeignClient("mall-product")
public interface ProductFeignService {

    @GetMapping("/product/pmsattr/info/{attrId}")
    R attrInfo(@PathVariable("attrId") Long attrId);


    @GetMapping("/product/pmsattr/infos")
    R brandInfo(@RequestParam("brandIds") List<Long> brandIds);
}
