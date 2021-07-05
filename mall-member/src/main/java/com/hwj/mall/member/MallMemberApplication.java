package com.hwj.mall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

//nacos注册发现
@EnableDiscoveryClient
//开启远程调用feign
@EnableFeignClients(basePackages = "com.hwj.mall.member.feign")
@MapperScan("com.hwj.mall.member.dao")
@SpringBootApplication
public class MallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallMemberApplication.class, args);
    }

}

