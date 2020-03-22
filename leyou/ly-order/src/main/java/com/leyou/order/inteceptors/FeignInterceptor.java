package com.leyou.order.inteceptors;

import com.leyou.order.config.JwtProperties;
import com.leyou.order.task.PrivilegeTokenHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class FeignInterceptor implements RequestInterceptor {

    @Autowired
    private PrivilegeTokenHolder tokenHolder;

    @Autowired
    private JwtProperties props;
    @Override
    public void apply(RequestTemplate template) {
        template.header(props.getApp().getHeaderName(),tokenHolder.getToken());
    }
}
