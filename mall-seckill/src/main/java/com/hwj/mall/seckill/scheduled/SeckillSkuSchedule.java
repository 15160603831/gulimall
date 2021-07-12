package com.hwj.mall.seckill.scheduled;

import com.hwj.mall.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 商品定时上架
 *
 * @author hwj
 */
@Service
@Slf4j
public class SeckillSkuSchedule {


    @Autowired
    SecKillService secKillService;

    /**
     * 商品定时上架
     */
//    @Scheduled(cron = "0 0 3 * * ？")
    @Scheduled(cron = "0 * * * * ?")
    public void UplocadSecillSkuLates3Days() {
        log.info("商品上架）））））");
        secKillService.uploadSeckillSkuLatest3Days();
    }
}
