package cn.itcast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrix
@EnableFeignClients //开启feign支持
public class UserConsumer {
    public static void main(String[] args) {
        SpringApplication.run(UserConsumer.class);
    }

    @Bean
    @LoadBalanced //加入注解LoadBalanced后，开启了ribbon负载均衡的支持，restTemplate内部多了一个打工仔ribbon
    public RestTemplate restTemplate(){//必须先获取host，port，
        return new RestTemplate();
    }
}
