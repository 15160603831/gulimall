package com.hwj.mall.order.feign;

import com.hwj.mall.order.vo.MemberAddressVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author hwj
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @ApiOperation("获取会员收货地址")
    @GetMapping("/member/umsmemberreceiveaddress/{memberId}/address")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
