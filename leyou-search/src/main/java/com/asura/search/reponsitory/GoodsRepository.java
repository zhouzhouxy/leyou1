package com.asura.search.reponsitory;

import com.asura.search.domain.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * 导入数据
 * 创建GoodsRepository
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
