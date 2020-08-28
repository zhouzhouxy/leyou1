package com.asura.filter;

import com.asura.config.FilterProperties;
import com.asura.config.JwtProperties;
import com.leyou.auth.auth.utils.JwtUtils;
import com.leyou.auth.common.utils.CookieUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.RequestContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/16/016 14:02
 */
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    @Qualifier("api-jwt")
    private JwtProperties properties;

    @Autowired
    private FilterProperties filterProp;

    @Override
    public String filterType() {
        return null;
    }

    @Override
    public int filterOrder() {
        return 5;
    }

    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        //获取路径
        String requestURI = request.getRequestURI();
        //判断白名单
        //遍历允许访问的路径
        for (String allowPath : this.filterProp.getAllowPaths()) {
            //判断是否符合
            if(requestURI.startsWith(allowPath)){
                return false;
            }
        }

        return true;
    }

    @Override
    public Object run() throws ZuulException {
        //获取上下午
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        //获取token
        String token = CookieUtils.getCookieValue(request, this.properties.getCookieName());
        //校验
        try {
            //校验通过什么都不做，即放行
            JwtUtils.getInfoFromToken(token,this.properties.getPublicKey());
        } catch (Exception e) {
            //校验出现异常，返回403
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
            //e.printStackTrace();
        }

        return null;
    }
}
