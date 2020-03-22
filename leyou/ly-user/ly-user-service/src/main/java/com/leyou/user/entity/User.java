package com.leyou.user.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    @NotNull(message = "用户名不能为空")
    @Length(min = 4,max = 30,message = "用户名长度要控制在4-30之间")
    private String username;
    private String password;
    private String phone;
    private Date createTime;
    private Date updateTime;
}