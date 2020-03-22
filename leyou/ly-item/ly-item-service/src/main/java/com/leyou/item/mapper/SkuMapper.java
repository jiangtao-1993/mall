package com.leyou.item.mapper;

import com.leyou.item.entity.Sku;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper  extends Mapper<Sku>, InsertListMapper<Sku>, SelectByIdListMapper<Sku,Long> {
}
