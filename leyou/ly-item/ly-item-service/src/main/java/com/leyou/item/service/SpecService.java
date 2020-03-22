package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecService {


    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;


    /**
     * 根据分类查询规格参数组
     * @param cid
     * @return
     */
    public List<SpecGroupDTO> querySpecGroupByCategoryId(Long cid) {

        SpecGroup record = new SpecGroup();
        record.setCid(cid);

        //根据分类的id查询对应的规格参数组
        List<SpecGroup> specGroups = this.specGroupMapper.select(record);

        if (CollectionUtils.isEmpty(specGroups)){
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(specGroups,SpecGroupDTO.class);
    }

    public List<SpecParamDTO> queryParams(Long gid,Long cid,Boolean searching) {
        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setSearching(searching);

        //目前根据规格参数组的id查询对应的组内参数
        List<SpecParam> specParams = null;
        try {
            specParams = this.specParamMapper.select(record);
        } catch (Exception e) {
            //TODO 诊断异常，起因，动态抛出，到底是数据库坏了还是，sql语句问题
            throw new LyException(ExceptionEnum.DATA_SERVER_OPERATION_ERROR);
        }

        if (CollectionUtils.isEmpty(specParams)){
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(specParams,SpecParamDTO.class);
    }


    public List<SpecGroupDTO> querySpecsByCid(Long id) {
        // 查询规格组
        List<SpecGroupDTO> groupList = querySpecGroupByCategoryId(id);

        // 查询分类下所有规格参数
        List<SpecParamDTO> params = queryParams(null, id, null);


        // 将规格参数按照groupId进行分组，得到每个group下的param的集合   key,groupId,value,当前组id对应的规格参数
        Map<Long, List<SpecParamDTO>> paramMap = params.stream()
                .collect(Collectors.groupingBy(SpecParamDTO::getGroupId));


        // 填写到group中
        for (SpecGroupDTO groupDTO : groupList) {
            //根据组id取值
            groupDTO.setParams(paramMap.get(groupDTO.getId()));
        }
        return groupList;
    }
}
