package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.ApplicationInfo;
import com.leyou.auth.mapper.AppMapper;
import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.clients.UserClient;
import com.leyou.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties props;

    public void login(String username, String password, HttpServletResponse response) {

        //跨服务获取用户信息
        UserDTO userDTO = null;
        try {
            userDTO = userClient.queryUserByUsernameAndPassword(username, password);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.USER_SERVICE_ERROR);
        }

        UserInfo userInfo = BeanHelper.copyProperties(userDTO, UserInfo.class);

        userInfo.setRole("假role");//后续可以添加用户的会员等级或者角色

        //注意这里使用了私钥加密生成token,以后从请求中获取token则必须使用公钥解密
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, props.getPrivateKey(), props.getUser().getExpire());

        CookieUtils.newBuilder()
                .name(props.getUser().getCookieName())
                .value(token)
                .domain(props.getUser().getCookieDomain())
                .path("/")
                .httpOnly(true)
                .maxAge(props.getUser().getCookieMaxAge())
                .response(response)
                .build();
    }

    /**
     * 不仅仅校验用户登录与否，如果下一次校验成功，此时应该重新生成token，cookie
     *
     * @param request
     * @param response
     * @return
     */
    public UserInfo verify(HttpServletRequest request, HttpServletResponse response) {
        //

        String token = CookieUtils.getCookieValue(request, props.getUser().getCookieName());

        Payload<UserInfo> infoFromToken = null;
        try {
            infoFromToken = JwtUtils.getInfoFromToken(token, props.getPublicKey(), UserInfo.class);

            //判定解析的载荷的信息，的id是否在redis，如果在，则直接报错(校验Token是否已经)
            if (redisTemplate.hasKey(infoFromToken.getId())) {
                //当发现token是假的，应该删除

                deleteCookie(response);
                throw new LyException(ExceptionEnum.INVALID_TOKEN_COOKIE);
            }

            //过期时间-29min如果比当前早，说明生成时间超过了1min
            if (new DateTime(infoFromToken.getExpiration()).minusMinutes(props.getUser().getExpire() - 1).isBeforeNow()) {
                //成功了应该重新生成token和cookie
                String newToken = JwtUtils.generateTokenExpireInMinutes(infoFromToken.getInfo(), props.getPrivateKey(), props.getUser().getExpire());

                CookieUtils.newBuilder()
                        .name(props.getUser().getCookieName())
                        .value(newToken)
                        .domain(props.getUser().getCookieDomain())
                        .path("/")
                        .httpOnly(true)
                        .maxAge(props.getUser().getCookieMaxAge())
                        .response(response)
                        .build();
            }

            log.info("【授权中心】解析用户的token成功");


        } catch (Exception e) {
            log.error("【授权中心】解析用户的token失败");
            throw new LyException(ExceptionEnum.INVALID_TOKEN_COOKIE);
        }


        return infoFromToken.getInfo();
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 退出登录时要把cliamsId存入redis，做成类似于黑名单
     *
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        String token = CookieUtils.getCookieValue(request, "LY_TOKEN");


        //获取所有的载荷信息
        Payload<UserInfo> infoFromToken = JwtUtils.getInfoFromToken(token, props.getPublicKey(), UserInfo.class);

        //获取过期时间
        Date expiration = infoFromToken.getExpiration();

        //当过期时间超过3s，则存入redis
        if (!new DateTime(expiration).minusSeconds(3).isBeforeNow()) {
            //如果过期时间还长，超过了3s，则把此tokenId存入redis，存储时长就是剩余过期时间
            redisTemplate.opsForValue().set(infoFromToken.getId(), "", expiration.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
        deleteCookie(response);
    }

    private void deleteCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("LY_TOKEN", "");
        cookie.setDomain("leyou.com");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public String authenticate(Long id, String secret) {

        //先根据服务id查询服务信息
        ApplicationInfo applicationInfo = this.appMapper.selectByPrimaryKey(id);

        //验证密码信息,和服务信息
        if (null==applicationInfo || !passwordEncoder.matches(secret,applicationInfo.getSecret())){
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        //生成token中存放的info信息
        AppInfo appInfo = new AppInfo();
        appInfo.setId(id);
        appInfo.setServiceName(applicationInfo.getServiceName());

        List<Long> targetIdList =  this.appMapper.selectTargetIdList(id);
        appInfo.setTargetList(targetIdList);

        //根据info生成token，有效时间25小时，24小时周期替换token
        return JwtUtils.generateTokenExpireInMinutes(appInfo, props.getPrivateKey(), props.getApp().getExpire());

    }
}
