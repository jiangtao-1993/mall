package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;
    /**
     * 分页查询品牌数据
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<BrandDTO>> pageQuery(
            @RequestParam(value = "page",defaultValue = "1")Integer page, //当前页
            @RequestParam(value = "rows",defaultValue = "5")Integer rows, //页容量
            @RequestParam(value = "sortBy",required = false)String sortBy, //required = false表示这是一个非必要参数
            @RequestParam(value = "desc",required = false)Boolean desc,
            @RequestParam(value = "key",required = false)String key
    ){

        return ResponseEntity.ok(this.brandService.pageQuery(page,rows,sortBy,desc,key));
    }

    @PostMapping
    public ResponseEntity<Void> addBrand(
            BrandDTO brandDTO, //根据brandDTO的属性，自动生成@requestParam信息，然后，标注required为false
            @RequestParam("cids") List<Long> cids){

        this.brandService.addBrand(brandDTO,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateBrand(
            BrandDTO brandDTO, //根据brandDTO的属性，自动生成@requestParam信息，然后，标注required为false
            @RequestParam("cids") List<Long> cids){

        this.brandService.updateBrand(brandDTO,cids);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据分类id查询对应的品牌集合
     * @param cid
     * @return
     */
    @GetMapping("of/category")
    public ResponseEntity<List<BrandDTO>> queryBrandByCategory(@RequestParam("id")Long cid){

        return ResponseEntity.ok(this.brandService.queryBrandByCategory(cid));
    }

    /**
     * 根据品牌的id集合查询对应的品牌信息
     * @param ids
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<BrandDTO>> queryBrandByIds(@RequestParam("ids")List<Long> ids){

        return ResponseEntity.ok(this.brandService.queryBrandByIds(ids));
    }


    /**
     * 根据品牌的id查询对应的品牌信息
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<BrandDTO> queryBrandById(@PathVariable("id")Long id){

        return ResponseEntity.ok(this.brandService.queryBrandById(id));
    }
}
