package com.leyou.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableConfigurationProperties(CORSProperties.class)
public class GlobalCORSConfig {


    @Autowired
    private CORSProperties props;

    @Bean
    public CorsFilter corsFilter() {
        //1.添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        //1) 允许的域,不要写*，否则cookie就无法使用了
        //获取所有允许请求的域名地址,循环，
        props.getAllowedOrigins().forEach(config::addAllowedOrigin);



        //2) 是否发送Cookie信息
        config.setAllowCredentials(props.getAllowCredentials());
        //3) 允许的请求方式
        //隐式传参
        props.getAllowedMethods().forEach(config::addAllowedMethod);



        // 4）允许的头信息

        props.getAllowedHeaders().forEach(config::addAllowedHeader);

        //给预检添加有效周期
        config.setMaxAge(props.getMaxAge());

        //2.添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration(props.getFilterPath(), config);

        //3.返回新的CORSFilter.
        return new CorsFilter(configSource);
    }
}