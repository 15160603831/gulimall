package com.hwj.mall.seckill.controller;

import com.hwj.common.utils.R;
import com.hwj.mall.seckill.service.SecKillService;
import com.hwj.mall.seckill.to.SeckillSkuRedisTo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
public class SeckillController {


    @Autowired
    private SecKillService secKillService;

    /**
     * 返回当前时间能秒杀的商品
     *
     * @return
     */
    @GetMapping("/getCurrenrSeckillSkus")
    @ApiOperation("返回当前秒杀项")
    public R getCurrenrSeckillSkus() {
        List<SeckillSkuRedisTo> tos = secKillService.getCurrenrSeckillSkus();
        return R.ok().setData(tos);
    }

    @ResponseBody
    @GetMapping(value = "/getSeckillSkuInfo/{skuId}")
    @ApiOperation("根据sku查询秒杀活动")
    public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo to = secKillService.getSeckillSkuInfo(skuId);
        return R.ok().put("SeckillSkuRedisTo", to);
    }

    @GetMapping(value = "/kill")
    @ApiOperation("立即购买")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num, Model model) {

        String orderSn = null;
        orderSn = secKillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);

        return "success";
    }
}
