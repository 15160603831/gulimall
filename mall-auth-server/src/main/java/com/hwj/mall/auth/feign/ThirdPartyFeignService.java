package com.hwj.mall.auth.feign;

import com.hwj.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendSms/{phone}/{code}")
    @ApiOperation("短信服务")
    R sendSms(@PathVariable String phone, @PathVariable String code);
}
