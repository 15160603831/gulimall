package com.hwj.mall.product.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ItemController {

    @GetMapping("/{skuId}.html")
    public String skuItem(Long skuId){
        System.out.println("准备查询："+skuId+"详情");
        return "item";
    }
}
