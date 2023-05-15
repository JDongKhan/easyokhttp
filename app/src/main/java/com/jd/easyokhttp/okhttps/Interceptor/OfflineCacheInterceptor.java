package com.jd.easyokhttp.okhttps.Interceptor;

import com.jd.easyokhttp.okhttps.AppUtil;
import com.jd.easyokhttp.utils.networks.NetWorkUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class OfflineCacheInterceptor implements Interceptor {
    private static OfflineCacheInterceptor offlineCacheInterceptor;
    //离线的时候的缓存的过期时间
    private int offlineCacheTime;

    public void setOfflineCacheTime(int time) {
        this.offlineCacheTime = time;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (!NetWorkUtils.isNetworkConnected(AppUtil.getApp())) {
            if (offlineCacheTime != 0) {
                int temp = offlineCacheTime;
                request = request.newBuilder()
//                        .cacheControl(new CacheControl
//                                .Builder()
//                                .maxStale(60,TimeUnit.SECONDS)
//                                .onlyIfCached()
//                                .build()
//                        ) 两种方式结果是一样的，写法不同
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + temp)
                        .build();
                offlineCacheTime = 0;
            } else {
                request = request.newBuilder()
//                        .cacheControl(new CacheControl
//                                .Builder()
//                                .maxStale(60,TimeUnit.SECONDS)
//                                .onlyIfCached()
//                                .build()
//                        ) 两种方式结果是一样的，写法不同
                        .header("Cache-Control", "no-cache")
                        .build();
            }
        }
        return chain.proceed(request);
    }
}
