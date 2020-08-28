package com.leyou.auth.service;

import com.leyou.auth.auth.entity.UserInfo;
import com.leyou.auth.auth.utils.JwtUtils;
import com.leyou.auth.client.UserClient;
import com.leyou.auth.user.pojo.User;
import com.leyou.auth.config.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/14/014 22:32
 */
@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    @Qualifier("auth-jwt")
    private JwtProperties jwtProperties;

    public String authentication(String username, String password) {
        try {
            //调用微服务，执行查询
            User user = this.userClient.queryUser(username, password);
            //如果查询结果为null，则直接返回null
            if(user==null){
                return null;
            }
            //如果有查询结果，则生成token
            String token= JwtUtils.generateToken(new UserInfo(user.getId(),user.getUsername()),
                    jwtProperties.getPrivateKey(),jwtProperties.getExpire());
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
