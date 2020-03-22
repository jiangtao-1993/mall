package com.leyou.order.task;


import com.leyou.auth.clients.AuthClient;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
public class PrivilegeTokenHolder {

    final long refreshTime = 86400000L;

    @Autowired
    private JwtProperties props;

    @Autowired
    private AuthClient authClient;

    private String token;

    @Scheduled(fixedDelay = refreshTime) //使用定时任务，第一次执行在项目启动时
    public void loadToken() {

        while (true) {
            try {
                //传入当前服务的id和密码，请求auth服务，获取到token然后保存
               this.token =  this.authClient.authorize(props.getApp().getId(), props.getApp().getSecret());
               log.info("【订单服务】获取token成功");
               break;
            } catch (Exception e) {
                log.error("【订单服务】获取token失败");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public String getToken() {
        return token;
    }
}
