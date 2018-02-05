package com.adatafun.dmpQuery.service;

import com.adatafun.dmpQuery.utils.ElasticSearch;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhiweicloud.guest.APIUtil.LZResult;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * UserPortraitService.java
 * Copyright(C) 2017 杭州风数科技有限公司
 * Created by wzt on 2017/11/25.
 */
@Service
public class DmpNumsService {

    private static ElasticSearch elasticSearch = new ElasticSearch();

    public String getDmpNumsResult(JSONObject request, Map<String, Object> param) throws Exception {
        Map<String,Object> labels = getParams(request);
        List<String> nameList = JSONArray.parseArray(JSONObject.toJSONString(labels.get("nameList")), String.class);
        List<List> valueList = JSONArray.parseArray(JSONObject.toJSONString(labels.get("valueList")), List.class);
        List<String> codeList = JSONArray.parseArray(JSONObject.toJSONString(labels.get("codeList")), String.class);

        JSONObject bool_json = createQuery(nameList,valueList,request);
        JSONObject aggs_json = creatAggs(nameList);
        JSONObject query_json = new JSONObject();
        query_json.put("query", bool_json);
        query_json.put("aggregations", aggs_json);
        elasticSearch.setUp();
        Map<String,Object> queryList = elasticSearch.getDmpNums(param, query_json, nameList);
        elasticSearch.tearDown();
        List<Map<String,Object>> labelList = getLabelList(queryList,codeList);
        Map<String, Object> result = new HashMap<>();
        result.put("totalNumber",queryList.get("totalNumber"));
        result.put("labelList", labelList);
        return JSON.toJSONString( new LZResult<>(result));
    }

    private List<Map<String,Object>> getLabelList(Map<String,Object> queryList, List<String> codeList) {
        List<Map<String,Object>> labelList = new ArrayList<>();
        Set<Map.Entry<String,Object>> set = queryList.entrySet();
        for (String s : codeList) {
            long value = 0;
            for (Map.Entry<String,Object> entry : set) {
                String name = getMatchString(entry.getKey());
                if (s.matches(name)) {
                    value += (long)entry.getValue();
                }
            }
            Map<String,Object> name = new HashMap<>();
            name.put("name", s);
            name.put("number", value);
            labelList.add(name);
        }
        return labelList;
    }

    private String getMatchString(String param) {
        StringBuffer str = new StringBuffer();
        String[] value = param.split(",");
        for (String s : value) {
            str.append(s+"|");
        }
        return str.substring(0,str.length()-1);
    }

    private Map<String,Object> getParams(JSONObject request) {
        Map<String,Object> labels = new HashMap<>();
        List<Map> labelList = JSON.parseArray(request.getString("labelList"), Map.class);
        List<String> nameList = new ArrayList<>();
        List<String> codeList = new ArrayList<>();
        List<List<String>> valueList = new ArrayList<>();
        for (Map map : labelList) {
            nameList.add(map.get("labelName").toString());
            List<String> value = Arrays.asList(map.get("labelValue").toString().split(","));
            valueList.add(value);
            for (String s : value) {
                codeList.add(s);
            }
        }
        labels.put("nameList", nameList);
        labels.put("valueList", valueList);
        labels.put("codeList", codeList);
        return labels;
    }

    private JSONObject createQuery(List<String> nameList, List<List> valueList,JSONObject request) throws Exception {
        JSONObject bool_json = new JSONObject();
        JSONObject must_json = new JSONObject();
        JSONArray must_json_array = new JSONArray();
        for (int i = 0; i < nameList.size(); i++) {
            JSONObject terms = new JSONObject();
            String labelName = nameList.get(i);
            List<String> value = valueList.get(i);
            terms = createTerms(labelName, value);
            must_json_array.add(terms);
        }
        if (request.get("type").equals("1")) {
            must_json.put("must", must_json_array);
        } else {
            must_json.put("should", must_json_array);
            must_json.put("minimum_should_match", 1);
        }
        bool_json.put("bool", must_json);
        return bool_json;
    }

    private JSONObject createTerms(String labelName, Object labelValue) throws Exception {
        JSONObject terms = new JSONObject();
        JSONObject terms_json = new JSONObject();
        if (!labelName.matches(getIntLabelName())) {
            labelName = labelName + ".keyword";
        }
        terms_json.put(labelName, labelValue);
        terms.put("terms", terms_json);
        return terms;
    }

    private JSONObject createTermsAgg(String labelName) throws Exception {
        JSONObject term = new JSONObject();
        JSONObject term_json = new JSONObject();
        if (!labelName.matches(getIntLabelName())) {
            labelName = labelName + ".keyword";
        }
        term_json.put("field", labelName);
        term_json.put("size", 34);
        term.put("terms", term_json);
        return term;
    }

    private JSONObject creatAggs(List<String> nameList) throws Exception {
        JSONObject nameJson = new JSONObject();
        for (String name : nameList) {
            JSONObject jsonObjectAgg = createTermsAgg(name);
            nameJson.put(name+"RangeAgg", jsonObjectAgg);
        }
        return nameJson;
    }

    private static String getIntLabelName() {
        String termsRegex = "logTimeThirty" + "|" + "loginNum_holiday"
                + "|" + "loginNum_weekday" + "|" + "loginNum_weekend"
                + "|" + "outTimeHalfYear" + "|" + "presentPoint"
                + "|" + "tradeCar" + "|" + "tradeFlash" + "|" + "tradeLounge"
                + "|"+ "tradeParking"  + "|" + "tradePoint";
        return termsRegex;
    }
}
