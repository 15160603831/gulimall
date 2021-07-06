package com.hwj.mall.ware.feign;

import com.hwj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hwj
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @RequestMapping("/member/member/info/{id}")
    R info(@PathVariable("id") Long id);
}
