package com.hwj.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.hwj.mall.product.entity.PmsProductAttrValueEntity;
import com.hwj.mall.product.service.PmsAttrAttrgroupRelationService;
import com.hwj.mall.product.service.PmsAttrGroupService;
import com.hwj.mall.product.service.PmsCategoryService;
import com.hwj.mall.product.service.PmsProductAttrValueService;
import com.hwj.mall.product.vo.AttrGroupRelationVo;
import com.hwj.mall.product.vo.AttrGroupWithAttrsVo;
import com.hwj.mall.product.vo.AttrResVO;
import com.hwj.mall.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hwj.mall.product.entity.PmsAttrEntity;
import com.hwj.mall.product.service.PmsAttrService;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;


/**
 * 商品属性
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:10
 */
@RestController
@RequestMapping("/product/pmsattr")
public class PmsAttrController {
    @Autowired
    private PmsAttrService pmsAttrService;
    @Autowired
    private PmsProductAttrValueService productAttrValueService;


    /**
     * 列表
     */
    @RequestMapping("/base/listforspu/{spuId}")
    public R baselistforspu(@PathVariable("spuId") Long spuId) {

        List<PmsProductAttrValueEntity> productAttrValueEntityList = productAttrValueService.baseAttrlistForspu(spuId);

        return R.ok().put("data", productAttrValueEntityList);
    }

    /**
     * 列表
     */
    @RequestMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String type
    ) {
        PageUtils page = pmsAttrService.queryBaseAttrPage(params, catelogId, type);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = pmsAttrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
//        PmsAttrEntity pmsAttr = pmsAttrService.getById(attrId);
        AttrResVO attrResVO = pmsAttrService.getAttrInfo(attrId);
        return R.ok().put("attr", attrResVO);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVO pmsAttr) {
        pmsAttrService.saveAttr(pmsAttr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVO attrVO) {
        pmsAttrService.updateByAttr(attrVO);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        pmsAttrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<PmsProductAttrValueEntity> entities) {
        productAttrValueService.updateSpuAttr(spuId,entities);

        return R.ok();
    }


}
