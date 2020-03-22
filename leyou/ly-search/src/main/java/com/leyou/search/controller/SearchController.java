package com.leyou.search.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> pageQuery(@RequestBody SearchRequest searchRequest){

        return ResponseEntity.ok(this.searchService.pageQuery(searchRequest));
    }

    @PostMapping("filter")
    public ResponseEntity<Map<String, List<?>>>filterQuery(@RequestBody SearchRequest searchRequest){

        return ResponseEntity.ok(this.searchService.filterQuery(searchRequest));
    }
}
