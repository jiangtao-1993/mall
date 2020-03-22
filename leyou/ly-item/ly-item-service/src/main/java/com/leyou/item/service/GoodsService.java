package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.Spu;
import com.leyou.item.entity.SpuDetail;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.common.base.insert.InsertSelectiveMapper;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.leyou.common.constants.MQConstants.Exchange.ITEM_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_DOWN_KEY;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_UP_KEY;


@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    public PageResult<SpuDTO> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {

        PageHelper.startPage(page, rows);

        //动态sql生成器
        Example example = new Example(Spu.class);

        //动态sql的拼接工具
        Example.Criteria criteria = example.createCriteria();
        //如果有值对name进行模糊查询
        if (!StringUtils.isBlank(key)) {
            criteria.andLike("name", "%" + key + "%");
        }

        //如果有值，匹配saleable对应的额值
        if (null != saleable) {
            criteria.andEqualTo("saleable", saleable);
        }

        List<Spu> spus = this.spuMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(spus)) {
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        //自动统计总元素个数，和页码数
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);

        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(spus, SpuDTO.class);

        spuDTOS.forEach(spuDTO -> {

            //跨业务根据id查询分类
            List<CategoryDTO> categoryDTOS = this.categoryService.queryCategoryByIds(spuDTO.getCategoryIds());


            //迭代分类集合，挨个取出name，并把所有的name，拼接为字符串，分隔符自定义
            String names = categoryDTOS.stream() //List<CategoryDTO>====>Stream<CategoryDTO>
                    .map(CategoryDTO::getName)//Stream<String>
                    .collect(Collectors.joining("/"));

            spuDTO.setCategoryName(names);

            //调用品牌查询的业务根据品牌的id查询对应的对象
            spuDTO.setBrandName(this.brandService.queryBrandById(spuDTO.getBrandId()).getName());
        });

        //封装spu查询结果的返回
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), spuDTOS);
    }

    @Transactional
    public void addGoods(SpuDTO spuDTO) {

        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);

        //保存spu，同时也有主键回显
        int count = this.spuMapper.insertSelective(spu);

        if (1 != count) {
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }

        SpuDetail spuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), SpuDetail.class);
        spuDetail.setSpuId(spu.getId());

        //保存spuDetail
        count = this.spuDetailMapper.insertSelective(spuDetail);
        if (1 != count) {
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }

        saveSkus(spuDTO, spu);
    }

    private void saveSkus(SpuDTO spuDTO, Spu spu) {
        int count;
        List<Sku> skus = spuDTO.getSkus() //从spuDTO中获取所有的skuDTO
                .stream().map(skuDTO -> { //循环迭代skuDTO
                    skuDTO.setSpuId(spu.getId()); //给每个skuDTO赋值spuId
                    return BeanHelper.copyProperties(skuDTO, Sku.class); //把skuDTO转为sku返回
                }).collect(Collectors.toList());//List<Sku>

        //批量保存sku
        count = this.skuMapper.insertList(skus);

        if (count != skus.size()) {
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }
    }

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 在商品修改时，进行消息的传递
     * 上架发up
     * 下架发down
     *
     * @param spuId
     * @param saleable
     */
    @Transactional
    public void modifySaleable(Long spuId, Boolean saleable) {
        Spu spu = this.spuMapper.selectByPrimaryKey(spuId);

        spu.setUpdateTime(new Date());

        spu.setSaleable(saleable);

        //修改spu的上下架状态
        this.spuMapper.updateByPrimaryKeySelective(spu);

        Sku record = new Sku();
        record.setSpuId(spu.getId());

        //根据spuId查询对应的sku
        List<Sku> skus = this.skuMapper.select(record);

        //遍历sku的集合修改每个sku的上下架状态
        skus.forEach(sku -> {
            sku.setEnable(saleable);
            sku.setUpdateTime(spu.getUpdateTime());
            this.skuMapper.updateByPrimaryKeySelective(sku);
        });

        String key = saleable ? ITEM_UP_KEY : ITEM_DOWN_KEY;

        //商品服务负责消息的发出，rk不同，所以其实可以把消息发给不同的队列
        this.amqpTemplate.convertAndSend(ITEM_EXCHANGE_NAME, key, spuId);
    }

    public SpuDetailDTO querySpuDetailBySpuId(Long spuId) {

        SpuDetail spuDetail = this.spuDetailMapper.selectByPrimaryKey(spuId);

        if (spuDetail == null) {

            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return BeanHelper.copyProperties(spuDetail, SpuDetailDTO.class);
    }

    public List<SkuDTO> querySkuBySpuId(Long spuId) {

        Sku record = new Sku();
        record.setSpuId(spuId);

        //基于spuId查询对应的sku
        List<Sku> skus = this.skuMapper.select(record);

        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(skus, SkuDTO.class);
    }

    @Transactional
    public void updateGoods(SpuDTO spuDTO) {

        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);

        spu.setUpdateTime(new Date());

        //修改spu
        int count = this.spuMapper.updateByPrimaryKeySelective(spu);

        if (1 != count) {
            throw new LyException(ExceptionEnum.DATA_MODIFY_ERROR);
        }

        //直接修改spuDetail
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), SpuDetail.class);
        spuDetail.setUpdateTime(spu.getUpdateTime());

        count = this.spuDetailMapper.updateByPrimaryKeySelective(spuDetail);

        if (1 != count) {
            throw new LyException(ExceptionEnum.DATA_MODIFY_ERROR);
        }

        //清理之前的sku

        Sku record = new Sku();
        record.setSpuId(spu.getId());

        //统计要删除的sku的数量
        int skuCount = this.skuMapper.selectCount(record);

        //删除sku
        count = this.skuMapper.delete(record);

        //比较sku的数量和要删除的商品的数量
        if (skuCount != count) {
            throw new LyException(ExceptionEnum.DATA_MODIFY_ERROR);
        }

        //重新保存sku
        saveSkus(spuDTO, spu);
    }

    public SpuDTO querySpuById(Long id) {
        Spu spu = this.spuMapper.selectByPrimaryKey(id);
        if (null == spu) {
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return BeanHelper.copyProperties(spu, SpuDTO.class);
    }

    public List<SkuDTO> querySkuByIds(List<Long> ids) {

        List<Sku> skus = this.skuMapper.selectByIdList(ids);

        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(skus, SkuDTO.class);
    }

    @Transactional
    public void minusStock(Map<Long, Integer> cartsMap) {

        //TODO 直接修改，不能先查询再修改，建议把修改方法，改为同步方法
        //根据sku的id集合查询要修改的sku
        List<Sku> skus = this.skuMapper.selectByIdList(new ArrayList<>(cartsMap.keySet()));

        //遍历sku集合，修改每个sku的数量
        for (Sku sku : skus) {
            //每个sku的库存，应该是原始-订单中消耗的库存

            Integer newStock = sku.getStock() - cartsMap.get(sku.getId());

            if (0 > newStock) {
                throw new LyException(ExceptionEnum.STOCK_MINUS_ERROR);
            }
            sku.setStock(newStock);

            this.skuMapper.updateByPrimaryKeySelective(sku);
        }
    }

    public void plusStock(Map<Long, Integer> skuMap) {
        //TODO 直接修改，不能先查询再修改，建议把修改方法，改为同步方法
        //根据sku的id集合查询要修改的sku
        List<Sku> skus = this.skuMapper.selectByIdList(new ArrayList<>(skuMap.keySet()));

        //遍历sku集合，修改每个sku的数量
        for (Sku sku : skus) {
            //每个sku的库存，应该是原始-订单中消耗的库存

            Integer newStock = sku.getStock() + skuMap.get(sku.getId());

            if (0 > newStock) {
                throw new LyException(ExceptionEnum.STOCK_MINUS_ERROR);
            }
            sku.setStock(newStock);

            this.skuMapper.updateByPrimaryKeySelective(sku);
        }
    }
}
