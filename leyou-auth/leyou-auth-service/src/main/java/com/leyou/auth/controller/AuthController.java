package com.leyou.auth.controller;

import com.leyou.auth.auth.utils.JwtUtils;
import com.leyou.auth.auth.entity.UserInfo;
import com.leyou.auth.common.utils.CookieUtils;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/14/014 22:29
 */
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    @Qualifier("auth-jwt")
    private JwtProperties prop;

    @RequestMapping("/accredit")
    public ResponseEntity<Void> authentication(
            @RequestParam("username")String username,
            @RequestParam("password")String password,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        //登录校验
        String token=this.authService.authentication(username,password);
        if(StringUtils.isBlank(token)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // 将token写入cookie，并制定httpOnly为true,防止通过JS获取并修改
        CookieUtils.setCookie(request,response,prop.getCookieName(),
                token,prop.getCookieMaxAge(),null,true);
        return ResponseEntity.ok().build();
    }

    /**
     * 验证用户信息
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("LY_TOKEN")String token,
                                               HttpServletRequest request, HttpServletResponse response){
        try {
            //从token中解析token信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, this.prop.getPublicKey());
            //解析成功要重新刷新token
            JwtUtils.generateToken(userInfo,
                    this.prop.getPrivateKey(),this.prop.getExpire());
            //更新cookie中的token
            CookieUtils.setCookie(request,response,
                    this.prop.getCookieName(),token,this.prop.getCookieMaxAge());
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //出现异常则，响应500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
