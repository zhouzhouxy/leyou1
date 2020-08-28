package com.leyou.cart.client;

import com.leyou.auth.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author asura
 * @version 1.0.0
 * @date 2020/4/17/017 17:06
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
