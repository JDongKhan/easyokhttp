package com.jd.easyokhttp.okhttps.Interceptor;

import android.util.Log;


import androidx.annotation.NonNull;

import com.jd.easyokhttp.okhttps.AppUtil;
import com.jd.easyokhttp.okhttps.EasyHttp;
import com.jd.easyokhttp.okhttps.cookie.CookieJarManager;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

/**
 * @author jd
 */
public class LoggerInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        long httpStart = System.currentTimeMillis();
        boolean log = "1".equalsIgnoreCase(originalRequest.header("Logger")) ? true : false;
        if (log) {
            Log.d("network","请求地址:" + originalRequest.url());
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("客户端开始请求:");
            stringBuilder.append(originalRequest.url());
            stringBuilder.append("\n开始请求时间戳:");
            stringBuilder.append(AppUtil.getDeviceId());
            stringBuilder.append("#");
            Log.i("network",stringBuilder.toString());
        }
        Request request =  originalRequest.newBuilder().removeHeader("log-able").build();
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            printErrorLog(request,e);
            throw e;
        }
        long now = System.currentTimeMillis();
        printSuccessLog(request,response,(now-httpStart));
        return response;
    }


    private void printErrorLog(Request request,IOException exception) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String requestString = bodyToString(request);
        stringBuilder.append("客户端开始请求(");
        stringBuilder.append(AppUtil.getDeviceId());
        String url = request.url().toString();
        stringBuilder.append("\n请求地址:");
        stringBuilder.append(url);
        stringBuilder.append("\nToken:"+ CookieJarManager.getInstance().getToken());
        stringBuilder.append("\n请求的Cookie:");
        if (CookieJarManager.getInstance().getCookieJar() != null) {
            stringBuilder.append(CookieJarManager.getInstance().getCookieJar().getCookieStore().getAllCookie().toString());
        }
        stringBuilder.append("\n请求内容:");
        stringBuilder.append(requestString);
        //响应报文
        stringBuilder.append("\n响应内容:");
        // 生成response报文
        stringBuilder.append(exception.getMessage());
        Log.i("network",stringBuilder.toString());
    }

    private void printSuccessLog(Request request, Response response,long time) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String requestString = bodyToString(request);
        stringBuilder.append("客户端开始请求(");
        stringBuilder.append(AppUtil.getDeviceId());
        stringBuilder.append(")");
        stringBuilder.append("请求耗时："+(time)+"ms");
        String url = request.url().toString();
        stringBuilder.append("\n请求地址:");
        stringBuilder.append(url);
        stringBuilder.append("\nToken:"+ CookieJarManager.getInstance().getToken());
        stringBuilder.append("\n请求的Cookie:");
        stringBuilder.append(CookieJarManager.getInstance().getCookieJar().getCookieStore().getAllCookie().toString());
        stringBuilder.append("\n请求内容:");
        stringBuilder.append(requestString);
        //响应报文
        stringBuilder.append("\nHTTP状态码:");
        stringBuilder.append(response.code());
        if (response != null && response.headers() != null) {
            stringBuilder.append("\n响应头:\n" + response.headers().toString());
        }
        stringBuilder.append("\n响应Cookie:");
        stringBuilder.append(EasyHttp.getInstance().getOkHttpClient().cookieJar().loadForRequest(HttpUrl.get(url)).toString());
        stringBuilder.append("\n响应内容:");
        // 生成response报文
        stringBuilder.append(response.peekBody(1024).string());
        Log.i("network",stringBuilder.toString());
    }

    /**
     * body转字符串
     *
     * @param request
     * @return
     */
    public String bodyToString(final okhttp3.Request request) {
        if (!"POST".equals(request.method())) {
            return "";
        }
        try {
            okhttp3.Request copy = request.newBuilder().build();
            Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final Exception e) {
            return "something error when show requestBody.";
        }
    }
}
