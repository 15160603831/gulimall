package com.hwj.mall.product.service.impl;

import com.hwj.mall.product.dao.PmsBrandDao;
import com.hwj.mall.product.dao.PmsCategoryDao;
import com.hwj.mall.product.entity.PmsBrandEntity;
import com.hwj.mall.product.entity.PmsCategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.product.dao.CategoryBrandRelationDao;
import com.hwj.mall.product.entity.CategoryBrandRelationEntity;
import com.hwj.mall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    PmsBrandDao pmsBrandDao;
    @Autowired
    PmsCategoryDao pmsCategoryDao;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        //查询名称
        PmsBrandEntity pmsBrandEntity = pmsBrandDao.selectById(brandId);
        PmsCategoryEntity pmsCategoryEntity = pmsCategoryDao.selectById(catelogId);

        categoryBrandRelation.setBrandName(pmsBrandEntity.getName());
        categoryBrandRelation.setCatelogName(pmsCategoryEntity.getName());

        this.save(categoryBrandRelation);
    }

}