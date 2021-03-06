package com.hwj.mall.member.feign;

import com.hwj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hwj
 */
@FeignClient("mall-coupon")
public interface MallCouponFeignServer {

    @RequestMapping("/coupon/smscoupon/member/list")
    R memberCoupon();
}
