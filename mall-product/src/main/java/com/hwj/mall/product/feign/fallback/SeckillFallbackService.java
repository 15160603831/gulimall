package com.hwj.mall.product.feign.fallback;


import com.hwj.common.exception.BizCodeEnum;
import com.hwj.common.utils.R;
import com.hwj.mall.product.feign.SeckilFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeckillFallbackService implements SeckilFeignService {
    @Override
    public R getSeckillSkuInfo(Long skuId) {
        log.info("熔断方法调用。。。。getSeckillSkuInfo");
        return R.error(BizCodeEnum.READ_TIME_OUT_EXCEPTION.getCode(), BizCodeEnum.READ_TIME_OUT_EXCEPTION.getMsg());
    }
}