package com.hwj.mall.product.web.controller;

/**
 * @author hwj
 */


import com.hwj.mall.product.entity.PmsCategoryEntity;
import com.hwj.mall.product.service.PmsCategoryService;

import com.hwj.mall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    PmsCategoryService categoryService;

    @GetMapping({"/", "index.html"})
    public String indexPage(Model model) {

        List<PmsCategoryEntity> categorys =categoryService.getLevel1Catagories();
        model.addAttribute("categorys", categorys);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    private Map<String, List<Catalog2Vo>> getCategorylogJson(){
        Map<String, List<Catalog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }
}
