package com.jd.easyokhttp.okhttps.builder;

import android.os.Handler;


import com.jd.easyokhttp.utils.GsonUtil;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PostBuilder extends  HttpBuilder {

    private MediaType mediaType = MediaType.parse("application/json;charset=utf-8");

    public PostBuilder(OkHttpClient okHttpClient, Handler delivery) {
        super(okHttpClient, delivery);
    }

    @Override
    protected Request.Builder createBuilder() {
        Request.Builder mBuilder = new Request.Builder();
        mBuilder.url(url);
        RequestBody requestBody = null;
        if (this.mediaType != null) {
            requestBody = RequestBody.create(mediaType, GsonUtil.ser(params));
        } else {
            FormBody.Builder formBody = new FormBody.Builder();
            addParams(formBody, params);
            requestBody = formBody.build();
        }
        //这里的.post是区分get请求的关键步骤
        mBuilder.post(requestBody);
        return mBuilder;
    }

    public PostBuilder mediaType(MediaType mediaType){
        this.mediaType = mediaType;
        return this;
    }

    //拼接头部参数
    public Headers appendHeaders(Map<String, String> headers) {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        for (String key : headers.keySet()) {
            headerBuilder.add(key, headers.get(key));
        }
        return headerBuilder.build();
    }

    //键值对拼接的参数
    private void addParams(FormBody.Builder builder, Map<String, String> params) {
        if (builder == null) {
            throw new IllegalArgumentException("builder can not be null .");
        }
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }
    }

    public PostBuilder json(){
        mediaType = MediaType.parse("application/json;charset=utf-8");
        return this;
    }

    @Override
    public PostBuilder url(String url) {
         super.url(url);
         return this;
    }

    @Override
    public  PostBuilder tag(String tag) {
        super.tag(tag);
        return this;
    }

    @Override
    public PostBuilder headers(Map<String, String> headers) {
        super.headers(headers);
        return this;
    }

    @Override
    public PostBuilder params(Map<String, String> params) {
         super.params(params);
        return this;
    }

    @Override
    public PostBuilder retryCount(int retryCount) {
        super.retryCount(retryCount);
        return this;
    }

    @Override
    public PostBuilder once(boolean once) {
         super.once(once);
         return this;
    }
}
