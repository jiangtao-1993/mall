package cn.itcast.controller;

import cn.itcast.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.Date;

@Controller
public class HelloController {
    @GetMapping("hello")
    public String sayHello(Model model){

        model.addAttribute("info","大清亡了,现在是民国");
        return "heima88";
    }


    @GetMapping("show2")
    public String show2(Model model){
        User user = new User();
        user.setAge(21);
        user.setName("Jack Chen");
        user.setFriend(new User("李小龙", 30));

        model.addAttribute("user", user);
        return "show2";
    }

    @GetMapping("show3")
    public String show3(Model model){

        model.addAttribute("salary","18K");
        model.addAttribute("today", new Date());
        return "show3";
    }


    @GetMapping("show4")
    public String show4(Model model){

        User user1 = new User();
        user1.setName("刘德华01");
        user1.setAge(51);

        User user2 = new User();
        user2.setName("刘德华02");
        user2.setAge(52);

        User user3 = new User();
        user3.setName("刘德华03");
        user3.setAge(53);

        User user4 = new User();
        user4.setName("刘德华04");
        user4.setAge(54);

        model.addAttribute("users", Arrays.asList(user1,user2,user3,user4));
        return "show4";
    }

    @GetMapping("show5")
    public String show5(Model model){

      model.addAttribute("name","刘德华");
      model.addAttribute("age",50);
      model.addAttribute("isMan",true);
      model.addAttribute("user",new User("张三",30));
        return "show5";
    }


}
