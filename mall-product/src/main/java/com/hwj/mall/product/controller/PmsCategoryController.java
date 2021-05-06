package com.hwj.mall.product.controller;

import java.util.Arrays;
import java.util.List;


import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hwj.mall.product.entity.PmsCategoryEntity;
import com.hwj.mall.product.service.PmsCategoryService;
import com.hwj.common.utils.R;


/**
 * 商品三级分类
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:10
 */
@RestController
@RequestMapping("product/pmscategory")
public class PmsCategoryController {
    @Autowired
    private PmsCategoryService pmsCategoryService;

    /**
     * 查出所有分类以及子分类，以树形结构组装
     */
    @ApiOperation(value = " 查出所有分类以及子分类，以树形结构展示")
    @GetMapping("/list/tree")
    public R list() {
        List<PmsCategoryEntity> entityList = pmsCategoryService.listWithTree();
        return R.ok().put("data", entityList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId) {
        PmsCategoryEntity pmsCategory = pmsCategoryService.getById(catId);

        return R.ok().put("pmsCategory", pmsCategory);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PmsCategoryEntity pmsCategory) {
        pmsCategoryService.save(pmsCategory);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PmsCategoryEntity pmsCategory) {
        pmsCategoryService.updateById(pmsCategory);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] catIds) {
//        pmsCategoryService.removeByIds(Arrays.asList(catIds));

        pmsCategoryService.removeMenuByIds(Arrays.asList(catIds));


        return R.ok();
    }

}
