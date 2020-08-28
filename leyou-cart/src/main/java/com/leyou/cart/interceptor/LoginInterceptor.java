package com.leyou.cart.interceptor;

import com.leyou.auth.auth.entity.UserInfo;
import com.leyou.auth.auth.utils.JwtUtils;
import com.leyou.auth.common.utils.CookieUtils;
import com.leyou.cart.config.JwtProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/17/017 16:01
 */
public class LoginInterceptor implements HandlerInterceptor {

    private JwtProperties jwtProperties;

    //定义一个线程域，存放登录用户
    private static final ThreadLocal<UserInfo> t1=new ThreadLocal<>();

    public LoginInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            //查询token
            String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
            if(StringUtils.isBlank(token)){
                //未登录，返回401
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            //有token，查询用户信息
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //放入线程域
            t1.set(user);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            //抛出异常，证明未登录，返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        t1.remove();
    }

    public static UserInfo getLoginUser(){
        return t1.get();
    }
}
