package com.asura.leyou.user.service;

import com.asura.leyou.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.auth.common.utils.CodecUtils;
import com.leyou.auth.user.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.leyou.auth.common.utils.NumberUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/11/011 13:47
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public Boolean checkData(String data, Integer type) {
        User user = new User();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        switch (type){
            case 1:
                wrapper.eq("username",data);
                break;
            case 2:
                 wrapper.eq("phone",data);
                break;
            default:
                return wrapper==null;
        }

        return this.userMapper.selectCount(wrapper)==0;
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    static final String KEY_PREFIX="user:code:phone";

    static final Logger logger= LoggerFactory.getLogger(UserService.class);
    public Boolean sendVerifyCode(String phone) {
        //生成验证码
        String code = NumberUtils.generateCode(6);

        try {
            Map<String, String> msg = new HashMap<>();
            msg.put("phone",phone);
            msg.put("code",code);
            this.amqpTemplate.convertAndSend("leyou.sms.exchange","sms.verify.code",msg);
            //将code存入redis 缓存时间为5分钟
            this.redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5, TimeUnit.MINUTES);
            return true;
        } catch (AmqpException e) {
            //e.printStackTrace();
            logger.error("发送信息失败。 phone:{},code:{}",phone,code,e);
            return false;
        }
     }

    public Boolean register(User user, String code) {
        //校验短信验证码
        String cacheCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if(!StringUtils.equals(code,cacheCode)){
            return false;
        }

        //生成盐
        String salt = CodecUtils.generateSalt();

        //对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        //强制设置不能指定的参数为null
        user.setId(null);
        user.setCreated(new Date());
        user.setSalt(salt);
        //添加到数据库
        boolean b=this.userMapper.insert(user)==1;

        if(b){
            //注册成功，删除redis中的记录
            redisTemplate.delete(KEY_PREFIX+user.getPhone());
        }
        return b;
    }

    public User queryUser(String username, String password) {

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username",username);
        User user = this.userMapper.selectOne(qw);

        if(user==null){
            return null;
        }
        //校验密码
        if(!user.getPassword().equals(CodecUtils.md5Hex(password,user.getSalt()))){
            return null;
        }
        //用户名密码都正确
        return user;
    }


}
