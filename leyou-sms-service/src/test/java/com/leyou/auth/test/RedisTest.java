package com.leyou.auth.test;

import com.asura.leyou.sms.LeyouSmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/11/011 21:59
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes= LeyouSmsApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void testRedis(){
        //存储数据
        this.redisTemplate.opsForValue().set("k","v");

        //获取数据
        String val= (String) this.redisTemplate.opsForValue().get("k");
        System.out.println("val = "+val);
    }

    @Test
    public void testRedis2(){
        //存储数据，并指定剩余生命时间，5小时
        this.redisTemplate.opsForValue().set("k2","v2",5, TimeUnit.HOURS);
    }

    @Test
    public void testHash(){
        BoundHashOperations<String,Object,Object> hashOps = this.redisTemplate.boundHashOps("user");

        //操作hash数据
        hashOps.put("name","jack");
        hashOps.put("age","21");

        //获取单个数据
        Object name = hashOps.get("name");
        System.out.println("name = "+name);

        //获取所有数据
        Map<Object, Object> map = hashOps.entries();
        for (Map.Entry<Object,Object> me : map.entrySet()) {
            System.out.println(me.getKey()+" : "+me.getValue());
        }
    }

}
