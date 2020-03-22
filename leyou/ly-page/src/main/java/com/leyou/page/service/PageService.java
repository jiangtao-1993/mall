package com.leyou.page.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.clients.ItemClient;
import com.leyou.item.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {


    @Autowired
    private ItemClient itemClient;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${ly.static.itemDir}")
    private String itemDir;
    @Value("${ly.static.itemTemplate}")
    private String itemTemplate;

    public Map<String, Object> loadData(Long spuId) {

        // 查询spu
        SpuDTO spu = itemClient.querySpuById(spuId);
        // 查询分类集合
        List<CategoryDTO> categories = itemClient.queryCategoryByIds(spu.getCategoryIds());
        // 查询品牌
        BrandDTO brand = itemClient.queryBrandById(spu.getBrandId());
        // 查询规格
        List<SpecGroupDTO> specs = itemClient.querySpecsByCid(spu.getCid3());

        //根据spu的id查询sku集合
        List<SkuDTO> skuDTOS = itemClient.querySkuBySpuId(spuId);

        //根据spuId查询spuDetail
        SpuDetailDTO spuDetailDTO = itemClient.querySpuDetailBySpuId(spuId);
        // 封装数据
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        data.put("brand", brand);
        data.put("spuName", spu.getName());
        data.put("subTitle", spu.getSubTitle());
        data.put("skus", skuDTOS);
        data.put("detail", spuDetailDTO);
        data.put("specs", specs);
        return data;
    }


    /**
     * 根据spu的id创建html静态页，
     * @param id
     */
    public void createItemHtml(Long id) {
        // 上下文，准备模型数据
        Context context = new Context();
        // 调用之前写好的加载数据方法
        context.setVariables(loadData(id));
        // 准备文件路径
        File dir = new File(itemDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                // 创建失败，抛出异常
                log.error("【静态页服务】创建静态页目录失败，目录地址：{}", dir.getAbsolutePath());
                throw new LyException(ExceptionEnum.DIRECTORY_WRITER_ERROR);
            }
        }
        File filePath = new File(dir, id + ".html");
        // 准备输出流
        try (PrintWriter writer = new PrintWriter(filePath, "UTF-8")) {
            //模版解析以及最终文件的输出
            templateEngine.process(itemTemplate, context, writer);
        } catch (IOException e) {
            log.error("【静态页服务】静态页生成失败，商品id：{}", id, e);
            throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
        }
    }

    public void deleteItemHtml(Long id) {
        File file = new File(itemDir, id + ".html");
        if(file.exists()){
            if (!file.delete()) {
                log.error("【静态页服务】静态页删除失败，商品id：{}", id);
                throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
            }
        }
    }
}
