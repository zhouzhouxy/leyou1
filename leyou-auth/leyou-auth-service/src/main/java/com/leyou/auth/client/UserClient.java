package com.leyou.auth.client;

import com.leyou.auth.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/15/015 18:04
 */
@FeignClient("user-service")
public interface UserClient extends UserApi {
}
