package cn.itcast.rabbitmq.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-05-23 18:08
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MqDemo {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void testSend() throws InterruptedException {
       Map<String,String> msg = new HashMap<>();
       msg.put("phone","13052050138");
       msg.put("code","helloheima88");
        this.amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code", msg);
        // 等待10秒后再结束
        Thread.sleep(10000);
    }
}
