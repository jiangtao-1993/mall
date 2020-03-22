package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecController {


    @Autowired
    private SpecService specService;

    /**
     * 以分类查询规格参数组
     * @param cid
     * @return
     */
    @GetMapping("groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> querySpecGroupByCategoryId(@RequestParam("id")Long cid){

        return ResponseEntity.ok(this.specService.querySpecGroupByCategoryId(cid));
    }

    /**
     * 根据规格参数组id，或者分类id，或者是否可搜索，查询对应的规格参数
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParamDTO>> queryParams(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching
    ){
        return ResponseEntity.ok(this.specService.queryParams(gid,cid,searching));
    }

    /**
     * 查询规格参数组，及组内参数
     * @param id 商品分类id
     * @return 规格组及组内参数
     */
    @GetMapping("/of/category")
    public ResponseEntity<List<SpecGroupDTO>> querySpecsByCid(@RequestParam("id") Long id){
        return ResponseEntity.ok(specService.querySpecsByCid(id));
    }
}
