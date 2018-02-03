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
public class DmpQueryService {

    private static ElasticSearch elasticSearch = new ElasticSearch();

    public String getDmpQueryResult(List<Map<String, Object>> queryList, Map<String, Object> param) throws Exception {
        int queryCount = getQueryCount(queryList);
        JSONObject query_json = new JSONObject();
        query_json.put("query", createDmpQuery(queryList));
        query_json.put("aggregations", createDmpTermsAgg(param));
        elasticSearch.setUp();
        Map<String, Object> result = elasticSearch.getDmpQuery(param, query_json);
        elasticSearch.tearDown();
//        LZResult<Map<String, Object>> productList = new LZResult<>(getProductList(result, param, queryCount));
        Map<String, Object> path = exportDmpToExcel(result, param, queryCount);
        return JSON.toJSONString(new LZResult<>(path));
    }

    private Integer getQueryCount(List<Map<String, Object>> queryList) {
        int queryCount = 0;
        for (Map map : queryList) {
            List<String> codeList = JSONArray.parseArray(JSONObject.toJSONString(map.get("codeList")), String.class);
            queryCount += codeList.size();
        }
        return queryCount;
    }

    private JSONObject createDmpTerms(String labelName, Object labelValue) throws Exception {
        JSONObject terms = new JSONObject();
        JSONObject terms_json = new JSONObject();
        terms_json.put(labelName, labelValue);
        terms.put("terms", terms_json);
        return terms;
    }

    private JSONObject createDmpQuery(List<Map<String, Object>> queryList) throws Exception {
        JSONObject bool = new JSONObject();
        JSONObject should_json = new JSONObject();
        JSONArray should_json_array = new JSONArray();
        for (Map map : queryList) {
            String id = map.get("id").toString();
            List<String> codeList = JSONArray.parseArray(JSONObject.toJSONString(map.get("codeList")), String.class);
            JSONObject terms = createDmpTerms(id, codeList);
            should_json_array.add(terms);
        }
        should_json.put("should", should_json_array);
        bool.put("bool", should_json);
        return bool;
    }

    private JSONObject createDmpTermsAgg(Map<String, Object> param) throws Exception {
        List<String> aggsRangeList = JSONArray.parseArray(JSONObject.toJSONString(param.get("aggsRangeList")), String.class);
        JSONObject nameJson = new JSONObject();
        for (String aggs : aggsRangeList) {
            JSONObject term_json = new JSONObject();
            JSONObject term = new JSONObject();
            term_json.put("field", aggs + ".keyword");
            term.put("terms", term_json);
            nameJson.put(aggs + "RangeAgg", term);
        }
        return nameJson;
    }

    private Map<String, Object> getProductList(Map<String, Object> result, Map<String, Object> param, int queryCount) {
        Map<String, Object> productList = new HashMap<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:MM");
        productList.put("导出日期", df.format(new Date()));
        productList.put("输入用户数", String.valueOf(queryCount));
        productList.put("有效用户数", result.get("total").toString());
        List<String> labelList = JSONArray.parseArray(JSONObject.toJSONString(param.get("labelList")), String.class);
        for (int i = 0; i < labelList.size(); i++) {
            if (result.containsKey(labelList.get(i))) {
                productList.put(labelList.get(i), result.get(labelList.get(i)).toString());
            }else {
                productList.put(labelList.get(i), "0");
            }
        }
        return productList;
    }

    public void setDmpCellValue(HSSFSheet sheet, int rowIndex, int column, String value) {
        HSSFRow row = sheet.getRow(rowIndex);
        if (row == null) row = sheet.createRow(rowIndex);
        row.createCell(column).setCellValue(value);
    }

    public void setCellByColumn(HSSFSheet sheet, List<String> values, int rowIndexFrom, int column) {
        for (int i = 0; i < values.size(); i ++) {
            HSSFRow row = sheet.getRow(rowIndexFrom + i);
            if (row == null) row = sheet.createRow(rowIndexFrom + i);
            if (column == 1 && rowIndexFrom + i == 0) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:MM");
                row.createCell(column).setCellValue(df.format(new Date()));
            }else {
                row.createCell(column).setCellValue(values.get(i));
            }
        }
    }

    public void setCellByRow(HSSFSheet sheet, List<String> values, int rowIndex, int columnFrom) {
        HSSFRow row = sheet.getRow(rowIndex);
        if (row == null) row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.size(); i ++) {
            row.createCell(columnFrom + i).setCellValue(values.get(i));
        }
    }

    public void mergeCells(HSSFSheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        for (int i = 0; i <= lastRow - firstRow; i ++) {
            sheet.addMergedRegion(new CellRangeAddress(firstRow+i,firstRow+i,firstCol,lastCol));
        }
    }

    public HSSFWorkbook resultToExcel(Map<String, Object> result, Map<String, Object> param, int queryCount) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        List<String> keys = Arrays.asList("导出日期","输入用户数","有效用户数");
        List<String> values = Arrays.asList("" ,String.valueOf(queryCount), result.get("total").toString());
        List<String> code = Arrays.asList("编号","标签值","标签数量");
        setCellByColumn(sheet, keys, 0, 0);
        setCellByColumn(sheet, values, 0, 1);
        mergeCells(sheet, 0, 2, 1, 2);
        setCellByRow(sheet, code, keys.size(), 0);

        List<String> labelList = JSONArray.parseArray(JSONObject.toJSONString(param.get("labelList")), String.class);
        List<String> numberList = JSONArray.parseArray(JSONObject.toJSONString(param.get("numberList")), String.class);
        setCellByColumn(sheet, numberList, keys.size()+1, 0);
        setCellByColumn(sheet, labelList, keys.size()+1, 1);
        for (int i = 0; i < labelList.size(); i++) {
            if (result.containsKey(labelList.get(i))) {
                setDmpCellValue(sheet, keys.size()+1+i, 2, result.get(labelList.get(i)).toString());
            }else {
                setDmpCellValue(sheet, keys.size()+1+i, 2, "0");
            }
        }
        return workbook;
    }

    public void setDmpCellSyle(HSSFWorkbook workbook) {
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        HSSFFont font = workbook.createFont();
        font.setFontName("黑体");
        font.setFontHeightInPoints((short) 10);
        cellStyle.setFont(font);
        HSSFSheet sheet = workbook.getSheetAt(0);
        int rowNum = sheet.getLastRowNum();
        for (int row = 0; row <= rowNum; row++) {
            int columnNum = sheet.getRow(row).getPhysicalNumberOfCells();
            HSSFRow hssfRow = sheet.getRow(row);
            hssfRow.setHeight((short) (25*20));
            for (int col = 0; col < columnNum; col++) {
                sheet.autoSizeColumn(col, true);
                HSSFCell cell = hssfRow.getCell(col);
                cell.setCellStyle(cellStyle);
            }
        }
        sheet.setColumnWidth(1, (short) 9000);
    }

    public Map<String, Object> exportDmpToExcel(Map<String, Object> result, Map<String, Object> param, int queryCount) {
        HSSFWorkbook workbook =  resultToExcel(result, param, queryCount);
        setDmpCellSyle(workbook);
        Map<String, Object> map = new HashMap<>();
        try {
            File tempFile = File.createTempFile("/Users/m2shad0w/Desktop/fileTest/testOutput", ".xls");
            tempFile.deleteOnExit();
            String path = "/Users/m2shad0w/Desktop/fileTest/testOutput.xls";
            OutputStream outputStream = new FileOutputStream(path);
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            map.put("fileName", path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public String getUserLabel(final JSONObject request) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String longTengId = request.getString("longTengId") + '*';
            String indexName = "dmp-user";
            String indexType = "dmp-user";
            List<Map> labelList = JSON.parseArray(request.getString("labelList"), Map.class);
            elasticSearch.setUp();
            Map map = elasticSearch.getUserLabel(indexName, indexType, longTengId);
            elasticSearch.tearDown();
            List<String> resultLabel = new ArrayList<>();
            for (Map label : labelList) {
                String labelName = label.get("labelName").toString();
                String labelValue_match = label.get("labelValue").toString();
                if (map.containsKey(labelName)) {
                    String labelValue_user = map.get(labelName).toString();
                    String[] labelValue_array = labelValue_user.replaceAll("\\[|]", "").split(",");
                    for (String value : labelValue_array) {
                        if (labelValue_match.contains(value)) {
                            resultLabel.add(value);
                        }
                    }
                }
            }
            resultMap.put("labelList", resultLabel);
            result.put("data", resultMap);
            result.put("state", "200");
            result.put("message", "操作成功");
            return JSON.toJSONString(result);
        } catch (Exception e) {
            result.put("data", null);
            result.put("state", "500");
            result.put("message", e.getMessage());
            return JSON.toJSONString(result);
        }
    }

    public String getUserDetail(final JSONObject request) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        String indexName = "dmp-user";
        String indexType = "dmp-user";
        String type = request.getString("type");
        try {
            List<Map> labelList = JSON.parseArray(request.getString("labelList"), Map.class);
            elasticSearch.setUp();
            List<Map> userList = elasticSearch.getUserDetail(indexName, indexType, type, labelList);
            elasticSearch.tearDown();
            resultMap.put("totalNumber", userList.get(0));
            userList.remove(0);
            resultMap.put("userList", userList);
            result.put("data", resultMap);
            result.put("state", "200");
            result.put("message", "操作成功");
            return JSON.toJSONString(result);
        } catch (Exception e) {
            result.put("data", null);
            result.put("state", "500");
            result.put("message", e.getMessage());
            return JSON.toJSONString(result);
        }
    }

}
