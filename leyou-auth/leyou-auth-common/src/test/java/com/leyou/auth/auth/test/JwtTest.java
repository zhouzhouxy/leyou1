package com.leyou.auth.auth.test;

import com.leyou.auth.auth.entity.UserInfo;
import com.leyou.auth.auth.utils.JwtUtils;
import com.leyou.auth.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/12/012 23:23
 */
@RunWith(SpringRunner.class)
public class JwtTest {
    private static final String pubKeyPath="C:\\TEMP\\rsa\\rsa.pub";
    private static final String priKeyPath="C:\\TEMP\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRas()throws Exception{
        RsaUtils.generateKey(pubKeyPath,priKeyPath,"234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU4Njg3MDEzNH0.OwPiNAqUPVh-hLleuQGyqeNUGLr_wXx5Rng7uDndzHteWdwYzmpsTcGkPCGseBmjq2x2dqKn9V6WurJMrqBkKPxID5cuIiErA7FnIFvQ3WoW8zbYKCuI9w7Wdz0T6l2wxSb06v3IJPxjXfBM8UN0A_7kuH_4QM_pOd4EGjqqajg";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
