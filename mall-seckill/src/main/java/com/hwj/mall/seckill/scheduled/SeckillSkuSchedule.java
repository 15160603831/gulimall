package com.hwj.mall.seckill.scheduled;

import com.hwj.mall.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
    @Autowired
    private RedissonClient redissonClient;


    private final String upload_lock = "seckill:upload:lock";

    /**
     * 商品定时上架
     */
//    @Scheduled(cron = "0 0 3 * * ？")
    @Scheduled(cron = "0 * * * * ?")
    public void UplocadSecillSkuLates3Days() {
        log.info("商品上架）））））");
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            secKillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }

    }
}
