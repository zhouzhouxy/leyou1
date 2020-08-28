package com.asura.test;

import com.asura.leyou.user.LeyouUserApplication;
import com.asura.leyou.user.mapper.UserMapper;
import com.asura.leyou.user.service.UserService;
import com.leyou.auth.common.utils.CodecUtils;
import com.leyou.auth.user.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/15/015 22:10
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouUserApplication.class)
public class Register {

    @Autowired
    private UserMapper userMapper;
    @Test
    public void register(){
        User user = new User();
        user.setUsername("zhouzhou");
        user.setPassword("123456");
        //生成盐
        String salt = CodecUtils.generateSalt();

        //对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        //强制设置不能指定的参数为null
        user.setId(null);
        user.setCreated(new Date());
        user.setSalt(salt);
        //添加到数据库
        int insert = userMapper.insert(user);
        System.out.println(insert);
    }
}
