package com.hwj.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.product.entity.PmsSpuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * spu图片
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:09
 */
public interface PmsSpuImagesService extends IService<PmsSpuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveImage(Long id, List<String> images);
}

