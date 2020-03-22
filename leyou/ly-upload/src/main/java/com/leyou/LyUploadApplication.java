package com.leyou;

import com.leyou.upload.config.OSSProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class LyUploadApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyUploadApplication.class);
    }
}
