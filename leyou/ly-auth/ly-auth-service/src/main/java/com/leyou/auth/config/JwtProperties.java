package com.leyou.auth.config;

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
    /**
     * 私钥地址
     */
    private String priKeyPath;



    private PublicKey publicKey;
    private PrivateKey privateKey;

    /**
     * 用户token相关属性
     */
    private UserTokenProperties user = new UserTokenProperties();
    @Data
    public class UserTokenProperties {
        /**
         * token过期时长
         */
        private int expire;
        /**
         * 存放token的cookie名称
         */
        private String cookieName;
        /**
         * 存放token的cookie的domain
         */
        private String cookieDomain;

        private Integer cookieMaxAge;
    }


    /**
     * 用户token相关属性
     */
    private AppTokenProperties app = new AppTokenProperties();
    @Data
    public class AppTokenProperties {
        /**
         * token过期时长
         */
        private int expire;

        private Long id;

        private String secret;

        private String headerName;

    }

    //afterPropertiesSet等到所有的属性全部注入了值以后
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}