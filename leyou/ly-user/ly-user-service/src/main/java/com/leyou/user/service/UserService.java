package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public Boolean checkData(String data, Integer type) {

        if (StringUtils.isBlank(data)) {
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        User record = new User();

        switch (type) {
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        boolean result = false;
        try {
            result = this.userMapper.selectCount(record) != 1;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.DATA_SERVER_OPERATION_ERROR);
        }


        return result;
    }

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //redis的key前缀
    private final String KEY_PREFIX = "ly:user:verify:code:";

    public void sendVerifyCode(String phone) {

        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);

        //指定长度生成随机验证码
        String code = NumberUtils.generateCode(6);

        msg.put("code", code);
        //向消息中间件，添加消息
        amqpTemplate.convertAndSend(SMS_EXCHANGE_NAME, VERIFY_CODE_KEY, msg);

        //把发出的验证码保存到redis中
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public void register(User user, String code) {

        String key = KEY_PREFIX + user.getPhone();

        //判断key是否存在，还要判断值是否匹配
        if (!redisTemplate.hasKey(key) && !redisTemplate.opsForValue().get(key).equals(code)) {

            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        //对user加密处理
        user.setPassword(passwordEncoder.encode(user.getPassword()));


        //把用户数据保存到数据库
        int count = userMapper.insertSelective(user);

        if (1!=count){
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }
        //注册完成后，可以删除redis中的内容
        //TODO 先判断，是否快过期，比如过期时间不足3s，则不要删除，超过则手动删除

    }

    public UserDTO queryUserByUsernameAndPassword(String username, String password) {

        User record = new User();
        record.setUsername(username);

        User user = this.userMapper.selectOne(record);

        //如果输入的用户名为空，或者密码不匹配则抛异常
        if (null==user || !passwordEncoder.matches(password,user.getPassword())){
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        return BeanHelper.copyProperties(user, UserDTO.class);
    }
}
