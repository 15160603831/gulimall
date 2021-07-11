package com.hwj.mall.coupon.controller;

import java.util.Arrays;
import java.util.Map;


import com.hwj.common.to.SkuReductionTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hwj.mall.coupon.entity.SmsSkuFullReductionEntity;
import com.hwj.mall.coupon.service.SmsSkuFullReductionService;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;


/**
 * 商品满减信息
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:34:44
 */
@RestController
@RequestMapping("coupon/skufullreduction")
public class SmsSkuFullReductionController {
    @Autowired
    private SmsSkuFullReductionService smsSkuFullReductionService;

    @PostMapping("/saveInfo")
    public R saveInfo(@RequestBody SkuReductionTo skuReductionTo) {
        smsSkuFullReductionService.saveSkuReduction(skuReductionTo);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = smsSkuFullReductionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SmsSkuFullReductionEntity smsSkuFullReduction = smsSkuFullReductionService.getById(id);

        return R.ok().put("smsSkuFullReduction", smsSkuFullReduction);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SmsSkuFullReductionEntity smsSkuFullReduction) {
        smsSkuFullReductionService.save(smsSkuFullReduction);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SmsSkuFullReductionEntity smsSkuFullReduction) {
        smsSkuFullReductionService.updateById(smsSkuFullReduction);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        smsSkuFullReductionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
