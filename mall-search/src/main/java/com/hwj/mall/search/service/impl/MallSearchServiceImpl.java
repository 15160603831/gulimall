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
import com.hwj.mall.search.vo.BrandResponseVo;
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
import java.util.NavigableMap;
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
     * @param searchParamVO ???????????????
     * @return ?????????????????????
     */
    @Override
    public SearchResult search(SearchParamVO searchParamVO) {

        //??????DSL??????
        SearchResult result = null;

        //??????????????????
        SearchRequest searchRequest = buildSearchRequest(searchParamVO);


        try {
            //??????????????????
            SearchResponse search = restHighLevelClient.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
            //????????????????????????????????????
            result = buildSearchResult(search, searchParamVO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /***
     * ??????????????????
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParamVO param) {
        //??????DSL??????
        SearchSourceBuilder builder = new SearchSourceBuilder();
        /**
         * ?????????????????????
         */
        //1???bool -query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1 bool must?????????????????????
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2???bool - filter ????????????id
        if (param.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2???bool - filter ??????id??????
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //1.2???bool - filter ???????????????0-????????? 1-????????????
        if (param.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        //1.2???bool - filter ?????????????????? 1_500 || 4_700
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

        //1.2.5 attrs-nested
        //attrs=1_5???:8???&2_16G:8G
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
        //1. bool query????????????
        builder.query(boolQueryBuilder);


        //2  ?????? ??????????????????
        //2.1 order ??????
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            //???????????? sort=price/salecount/hotscore_desc/asc
            String[] s = sort.split("_");
            builder.sort(s[0], s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC);
        }
        //2.2 ??????
        builder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        builder.size(EsConstant.PRODUCT_PAGESIZE);
        //2.3 ??????
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        //????????????
        //1.????????????
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg");
        brandAgg.field("brandId").size(50);
        //1.1 ?????????????????????
        brandAgg.subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brandImgAgg").field("brandImg").size(1));
        builder.aggregation(brandAgg);

        //1.2 catalogAgg??????
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalogAgg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalogNameAgg").field("catalogName").size(1));
        builder.aggregation(catalogAgg);

        //1.3 attrs??????
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder("attrs", "attrs");
        //??????attrId??????
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        //??????attrId?????????????????????attrName???attrValue??????
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attrNameAgg").field("attrs.attrName");
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue");
        attrIdAgg.subAggregation(attrNameAgg);
        attrIdAgg.subAggregation(attrValueAgg);

        nestedAggregationBuilder.subAggregation(attrIdAgg);
        builder.aggregation(nestedAggregationBuilder);

        System.out.println("?????????DSL???" + builder.toString());
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);
        return searchRequest;
    }

    /**
     * ??????????????????
     *
     * @param
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse searchResponse, SearchParamVO param) {

        SearchResult result = new SearchResult();
        SearchHits hits = searchResponse.getHits();
        //??????????????????????????????
        //2.1 ????????????
        result.setPageNum(param.getPageNum());
        //2.2 ????????????
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //2.3 ?????????
        Integer totalPage =
                (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ?
                        (int) total / EsConstant.PRODUCT_PAGESIZE : (int) total / EsConstant.PRODUCT_PAGESIZE + 1;
        result.setTotalPages(totalPage);

//??????????????????????????????

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
//3. ??????????????????????????????

        List<SearchResult.BrandVO> brandVOS = new ArrayList<>();
        ParsedLongTerms brandAgg = searchResponse.getAggregations().get("brandAgg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            //??????id
            Long brandId = bucket.getKeyAsNumber().longValue();
            //????????????
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brandNameAgg")).getBuckets().get(0).getKeyAsString();
            //????????????
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brandImgAgg")).getBuckets().get(0).getKeyAsString();
            SearchResult.BrandVO brandVO = new SearchResult.BrandVO(brandId, brandName, brandImg);
            brandVOS.add(brandVO);
        }

        result.setBrands(brandVOS);
//4. ??????????????????????????????
        ParsedLongTerms catalogAgg = searchResponse.getAggregations().get("catalogAgg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();

            //????????????ID
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //???????????????

            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalogNameAgg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        //5 ??????????????????????????????
        ParsedNested attrsAgg = searchResponse.getAggregations().get("attrs");
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            //??????id
            long attrId = bucket.getKeyAsNumber().longValue();
            //????????????
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attrNameAgg")).getBuckets().get(0).getKeyAsString();
            //???????????????
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

        // 6. ?????????????????????
        List<String> attrs = param.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                //???????????????????????????attr???
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] split = attr.split("_");
                //6.1 ???????????????
                navVo.setNavValue(split[1]);
                //6.2 ????????????????????????
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
                    log.error("??????????????????????????????????????????", e);
                }
                //6.3 ???????????????????????????
//                String encode = null;
//                try {
//                    encode = URLEncoder.encode("attr", "UTF-8");
//                    encode.replace("+", "%20");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
                String queryString = param.get_queryString();
                String replace = queryString.replace("&attrs=" + attr, "").replace("attrs=" + attr + "&", "").replace("attrs=" + attr, "");
                navVo.setLink("http://search.mall.com/list.html" + (replace.isEmpty() ? "" : "?" + replace));
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }
        return result;
    }
}
