package com.leyou.user.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    /**
     * 公钥地址
     */
    private String pubKeyPath;

    private PublicKey publicKey;


    /**
     * 用户token相关属性
     */
    private AppTokenProperties app = new AppTokenProperties();
    @Data
    public class AppTokenProperties {

        private Long id;
        /**
         * 请求头中的key信息
         */
        private String headerName;

    }

    //afterPropertiesSet等到所有的属性全部注入了值以后
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("【用户服务】初始化公钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}