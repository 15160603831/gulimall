package com.hwj.mall.ware.feign;

import com.hwj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author hwj
 */
@FeignClient("mall-order")
public interface OrderFeignService {

    @GetMapping("/order/order/infoByOrderSn/{OrderSn}")
    R infoByOrderSn(@PathVariable("OrderSn") String OrderSn);
}
