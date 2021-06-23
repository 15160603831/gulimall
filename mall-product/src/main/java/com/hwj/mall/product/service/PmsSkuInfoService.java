package com.hwj.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.product.entity.PmsSkuInfoEntity;
import com.hwj.mall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:09
 */
public interface PmsSkuInfoService extends IService<PmsSkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(PmsSkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 查询所有spuid对应的sku信息
     *
     * @param spuId
     * @return
     */
    List<PmsSkuInfoEntity> getSkuBySpuId(Long spuId);

    /**
     * 获取sku基本信息
     *
     * @param skuId sku
     */
    SkuItemVo item(Long skuId);
}

