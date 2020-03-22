package cn.itcast.demo.controller;

import cn.itcast.demo.clients.UserClient;
import cn.itcast.demo.pojo.User;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("consumer")
@Slf4j
public class UserControllerNew {


    @Autowired
    private UserClient userClient;

    @GetMapping("{id}")
    @HystrixCommand(fallbackMethod = "queryUserByIdFallBack")
    public User queryUserById(@PathVariable("id") Long userId) {
        log.info("{}，对应的用户信息查询成功",userId);
        //不需要手动拼接地址，不需要关注放回类型
        return this.userClient.queryUserById(userId);
    }


    public User queryUserByIdFallBack(Long userId) {
        log.error("{}，对应的用户信息查询失败",userId);
        User user = new User();
        user.setId(userId);
        user.setNote("服务不可用，请重试");
        return user;
    }



}
