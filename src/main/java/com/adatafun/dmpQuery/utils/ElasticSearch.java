package com.adatafun.dmpQuery.utils;

import com.adatafun.dmpQuery.conf.ESFactory;
import com.adatafun.dmpQuery.model.User;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.Range;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.*;

/**
 * ElasticSearch.java
 * Copyright(C) 2017 杭州风数科技有限公司
 * Created by wzt on 05/09/2017.
 */
public class ElasticSearch {

    private JestClient jestClient;
    private ESFactory esFactory;
    private JestService jestService;

    @Before
    public void setUp() throws Exception {

        esFactory = new ESFactory();
        jestService = new JestService();
        jestClient = esFactory.getJestClient();
    }

    @After
    public void tearDown() throws Exception {

        esFactory.closeJestClient(jestClient);
    }

    public Map getUserLabel(String indexName, String indexType, String indexId) {

        return jestService.getUserLabel(jestClient, indexName, indexType, indexId);

    }

    public List<Map> getUserDetail(String indexName, String indexType, String type, List<Map> labelList) throws Exception {

        JSONObject query_json = new JSONObject();
        JSONObject bool_json = new JSONObject();
        JSONObject must_or_should_json = new JSONObject();
        JSONArray must_or_should_json_array = new JSONArray();
        JSONObject must_not_terms = new JSONObject();
        JSONObject must_not_terms_json = new JSONObject();
        must_not_terms_json.put("longTengId.keyword", "");
        must_not_terms.put("term", must_not_terms_json);
        must_or_should_json.put("must_not", must_not_terms);
        for (Map label : labelList) {
            String labelName = label.get("labelName").toString() + ".keyword";
            String labelValue_match = label.get("labelValue").toString();
            String[] labelValue_array = labelValue_match.split(",");
            JSONObject terms = new JSONObject();
            JSONObject terms_json = new JSONObject();
            terms_json.put(labelName, labelValue_array);
            terms.put("terms", terms_json);
            must_or_should_json_array.add(terms);
        }
        if (type.equals("1")) {
            must_or_should_json.put("must", must_or_should_json_array);
        } else {
            must_or_should_json.put("should", must_or_should_json_array);
        }
        bool_json.put("bool", must_or_should_json);
        query_json.put("query", bool_json);
        String query = query_json.toJSONString();
        SearchResult result = jestService.search(jestClient, indexName, indexType, query);
        List<SearchResult.Hit<Map, Void>> hits = result.getHits(Map.class);
        List<Map> userList = new ArrayList<>();
        Map<String, Integer> totalNumber = new HashMap<>();
        totalNumber.put("totalNumber", hits.size());
        userList.add(totalNumber);
        for (SearchResult.Hit<Map, Void> hit : hits) {
            userList.add(hit.source);
        }
        return userList;

    }

    public boolean createIndex(Map<String, Object> param) throws Exception {

        return jestService.createIndex(jestClient, param.get("indexName").toString());

    }

    public boolean createIndexMapping(Map<String, Object> param) throws Exception {

//        String source = "{\"" + param.get("typeName").toString() + "\":{\"properties\":{"
//                + "\"id\":{\"type\":\"integer\"}"
//                + ",\"name\":{\"type\":\"string\",\"index\":\"not_analyzed\"}"
//                + ",\"birth\":{\"type\":\"date\",\"format\":\"strict_date_optional_time||epoch_millis\"}"
//                + "}}}";
        String source = "{\"" + param.get("typeName").toString() + "\":{\"properties\":{"
                + "\"id\":{\"type\":\"string\"}"
                + "\"userId\":{\"type\":\"string\",\"index\":\"not_analyzed\"}"
                + ",\"restaurantCode\":{\"type\":\"string\",\"index\":\"not_analyzed\"}"
                + ",\"consumptionNum\":{\"type\":\"integer\",\"index\":\"not_analyzed\"}"
                + ",\"collectionNum\":{\"type\":\"integer\",\"index\":\"not_analyzed\"}"
                + ",\"commentNum\":{\"type\":\"integer\",\"index\":\"not_analyzed\"}"
                + ",\"multiTimeConsumption\":{\"type\":\"boolean\",\"index\":\"not_analyzed\"}"
                + ",\"perCustomerTransaction\":{\"type\":\"string\",\"index\":\"not_analyzed\"}"
                + ",\"averageOrderAmount\":{\"type\":\"double\",\"index\":\"not_analyzed\"}"
                + ",\"restaurantPreferences\":{\"type\":\"string\",\"index\":\"not_analyzed\"}"
                + "}}}";
        return jestService.createIndexMapping(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), source);

    }

    public String getIndexMapping(Map<String, Object> param) throws Exception {

        return jestService.getIndexMapping(jestClient, param.get("indexName").toString(), param.get("typeName").toString());

    }

    public boolean index(Map<String, Object> param, List<Object> userList) throws Exception {

        return jestService.index(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), userList);

    }

    public Boolean articleQuery(Map<String, Object> param) throws Exception {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("userId", param.get("userId")))
                .must(QueryBuilders.matchQuery("restaurantCode", param.get("subjectArticleId")));
        searchSourceBuilder.query(queryBuilder);
        String query = searchSourceBuilder.toString();
        return jestService.isBrowse(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);

    }

    public User getUserPreference(Map<String, Object> param) throws Exception {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("userId", param.get("userId").toString()))
                .must(QueryBuilders.boolQuery()
                        .should(QueryBuilders.rangeQuery("business").gte("0"))
                        .should(QueryBuilders.rangeQuery("crowd").gte("0"))
                        .should(QueryBuilders.rangeQuery("student").gte("0"))
                        .should(QueryBuilders.rangeQuery("eleven").gte("0"))
                        .should(QueryBuilders.rangeQuery("home").gte("0")));
        searchSourceBuilder.query(queryBuilder);
        String query = searchSourceBuilder.toString();
        SearchResult result = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
        List<SearchResult.Hit<User, Void>> hits = result.getHits(User.class);
        User user = new User();
        if (hits != null && hits.size() != 0) {
            user = hits.get(0).source; //在es中每个用户只有一条偏好记录
        }
        return user;

    }

    public List<User> termQuery(Map<String, Object> param) throws Exception {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder; //单值完全匹配查询
        queryBuilder = QueryBuilders
                .termQuery("userId", param.get("userId").toString());
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(10);
        searchSourceBuilder.from(0);
        String query = searchSourceBuilder.toString();
        SearchResult result = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
        List<SearchResult.Hit<User, Void>> hits = result.getHits(User.class);
        List<User> userList = new ArrayList<>();
        for (SearchResult.Hit<User, Void> hit : hits) {
            userList.add(hit.source);
        }
        return userList;

    }

    public List<User> privilegeLabelQuery(Map<String, Object> param, List<Map<String, Object>> productList) throws Exception {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        List<User> userList = new ArrayList<>();
        QueryBuilder queryBuilder; //多值完全匹配查询
        for (Map<String, Object> restaurant : productList) {
            queryBuilder = QueryBuilders
                    .matchQuery("restaurantCode", restaurant.get("fd_code").toString());
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.size(1);
            searchSourceBuilder.from(0);
            String query = searchSourceBuilder.toString();
            SearchResult result = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
            List<SearchResult.Hit<User, Void>> hits = result.getHits(User.class);
            if (hits != null) {
                userList.add(hits.get(0).source);
            }
        }
        return userList;

    }

    public List<User> termsQuery(Map<String, Object> param) throws Exception {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder; //多值完全匹配查询
        queryBuilder = QueryBuilders
                .termsQuery("name", "rer", "yty");
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(10);
        searchSourceBuilder.from(0);
        String query = searchSourceBuilder.toString();
        SearchResult result = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
        List<SearchResult.Hit<User, Void>> hits = result.getHits(User.class);
        List<User> userList = new ArrayList<>();
        for (SearchResult.Hit<User, Void> hit : hits) {
            userList.add(hit.source);
        }
        return userList;

    }

    public List<User> wildcardQuery(Map<String, Object> param) throws Exception {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders
                .wildcardQuery("restaurantCode", "*000*");//通配符和正则表达式查询
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(10);
        searchSourceBuilder.from(0);
        String query = searchSourceBuilder.toString();
        SearchResult result = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
        List<SearchResult.Hit<User, Void>> hits = result.getHits(User.class);
        List<User> userList = new ArrayList<>();
        for (SearchResult.Hit<User, Void> hit : hits) {
            userList.add(hit.source);
        }
        return userList;

    }

    public List<User> prefixQuery(Map<String, Object> param) throws Exception {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders
                .prefixQuery("restaurantCode", "R");//前缀查询
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(10);
        searchSourceBuilder.from(0);
        String query = searchSourceBuilder.toString();
        SearchResult result = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
        List<SearchResult.Hit<User, Void>> hits = result.getHits(User.class);
        List<User> userList = new ArrayList<>();
        for (SearchResult.Hit<User, Void> hit : hits) {
            userList.add(hit.source);
        }
        return userList;

    }

    public List<User> rangeQuery(Map<String, Object> param) throws Exception {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders
                .rangeQuery("birth")
                .gte("2017-09-01T00:00:00")
                .lte("2017-10-01T00:00:00")
                .includeLower(true)
                .includeUpper(true);//区间查询
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(10);
        searchSourceBuilder.from(0);
        String query = searchSourceBuilder.toString();
        SearchResult result = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
        List<SearchResult.Hit<User, Void>> hits = result.getHits(User.class);
        List<User> userList = new ArrayList<>();
        for (SearchResult.Hit<User, Void> hit : hits) {
            userList.add(hit.source);
        }
        return userList;

    }

    public List<User> queryString(Map<String, Object> param) throws Exception {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders
                .queryStringQuery(QueryParser.escape("T:o\""));//文本检索，应该是将查询的词先分成词库中存在的词，然后分别去检索，存在任一存在的词即返回，查询词分词后是OR的关系。需要转义特殊字符
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(10);
        searchSourceBuilder.from(0);
        String query = searchSourceBuilder.toString();
        SearchResult result = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
        List<SearchResult.Hit<User, Void>> hits = result.getHits(User.class);
        List<User> userList = new ArrayList<>();
        for (SearchResult.Hit<User, Void> hit : hits) {
            userList.add(hit.source);
        }
        return userList;

    }

    public User get(Map<String, Object> param) throws Exception {

        User user = new User();
        JestResult result = jestService.get(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), param.get("id").toString());
        if (result.isSucceeded()) {
            user = result.getSourceAsObject(User.class);
            return user;
        } else {
            return user;
        }

    }

    public boolean deleteIndexDocument(Map<String, Object> param) throws Exception {

        return jestService.delete(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), param.get("id").toString());

    }

    public boolean deleteIndex(Map<String, Object> param) throws Exception {

        return jestService.delete(jestClient, param.get("indexName").toString());
    }

    public Double count(Map<String, Object> param) throws Exception {

        String[] name = new String[]{ "T:o\"m-", "Jerry" };
        String from = "2016-09-01T00:00:00";
        String to = "2016-10-01T00:00:00";
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("name", name))
                .must(QueryBuilders.rangeQuery("birth").gte(from).lte(to));
        searchSourceBuilder.query(queryBuilder);
        String query = searchSourceBuilder.toString();
        return jestService.count(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);

    }

    public Map<String, Object> getDmpQuery(Map<String, Object> param, JSONObject query_json) throws Exception {
        String query = query_json.toJSONString();
        SearchResult searchResult = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
        JsonElement jsonElement = searchResult.getJsonObject().get("aggregations").getAsJsonObject();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        List<String> aggsRangeList = JSONArray.parseArray(JSONObject.toJSONString(param.get("aggsRangeList")), String.class);
        Map<String, Object> result = new HashMap<>();
        for (String aggs : aggsRangeList) {
            JsonElement jsonElement0 = jsonObject.get(aggs+"RangeAgg");
            if (jsonElement0 == null) continue;
            String ret = jsonElement0.getAsJsonObject().get("buckets").toString();
            List<Map> list = JSON.parseArray(ret, Map.class);
            for (int i = 0; i < list.size(); i++) {
                result.put(list.get(i).get("key").toString(), list.get(i).get("doc_count"));
            }
        }
        result.put("total", searchResult.getTotal());
        return result;
    }

    public JSONObject getDmpNums(Map<String, Object> param, JSONObject query_json) throws Exception {
        String query = query_json.toJSONString();
        SearchResult searchResult = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
        JsonElement jsonElement = searchResult.getJsonObject().get("aggregations").getAsJsonObject();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JSONObject result_json = new JSONObject();
        JSONArray labelList_json = new JSONArray();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement jsonElement0 = entry.getValue();
            if (jsonElement0 == null) continue;
            String ret = jsonElement0.getAsJsonObject().get("buckets").toString();
            List<Map> list = JSON.parseArray(ret, Map.class);
            for (int i = 0; i < list.size(); i++) {
                JSONObject name_json = new JSONObject();
                name_json.put("name", list.get(i).get("key").toString());
                name_json.put("number", list.get(i).get("doc_count"));
                labelList_json.add(name_json);
            }
        }
        result_json.put("labelList", labelList_json);
        result_json.put("totalNumber", searchResult.getTotal());
        return result_json;
    }

    private List<Map> transferDataForm (List<Map> result) {
        List<Map> newResult = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            Map<String, Object> mapResult = new HashMap<>();
            mapResult.put("name", result.get(i).get("key"));
            mapResult.put("id", i);
            mapResult.put("value", result.get(i).get("doc_count"));
            newResult.add(mapResult);
        }
        return newResult;
    }


    public JSONObject getUserPortrait(Map<String, Object> param, JSONObject bool_json, JSONObject agg) throws Exception {
        JSONObject query_json = new JSONObject();
        query_json.put("query", bool_json);
        query_json.put("aggregations", agg);
        String query = query_json.toJSONString();
        SearchResult searchResult = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query);
        JsonElement jsonElement = searchResult.getJsonObject().get("aggregations").getAsJsonObject();
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        JSONObject user = new JSONObject();
        JSONObject result = new JSONObject();
        List<String> aggsList = JSONArray.parseArray(JSONObject.toJSONString(param.get("aggsList")), String.class);
        for (String aggs : aggsList) {
            JsonElement jsonElement0 = jsonObject.get(aggs+"RangeAgg");
            if (jsonElement0 == null) {
                continue;
            }
            String ret = jsonElement0.getAsJsonObject().get("buckets").toString();
            List<Map> list = JSON.parseArray(ret, Map.class);
            List<Map> sexResult = transferDataForm(list);
            user.put(aggs, sexResult);
        }
        user.put("total", searchResult.getTotal());
        result.put("user", user);
        return result;
    }


    public List<Map> getUserPortraitTest(Map<String, Object> param, List<Long> ages) throws Exception {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("cityLevel.keyword", "一线城市", "二线城市"))
                .must(QueryBuilders.rangeQuery("age").gte(1960).lte(1970))
                .must(QueryBuilders.termsQuery("nameValidation", "1"))
                .must(QueryBuilders.boolQuery()
                        .should(QueryBuilders.rangeQuery("business").gte("0"))
                        .should(QueryBuilders.rangeQuery("crowd").gte("0"))
                        .should(QueryBuilders.rangeQuery("student").gte("0"))
                        .should(QueryBuilders.rangeQuery("eleven").gte("0"))
                        .should(QueryBuilders.rangeQuery("home").gte("0")));
        searchSourceBuilder.query(queryBuilder);

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("gradeAgg").field("city.keyword").size(34);
        searchSourceBuilder.aggregation(termsAggregationBuilder);
        searchSourceBuilder.size(10);
        searchSourceBuilder.from(0);
        String query = searchSourceBuilder.toString();

        int[] ageArray = new int[1];
        ageArray[0] = 1962;
        String[] cityLevelArray = new String[2];
        cityLevelArray[0] = "一线城市";
        cityLevelArray[1] = "二线城市";

        JSONObject query_json = new JSONObject();
        JSONObject bool_json = new JSONObject();
        JSONObject must_json = new JSONObject();
        JSONArray must_json_array = new JSONArray();

        JSONObject terms = new JSONObject();
        JSONObject terms_json = new JSONObject();
        terms_json.put("cityLevel.keyword", cityLevelArray);
        terms.put("terms", terms_json);

        JSONObject range0 = new JSONObject();
        JSONObject range0_json = new JSONObject();
        JSONObject range00_json = new JSONObject();
        range00_json.put("from", 1960);
        range00_json.put("to", 1970);
        range0_json.put("age", range00_json);
        range0.put("range", range0_json);

        JSONObject range = new JSONObject();
        JSONObject age = new JSONObject();
        JSONObject age_json = new JSONObject();
        Map<String, Object> age_map = new HashMap<>();
        age_map.put("from", 1960);
        age_map.put("to", 1980);
        age_map.put("include_lower", true);
        age_map.put("include_upper", false);
        age_map.put("boost", 1.0);
        age_json.putAll(age_map);
        age.put("age", age_json);
        range.put("range", age);

        must_json_array.add(terms);
        must_json.put("must", must_json_array);
        bool_json.put("bool", must_json);
        query_json.put("query", bool_json);

        JSONObject term = new JSONObject();
        JSONObject term_json = new JSONObject();
        JSONObject agg_json = new JSONObject();
        term_json.put("field", "city.keyword");
        term_json.put("size", 34);
        term.put("terms", term_json);
        agg_json.put("gradeAgg", term);

        JSONObject ranges = new JSONObject();
        JSONObject range_json = new JSONObject();
        JSONObject agg0_json = new JSONObject();
        term_json.put("field", "tdc_accumulationUsage");
        term_json.put("size", 34);
        term.put("terms", term_json);
        agg_json.put("gradeAgg", term);

        query_json.put("aggregations", agg0_json);

        String query0 = query_json.toJSONString();
        SearchResult result = jestService.search(jestClient, param.get("indexName").toString(), param.get("typeName").toString(), query0);
        JsonElement jsonElement = result.getJsonObject().get("aggregations").getAsJsonObject();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement jsonElement0 = jsonObject.get("gradeAgg");
        JsonObject jsonObject0 = jsonElement0.getAsJsonObject();
        JsonElement jsonElement1 = jsonObject0.get("buckets");
        String vdfd = jsonElement1.toString();
        List<Map> list = JSON.parseArray(vdfd, Map.class);
        List<SearchResult.Hit<User, Void>> hits = result.getHits(User.class);
        List<User> userList = new ArrayList<>();
        for (SearchResult.Hit<User, Void> hit : hits) {
            userList.add(hit.source);
        }
        return list;

    }

}