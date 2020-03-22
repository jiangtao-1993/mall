package com.leyou.search.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.clients.ItemClient;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private ItemClient itemClient;


    public PageResult<Goods> pageQuery(SearchRequest searchRequest) {


        //自定义查询工具
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();


        //添加查询添加
        queryBuilder.withQuery(buildBasicQuery(searchRequest));


        //springData的分页从0开始，所以一定要-1
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize()));

        //执行查询，获取分页聚合结果，
        AggregatedPage<Goods> goodsAggregatedPage = this.esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        //封装返回分页结果
        return new PageResult<>(goodsAggregatedPage.getTotalElements(), goodsAggregatedPage.getTotalPages(), goodsAggregatedPage.getContent());
    }

    //规格参数的查询
    public Map<String, List<?>> filterQuery(SearchRequest searchRequest) {

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        QueryBuilder query = buildBasicQuery(searchRequest);
        //添加查询条件
        queryBuilder.withQuery(query);

        //springData强行要求分页展示必须至少展示1个，
        queryBuilder.withPageable(PageRequest.of(0, 1));

        String brandAggName = "brands";

        String categoryAggName = "categories";

        //添加品牌聚合条件
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //添加分类聚合条件
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("categoryId"));

        //执行查询聚合
        AggregatedPage<Goods> goodsAggregatedPage = this.esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        //从查询结果中取出所有的聚合结果
        Aggregations aggregations = goodsAggregatedPage.getAggregations();

        //根据品牌聚合名称，获取聚合的结果
        LongTerms brandTerms = aggregations.get(brandAggName);

        //获取聚合分桶
        List<Long> brandIds = brandTerms.getBuckets().stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());

        //根据分类聚合的名称，获取分类聚合的结果
        LongTerms categoryTerms = aggregations.get(categoryAggName);

        List<Long> categoryIds = categoryTerms.getBuckets().stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());

        Map<String, List<?>> result = new LinkedHashMap<>();

        //TODO 查询之前先做健壮性，判断，如果不论品牌id还是分类不存在，就不应该去查询
        result.put("分类", this.itemClient.queryCategoryByIds(categoryIds));
        result.put("品牌", this.itemClient.queryBrandByIds(brandIds));

        //除了品牌和分类之外，还要展示其他可搜索规格参数，展示的实际，只有一个，确定分类时候，

        if (null != categoryIds && categoryIds.size() == 1) {
            //获取其他可搜索规格参数以及其聚合值，1，分类id，2，查询条件,3返回结果
            getSpecs(categoryIds.get(0), query, result);
        }

        return result;
    }

    //处理可搜索规格参数，对应的所有的聚合结果值
    private void getSpecs(Long cid, QueryBuilder query, Map<String, List<?>> result) {

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加分页
        queryBuilder.withPageable(PageRequest.of(0,1));
        //添加查询条件
        queryBuilder.withQuery(query);



        //先根据分类id，查询可搜索规格参数
        List<SpecParamDTO> specParamDTOS = this.itemClient.queryParams(null, cid, true);

        //循环添加聚合条件
        specParamDTOS.forEach(specParamDTO -> {
            //可搜索规格参数的名称，也是聚合的名称
            String name = specParamDTO.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        });

        //执行聚合查询
        AggregatedPage<Goods> goodsAggregatedPage = this.esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        //获取所有的聚合
        Aggregations aggregations = goodsAggregatedPage.getAggregations();

        //循环解析聚合
        specParamDTOS.forEach(specParamDTO -> {
            String name = specParamDTO.getName();
            //根据聚合名称获取聚合结果
            StringTerms stringTerms = aggregations.get(name);

            List<String> options = stringTerms.getBuckets().stream()
                    .map(StringTerms.Bucket::getKeyAsString)
                    .collect(Collectors.toList());

            //把解析出来的结果封装返回到页面中
            result.put(name,options);
        });
    }


    //查询构建方法
    public QueryBuilder buildBasicQuery(SearchRequest searchRequest) {
        String key = searchRequest.getKey();

        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        //filter过滤必须发生在bool查询中
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //添加查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", key).operator(Operator.AND));

        //获取请求提交的所有的过滤条件
        Map<String, String> filter = searchRequest.getFilter();

        filter.entrySet().forEach(filterEntry->{
            String key1 = filterEntry.getKey();
            String value1 = filterEntry.getValue();

            if ("品牌".equals(key1)){
                key1 = "brandId";
            }else if ("分类".equals(key1)){
                key1 = "categoryId";
            }else{
                key1 = "specs."+key1+".keyword";
            }

            //添加过滤条件
            queryBuilder.filter(QueryBuilders.termQuery(key1,value1));
        });

        //添加查询条件，查询all字段
        return queryBuilder;
    }
}
