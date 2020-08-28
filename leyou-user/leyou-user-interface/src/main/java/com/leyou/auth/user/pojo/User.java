package com.leyou.auth.user.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/11/011 13:53
 */
@TableName("tb_user")
@Data
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Length(min=4,max=30,message="用户名只能在4-30位之间")
    private String username;    //用户名

    /**
     * 为了安全考虑，这里对password和salt添加了注解@JsonIgnore，这样在序列化时，
     * 就不会把password和salt返回。
     */
    @JsonIgnore
    @Length(min=4,max=30,message = "密码只能在4-30位之间")
    private String password;    //密码

    @Pattern(regexp = "^1[35678]\\d{9}",message = "手机号格式不正确")
    private String phone;       //电话


    private Date created;       //创建时间

    @JsonIgnore
    private String salt;        //密码的盐值

}
