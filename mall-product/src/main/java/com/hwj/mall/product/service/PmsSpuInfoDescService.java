package com.hwj.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.product.entity.PmsSpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:09
 */
public interface PmsSpuInfoDescService extends IService<PmsSpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoDesc(PmsSpuInfoDescEntity descEntity);

}

