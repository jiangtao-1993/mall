package cn.itcast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer //springcloud中引入了开关机制，即便引入的对应的jar包也要手动开启
public class EurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class);
    }
}
