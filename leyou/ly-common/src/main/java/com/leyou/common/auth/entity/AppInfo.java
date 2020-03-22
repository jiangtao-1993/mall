package com.leyou.common.auth.entity;

import lombok.Data;

import java.util.List;


@Data
public class AppInfo {
    private Long id; //当前服务的id
    private String serviceName; //当前服务的名称
    private List<Long> targetList; //目标服务id集合
}