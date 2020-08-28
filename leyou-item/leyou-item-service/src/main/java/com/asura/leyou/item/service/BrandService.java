package com.asura.leyou.item.service;

import com.asura.leyou.item.mapper.BrandMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.auth.common.pojo.PageResult;
import com.leyou.auth.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrandService {


    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBransByPage(String key,Integer page,
                                              Integer rows,String sortBy,Boolean desc){
        QueryWrapper<Brand> query = new QueryWrapper<>();
        //根据name模糊查询，或者根据首字母查询
        if(StringUtils.isNotBlank(key)){
            query.lambda().like(Brand::getName,key).or().eq(Brand::getLetter,key);
        }
        //添加排序条件
        if(StringUtils.isNotBlank(sortBy)){
            query.orderBy(true,!desc,sortBy);
        }
        if(StringUtils.isBlank(key)&&StringUtils.isBlank(sortBy)){
            query=null;
        }
        //添加分页
        Page<Brand> brandPage = this.brandMapper.selectPage(new Page<>(page, rows), query);

        //包装成分页结果集返回
        //总页数
        Integer totalPage= Math.toIntExact(brandPage.getTotal() % rows == 0 ? brandPage.getTotal() / rows : brandPage.getTotal() / rows + 1);
        return new PageResult<>(brandPage.getTotal(),totalPage,brandPage.getRecords());
    }

    /**
     * 新增品牌
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增brand
        this.brandMapper.insert(brand);

        //新增中间表
        cids.forEach(cid->this.brandMapper.insertCategoryAndBrand(cid,brand.getId()));
    }

    public List<Brand> queryBrandByCid(Long cid) {
        return brandMapper.queryBrandByCid(cid);
    }

    public Brand queryBrandById(Long id) {
        return brandMapper.selectById(id);
    }
}
