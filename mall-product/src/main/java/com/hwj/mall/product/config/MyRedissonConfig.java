package com.hwj.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hwj
 */
@Configuration
public class MyRedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        //使用redis://来启用ssl连接
        config.useSingleServer().setAddress("redis://47.107.108.206:6379");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
