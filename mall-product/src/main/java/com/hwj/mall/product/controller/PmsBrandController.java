package com.hwj.mall.product.controller;

import java.util.Arrays;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.hwj.mall.product.entity.PmsBrandEntity;
import com.hwj.mall.product.service.PmsBrandService;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:10
 */
@RestController
@RequestMapping("/product/pmsbrand")
public class PmsBrandController {
    @Autowired
    private PmsBrandService pmsBrandService;

    /**
     * 列表
     */
    @PostMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = pmsBrandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		PmsBrandEntity pmsBrand = pmsBrandService.getById(brandId);

        return R.ok().put("data", pmsBrand);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@Valid @RequestBody PmsBrandEntity pmsBrand){
		pmsBrandService.save(pmsBrand);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public R update(@RequestBody PmsBrandEntity pmsBrand){
		pmsBrandService.updateById(pmsBrand);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		pmsBrandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
