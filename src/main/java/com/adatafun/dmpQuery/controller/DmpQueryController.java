package com.adatafun.dmpQuery.controller;

import com.adatafun.dmpQuery.model.User;
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
public class DmpQueryController {

    private final DmpQueryService dmpQueryService;

    @Autowired
    public DmpQueryController(DmpQueryService dmpQueryService) {
        this.dmpQueryService = dmpQueryService;
    }

    public String getDmpQueryResult(final JSONObject request) {
        try {
//            List<Map<String, Object>> queryList = getJsonList(request);
            List<Map<String, Object>> queryList = getCodeList(request);
            System.out.println("queryList" + queryList);
            Map<String, Object> param = new HashMap<>();
            List<String> labelList = Arrays.asList("男","女","55岁以上","26-35岁","36-45岁","46-55岁",
                                            "19-25岁","19岁以下","新用户","老用户","高登录频次","中登录频次",
                                            "低登录频次","凌晨","上午","中午","下午","晚上");
            List<String> numberList = Arrays.asList("010101","010102","010201","010202","010203",
                                            "010204","010205","010206","020101","020102","020201",
                                            "020202","020203","020301","020302","020303","020304","020305");
            List<String> aggsRangeList = Arrays.asList("gender", "ageRange", "newold", "logWithInThirty", "LogTimePrefer");
            param.put("aggsRangeList", aggsRangeList);
            param.put("labelList", labelList);
            param.put("numberList", numberList);
            param.put("indexName", "dmp-ltuser");
            param.put("typeName", "dmp-ltuser");
            return dmpQueryService.getDmpQueryResult(queryList, param);
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(LXResult.build(LZStatus.ERROR.value(), LZStatus.ERROR.display()));
        }
    }

    public String getCodeId(String code) {
        List<String> codeList = Arrays.asList("手机号码","IMEI/IDFA",
                "身份证号","其他证件","龙腾出行ID","白云机场ID","支付宝ID","邮箱");
        List<String> idList = Arrays.asList("phoneNum","deviceNum",
                "idNum","passportNum","longTengId","baiYunId","alipayId","email");
        return idList.get(codeList.indexOf(code));
    }

    private List<Map<String, Object>> getJsonList(JSONObject jsonObject) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> set = jsonObject.keySet();
        for (String key : set) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", key);
            List<String> codeList = JSONArray.parseArray(JSONObject.toJSONString(jsonObject.get(key)), String.class);
            map.put("codeList", codeList);
            result.add(map);
        }
        return result;
    }

    private List<Map<String, Object>> getCodeList(JSONObject jsonObject) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (jsonObject.containsKey("fileName")) {
            String fileName = jsonObject.getString("fileName");
            int index = fileName.lastIndexOf(".");
            String ext = (index < 0)? "" : fileName.substring(index+1).toLowerCase();
            if ("xls,xlsx".contains(ext)) {
                result = getcodeListByExcel(fileName);
            }else if (ext.equals("txt")) {
                result = getcodeListByTxt(getCodeId(jsonObject.getString("codeType")),fileName);
            }
        }else {
            result = getcodeListByCode(jsonObject);
        }
        return result;
    }

    private List<Map<String, Object>> getcodeListByCode(JSONObject jsonObject) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> codeList = JSONArray.parseArray(JSONObject.toJSONString(jsonObject.get("code")), String.class);
        Map<String, Object> map = new HashMap<>();
        map.put("id", getCodeId(jsonObject.getString("codeType")));
        map.put("codeList", codeList);
        result.add(map);
        return result;
    }

    private List<Map<String, Object>> getcodeListByTxt(String codeType, String fileName) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            File file = new File(fileName);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            List<String> list = new ArrayList<>();
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                list.add(tempStr);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", codeType);
            map.put("codeList", list);
            result.add(map);
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<Map<String, Object>> getcodeListByExcel(String fileName) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Workbook workbook = WorkbookFactory.create(new FileInputStream(fileName));
            Sheet sheet = workbook.getSheetAt(0);
            int columnNum = sheet.getRow(0).getPhysicalNumberOfCells();
            for (int col = 0; col < columnNum; col++) {
                List<String> list = new ArrayList<>();
                String id = sheet.getRow(0).getCell(col).getStringCellValue();
                if (id.equals("")) continue;
                for (int r = 1; r < sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    row.getCell(col).setCellType(CellType.STRING);
                    String str = row.getCell(col).getStringCellValue();
                    if (!str.equals("")) list.add(str);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", getCodeId(id));
                map.put("codeList", list);
                result.add(map);
            }
            System.out.println(result);
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
