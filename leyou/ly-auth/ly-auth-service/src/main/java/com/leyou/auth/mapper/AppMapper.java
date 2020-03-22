package com.leyou.auth.mapper;

import com.leyou.auth.entity.ApplicationInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface AppMapper extends Mapper<ApplicationInfo> {

    @Select("select target_id from tb_application_privilege where service_id = #{serviceId}")
    List<Long> selectTargetIdList(@Param("serviceId") Long id);
}
