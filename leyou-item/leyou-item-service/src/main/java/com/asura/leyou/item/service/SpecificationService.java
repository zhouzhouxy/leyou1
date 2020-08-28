package com.asura.leyou.item.service;

import com.asura.leyou.item.mapper.SpecGroupMapper;
import com.asura.leyou.item.mapper.SpecParamMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.auth.item.pojo.SpecGroup;
import com.leyou.auth.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据条件查询规格参数
     *
     * @param cid
     * @param gid
     * @param generic
     * @param searching
     * @return
     */
    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching){
        QueryWrapper<SpecParam> query = new QueryWrapper<>();

        if(gid!=null){
            query.eq("group_id",gid);
        }
        if(cid!=null){
            query.eq("cid",cid);
        }
        if(generic!=null){
            query.eq("generic",generic);
        }
        if(searching!=null){
            query.eq("searching",searching);
        }
        return this.specParamMapper.selectList(query);
    }

    /**
     * 根据分类id查询分组
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsByCid(Long cid){
        QueryWrapper<SpecGroup> query=new QueryWrapper<>();
        query.eq("cid",cid);
        return specGroupMapper.selectList(query);
    }

    public List<SpecGroup> querySpecsByCid(Long cid) {
        //查询规格组
        List<SpecGroup> groups = this.queryGroupsByCid(cid);
        groups.forEach(g->{
            //查询组内参数
            g.setParams(this.queryParams(g.getId(),null,null,null));
        });
        return groups;
    }
}
