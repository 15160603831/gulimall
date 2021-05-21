package com.hwj.mall.product.feign;

import com.hwj.common.to.SkuReductionTo;
import com.hwj.common.to.SpuBoundTo;
import com.hwj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hwj
 */
@FeignClient("mall-coupon")
public interface CouponFeignServer {

    @RequestMapping("/coupon/smsspubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/smsskufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
