package com.hwj.mall.order.feign;

import com.hwj.common.to.SkuHasStockVO;
import com.hwj.common.utils.R;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/ware/wareinfo/fare/{id}")
    @ApiModelProperty("计算运费")
    R getFare(@PathVariable("id") Long id);
}
