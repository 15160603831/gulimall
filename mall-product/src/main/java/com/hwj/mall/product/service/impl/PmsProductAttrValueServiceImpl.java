package com.hwj.mall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.product.dao.PmsProductAttrValueDao;
import com.hwj.mall.product.entity.PmsProductAttrValueEntity;
import com.hwj.mall.product.service.PmsProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
@Service("pmsProductAttrValueService")
public class PmsProductAttrValueServiceImpl extends ServiceImpl<PmsProductAttrValueDao, PmsProductAttrValueEntity> implements PmsProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsProductAttrValueEntity> page = this.page(
                new Query<PmsProductAttrValueEntity>().getPage(params),
                new QueryWrapper<PmsProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveProductAttr(List<PmsProductAttrValueEntity> collect) {
        this.saveBatch(collect);
    }

}