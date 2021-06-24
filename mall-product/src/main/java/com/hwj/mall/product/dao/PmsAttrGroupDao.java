package com.hwj.mall.product.dao;

import com.hwj.mall.product.entity.PmsAttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hwj.mall.product.vo.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 属性分组
 * 
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:10
 */
@Mapper
public interface PmsAttrGroupDao extends BaseMapper<PmsAttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAtrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}
