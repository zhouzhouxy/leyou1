package com.leyou.auth.item.api;

import com.leyou.auth.item.pojo.SpecGroup;
import com.leyou.auth.item.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("spec")
public interface SpecificationApi {
    @GetMapping("groups/{id}")
    public List<SpecGroup> querySpecGroups(@PathVariable("cid")Long cid);

    @GetMapping("params")
    public List<SpecParam> queryParams(
            @RequestParam(value="gid",required = false)Long gid,
            @RequestParam(value="cid",required = false)Long cid,
            @RequestParam(value="generic",required = false)Boolean generic,
            @RequestParam(value="searching",required = false)Boolean searching
            );
    //查询规格参数组，及组内参数
    @GetMapping("{cid}")
    public List<SpecGroup>
      querySpecsByCid(@PathVariable("cid")Long cid);
}
