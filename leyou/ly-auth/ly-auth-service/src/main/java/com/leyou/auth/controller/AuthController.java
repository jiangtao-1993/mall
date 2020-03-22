package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletResponse response) {

        this.authService.login(username, password, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(HttpServletRequest request, HttpServletResponse response) {

        return ResponseEntity.ok(this.authService.verify(request, response));
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        this.authService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    /**
     * 微服务认证并申请令牌
     *
     * @param id     服务id
     * @param secret 密码
     * @return 无
     */
    @GetMapping("authorization")
    public ResponseEntity<String> authorize(@RequestParam("id") Long id,
                                            @RequestParam("secret") String secret) {
        return ResponseEntity.ok(authService.authenticate(id, secret));
    }
}
