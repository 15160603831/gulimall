package com.hwj.mall.coupon.controller;

import java.util.Arrays;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import com.hwj.mall.coupon.entity.SmsCouponEntity;
import com.hwj.mall.coupon.service.SmsCouponService;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;


/**
 * 优惠券信息
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:34:45
 */
@RefreshScope
@RestController
@RequestMapping("coupon/coupon")
public class SmsCouponController {
    @Autowired
    private SmsCouponService smsCouponService;

    @Value("${coupon.user.name}")
    private String name;
    @Value("${coupon.user.age}")
    private String age;

    /**
     * 获取配置文件信息
     *
     * @return
     */
    @GetMapping("test")
    public R test() { return R.ok().put("name", name).put("age", age); }

    @RequestMapping("/member/list")
    public R memberCoupon() {
        SmsCouponEntity entity = new SmsCouponEntity();
        entity.setCouponName("满100减100");
        return R.ok().put("coupons", Arrays.asList(entity));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = smsCouponService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SmsCouponEntity smsCoupon = smsCouponService.getById(id);

        return R.ok().put("smsCoupon", smsCoupon);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SmsCouponEntity smsCoupon) {
        smsCouponService.save(smsCoupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SmsCouponEntity smsCoupon) {
        smsCouponService.updateById(smsCoupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        smsCouponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
