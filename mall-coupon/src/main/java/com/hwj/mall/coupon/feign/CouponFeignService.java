package com.hwj.mall.coupon.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hwj
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {
}
