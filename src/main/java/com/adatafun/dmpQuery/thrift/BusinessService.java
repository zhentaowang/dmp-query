/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2019 wangzhentao@iairportcloud.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.adatafun.dmpQuery.thrift;

import com.adatafun.dmpQuery.controller.DmpNumsController;
import com.adatafun.dmpQuery.controller.DmpQueryController;
import com.adatafun.dmpQuery.service.DmpQueryService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wyun.thrift.server.business.IBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * BusinessService.java
 * Copyright(C) 2017 杭州风数科技有限公司
 * Created by wzt on 05/09/2017.
 */
@Component
public class BusinessService implements IBusinessService {

    private final DmpQueryController dmpQueryController;
    private final DmpNumsController dmpNumsController;
    private final DmpQueryService dmpQueryService;

    @Autowired
    public BusinessService(DmpQueryController dmpQueryController,
                           DmpNumsController dmpNumsController,
                           DmpQueryService dmpQueryService) {
        this.dmpQueryController = dmpQueryController;
        this.dmpNumsController = dmpNumsController;
        this.dmpQueryService = dmpQueryService;
    }

    @Override
    public JSONObject handle(String operation,JSONObject request) {
        String success = null;

        switch (operation) {
            case "getDmpQueryResult":
                success = dmpQueryController.getDmpQueryResult(request);
                break;
            case "getDmpNumsResult":
                success = dmpNumsController.getDmpNumsResult(request);
                break;
            case "getUserLabel":
                success = dmpQueryService.getUserLabel(request);
                break;
            case "getUserDetail":
                success = dmpQueryService.getUserDetail(request);
            default:
                break;
        }
        return JSON.parseObject(success);
    }

}
