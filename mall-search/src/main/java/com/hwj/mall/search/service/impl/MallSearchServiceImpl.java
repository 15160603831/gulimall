package com.hwj.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hwj.common.to.es.SkuEsModel;
import com.hwj.common.utils.R;
import com.hwj.mall.search.config.MallElasticSearchConfig;
import com.hwj.mall.search.constant.EsConstant;
import com.hwj.mall.search.feign.ProductFeignService;
import com.hwj.mall.search.service.MallSearchService;
import com.hwj.mall.search.vo.AttrResponseVo;
import com.hwj.mall.search.vo.SearchParamVO;
import com.hwj.mall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hwj
 */
@Service
@Slf4j
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private ProductFeignService productFeignService;

    /**
     * @param searchParamVO 检索的条件
     * @return 返回检索的结果
     */
    @Override
    public SearchResult search(SearchParamVO searchParamVO) {

        //构建DSL语句
        SearchResult result = null;

        //准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParamVO);


        try {
            //执行检索请求
            SearchResponse search = restHighLevelClient.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
            //分析响应数据封装需要格式
            result = buildSearchResult(search, searchParamVO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /***
     * 准备检索请求
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParamVO param) {
        //构建DSL语句
        SearchSourceBuilder builder = new SearchSourceBuilder();
        /**
         * 模糊查询，过滤
         */
        //1、bool -query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1 bool must模糊匹配关键字
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2、bool - filter 三级分类id
        if (param.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2、bool - filter 品牌id查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //1.2、bool - filter 库存校验（0-无库存 1-有库存）
        if (param.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        //1.2、bool - filter 价格区间检索 1_500 || 4_700
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        //1.2、bool - filter 按照是指定属性进行查询
//        List<String> attrs = param.getAttrs();
//        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
//            for (String attr : attrs) {
//                String[] s = attr.split("_");
//                String attrId = s[0];
//                String[] attrValue = s[1].split(":");
//                queryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
//                queryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
//            }
//        }
//        //每一个都需要生成 nested
//        NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", queryBuilder, ScoreMode.None);
//        boolQueryBuilder.filter(nestedQuery);
//        //所有条件都拿来封装
//        builder.query(boolQueryBuilder);

        //1.2.5 attrs-nested
        //attrs=1_5寸:8寸&2_16G:8G
        List<String> attrs = param.getAttrs();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        if (attrs != null && attrs.size() > 0) {
            attrs.forEach(attr -> {
                String[] attrSplit = attr.split("_");
                queryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrSplit[0]));
                String[] attrValues = attrSplit[1].split(":");
                queryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
            });
        }
        NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", queryBuilder, ScoreMode.None);
        boolQueryBuilder.filter(nestedQueryBuilder);
        //1. bool query构建完成
        builder.query(boolQueryBuilder);


        //2  排序 、分页、高亮
        //2.1 order 排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            //排序条件 sort=price/salecount/hotscore_desc/asc
            String[] s = sort.split("_");
            builder.sort(s[0], s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC);
        }
        //2.2 分页
        builder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        builder.size(EsConstant.PRODUCT_PAGESIZE);
        //2.3 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        //聚合分析
        //1.品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg");
        brandAgg.field("brandId").size(50);
        //1.1 品牌聚合子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName.keyword").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brandImgAgg").field("brandImg.keyword").size(1));
        builder.aggregation(brandAgg);

        //1.2 catalogAgg聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalogAgg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalogNameAgg").field("catalogName.keyword").size(1));
        builder.aggregation(catalogAgg);

        //1.3 attrs聚合
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder("attrs", "attrs");
        //按照attrId聚合
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        //按照attrId聚合之后再按照attrName和attrValue聚合
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attrNameAgg").field("attrs.attrName.keyword");
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue.keyword");
        attrIdAgg.subAggregation(attrNameAgg);
        attrIdAgg.subAggregation(attrValueAgg);

        nestedAggregationBuilder.subAggregation(attrIdAgg);
        builder.aggregation(nestedAggregationBuilder);

        System.out.println("构建的DSL：" + builder.toString());
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);
        return searchRequest;
    }

    /**
     * 构建结果数据
     *
     * @param
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse searchResponse, SearchParamVO param) {

        SearchResult result = new SearchResult();
        SearchHits hits = searchResponse.getHits();
        //返回所有查询到的商品
        //2.1 当前页码
        result.setPageNum(param.getPageNum());
        //2.2 总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //2.3 总页码
        Integer totalPage =
                (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ?
                        (int) total / EsConstant.PRODUCT_PAGESIZE : (int) total / EsConstant.PRODUCT_PAGESIZE + 1;
        result.setTotalPages(totalPage);

//以下从聚合信息中获取

        ArrayList<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].toString();
                    esModel.setSkuTitle(string);
                }

                esModels.add(esModel);
            }
            result.setProducts(esModels);
        }
//3. 查询结果涉及到的品牌

        List<SearchResult.BrandVO> brandVOS = new ArrayList<>();
        ParsedLongTerms brandAgg = searchResponse.getAggregations().get("brandAgg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            //品牌id
            Long brandId = bucket.getKeyAsNumber().longValue();
            //品牌名字
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brandNameAgg")).getBuckets().get(0).getKeyAsString();
            //品牌图片
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brandImgAgg")).getBuckets().get(0).getKeyAsString();
            SearchResult.BrandVO brandVO = new SearchResult.BrandVO(brandId, brandName, brandImg);
            brandVOS.add(brandVO);
        }

        result.setBrands(brandVOS);
//4. 查询涉及到的所有分类
        ParsedLongTerms catalogAgg = searchResponse.getAggregations().get("catalogAgg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();

            //得到分类ID
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //得到分类名

            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalogNameAgg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        //5 查询涉及到的所有属性
        ParsedNested attrsAgg = searchResponse.getAggregations().get("attrs");
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            //属性id
            long attrId = bucket.getKeyAsNumber().longValue();
            //属性名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attrNameAgg")).getBuckets().get(0).getKeyAsString();
            //属性所有值
            List<String> attrValue = ((ParsedStringTerms) bucket.getAggregations().get("attrValueAgg")).getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo(attrId, attrName, attrValue);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // 6. 构建面包屑导航
        List<String> attrs = param.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                //分析每一个传过来的attr值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] split = attr.split("_");
                //6.1 设置属性值
                navVo.setNavValue(split[1]);
                //6.2 查询并设置属性名
                try {
                    R r = productFeignService.attrInfo(Long.parseLong(split[0]));
                    if (r.getCode() == 0) {
                        AttrResponseVo data = JSON.parseObject(JSON.toJSONString(r.get("attr")), new TypeReference<AttrResponseVo>() {
                        });
                        navVo.setNavName(data.getAttrName());
                    } else {
                        navVo.setNavName(split[0]);
                    }
                } catch (Exception e) {
                    log.error("远程调用商品服务查询属性失败", e);
                }
                //6.3 设置面包屑跳转链接
                String encode = null;
                try {
                    encode = URLEncoder.encode("attr", "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String queryString = param.get_queryString();
                String replace = queryString.replace("&attrs=" + encode, "")
                        .replace("attrs=" + attr + "&", "").replace("attrs=" + attr, "");
                navVo.setLink("http://search.mall.com/list.html" + (replace.isEmpty() ? "" : "?" + replace));
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }
        return result;
    }
}
