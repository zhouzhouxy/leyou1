package com.asura.leyou.item.controller;

import com.asura.leyou.item.service.SpecificationService;
import com.leyou.auth.item.pojo.SpecGroup;
import com.leyou.auth.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>>
        queryGroupsByCid(@PathVariable("cid")Long cid){
        System.out.println(cid);
        List<SpecGroup> specGroups = this.specificationService.queryGroupsByCid(cid);
        if(CollectionUtils.isEmpty(specGroups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(specGroups);
    }

    @GetMapping("params")
    public ResponseEntity<List<SpecParam>>
        queryParams(@RequestParam(value = "gid",required = false)Long gid,
                    @RequestParam(value = "cid",required = false)Long cid,
                    @RequestParam(value="generic",required = false)Boolean generic,
                    @RequestParam(value="searching",required = false)Boolean searching){
        List<SpecParam> params = this.specificationService.queryParams(gid,cid,generic,searching);
        if(CollectionUtils.isEmpty(params)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(params);
    }

    @GetMapping("{cid}")
    public ResponseEntity<List<SpecGroup>>
    querySpecsByCid(@PathVariable("cid")Long cid){
        List<SpecGroup> list=
                this.specificationService.querySpecsByCid(cid);
        if(list==null|| list.size()==0){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }
}
