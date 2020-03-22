package cn.itcast.demo.service;

import cn.itcast.demo.mapper.UserMapper;
import cn.itcast.demo.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User queryUserById(Long userId) {


        return this.userMapper.selectByPrimaryKey(userId);
    }
}
