package com.asura.leyou.item.controller;


import com.asura.leyou.item.service.CategoryService;
import com.asura.leyou.item.service.GoodsService;
import com.leyou.auth.common.pojo.PageResult;
import com.leyou.auth.item.bo.SpuBo;
import com.leyou.auth.item.pojo.Sku;
import com.leyou.auth.item.pojo.Spu;
import com.leyou.auth.item.pojo.SpuDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin
@RestController
@EnableConfigurationProperties
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private CategoryService categoryService;
    /**
     *
     * @param key   过滤条件
     * @param saleable  上架或下架
     * @param page      当前页
     * @param rows      每页大小
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuBoByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value="saleable",required = false)Boolean saleable,
            @RequestParam(value="page",defaultValue = "1")Integer page,
            @RequestParam(value="rows",defaultValue = "5")Integer rows
    ){
        PageResult<SpuBo> pageResult = this.goodsService.querySpuBoByPage(key, saleable, page, rows);
        if(CollectionUtils.isEmpty(pageResult.getItems())){
            return ResponseEntity.notFound().build();




        }
        return ResponseEntity.ok(pageResult);
    }

    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        System.out.println(spuBo);
        this.goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail>
    querySpuDetailBySpuId(@PathVariable("spuId")Long spuId){
        SpuDetail spuDetail=this.goodsService.querySpuDetailBySpuId(spuId);
        if(spuDetail==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuDetail);
    }

    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>>
        querySkuBySpuId(@RequestParam("id")Long spuId){
        List<Sku> list=this.goodsService.querySkuSpuId(spuId);
        if(CollectionUtils.isEmpty(list)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){
        Spu spu=this.goodsService.querySpuById(id);
        if(spu==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spu);
    }

    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("id")Long id){
         Sku sku = this.goodsService.querySkuById(id);
         if(sku==null){
             return new ResponseEntity<>(HttpStatus.NOT_FOUND);
         }
         return ResponseEntity.ok(sku);
    }

}