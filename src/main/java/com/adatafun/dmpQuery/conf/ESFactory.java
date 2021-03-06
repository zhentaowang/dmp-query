package com.adatafun.dmpQuery.conf;

import com.google.gson.GsonBuilder;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * ESFactory.java
 * Copyright(C) 2017 杭州风数科技有限公司
 * Created by wzt on 05/09/2017.
 */
public class ESFactory {

    /**
     * 获取JestClient对象
     * @return JestClient
     */
    public JestClient getJestClient() throws Exception{

        // 本地使用: 192.168.1.128(测试)，116.62.184.103(生产)；线上使用：122.224.248.26(测试)，192.168.142.57(生产)
        Properties props = new Properties();
        InputStream inputStream = this.getClass().getResourceAsStream("/application.properties");
        props.load(inputStream);     ///加载属性列表
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(props.getProperty("elasticsearch.server.uri"))
                .gson(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create())
                .connTimeout(Integer.valueOf(props.getProperty("elasticsearch.connection.timeout")))
                .readTimeout(Integer.valueOf(props.getProperty("elasticsearch.read.timeout")))
                .multiThreaded(true)
                .build());
        return factory.getObject();
    }

    /**
     * 关闭JestClient客户端
     */
    public void closeJestClient(JestClient jestClient) throws Exception {

        if (jestClient != null) {
            jestClient.shutdownClient();
        }
    }

}
