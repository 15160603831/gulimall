package com.hwj.mall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.product.dao.PmsSkuImagesDao;
import com.hwj.mall.product.entity.PmsSkuImagesEntity;
import com.hwj.mall.product.service.PmsSkuImagesService;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
@Service("pmsSkuImagesService")
public class PmsSkuImagesServiceImpl extends ServiceImpl<PmsSkuImagesDao, PmsSkuImagesEntity> implements PmsSkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsSkuImagesEntity> page = this.page(
                new Query<PmsSkuImagesEntity>().getPage(params),
                new QueryWrapper<PmsSkuImagesEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询关于sku下的所有图片
     *
     * @param skuId skuId
     */
    @Override
    public List<PmsSkuImagesEntity> getImagesBySkuId(Long skuId) {
        PmsSkuImagesDao imagesDao = this.baseMapper;

        List<PmsSkuImagesEntity> imagesEntities =
                imagesDao.selectList(new QueryWrapper<PmsSkuImagesEntity>().lambda().eq(PmsSkuImagesEntity::getSkuId, skuId));
        return imagesEntities;
    }

}