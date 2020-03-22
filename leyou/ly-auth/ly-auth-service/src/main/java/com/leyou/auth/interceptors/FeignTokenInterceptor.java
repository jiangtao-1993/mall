package com.leyou.auth.interceptors;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.task.PrivilegeTokenHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * feign提供的拦截器，可以在请求中添加任何想要添加的内容
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class FeignTokenInterceptor implements RequestInterceptor {

    @Autowired
    private JwtProperties props;

    @Autowired
    private PrivilegeTokenHolder tokenHolder;

    @Override
    public void apply(RequestTemplate template) {
        //给请求头中添加数据,为服务间鉴权准备
        template.header(props.getApp().getHeaderName(),tokenHolder.getToken());
    }
}
