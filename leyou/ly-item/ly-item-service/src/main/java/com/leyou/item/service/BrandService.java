package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.mapper.BrandMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.lang.invoke.MethodHandleInfo;
import java.util.Date;
import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<BrandDTO> pageQuery(Integer page, Integer rows, String sortBy, Boolean desc, String key) {

        //分页查询
        PageHelper.startPage(page, rows);

        //动态sql，select xxx from xx where xxx = xxx group by sortBy ASC


        //动态sql生成器
        Example example = new Example(Brand.class);

        //sortBy不为空，要写动态sql，
        if (!StringUtils.isBlank(sortBy)) {
            example.setOrderByClause(sortBy + (desc ? " DESC" : " ASC")); //写了这句话，相当于写了group by，
        }


        //动态sql的拼接工具
        Example.Criteria criteria = example.createCriteria();

        //判断动态查询条件key是否为空，如果不为空，则需要生成动态sql where a = b or f like '%c%' and t >5
        if (!StringUtils.isBlank(key)) {
            //对name进行模糊查询
            criteria.andLike("name", "%" + key + "%");

            criteria.orEqualTo("letter", key);
        }


        //brandMapper.select(record); 这个查询本身就是动态sql查询，哪个属性有值就生成一个where条件，不过只能生成的是equal比值以及多个条件只能and 连接

        //无条件，只分页查询品牌
        List<Brand> brands = this.brandMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        //根据查询条件，自动统计总元素个数，和总的页码数
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //返回分页组合的结果，三个参数, 总元素个数，总页数
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), BeanHelper.copyWithCollection(brands, BrandDTO.class));
    }

    @Transactional
    public void addBrand(BrandDTO brandDTO, List<Long> cids) {

        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        //保存品牌信息，并伴随主键回显
        int count = this.brandMapper.insertSelective(brand);

        if (1 != count) {
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }

        //保存品牌和分类中间表的数据,不能使用通用mapper，所以要手写sql
        count = this.brandMapper.insertCategoryBrand(cids, brand.getId());

        if (cids.size() != count) {
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
    }

    @Transactional
    public void updateBrand(BrandDTO brandDTO, List<Long> cids) {
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        brand.setUpdateTime(new Date());
        //对品牌进行修改
        int count = this.brandMapper.updateByPrimaryKeySelective(brand);

        if (1!=count){
            throw new LyException(ExceptionEnum.DATA_MODIFY_ERROR);
        }

        //先删除之前的品牌和分类之间的关系

        //TODO 先统计要删除的个数，然后执行删除，删除成功后，比较删除成功的行数，以及要删除的行数
        count = this.brandMapper.deleteCategoryBrandByBrandId(brand.getId());


        //重建品牌和分类之间的关系

        //保存品牌和分类中间表的数据,不能使用通用mapper，所以要手写sql
        count = this.brandMapper.insertCategoryBrand(cids, brand.getId());

        if (cids.size() != count) {
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
    }

    public BrandDTO queryBrandById(Long brandId) {

        Brand brand = this.brandMapper.selectByPrimaryKey(brandId);
        if (null==brand){
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return BeanHelper.copyProperties(brand,BrandDTO.class);
    }

    public List<BrandDTO> queryBrandByCategory(Long cid) {

        List<Brand> brands = this.brandMapper.queryBrandByCategory(cid);

        if (CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(brands,BrandDTO.class);
    }

    public List<BrandDTO> queryBrandByIds(List<Long> ids) {

        List<Brand> brands = this.brandMapper.selectByIdList(ids);

        if (CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(brands,BrandDTO.class);
    }
}
