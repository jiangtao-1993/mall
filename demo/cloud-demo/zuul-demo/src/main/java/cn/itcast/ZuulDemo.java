package cn.itcast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy  //开启zuul的代理功能
public class ZuulDemo {
    public static void main(String[] args) {
        SpringApplication.run(ZuulDemo.class);
    }
}
