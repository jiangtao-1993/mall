package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuDTO>> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "key",required = false)String key){
        return ResponseEntity.ok(this.goodsService.querySpuByPage(page,rows,saleable,key));
    }

    @PostMapping("goods")
    public ResponseEntity<Void> addGoods(@RequestBody SpuDTO spuDTO){

        this.goodsService.addGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("spu/saleable")
    public ResponseEntity<Void> modifySaleable(
            @RequestParam("id")Long spuId,
            @RequestParam("saleable")Boolean saleable){

        this.goodsService.modifySaleable(spuId,saleable);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据spuId查询对应的spuDetail
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail")
    public ResponseEntity<SpuDetailDTO> querySpuDetailBySpuId(@RequestParam("id")Long spuId){
        return ResponseEntity.ok(this.goodsService.querySpuDetailBySpuId(spuId));
    }

    /**
     * 根据spuId查询对应的sku
     * @param spuId
     * @return
     */
    @GetMapping("sku/of/spu")
    public ResponseEntity<List<SkuDTO>> querySkuBySpuId(@RequestParam("id")Long spuId){
        return ResponseEntity.ok(this.goodsService.querySkuBySpuId(spuId));
    }


    /**
     * 商品修改业务
     * @param spuDTO
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO){

        this.goodsService.updateGoods(spuDTO);
        return ResponseEntity.ok().build();
    }


    /**
     * 根据spuId查询spuDTO
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<SpuDTO> querySpuById(@PathVariable("id")Long id){

        return ResponseEntity.ok(this.goodsService.querySpuById(id));
    }

    /**
     * 根据sku的id集合查询sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<SkuDTO>> querySkuByIds(@RequestParam("ids")List<Long> ids){

        return ResponseEntity.ok(this.goodsService.querySkuByIds(ids));
    }

    @PutMapping("minusstock")
    public ResponseEntity<Void> minusStock(@RequestBody Map<Long,Integer> cartsMap){

        this.goodsService.minusStock(cartsMap);
        return ResponseEntity.ok().build();
    }

    @PutMapping("plusstock")
    public ResponseEntity<Void> plusStock(@RequestBody Map<Long,Integer> cartsMap){

        this.goodsService.plusStock(cartsMap);
        return ResponseEntity.ok().build();
    }
}
