package com.hwj.mall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwj.common.utils.PageUtils;
import com.hwj.common.utils.Query;

import com.hwj.mall.product.dao.PmsSpuImagesDao;
import com.hwj.mall.product.entity.PmsSpuImagesEntity;
import com.hwj.mall.product.service.PmsSpuImagesService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Transactional(rollbackFor = Exception.class)
@Service("pmsSpuImagesService")
public class PmsSpuImagesServiceImpl extends ServiceImpl<PmsSpuImagesDao, PmsSpuImagesEntity> implements PmsSpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsSpuImagesEntity> page = this.page(
                new Query<PmsSpuImagesEntity>().getPage(params),
                new QueryWrapper<PmsSpuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveImage(Long id, List<String> images) {
        if (CollectionUtils.isEmpty(images)) {

        } else {
            List<PmsSpuImagesEntity> collect = images.stream().map(img -> {
                PmsSpuImagesEntity pmsSpuImagesEntity = new PmsSpuImagesEntity();
                pmsSpuImagesEntity.setSpuId(id);
                pmsSpuImagesEntity.setImgUrl(img);
                return pmsSpuImagesEntity;
            }).collect(Collectors.toList());
            this.saveBatch(collect);
        }
    }

}