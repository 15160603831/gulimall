package com.hwj.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.product.entity.PmsSpuInfoDescEntity;
import com.hwj.mall.product.entity.PmsSpuInfoEntity;
import com.hwj.mall.product.vo.SpuSaveVO;

import java.util.Map;

/**
 * spu信息
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:29:09
 */
public interface PmsSpuInfoService extends IService<PmsSpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveInfo(SpuSaveVO spuSaveVO);

    void saveBaseSpuInfo(PmsSpuInfoEntity pmsSpuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 商品上架
     *
     * @param spuId
     */
    void up(Long spuId);

    /**
     * 根据skuId查询
     *
     * @param skuId
     * @return
     */
    PmsSpuInfoEntity getSpuInfoBySKuId(Long skuId);
}

