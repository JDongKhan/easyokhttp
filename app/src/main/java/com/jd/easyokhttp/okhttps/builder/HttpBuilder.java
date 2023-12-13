package com.jd.easyokhttp.okhttps.builder;

import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.jd.easyokhttp.okhttps.cookie.CookieJarManager;
import com.jd.easyokhttp.okhttps.okcallback.NetworkCallback;
import com.jd.easyokhttp.utils.GsonUtil;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author jd
 */
public abstract class HttpBuilder {
    /**
     * url
     */
    protected String url;
    /**
     * tag
     */
    protected String tag;
    /**
     * 参数
     */
    protected Map<String, String> params;
    /**
     * header
     */
    protected Map<String, String> headers;
    /**
     * 默认同时多次请求一个接口 只请求一次
     */
    protected boolean once;
    /**
     * 默认不重连
     */
    protected int retryCount;
    protected int currentRetryCount;
    protected OkHttpClient okHttpClient;
    protected Handler mDelivery;
    private Request okHttpRequest;
    protected NetworkCallback callback;

    final private static ArrayList<String> onceTagList = new ArrayList<>();;

    public HttpBuilder(OkHttpClient okHttpClient,Handler delivery) {
        this.okHttpClient = okHttpClient;
        this.mDelivery = delivery;
    }

    public HttpBuilder build() {
        Request.Builder mBuilder =  createBuilder();
        if (!TextUtils.isEmpty(tag)) {
            mBuilder.tag(tag);
        }
        Headers headers1 = appendHeaders(headers);
        if (headers1 != null) {
            mBuilder.headers(headers1);
        }
        mBuilder.addHeader("User-Agent", CookieJarManager.getInstance().getUserAgent());
        mBuilder.addHeader("Request-Time", String.valueOf(System.currentTimeMillis()));
        mBuilder.addHeader("Begin-Http-Time", new Date().toString());
        if(!TextUtils.isEmpty(CookieJarManager.getInstance().getToken())) {
            mBuilder.addHeader("Authorization", CookieJarManager.getInstance().getToken());
        }
        okHttpRequest = mBuilder.build();
        return this;
    }

    protected abstract Request.Builder createBuilder();

    /**
     * 获取tag
     * @return
     */
    private String getTag() {
        if (!TextUtils.isEmpty(tag)) {
            return tag;
        }
        return url;
    }

    public final void removeOnceTag() {
        if (once) {
            String tag = getTag();
            onceTagList.remove(tag);
        }
    }

    /**
     * 前置判断
     * @return
     */
    protected boolean canRequest() {
        if (once) {
            String tag = getTag();
            if (onceTagList.contains(tag)) {
                return false;
            }
            onceTagList.add(tag);
        }
        return true;
    }

    /**
     * 失败请求
     * @param call
     * @param e
     * @param resultCall
     * @param callback
     */
    protected void doFailureCallback(Call call, final IOException e, final NetworkCallback resultCall, Callback callback) {
        if (e instanceof SocketException) {

        } else {
            //如果在重连的情况下，是主动取消网络是java.net.SocketException: Socket closed
            if (currentRetryCount < retryCount && retryCount > 0) {
                // 如果超时并未超过指定次数，则重新连接
                currentRetryCount++;
                okHttpClient.newCall(call.request()).enqueue(callback);
                return;
            }
        }
        removeOnceTag();
        if (resultCall == null) {
            return;
        }
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                resultCall.onAfter();
                String errorMsg = "服务器异常,请稍后再试";;
                if (e instanceof ConnectException) {
                    errorMsg = "网络不可用,请检查网络";
                } else if (e instanceof SocketTimeoutException) {
                    errorMsg = "请求超时,请稍后再试";
                }
                resultCall.onError(errorMsg);
            }
        });

    }

    protected void doSuccessCallback(Call call, final Response response, final NetworkCallback resultCall, Callback callback) throws IOException {
        removeOnceTag();
        if (resultCall == null) {
            return;
        }
        //网络请求成功
        if (response.isSuccessful()) {
            String result = response.body().string();
            Object successObject = null;
            if (resultCall.getType() == null) {
                successObject = result;
            } else {
                successObject = GsonUtil.deser(result, resultCall.getType());
            }
            if (successObject == null) {
                successObject = result;
            }
            final Object finalSuccessObject = successObject;
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    resultCall.onAfter();
                    resultCall.onSuccess(finalSuccessObject);
                }
            });

        } else {
            //接口请求确实成功了，code 不是 200..299
            final String errorMsg = response.body().string();
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    resultCall.onAfter();
                    resultCall.onError(errorMsg);
                }
            });
        }

    }

    /**
     * 非封装单独使用
     * @param resultCall
     */
    public void enqueue(final NetworkCallback resultCall) {
        if (okHttpRequest == null) {
            return;
        }
        this.callback = resultCall;
        if (!canRequest()) {
          return;
        }

        if (resultCall != null) {
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    resultCall.onBefore();
                }
            });
        }

        okHttpClient.newCall(okHttpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                doFailureCallback(call,e,resultCall,this);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                doSuccessCallback(call,response,resultCall,this);
            }
        });
    }


    public HttpBuilder url(String url) {
        this.url = url;
        return this;
    }

    public HttpBuilder retryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }


    public HttpBuilder once(boolean once) {
        this.once = once;
        return this;
    }

    public  HttpBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }

    public HttpBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    @Nullable
    private Headers appendHeaders(Map<String, String> headers) {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (headers == null || headers.isEmpty()) {
            return null;
        }

        for (String key : headers.keySet()) {
            headerBuilder.add(key, headers.get(key));
        }
        return headerBuilder.build();
    }

}
