package com.leyou.gateway.filters;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
@Slf4j
public class UserTokenFilter extends ZuulFilter {

    @Autowired
    private JwtProperties props;

    @Autowired
    private FilterProperties filterProps;


    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SERVLET_DETECTION_FILTER_ORDER - 1;
    }

    //这一步相当重要,防止将有些不需要登录就能操作的步骤给过滤掉
    @Override
    public boolean shouldFilter() {//判断过滤器是否生效，返回true，生效，false不生效

        RequestContext currentContext = RequestContext.getCurrentContext();

        HttpServletRequest request = currentContext.getRequest();

        //获取请求的URI
        String requestURI = request.getRequestURI();

        //遍历请求URI，如果当前请求以配置某个白名单为开始，则放行
        for (String allowPath : filterProps.getAllowPaths()) {
            if (requestURI.startsWith(allowPath)) {
                return false;
            }
        }


        return true;
    }

    @Override
    public Object run() throws ZuulException {

        //获取当前请求的资源管理
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();

        //从请求cookie中获取token
        String token = CookieUtils.getCookieValue(request, props.getUser().getCookieName());

        //解析token
        try {
            Payload<UserInfo> infoFromToken = JwtUtils.getInfoFromToken(token, props.getPublicKey(), UserInfo.class);
            log.info("【网关服务】解析用户token成功，此时请求URI为:{}", request.getRequestURI());
        } catch (Exception e) {
            log.error("【网关服务】解析用户token失败，此时请求URI为:{}", request.getRequestURI());
            currentContext.setSendZuulResponse(false);//设置不响应
            currentContext.setResponseStatusCode(401);//未授权不能请求
        }

        return null;
    }
}
