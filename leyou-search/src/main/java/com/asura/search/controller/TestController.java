package com.asura.search.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
public class TestController {

    @RequestMapping("/test")
    public String sayHi(){
        System.out.println("hello");
        return "hello";
    }
}
