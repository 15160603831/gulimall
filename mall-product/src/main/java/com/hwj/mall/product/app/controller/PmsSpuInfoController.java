package com.hwj.mall.product.app.controller;

import java.util.Arrays;
import java.util.Map;


import com.hwj.mall.product.vo.SpuSaveVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hwj.mall.product.entity.PmsSpuInfoEntity;
import com.hwj.mall.product.service.PmsSpuInfoService;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;


/**
 * spu信息
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:09
 */
@RestController
@RequestMapping("product/pmsspuinfo")
public class PmsSpuInfoController {
    @Autowired
    private PmsSpuInfoService pmsSpuInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = pmsSpuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        PmsSpuInfoEntity pmsSpuInfo = pmsSpuInfoService.getById(id);

        return R.ok().put("pmsSpuInfo", pmsSpuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SpuSaveVO vo) {
        pmsSpuInfoService.saveInfo(vo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PmsSpuInfoEntity pmsSpuInfo) {
        pmsSpuInfoService.updateById(pmsSpuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        pmsSpuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 商品上架
     */
    @PostMapping("/{spuId}/up")
    @ApiOperation(value = "商品上架")
    public R spuUp(@PathVariable("spuId") Long spuId) {

        pmsSpuInfoService.up(spuId);
        return R.ok();
    }

    @GetMapping("/skuId/{skuId}")
    @ApiOperation("根据skuId查询spu")
    public R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId) {
        PmsSpuInfoEntity spuInfoEntity = pmsSpuInfoService.getSpuInfoBySKuId(skuId);
        return R.ok().put("spuInfoEntity", spuInfoEntity);
    }

}
