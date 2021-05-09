package com.hwj.mall.gateway.config;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

/**
 * 跨域配置类
 *
 * @author hwj
 */
@Configuration
public class MallCorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.setAllowCredentials(true);

        //注册入域的地址
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }

//    /**
//     * 配置请求重试
//     *
//     */
//    @Bean
//    public Retryer feignRetryer() {
//        return new Retryer.Default(200, SECONDS.toMillis(2), 10);
//    }


    /**
     * 设置请求超时时间
     *默认
     * public Options() {
     * this(10 * 1000, 60 * 1000);
     * }
     *
     */
    @Bean
    Request.Options feignOptions() {
        return new Request.Options(60 * 1000, 60 * 1000);
    }



    /**
     * 打印请求日志
     * @return
     */
    @Bean
    public feign.Logger.Level multipartLoggerLevel() {
        return feign.Logger.Level.FULL;
    }

}
