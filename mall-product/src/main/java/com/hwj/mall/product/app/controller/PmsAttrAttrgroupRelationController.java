package com.hwj.mall.product.app.controller;

import java.util.Arrays;
import java.util.Map;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hwj.mall.product.entity.PmsAttrAttrgroupRelationEntity;
import com.hwj.mall.product.service.PmsAttrAttrgroupRelationService;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.R;



/**
 * 属性&属性分组关联
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:10
 */
@RestController
@RequestMapping("product/pmsattrattrgrouprelation")
@Api(tags = "属性&属性分组关联")
public class PmsAttrAttrgroupRelationController {
    @Autowired
    private PmsAttrAttrgroupRelationService pmsAttrAttrgroupRelationService;

    /**
     * 列表
     */
    @PostMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = pmsAttrAttrgroupRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @PostMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		PmsAttrAttrgroupRelationEntity pmsAttrAttrgroupRelation = pmsAttrAttrgroupRelationService.getById(id);

        return R.ok().put("pmsAttrAttrgroupRelation", pmsAttrAttrgroupRelation);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@RequestBody PmsAttrAttrgroupRelationEntity pmsAttrAttrgroupRelation){
		pmsAttrAttrgroupRelationService.save(pmsAttrAttrgroupRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public R update(@RequestBody PmsAttrAttrgroupRelationEntity pmsAttrAttrgroupRelation){
		pmsAttrAttrgroupRelationService.updateById(pmsAttrAttrgroupRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		pmsAttrAttrgroupRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
