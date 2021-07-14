package com.hwj.mall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class HelloScheduled {

//    @Scheduled(cron = "0 0 3 * * ?")
//    @Async
//    public void hello() {
//        log.info("hello");
//    }

}
