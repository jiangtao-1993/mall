//package cn.itcast.demo.controller;
//
//import cn.itcast.demo.pojo.User;
//import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//@RestController
//@RequestMapping("consumer")
//public class UserController {
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @GetMapping("{id}")
//    @HystrixCommand(fallbackMethod = "queryUserByIdFallBack") //回调触发条件，2，1），超时1s，2）hystrix会给请求分配线程池，如果线程池，也要回调
//    public String queryUserById(@PathVariable("id") Long userId) {
//        if (1==userId){
//            throw new RuntimeException("自定义异常返回");
//        }
//
//        //先查询获取到json，然后转为User，其实真正的请求者变成了ribbon---->user-service(注册表)--->选则一个
//        return this.restTemplate.getForObject("http://user-service/user/hello/" + userId, String.class);
//
//    }
//
//
//    public String queryUserByIdFallBack(Long userId) {
//        return userId+",抱歉，目前服务不可用，请15min后重试";
//    }
//}
