package com.adatafun.dmpQuery.controller;

import com.adatafun.dmpQuery.service.DmpNumsService;
import com.adatafun.dmpQuery.service.DmpQueryService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhiweicloud.guest.APIUtil.LXResult;
import com.zhiweicloud.guest.APIUtil.LZStatus;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

/**
 * UserPortraitController.java
 * Copyright(C) 2017 杭州风数科技有限公司
 * Created by wzt on 2017/11/29.
 */
@Component
public class DmpNumsController {

    private final DmpNumsService dmpNumsService;

    @Autowired
    public DmpNumsController(DmpNumsService dmpNumsService) {
        this.dmpNumsService = dmpNumsService;
    }

    public String getDmpNumsResult(final JSONObject request) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("indexName", "dmp-user");
            param.put("typeName", "dmp-user");
            return dmpNumsService.getDmpNumsResult(request, param);
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(LXResult.build(LZStatus.ERROR.value(), LZStatus.ERROR.display()));
        }
    }

}
