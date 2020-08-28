package com.asura.test;

import com.asura.search.LeyouSearchService;
import com.asura.search.client.GoodsClient;
import com.asura.search.domain.Goods;
import com.asura.search.reponsitory.GoodsRepository;
import com.asura.search.service.SearchService;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Spu;
import com.netflix.discovery.converters.Auto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouSearchService.class)
public class GoodsRepositoryTest {

    /**
     * 创建索引
     */

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;
    /**
     * 导入数据
     * 导入数据其实就是查询数据，然后把查询到的Spu转变Goods来保存，因此我们先编写一个SearchService
     * 然后在里面定义一个方法，把spu转为Goods
     */
    @Test
    public void createIndex(){
        //创建索引库，以及映射
        this.template.createIndex(Goods.class);
        this.template.putMapping(Goods.class);

        Integer page=1;
        Integer rows=100;

        do {
            //分批查询spuBo
             PageResult<SpuBo> pageResult = this.goodsClient.querySpuBoByPage(null,true,page,rows);

            //遍历spubo集合转化为List<Goods>
           List<Goods> goodsList= pageResult.getItems().stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoods((Spu)spuBo);
                }catch (IOException e){
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);

            //获取当前页的数据条数，如果是最后一页，没有100条
            rows=pageResult.getItems().size();
            //每次训话页码加1
            page++;
        }while(rows==100);

    }

    @Test
    public void deleteIndex(){
        this.template.deleteIndex(Goods.class);
    }


}