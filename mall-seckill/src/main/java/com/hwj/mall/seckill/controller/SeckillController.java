package com.hwj.mall.seckill.controller;

import com.hwj.common.utils.R;
import com.hwj.mall.seckill.service.SecKillService;
import com.hwj.mall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {


    @Autowired
    private SecKillService secKillService;

    /**
     * 返回当前时间能秒杀的商品
     *
     * @return
     */
    @GetMapping("/getCurrenrSeckillSkus")
    public R getCurrenrSeckillSkus() {
        List<SeckillSkuRedisTo> tos = secKillService.getCurrenrSeckillSkus();
        return R.ok().setData(tos);
    }

    @ResponseBody
    @GetMapping(value = "/getSeckillSkuInfo/{skuId}")
    public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo to = secKillService.getSeckillSkuInfo(skuId);
        return R.ok().put("SeckillSkuRedisTo", to);
    }

}
