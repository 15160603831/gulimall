package com.hwj.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.hwj.common.to.es.SkuEsModel;
import com.hwj.mall.search.config.MallElasticSearchConfig;
import com.hwj.mall.search.constant.EsConstant;
import com.hwj.mall.search.service.MallSearchService;
import com.hwj.mall.search.vo.SearchParamVO;
import com.hwj.mall.search.vo.SearchResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hwj
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

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
            SearchResponse search = client.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
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
        if (param.getHasStock()!=null){
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
        }
        //1.2、bool - filter 按照是指定属性进行查询
        List<String> attrs = param.getAttrs();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attr : attrs) {
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValue = s[1].split(":");
                queryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                queryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
            }
        }
        //每一个都需要生成 nested
        NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", queryBuilder, ScoreMode.None);
        boolQueryBuilder.filter(nestedQuery);
        //所有条件都拿来封装
        builder.query(boolQueryBuilder);


        //2  排序 、分页、高亮
        //2.1 order 排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            //排序条件 sort=price/salecount/hotscore_desc/asc
            String[] s = sort.split("_");
            SortOrder ase = s[1].equalsIgnoreCase("ase") ? SortOrder.ASC : SortOrder.DESC;
            builder.sort(s[0], ase);
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
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder("attrsAgg", "attrs");
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
        ParsedNested attrsAgg = searchResponse.getAggregations().get("attrsAgg");
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


        return result;
    }
}
