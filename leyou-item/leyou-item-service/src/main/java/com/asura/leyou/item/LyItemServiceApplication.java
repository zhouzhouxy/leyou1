package com.asura.leyou.item;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.asura.leyou.item.mapper")      //mapper接口的包扫描
public class LyItemServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyItemServiceApplication.class,args);
    }
}
