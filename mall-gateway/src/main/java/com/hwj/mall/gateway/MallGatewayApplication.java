package com.hwj.mall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 开启服务注册发现
 * （配置服务的中心地址
 * @author hwj
 */
//nacos注册发现
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//排除自动配置
public class MallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallGatewayApplication.class, args);
    }

}
