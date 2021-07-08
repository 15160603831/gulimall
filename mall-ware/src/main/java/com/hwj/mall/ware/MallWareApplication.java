package com.hwj.mall.ware;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableRabbit   //rabbit消息队列
@EnableDiscoveryClient
@SpringBootApplication(exclude = {GlobalTransactionAutoConfiguration.class})
@EnableTransactionManagement
@MapperScan("com.hwj.mall.ware.dao")
@EnableFeignClients(basePackages = "com.hwj.mall.ware.feign")
public class MallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallWareApplication.class, args);
    }

}
