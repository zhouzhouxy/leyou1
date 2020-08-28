package com.asura.goods.listener;

import com.asura.goods.service.GoodsHtmlService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/10/010 22:18
 */
@Component
public class GoodsListener {

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @RabbitListener(bindings = @QueueBinding(
            value=@Queue(value="leyou.create.web.queue",durable = "true"),
            exchange=@Exchange(
                    value = "leyou.item.exchange",
                    ignoreDeclarationExceptions = "ture",
                    type = ExchangeTypes.TOPIC
            ),
            key = {"item.insert","item.update"}
    ))
    public void listenCreate(Long id)throws Exception{
        if(id==null){
            return;
        }
        //创建页面
        goodsHtmlService.createHtml(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.delete.web.queue",durable = "true"),
            exchange = @Exchange(
                    value="leyou.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC
            ),
            key="item.delete"
    ))
    public void listenDelete(Long id){
        if(id==null){
            return;
        }
        //删除页面
        goodsHtmlService.deleteHtml(id);
    }
}
