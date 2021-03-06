package com.hwj.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.product.entity.PmsAttrGroupEntity;
import com.hwj.mall.product.vo.AttrGroupWithAttrsVo;
import com.hwj.mall.product.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:10
 */
public interface PmsAttrGroupService extends IService<PmsAttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long cateLogId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    PmsAttrGroupEntity getById(Long attrGroupId);

    List<SpuItemAttrGroupVo> getAtrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

