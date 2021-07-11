package com.hwj.mall.coupon.controller;

import java.util.Arrays;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hwj.mall.coupon.entity.SmsSkuBoundsEntity;
import com.hwj.mall.coupon.service.SmsSkuBoundsService;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;



/**
 * 商品sku积分设置
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:34:44
 */
@RestController
@RequestMapping("coupon/skubounds")
public class SmsSkuBoundsController {
    @Autowired
    private SmsSkuBoundsService smsSkuBoundsService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = smsSkuBoundsService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SmsSkuBoundsEntity smsSkuBounds = smsSkuBoundsService.getById(id);

        return R.ok().put("smsSkuBounds", smsSkuBounds);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SmsSkuBoundsEntity smsSkuBounds){
		smsSkuBoundsService.save(smsSkuBounds);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SmsSkuBoundsEntity smsSkuBounds){
		smsSkuBoundsService.updateById(smsSkuBounds);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		smsSkuBoundsService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
