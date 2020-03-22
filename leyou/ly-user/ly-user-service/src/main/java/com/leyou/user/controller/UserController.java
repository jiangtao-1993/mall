package com.leyou.user.controller;

import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 验证用户名，或手机号的唯一性
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    @ApiOperation(value = "校验用户名数据是否可用，如果不存在则可用")
    @ApiResponses({
            @ApiResponse(code = 200, message = "校验结果有效，true或false代表可用或不可用"),
            @ApiResponse(code = 400, message = "请求参数有误，比如type不是指定值"),
            @ApiResponse(code = 500, message = "服务器内部操作错误")
    })
    public ResponseEntity<Boolean> checkData(
            @ApiParam(value = "要校验的数据", example = "lisi") @PathVariable("data") String data,
            @ApiParam(value = "数据类型，1：用户名，2：手机号", example = "1") @PathVariable(value = "type") Integer type){

        return ResponseEntity.ok(this.userService.checkData(data,type));
    }

    //发送短信验证码
    @PostMapping("code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone")String phone){

        this.userService.sendVerifyCode(phone);

        return ResponseEntity.ok().build();
    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */

    @PostMapping("register")
    public ResponseEntity<Void> register(
            @Valid User user,
            @RequestParam("code")String code){

        this.userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @GetMapping("query")
    public ResponseEntity<UserDTO> queryUserByUsernameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password){
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username, password));
    }
}
