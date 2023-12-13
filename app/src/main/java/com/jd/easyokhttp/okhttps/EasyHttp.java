package com.jd.easyokhttp.okhttps;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.jd.easyokhttp.okhttps.Interceptor.LoggerInterceptor;
import com.jd.easyokhttp.okhttps.Interceptor.NetCacheInterceptor;
import com.jd.easyokhttp.okhttps.Interceptor.OfflineCacheInterceptor;
import com.jd.easyokhttp.okhttps.builder.DownloadBuilder;
import com.jd.easyokhttp.okhttps.builder.GetBuilder;
import com.jd.easyokhttp.okhttps.builder.PostBuilder;
import com.jd.easyokhttp.okhttps.builder.UploadBuilder;
import com.jd.easyokhttp.okhttps.cookie.CookieJarManager;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * @author jd
 */
public class EasyHttp {
    private static EasyHttp easyOk;
    private OkHttpClient okHttpClient;
    private Handler mDelivery;

    private EasyHttp() {
        mDelivery = new Handler(Looper.getMainLooper());
        //证书信任
        okHttpClient = new OkHttpClient.Builder()
                //设置缓存文件路径，和文件大小
                .cache(new Cache(new File(Environment.getExternalStorageDirectory() + "/okhttp_cache/"), 50 * 1024 * 1024))
                .hostnameVerifier((hostname, session) -> true)
                .cookieJar(CookieJarManager.getInstance().getCookieJar())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                //这里是网上对cookie的封装 github : https://github.com/franmontiel/PersistentCookieJar
                //如果你的项目没有遇到cookie管理或者你想通过网络拦截自己存储，那么可以删除persistentcookiejar包
//                .cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext())))
                .addInterceptor(new LoggerInterceptor())
                .addInterceptor(new OfflineCacheInterceptor())
                .addNetworkInterceptor(new NetCacheInterceptor())
                .build();
    }


    public static EasyHttp getInstance() {
        if (easyOk == null) {
            synchronized (EasyHttp.class) {
                if (easyOk == null) {
                    easyOk = new EasyHttp();
                }
            }
        }
        return easyOk;
    }


    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public Handler getDelivery() {
        return mDelivery;
    }

    public static GetBuilder get() {
        return new GetBuilder(EasyHttp.getInstance().getOkHttpClient(), EasyHttp.getInstance().getDelivery());
    }

    public static PostBuilder post() {
        return new PostBuilder(EasyHttp.getInstance().getOkHttpClient(), EasyHttp.getInstance().getDelivery());
    }

    public static UploadBuilder upload() {
        return new UploadBuilder(EasyHttp.getInstance().getOkHttpClient(), EasyHttp.getInstance().getDelivery());
    }

    public static DownloadBuilder download() {
        return new DownloadBuilder(EasyHttp.getInstance().getOkHttpClient(), EasyHttp.getInstance().getDelivery());
    }

    /**
     * tag取消网络请求
     */

    public void cancelOkhttpTag(String tag) {
        Dispatcher dispatcher = okHttpClient.dispatcher();
        synchronized (dispatcher) {
            //请求列表里的，取消网络请求
            for (Call call : dispatcher.queuedCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
            //正在请求网络的，取消网络请求
            for (Call call : dispatcher.runningCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
        }
    }

}
