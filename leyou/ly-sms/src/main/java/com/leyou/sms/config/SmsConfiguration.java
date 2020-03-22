package com.leyou.sms.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsConfiguration {
    //开启后的属性类不需要@Autowired注入,如果方法参数不是属性类,而是其他Bean的话,要在方法上加上注解@Autowired
    @Bean
    public IAcsClient acsClient(SmsProperties prop){
        DefaultProfile profile = DefaultProfile.getProfile(
                prop.getRegionID(), prop.getAccessKeyID(), prop.getAccessKeySecret());
        return new DefaultAcsClient(profile);
    }
}