package com.hwj.mall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author hwj
 */
@Configuration
@EnableAsync  //异步
@EnableScheduling //定时调度
public class ScheduledConfig {
}
