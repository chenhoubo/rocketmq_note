package com.example.common.utils;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;


/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/25 18:33
 * @Desc
 * @since seeingflow
 */
@Slf4j
public class HttpUtil {

    public static JSONObject get(final String url, final Map<String, Object> params) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        if (MapUtil.isNotEmpty(params)) {
            for (Map.Entry<String, Object> p : params.entrySet()) {
                urlBuilder.append(p.getKey()).append("=").append(p.getValue()).append("&");
            }
        }
        try {
            HttpGet httpGet = new HttpGet();
            httpGet.setURI(new URI(urlBuilder.substring(0, urlBuilder.length() - 1)));
            log.info("\nGET 地址为:" + httpGet.getURI().toString());
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            log.debug("\nGET 响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                String entityStr = EntityUtils.toString(responseEntity);
                log.debug("\nGET 响应内容: " + entityStr);
                log.debug("\nGET 响应内容长度为:" + responseEntity.getContentLength());
                return JSON.parseObject(entityStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static JSONObject post(final String url, final Map<String, Object> params) {
        HttpPost post = new HttpPost();
        post.setURI(URI.create(url));
        post.setHeader(new BasicHeader("Content-Type", "application/json;charset=UTF-8"));
        post.setEntity(new StringEntity(JSON.toJSONString(params), StandardCharsets.UTF_8));
        JSONObject jsonObject = post(post);
        return jsonObject;

//        HttpPost post = new HttpPost();
//        post.setURI(URI.create("https://bbb.com/c/d?e=f"));
//        post.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
//        List<NameValuePair> pairs = CollUtil.newArrayList();
//        pairs.add(new BasicNameValuePair("g", "h"));
//        pairs.add(new BasicNameValuePair("i", "j"));
//        post.setEntity(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
//        JSONObject jsonObject = post(post);
//        System.out.println(jsonObject);
    }
    public static JSONObject post(HttpPost httpPost) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;
        try {
            log.info("\nPOST 地址为: " + httpPost.getURI().toString());
            log.info("\nPOST 请求头为: " + Arrays.toString(httpPost.getAllHeaders()));
            log.info("\nPOST 内容为: " + httpPost.getEntity());
            // 由客户端执行(发送)Post请求
            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            log.debug("\nPOST 响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                String entityStr = EntityUtils.toString(responseEntity);
                log.debug("\nPOST 响应内容: " + entityStr);
                log.debug("\nPOST 响应内容长度为:" + responseEntity.getContentLength());
                return JSON.parseObject(entityStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}