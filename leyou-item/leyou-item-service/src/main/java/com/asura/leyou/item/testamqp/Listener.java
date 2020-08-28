package com.asura.leyou.item.testamqp;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/10/010 19:46
 */
@Component
public class Listener {

    /**
     * @RabiitListener ：方法上的注解，声明这个方法是一个消费者方法，需要指定下面的属性：
     *  bindings:   指定绑定关系，可以有多个，值时@QueueBiding的数组
     *  @QueueBinding 包含下面属性：
     *   value：这个消费者关联的队列。值时@Queue，代表一个队列
     *   exchange: 队列所绑定的交换机，值时@Exchange类型
     *   key:队列和交换机绑定的RoutingKey
     *
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "spring.test.queue",durable = "ture"),
            exchange = @Exchange(
                    value="spring.test.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC
            ),
            key = {"#.#"}
    ))
    public void listener(String msg){
        System.out.println("接收到消息："+msg);
    }
}
