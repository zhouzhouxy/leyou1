package com.asura.search.service;

import com.asura.search.client.BrandClient;
import com.asura.search.client.CategoryClient;
import com.asura.search.client.GoodsClient;
import com.asura.search.client.SpecificationClient;
import com.asura.search.domain.Goods;
import com.asura.search.domain.SearchRequest;
import com.asura.search.domain.SearchResult;
import com.asura.search.reponsitory.GoodsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.auth.common.pojo.PageResult;
import com.leyou.auth.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;

import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

     private static final Logger LOGGER= LoggerFactory.getLogger(SearchService.class);
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private SpecificationClient specificationClient;

    private static final ObjectMapper MAPPER=new ObjectMapper();

    public Goods buildGoods(Spu spu) throws JsonProcessingException {
        //创建goods对象
        Goods goods = new Goods();
        //查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
        //查询分类名称
        List<String> names=this.categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
        //查询spu下所有的sku
        List<Sku> skus = this.goodsClient.querySkuBySpuId(spu.getId());
        List<Long> prices= new ArrayList<>();
        List<Map<String,Object>> skuMapList=new ArrayList<>();
        //遍历skus,获取价格集合
        skus.forEach(sku->{
            prices.add(sku.getPrice());
            Map<String,Object> skuMap=new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            skuMap.put("image",
                    StringUtils.isNotBlank(sku.getImages())?
                    StringUtils.split(sku.getImages(),",")[0]:"");
            skuMapList.add(skuMap);
        });

        //查询出所有的搜索规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, true);
        //查询spuDetail，获取规格参数值
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spu.getId());
        //获取通用的规格参数
        Map<Long, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<Long, Object>>() {
        });
        // 获取特殊的规格参数
        Map<Long, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<Object>>>() {
        });
        //定义map接收{规格参数名，规格参数值}
        Map<String, Object> paramMap = new HashMap<>();
        params.forEach(param->{
            //判断是否通用规格参数
            if(param.getGeneric()){
                //获取通用规格参数值
                String value=genericSpecMap.get(param.getId()).toString();
                //判断是否是数值类型
                if(param.getNumc()){
                    //如果是数值的话，判断该数值落在那个区间
                    value=chooseSegment(value,param);
                }
                //把参数名和值放入结果集中
                paramMap.put(param.getName(),value);
            }else{
                paramMap.put(param.getName(),specialSpecMap.get(param.getId()));
            }
        });

        //设置参数
        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        goods.setAll(spu.getTitle()+brand.getName()+
                StringUtils.join(names," "));
        goods.setPrice(prices);
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        goods.setSpecs(paramMap);
        return goods;
    }

    private String chooseSegment(String value, SpecParam param) {
        double val= NumberUtils.toDouble(value);
        String result="其它";
        //保存数值短
        for (String segment : param.getSegments().split(".")){
            String[] segs=segment.split("-");
            //获取数值范围
            double begin=NumberUtils.toDouble(segs[0]);
            double end=Double.MAX_VALUE;
            if(segs.length==2){
                end=NumberUtils.toDouble(segs[1]);
            }

            //判断是否在范围内
            if(val>=begin&&val<end){
                if(segs.length==1){
                    result=segs[0]+ param.getUnit()+"以上";
                }else if(begin==0){
                    result=segs[1]+ param.getUnit()+"以下";
                }else {
                    result=segment+ param.getUnit();
                }
                break;
            }
        }
        return result;
    }

    @Autowired
    private GoodsRepository repository;

    public PageResult<Goods> search1(SearchRequest request) {
        String key = request.getKey();
        //判断是否有搜索条件，如果没有，直接返回null，不允许搜索全部商品
        if(StringUtils.isBlank(key)){
            return null;
        }
        //构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //1.对key
        queryBuilder.withQuery(QueryBuilders.matchQuery("all",key).operator(Operator.AND));
        //通过sourceFilter设置返回的结果字段，我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id","skus","subTitle"},null));
        //3、分页
        int page=request.getPage();
        int size=request.getSize();
        queryBuilder.withPageable(PageRequest.of(page-1,size));


        //排序
        String sortBy=request.getSortBy();
        Boolean desc=request.getDescending();
        if(StringUtils.isNotBlank(sortBy)){
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc?SortOrder.DESC:SortOrder.ASC));
        }
        //4.查询
        Page<Goods> pageInfo = this.repository.search(queryBuilder.build());

        //封装结果并返回
        return new PageResult<>(pageInfo.getTotalElements(),pageInfo.getTotalPages(),pageInfo.getContent());
    }

    /**
     * 解析品牌聚合结果集
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        //强转成LongTerms
        LongTerms terms=(LongTerms)aggregation;
        //获取桶集合
        return terms.getBuckets().stream().map(bucket ->{
            Long id=bucket.getKeyAsNumber().longValue();
            return this.brandClient.queryBrandById(id);
           }
        ).collect(Collectors.toList());
    }

    /**
     * 解析分类聚合结果集
     * @param aggregation
     * @return
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms terms=(LongTerms)aggregation;

        return terms.getBuckets().stream().map(bucket -> {
            long cid3 = bucket.getKeyAsNumber().longValue();
            Map<String, Object> map=new HashMap<>();
            map.put("id",cid3);
            //通过cid3查询分类名称
            List<String> cnames = this.categoryClient.queryNamesByIds(Arrays.asList(cid3));
            map.put("name",cnames.get(0));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 聚合商品分类和品牌
     *我们修改搜索的业务逻辑，对分类和品牌聚合。
     * 因为索引库中只有id，所以我们根据id聚合，然后再根据id去查询完整数据。
     * 所以，商品微服务需要提供一个接口：根据品牌id集合，批量查询品牌
     * @param request
     * @return
     */
    public SearchResult search(SearchRequest request) {
        String key = request.getKey();
        //判断是否有搜索条件，如果没有，直接返回null，不允许搜索全部商品
        if(StringUtils.isBlank(key)){
            return null;
        }
        //构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //1.对key
        //因为要用到参数过滤不能用下面的查询方法所以进行改造
        // MatchQueryBuilder basicQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);
        BoolQueryBuilder boolQueryBuilder=buildBooleanQueryBuilder(request);
        queryBuilder.withQuery(boolQueryBuilder);
        //通过sourceFilter设置返回的结果字段，我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id","skus","subTitle"},null));
        //3、分页
        int page=request.getPage();
        int size=request.getSize();
        queryBuilder.withPageable(PageRequest.of(page-1,size));
       //排序
        String sortBy=request.getSortBy();
        Boolean desc=request.getDescending();
        if(StringUtils.isNotBlank(sortBy)){
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc?SortOrder.DESC:SortOrder.ASC));
        }
        //4.查询
        Page<Goods> pageInfo = this.repository.search(queryBuilder.build());
        String categoryAggName="categories";
        String brandAggName="brands";

        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //执行搜索,获取搜索的结果集
        AggregatedPage<Goods> goodsPage=
                (AggregatedPage<Goods>)this.repository.search(queryBuilder.build());

        //解析聚合结果集
        List<Map<String,Object>> categories=
                getCategoryAggResult(goodsPage.getAggregation(categoryAggName));
        List<Brand> brands=
                getBrandAggResult(goodsPage.getAggregation(brandAggName));
        //判断分类聚合的结果集大小，等于1则聚合
        List<Map<String,Object>> specs=null;
        if(!categories.isEmpty()||categories.size()==1){
            specs= getParamAggResult(categories.get(0).get("id"),boolQueryBuilder);
        }

        //封装结果并返回
        return new SearchResult(goodsPage.getContent(),
                goodsPage.getTotalElements(),goodsPage.getTotalPages(),
                categories,brands,specs);
    }

    /**
     * 构建bool查询构建器
     * @param request
     * @return
     */
    private BoolQueryBuilder buildBooleanQueryBuilder(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //添加基本查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",
                request.getKey()).operator(Operator.AND));
        //添加过滤条件
        if(CollectionUtils.isEmpty(request.getFilter())){
            return boolQueryBuilder;
        }
        for (Map.Entry<String, String> entry : request.getFilter().entrySet()) {
            String key = entry.getKey();
            //如果过滤条件是“品牌”，过滤字段名：brandId
            if(StringUtils.equals("品牌",key)){
                key="brandId";
            }else if(StringUtils.equals("分类",key)){
                //如果是"分类"，过滤字段名：cid3
                key="cid3";
            }else{
                //如果是规格参数名，过滤字段名：specs.key.keyword
                key="specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return boolQueryBuilder;
    }

    /**
     * 聚合出规格参数过滤条件
     * @param id
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getParamAggResult(Object id, BoolQueryBuilder basicQuery) {
      /*  //创建自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //基于基本的查询条件，聚合规格参数
        queryBuilder.withQuery(basicQuery);
        //查询要聚合的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, (Long) id, null, true);
        //添加聚合
        params.forEach(param->{
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("spcs."+param.getNumc()+".keyword"));
        });
        //只需要聚合结果集，不需要查询结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        //执行聚合查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) this.repository.search(queryBuilder.build());
        LOGGER.info(goodsPage.toString());
        //定义一个集合，收集聚合结果集
        List<Map<String,Object>> paramMapList= new ArrayList<>();
        //获取所有的规格参数聚合结果集Map<paramMap,aggregation>
        Map<String,Aggregation> aggregationMap=
                goodsPage.getAggregations().asMap();
        for (Map.Entry<String, Aggregation> entry :
                aggregationMap.entrySet()) {
            Map<String,Object> map=new HashMap<>();
            //放入规格参数名,设置k字段
            map.put("k",entry.getKey());

            //解析每个聚合
            StringTerms terms=(StringTerms)entry.getValue();
            //遍历每个聚合中的桶，把桶中的可以放入收集规格参数的集合中
            List<Object> options=terms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            map.put("options",options);
            paramMapList.add(map);
        }
        return paramMapList;*/
        // 创建自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 基于基本的查询条件，聚合规格参数
        queryBuilder.withQuery(basicQuery);
        // 查询要聚合的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, (Long) id, null, true);
        // 添加聚合
        params.forEach(param -> {
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs." + param.getName() + ".keyword"));
        });
        // 只需要聚合结果集，不需要查询结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));

        // 执行聚合查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.repository.search(queryBuilder.build());

        // 定义一个集合，收集聚合结果集
        List<Map<String, Object>> paramMapList = new ArrayList<>();
        // 解析聚合查询的结果集
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for (Map.Entry<String, Aggregation> entry : aggregationMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            // 放入规格参数名
            map.put("k", entry.getKey());
            // 收集规格参数值
            List<Object> options = new ArrayList<>();
            // 解析每个聚合
            StringTerms terms = (StringTerms)entry.getValue();
            // 遍历每个聚合中桶，把桶中key放入收集规格参数的集合中
            terms.getBuckets().forEach(bucket -> options.add(bucket.getKeyAsString()));
            map.put("options", options);
            paramMapList.add(map);
        }

        return paramMapList;
    }

    public void createIndex(Long id) throws JsonProcessingException {
        Spu spu = this.goodsClient.querySpuById(id);
        //构建商品
        Goods goods = this.buildGoods(spu);

        //保存数据到索引库
        this.repository.save(goods);
    }

    public void deleteIndex(Long id) {
        this.repository.deleteById(id);
    }
}

