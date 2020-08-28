package com.asura.goods.controller;


import com.asura.goods.service.GoodsHtmlService;
import com.asura.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("item")
public class GoodsController {

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    /**
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("{id}.html")
    public String toItemPage(Model model, @PathVariable("id")Long id){
        //加载所需的数据
        Map<String, Object> dataMap = this.goodsService.loadData(id);

        //放入模型
        model.addAllAttributes(dataMap);

        //把页面静态化
        //注意:生成html代码不能对用户请求产生影响，所以这里我们使用额外的县城进行一步创建
        this.goodsHtmlService.asyncExcute(id);
        return "item";
    }

    @Autowired
    private GoodsService goodsService;


     
}
