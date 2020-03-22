package com.leyou.gateway.filters;

import com.leyou.gateway.config.JwtProperties;
import com.leyou.gateway.task.PrivilegeTokenHolder;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
//其他微服务并不调用网关,但是它要将请求路由到其他微服务,所以所有从网关发出的请求都需要加上权限Token在头上
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class TokenFilter extends ZuulFilter {

    @Autowired
    private JwtProperties props;

    @Autowired
    private PrivilegeTokenHolder privilegeTokenHolder;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        //获取当前请求的资源管理器
        RequestContext currentContext = RequestContext.getCurrentContext();
        //把token存入请求头中
        currentContext.addZuulRequestHeader(props.getApp().getHeaderName(),privilegeTokenHolder.getToken());
        return null;
    }
}
