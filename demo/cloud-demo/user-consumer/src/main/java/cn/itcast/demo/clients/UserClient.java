package cn.itcast.demo.clients;

import cn.itcast.demo.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("user-service") //声明今后请求的服务名称（service-id）
public interface UserClient {

    @GetMapping("user/hello/{id}") //声明请求服务的地址
    User queryUserById(@PathVariable("id") Long userId);

    //http请求
    //FeignClient,打工仔ribbon，user-service===》注册表----》筛选host:port/user/hello/userId===>json===>消息转换
    //1，拼接请求地址
    //2,发送请求
    //3,获取到json后，转为声明的对象
}
