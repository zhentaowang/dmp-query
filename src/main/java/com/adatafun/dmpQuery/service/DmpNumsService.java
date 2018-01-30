package com.adatafun.dmpQuery.service;

import com.adatafun.dmpQuery.utils.ElasticSearch;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhiweicloud.guest.APIUtil.LZResult;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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
        JSONObject bool_json = createQuery(request);
        JSONObject aggs_json = creatAggs(request);
        JSONObject query_json = new JSONObject();
        query_json.put("query", bool_json);
        query_json.put("aggregations", aggs_json);
        elasticSearch.setUp();
        LZResult<JSONObject> result = new LZResult<>(elasticSearch.getDmpNums(param, query_json));
        elasticSearch.tearDown();
        return JSON.toJSONString(result);
    }

    private JSONObject createQuery(JSONObject request) throws Exception {
        JSONArray labelList = request.getJSONArray("labelList");
        JSONObject bool_json = new JSONObject();
        JSONObject must_json = new JSONObject();
        JSONArray must_json_array = new JSONArray();
        for (int i = 0; i < labelList.size(); i++) {
            JSONObject terms = new JSONObject();
            String labelName = labelList.getJSONObject(i).getString("labelName");
            String labelValue = labelList.getJSONObject(i).getString("labelValue");
            List<String> value = Arrays.asList(labelValue.split(","));
            terms = createTerms(labelName, value);
            must_json_array.add(terms);
        }
        if (request.get("type").equals("1")) {
            must_json.put("must", must_json_array);
            bool_json.put("bool", must_json);
        } else {
            must_json.put("should", must_json_array);
            must_json.put("minimum_should_match", 1);
            bool_json.put("bool", must_json);
        }
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

    private JSONObject creatAggs(JSONObject request) throws Exception {
        JSONArray labelList = request.getJSONArray("labelList");
        JSONObject nameJson = new JSONObject();
        for (int i = 0; i < labelList.size(); i++) {
            JSONObject jsonObjectAgg;
            String labelName = labelList.getJSONObject(i).getString("labelName");
            jsonObjectAgg = createTermsAgg(labelName);
            nameJson.put(labelName+"RangeAgg", jsonObjectAgg);
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
