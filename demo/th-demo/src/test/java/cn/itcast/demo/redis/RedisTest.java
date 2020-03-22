package cn.itcast.demo.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testAddData(){
        redisTemplate.opsForValue().set("heima88","18K",30, TimeUnit.SECONDS);

//        String value = redisTemplate.opsForValue().get("heima88");
//
//        System.out.println("value = " + value);
//
//        if (redisTemplate.hasKey("heima88")){
//            redisTemplate.delete("heima88");
//        }
    }

    @Test
    public void testAddDataObj(){
        //基于key创建了一个关于此key的map操作
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps("user");

        ops.put("name","ldh");
        ops.put("age","50");

        String name = ops.get("name");

        System.out.println("name = " + name);

        Long rows = ops.delete("name", "age");

        System.out.println("rows = " + rows);
    }


    @Test
    public void testOut(){
        System.out.printf("大家%s好,工资%dK","下午",18);
    }
}
