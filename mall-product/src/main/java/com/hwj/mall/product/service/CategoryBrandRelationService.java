package com.hwj.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.product.entity.CategoryBrandRelationEntity;
import com.hwj.mall.product.entity.PmsBrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author hwj
 * @email huangwenjun@gmail.com
 * @date 2021-05-17 10:57:29
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    List<PmsBrandEntity> getBrandsByCatId(Long catId);
}

