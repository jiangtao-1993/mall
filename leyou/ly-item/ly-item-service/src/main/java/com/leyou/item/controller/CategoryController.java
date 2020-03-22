package com.leyou.item.controller;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/of/parent")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByPid(@RequestParam("pid") Long pid){

        return ResponseEntity.ok(this.categoryService.queryCategoryByPid(pid));
    }

    /**
     * 根据品牌id查询对应的分类信息
     * @param brandId
     * @return
     */
    @GetMapping("of/brand/{id}")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByBrandId(@PathVariable("id")Long brandId){
        return ResponseEntity.ok(this.categoryService.queryCategoryByBrandId(brandId));
    }

    /**
     * 根据分类的id集合查询分类集合信息
     * @param cids
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByIds(@RequestParam("ids") List<Long> cids){

        return ResponseEntity.ok(this.categoryService.queryCategoryByIds(cids));
    }

}
