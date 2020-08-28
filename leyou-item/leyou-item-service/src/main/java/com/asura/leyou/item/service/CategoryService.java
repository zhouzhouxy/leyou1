package com.asura.leyou.item.service;

import com.asura.leyou.item.mapper.CategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.auth.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据parentId查询子类目
     * @param pid
     * @return
     */
    public List<Category> queryCategoriesByPid(Long pid){
        QueryWrapper<Category> query = new QueryWrapper<>();
        query.lambda().eq(Category::getParentId,pid);
        return this.categoryMapper.selectList(query);
    }

    public List<String> queryNameByIds(List<Long> ids) {
        List<Category> list = this.categoryMapper.selectBatchIds(ids);

        List<String> names = new ArrayList<>();

        for (Category category : list) {
            names.add(category.getName());
        }
        return names;
        // return list.stream().map(category ->
        // category.getName()).collect(Collectors.toList());
    }

    public List<Category> queryAllByCid3(Long id) {
        Category c3 = this.categoryMapper.selectById(id);
        Category  c2= this.categoryMapper.selectById(c3.getParentId());
        Category c1 = this.categoryMapper.selectById(c2.getParentId());

        return Arrays.asList(c1,c2,c3);
    }
}
