package com.asura.leyou.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/11/011 13:24
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.asura.leyou.user.mapper")
public class LeyouUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeyouUserApplication.class,args);
    }
}
