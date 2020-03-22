package com.leyou.order.inteceptors;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
public class UserTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties props;

    //threadLocal和当前线程相关，线程在使用threadLocal时，会以线程id去绑定，线程id是唯一的
    //当线程消亡时，其内部的局部变量threadLocal，必然要销毁，线程的局部threadLocal，要只有当前线程才能访问
    private ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        //如果请求地址为微信回调，则直接返回true，放行
        if ("/pay/wx/notify".equals(uri)){
            return true;
        }
        //从请求中从cookie中获取token
        try {
            String token = CookieUtils.getCookieValue(request, props.getUser().getCookieName());

            //解析token
            Payload<UserInfo> infoFromToken = JwtUtils.getInfoFromToken(token, props.getPublicKey(), UserInfo.class);

            tl.set(infoFromToken.getInfo());
            log.info("【订单服务】解析用户信息成功，请求地址为:{}",uri);
            return true;
        } catch (Exception e) {
            log.error("【订单服务】解析用户信息失败，请求地址为:{}",uri);
            response.setStatus(401);
            return false;
        }
    }

    public UserInfo getUserInfo() {
        return tl.get();
    }

    //最终执行时，一定要清空threadLocal内容
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }
}
