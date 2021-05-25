package com.hwj.mall.ware.controller;

import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;
import com.hwj.mall.ware.entity.WmsPurchaseEntity;
import com.hwj.mall.ware.service.WmsPurchaseService;
import com.hwj.mall.ware.vo.MergeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 采购信息
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:50:33
 */
@RestController
@RequestMapping("ware/purchase")
public class WmsPurchaseController {
    @Autowired
    private WmsPurchaseService wmsPurchaseService;


    /**
     * 领取采购单
     */
    @RequestMapping("/received")
    public R received(@RequestBody List<Long> ids) {
        wmsPurchaseService.received(ids);

        return R.ok();
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wmsPurchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WmsPurchaseEntity wmsPurchase = wmsPurchaseService.getById(id);

        return R.ok().put("wmsPurchase", wmsPurchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WmsPurchaseEntity wmsPurchase) {
        wmsPurchase.setCreateTime(new Date());
        wmsPurchase.setUpdateTime(new Date());
        wmsPurchaseService.save(wmsPurchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WmsPurchaseEntity wmsPurchase) {
        wmsPurchaseService.updateById(wmsPurchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wmsPurchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 查询未领取的采购单
     *
     * @return
     */
    @GetMapping("/unreceive/list")
    public R unreceivedList(@RequestParam Map<String, Object> params) {
        PageUtils page = wmsPurchaseService.queryPageUnreceived(params);

        return R.ok().put("page", page);
    }

    /**
     * 合并采购单
     *
     * @return
     */
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVO mergeVO) {
        wmsPurchaseService.mergePurchase(mergeVO);

        return R.ok();
    }
}
