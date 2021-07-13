package com.hwj.mall.seckill;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@EnableRabbit    不监听消息，只发消息
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients("com.hwj.mall.seckill.feign")
public class MallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallSeckillApplication.class, args);
    }

}
