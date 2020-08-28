package com.asura.leyou.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.auth.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {

    /**
     * 新增商品分类和品牌中间表数据
     * @param cid
     * @param bid
     * @return
     */
    @Insert("insert into tb_category_brand(category_id,brand_id) values(#{cid},#{bid})")
    int insertCategoryAndBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    @Select("select b.* from tb_category_brand cb,tb_brand b,tb_category c\n" +
            "where cb.category_id=c.id and cb.brand_id=b.id\n" +
            "and c.id=#{cid}")
    List<Brand> queryBrandByCid(@Param("cid")Long cid);
}
