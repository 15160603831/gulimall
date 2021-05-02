package com.hwj.mall.coupon.feign;

import com.hwj.common.utils.R;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author hwj
 */
public interface CouponFeignServer {

    @PostMapping("/coupon/smscoupon/member/list")
     R memberCoupon();
}
