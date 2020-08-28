package com.asura.search.controller;

import com.asura.search.domain.SearchRequest;
import com.asura.search.domain.SearchResult;
import com.asura.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("page")
    public ResponseEntity<SearchResult> search(@RequestBody SearchRequest request){
        SearchResult result=this.searchService.search(request);
        if(result==null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
         return ResponseEntity.ok(result);
    }
}
