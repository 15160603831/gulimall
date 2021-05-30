package com.hwj.mall.search;

import com.alibaba.fastjson.JSON;
import com.hwj.mall.search.config.MallElasticSearchConfig;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void contextLoads() {

    }

    /**
     * 保存数据
     *
     * @throws IOException
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest request = new IndexRequest("users");
//        request.id("1");


        User user = new User();
        user.setUserName("黄三");
        user.setAge(100);
        user.setGender("女");
        String jsonString = JSON.toJSONString(user);

        //设置要保存的内容，指定数据和类型
        request.source(jsonString, XContentType.JSON);

        IndexResponse index = client.index(request, MallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Test
    public void find() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices("users");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 构造检索条件
//        sourceBuilder.query();
//        sourceBuilder.from();
//        sourceBuilder.size();
//        sourceBuilder.aggregation();
        sourceBuilder.query(QueryBuilders.matchQuery("userName", "三"));
        System.out.println(sourceBuilder.toString());

        searchRequest.source(sourceBuilder);

        // 2 执行检索
        SearchResponse response = client.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
        // 3 分析响应结果
        System.out.println(response.toString());
    }

    @Data
    class User {
        private String userName;
        private Integer age;
        private String gender;

    }


}
