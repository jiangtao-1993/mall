package com.leyou.item.clients;


import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("item-service")
public interface ItemClient {

    @GetMapping("spu/page")
    PageResult<SpuDTO> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key);

    /**
     * 根据分类的id集合查询分类集合信息
     *
     * @param ids
     * @return
     */
    @GetMapping("category/list")
    List<CategoryDTO> queryCategoryByIds(@RequestParam("ids") List<Long> ids);


    /**
     * 根据spuId查询对应的sku
     *
     * @param spuId
     * @return
     */
    @GetMapping("sku/of/spu")
    List<SkuDTO> querySkuBySpuId(@RequestParam("id") Long spuId);


    @GetMapping("spec/params")
    List<SpecParamDTO> queryParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching);


    /**
     * 根据spuId查询对应的spuDetail
     *
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail")
    SpuDetailDTO querySpuDetailBySpuId(@RequestParam("id") Long spuId);


    /**
     * 根据品牌的id集合查询对应的品牌信息
     *
     * @param ids
     * @return
     */
    @GetMapping("brand/list")
    List<BrandDTO> queryBrandByIds(@RequestParam("ids") List<Long> ids);


    /**
     * 根据spuId查询spuDTO
     *
     * @param id
     * @return
     */
    @GetMapping("{id}")
    SpuDTO querySpuById(@PathVariable("id") Long id);

    /**
     * 根据品牌的id查询对应的品牌信息
     *
     * @param id
     * @return
     */
    @GetMapping("brand/{id}")
    BrandDTO queryBrandById(@PathVariable("id") Long id);


    /**
     * 查询规格参数组，及组内参数
     *
     * @param id 商品分类id
     * @return 规格组及组内参数
     */
    @GetMapping("spec/of/category")
    List<SpecGroupDTO> querySpecsByCid(@RequestParam("id") Long id);

    /**
     * 根据sku的id集合查询sku
     *
     * @param ids
     * @return
     */
    @GetMapping("sku/list")
    List<SkuDTO> querySkuByIds(@RequestParam("ids") List<Long> ids);

    @PutMapping("minusstock")
    Void minusStock(@RequestBody Map<Long, Integer> cartsMap);

    @PutMapping("plusstock")
    Void plusStock(@RequestBody Map<Long,Integer> cartsMap);
}
