package com.asura.goods.service;


import com.asura.goods.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

@Service
public class GoodsHtmlService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private TemplateEngine templateEngine;

    private static final Logger LOGGER=
            LoggerFactory.getLogger(GoodsHtmlService.class);

    /**
     * 创建Html页面
     * @param spuId
     */
    public void createHtml(Long spuId){
        PrintWriter writer=null;
        try {
            Map<String, Object> spuMap = this.goodsService.loadData(spuId);
            //创建thymeleaf对象
            Context context = new Context();
            //把数据放入上下文对象
            context.setVariables(spuMap);
            //创建输出流
            File file=new File("I:\\nginx-1.14.0\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
            writer = new PrintWriter(file);
            //执行静态化页面
        } catch (Exception e) {
            LOGGER.error("页面静态化出错:{}"+e,spuId);
            e.printStackTrace();
        }finally {
            if(writer!=null){
                writer.close();
            }
        }
    }

    public void asyncExcute(Long spuId){
        ThreadUtils.execute(()->createHtml(spuId));
        /*ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                createHtml(spuId);
            }
        });*/
    }

    /**
     * 根据id删除页面
     * @param id
     */
    public void deleteHtml(Long id) {
        File file=new File("I:\\nginx-1.14.0\\nginx-1.14.0\\html\\item\\" + id + ".html");
        file.deleteOnExit();
    }
}
