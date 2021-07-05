package com.hwj.mall.ware.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;


import com.hwj.mall.ware.vo.FareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hwj.mall.ware.entity.WmsWareInfoEntity;
import com.hwj.mall.ware.service.WmsWareInfoService;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;


/**
 * 仓库信息
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:50:33
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WmsWareInfoController {
    @Autowired
    private WmsWareInfoService wmsWareInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wmsWareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WmsWareInfoEntity wmsWareInfo = wmsWareInfoService.getById(id);

        return R.ok().put("wmsWareInfo", wmsWareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WmsWareInfoEntity wmsWareInfo) {
        wmsWareInfoService.save(wmsWareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WmsWareInfoEntity wmsWareInfo) {
        wmsWareInfoService.updateById(wmsWareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wmsWareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 删除
     */
    @GetMapping("/fare/{id}")
    public R getFare(@PathVariable("id") Long id) {

        FareVo fare = wmsWareInfoService.getFare(id);
        return R.ok().put("fare", fare);
    }

}
