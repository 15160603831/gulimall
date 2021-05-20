package com.hwj.mall.product.service.impl;

import com.hwj.mall.product.dao.PmsBrandDao;
import com.hwj.mall.product.dao.PmsCategoryDao;
import com.hwj.mall.product.entity.PmsBrandEntity;
import com.hwj.mall.product.entity.PmsCategoryEntity;
import com.hwj.mall.product.service.PmsBrandService;
import com.hwj.mall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    CategoryBrandRelationDao relationDao;

    @Autowired
    PmsBrandService brandService;


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

    @Override
    public List<PmsBrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> catelogId = relationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<PmsBrandEntity> collect = catelogId.stream().map(item -> {
            Long brandId = item.getBrandId();
            PmsBrandEntity byId = brandService.getById(brandId);
            return byId;
        }).collect(Collectors.toList());
        return collect;
    }

}