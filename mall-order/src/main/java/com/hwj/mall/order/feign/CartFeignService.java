package com.hwj.mall.order.feign;

import com.hwj.mall.order.vo.OrderItemVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("mall-cart")
public interface CartFeignService {


    @ApiOperation("获取用户选中购物项")
    @GetMapping("/currentUserCartItem")
    List<OrderItemVo> currentUserCartItem();
}
