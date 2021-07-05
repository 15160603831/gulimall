package com.hwj.mall.order.feign;

import com.hwj.common.to.SkuHasStockVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author hwj
 */
@FeignClient("mall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/has-stock")
    @ApiOperation(value = "查询sku是否有库存")
    List<SkuHasStockVO> getSkuHasStock(@RequestBody List<Long> skuIdList);
}
