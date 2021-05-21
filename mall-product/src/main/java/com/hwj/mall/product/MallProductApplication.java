package com.hwj.mall.product;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 整合mybatis-plus
 * 1.导入依赖
 * 2.配置数据源
 *  1.数据库驱动
 *  2.
 */
@MapperScan("com.hwj.mall.product.dao")
@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = "com.hwj.mall.product.feign")
public class MallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallProductApplication.class, args);
    }

}
