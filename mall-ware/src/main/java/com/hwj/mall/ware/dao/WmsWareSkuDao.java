package com.hwj.mall.ware.dao;

import com.hwj.mall.ware.entity.WmsWareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:50:33
 */
@Mapper
public interface WmsWareSkuDao extends BaseMapper<WmsWareSkuEntity> {
    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(@Param("skuId") Long skuId);


    /**
     * /找出所有库存大于商品数的仓库
     *
     * @param skuId
     * @return
     */
    List<Long> listWareIdHasStock(@Param("skuId") Long skuId);

    /**
     * 锁定库存
     *
     * @param skuId
     * @param num
     * @param wareId
     * @return
     */
    Long lockWareSku(@Param("skuId") Long skuId, @Param("num") Integer num, @Param("wareId") Long wareId);
}
