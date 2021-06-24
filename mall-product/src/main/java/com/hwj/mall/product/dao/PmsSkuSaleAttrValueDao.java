package com.hwj.mall.product.dao;

import com.hwj.mall.product.entity.PmsSkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hwj.mall.product.vo.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:09
 */
@Mapper
public interface PmsSkuSaleAttrValueDao extends BaseMapper<PmsSkuSaleAttrValueEntity> {

    /**
     * 获取spu下所有销售属性
     *
     * @param spuId
     * @return
     */
    List<SkuItemSaleAttrVo> getSaleAttrBySpuId(@Param("spuId") Long spuId);
}
