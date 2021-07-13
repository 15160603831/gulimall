package com.hwj.mall.seckill.service;

import com.hwj.mall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SecKillService {
    void uploadSeckillSkuLatest3Days();

    /**
     * 返回当前时间能秒杀的商品
     *
     * @return
     */
    List<SeckillSkuRedisTo> getCurrenrSeckillSkus();

    /**
     * 查询sku优惠信息
     *
     * @param skuId
     * @return
     */
    SeckillSkuRedisTo getSeckillSkuInfo(Long skuId);

    /**
     * 立即购买
     *
     * @param killId
     * @param key
     * @param num
     * @return
     */
    String kill(String killId, String key, Integer num);


//
//    SeckillSkuRedisTo getSeckillSkuInfo(Long skuId);
//
//    String kill(String killId, String key, Integer num) throws InterruptedException;
}
