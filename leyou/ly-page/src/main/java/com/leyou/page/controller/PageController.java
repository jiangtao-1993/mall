package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    /**
     * 根据spuId动态渲染生成对应的spu的详情页面
     * @param spuId
     * @return
     */
    @GetMapping("item/{spuId}.html")
    public String loadData(@PathVariable("spuId")Long spuId, Model model){
        //把查询返回所有的key：value存入model中
        model.addAllAttributes(this.pageService.loadData(spuId));
        return "item";
    }
}
