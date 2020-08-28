package com.leyou.auth.user.api;

import com.leyou.auth.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/15/015 18:05
 */
public interface UserApi {
    @GetMapping("query")
    public User queryUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    );
}
