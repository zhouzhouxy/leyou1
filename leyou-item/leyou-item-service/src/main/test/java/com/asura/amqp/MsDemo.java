package com.asura.amqp;

import com.asura.leyou.item.LyItemServiceApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/10/010 20:02
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LyItemServiceApplication.class)
public class MsDemo {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void testSend() throws InterruptedException {
        String msg="hello,spring boot amqp";
        this.amqpTemplate.convertAndSend("spring.test.exchange","a.b",msg);
        //等待十秒后结束
        Thread.sleep(10000);
    }
}
