package com.leyou.cart.config;

import com.leyou.auth.auth.utils.RsaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/17/017 14:48
 */
@ConfigurationProperties(prefix = "leyou.jwt")
public class JwtProperties {
    private String pubKeyPath;

    private PublicKey publicKey;

    private String cookieName;

    private static final Logger LOGGER=
            LoggerFactory.getLogger(JwtProperties.class);

    @PostConstruct
    public void init(){
        try {
            //获取公钥和私钥
            this.publicKey= RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
//            e.printStackTrace();
            LOGGER.error("初始化公钥失败！",e);
            throw new RuntimeException();
        }
    }

    public String getPubKeyPath() {
        return pubKeyPath;
    }

    public void setPubKeyPath(String pubKeyPath) {
        this.pubKeyPath = pubKeyPath;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }
}
