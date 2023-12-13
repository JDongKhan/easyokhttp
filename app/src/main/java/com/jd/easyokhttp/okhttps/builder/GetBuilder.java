package com.jd.easyokhttp.okhttps.builder;

import android.os.Handler;

import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author jd
 */
public class GetBuilder extends  HttpBuilder {

    public GetBuilder(OkHttpClient okHttpClient, Handler delivery) {
        super(okHttpClient, delivery);
    }

    @Override
    protected Request.Builder createBuilder() {
        Request.Builder mBuilder = new Request.Builder();
        if (params != null) {
            mBuilder.url(appendParams(url, params));
        } else {
            mBuilder.url(url);
        }
        return mBuilder;
    }

    /**
     * get 参数拼在url后面
     * @param url
     * @param params
     * @return
     */
    private String appendParams(String url, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        if (url.indexOf("?") == -1) {
            sb.append(url + "?");
        } else {
            sb.append(url + "&");
        }
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                sb.append(key).append("=").append(params.get(key)).append("&");
            }
        }
        sb = sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
