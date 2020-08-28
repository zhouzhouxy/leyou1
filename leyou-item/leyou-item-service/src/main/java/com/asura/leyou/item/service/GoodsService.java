package com.asura.leyou.item.service;

import com.asura.leyou.item.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.auth.common.pojo.PageResult;
import com.leyou.auth.item.bo.SpuBo;
import com.leyou.auth.item.pojo.Sku;
import com.leyou.auth.item.pojo.Spu;
import com.leyou.auth.item.pojo.SpuDetail;
import com.leyou.auth.item.pojo.Stock;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class GoodsService {

    Logger logger=LoggerFactory.getLogger(GoodsService.class);

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    public PageResult querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {
        QueryWrapper<Spu> query = new QueryWrapper<>();
        //搜索条件
        if(StringUtils.isNotBlank(key)){
            query.lambda().like(Spu::getTitle,key);
        }
        if(saleable!=null){
            query.eq("saleable",saleable);
        }
        //分页条件
        Page<Spu> page1 = this.spuMapper.selectPage(new Page<>(page, rows), query);
        List<Spu> spus=page1.getRecords();

        List<SpuBo> spuBos = new ArrayList<>();

        spus.forEach(spu-> {
            SpuBo spuBo=new SpuBo();
            //copy共同属性的值到新的对象
            BeanUtils.copyProperties(spu,spuBo);
            //查询分类名称
            List<String> names=this.categoryService.queryNameByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));

            spuBo.setCname(StringUtils.join(names,"/"));

            //查询品牌的名称
            spuBo.setBname(this.brandMapper.selectById(spu.getBrandId()).getName());
            spuBos.add(spuBo);
        });
        //总页数
        Integer totalPage= Math.toIntExact(page1.getTotal() % rows == 0 ? page1.getTotal() / rows : page1.getTotal() / rows + 1);
        return new PageResult<>(page1.getTotal(),totalPage,spuBos);
    }

    /**
     * 新增商品
     * @param spuBo
     */
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //新增spu
        //设置默认字段

        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insert(spuBo);
        //新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insert(spuDetail);
        
        saveSkuAndStock(spuBo);

        sendMessage(spuBo.getId(),"insert");
    }

    private void saveSkuAndStock(SpuBo spuBo) {

        spuBo.getSkus().forEach(sku -> {
            // 新增sku
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);
            // 新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        });
    }

    /**
     *  根据SpuId查询spuDetail
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectById(spuId);
    }

    /**
     * 根据spuId查询Spu
     * @param spuId
     * @return
     */
    public List<Sku> querySkuSpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);

        QueryWrapper<Sku> query=new QueryWrapper<>();
        query.eq("spu_id",spuId);
        List<Sku> skus = this.skuMapper.selectList(query);
        skus.forEach(s->{
            Stock stock = this.stockMapper.selectById(s.getId());
            s.setStock(stock.getStock());
        });
        return  skus;
    }


    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //查询以前sku
        QueryWrapper<Sku> query = new QueryWrapper<>();
        query.eq("spu_id",spuBo.getId());
        List<Sku> skus = this.skuMapper.selectList(query);
        if(!CollectionUtils.isEmpty(skus)){
            List<Long> ids = skus.stream().map(s -> s.getId()).collect(Collectors.toList());
            //删除以前库存
            this.stockMapper.deleteBatchIds(ids);
            //删除以前的sku
            UpdateWrapper<Sku> wrapper=new UpdateWrapper<>();
            wrapper.eq("spu_id",spuBo.getId());
            this.skuMapper.delete(wrapper);
        }
        // 新增sku和库存
        saveSkuAndStock(spuBo);
        //更新spu
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        this.spuMapper.updateById(spuBo);

        //更新spu详情
        this.spuDetailMapper.updateById(spuBo.getSpuDetail());

        this.sendMessage(spuBo.getId(),"update");
     }

    public Spu querySpuById(Long id) {
        return this.spuMapper.selectById(id);
    }

    @Autowired
    private AmqpTemplate amqpTemplate;

    private void sendMessage(Long id,String type){
        //发送消息
        try {
            this.amqpTemplate.convertAndSend("item."+type,id);
        } catch (AmqpException e) {
            logger.error("{}商品信息发送异常，商品id:{}",type,id,e);
        }
    }

    public Sku querySkuById(Long id) {
        return skuMapper.selectById(id);
    }
}
