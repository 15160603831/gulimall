package com.hwj.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwj.common.utils.PageUtils;
import com.hwj.mall.ware.entity.WmsWareSkuEntity;
import com.hwj.mall.ware.vo.LockStockResult;
import com.hwj.mall.ware.vo.SkuHasStockVO;
import com.hwj.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author hwj
 * @email huangwenjun@mail.com
 * @date 2021-03-23 17:50:33
 */
public interface WmsWareSkuService extends IService<WmsWareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    /**
     * 查询sku是否有库存
     */
    List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds);

    /**
     * 锁库存
     *
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo vo);
}

