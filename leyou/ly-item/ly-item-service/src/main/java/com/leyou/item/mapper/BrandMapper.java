package com.leyou.item.mapper;

import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>, SelectByIdListMapper<Brand, Long> {

    int insertCategoryBrand(@Param("cids") List<Long> cids, @Param("bid") Long id);

    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    int deleteCategoryBrandByBrandId(@Param("bid") Long id);

    @Select("select tb.id,tb.name from tb_brand tb inner join tb_category_brand cb on tb.id = cb.brand_id where cb.category_id = #{cid}")
    List<Brand> queryBrandByCategory(@Param("cid") Long cid);
}
