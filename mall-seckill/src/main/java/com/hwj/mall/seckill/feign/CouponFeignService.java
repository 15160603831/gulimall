package com.hwj.mall.seckill.feign;

import com.hwj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author hwj
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/getLates3DaysSession")
    R getLates3DaysSession();
}
