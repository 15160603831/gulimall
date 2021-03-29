package com.hwj.mall.member.feign;

import com.hwj.mall.coupon.feign.CouponFeignServer;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hwj
 */
@FeignClient("mall-coupon")
public interface MallCouponFeignServer extends CouponFeignServer {

}
