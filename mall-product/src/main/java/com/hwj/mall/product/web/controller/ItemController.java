package com.hwj.mall.product.web.controller;

import com.hwj.mall.product.service.PmsSkuInfoService;
import com.hwj.mall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    private PmsSkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId) {
        SkuItemVo vo = skuInfoService.item(skuId);
        System.out.printf("商品信息：", vo);
        return "item";
    }
}
