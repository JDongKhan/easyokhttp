package com.jd.easyokhttp.okhttps.Interceptor;

import android.text.TextUtils;

import com.jd.easyokhttp.utils.PreferenceUtil;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author jd
 */
public class NetCacheInterceptor implements Interceptor {
    /**
     * 30在线的时候的缓存过期时间，如果想要不缓存，直接时间设置为0
     */
    private int onlineCacheTime;
    public void setOnlineTime(int time) {
        this.onlineCacheTime = time;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder1 = request.newBuilder();

        //这里坐了自动解析头部和取值。之前一个项目要用头部的Token字段。我也不知道为什么不用cookie
        //(到时候最好用csdn登录来做)
        String token = (String) PreferenceUtil.get("USER_TOKEN", "");
        if (!TextUtils.isEmpty(token)) {
            builder1.addHeader("Token", token)
                    .build();
        }
        request = builder1.build();
        Response response = chain.proceed(request);
        List<String> list = response.headers().values("Token");
        if (list.size() > 0) {
            PreferenceUtil.put("USER_TOKEN", list.get(0));
        }

        if (onlineCacheTime != 0) {
            //如果有时间就设置缓存
            int temp = onlineCacheTime;
            Response response1 = response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + temp)
                    .removeHeader("Pragma")
                    .build();
            onlineCacheTime = 0;
            return response1;
        } else {
            //如果没有时间就不缓存
            Response response1 = response.newBuilder()
                    .header("Cache-Control", "no-cache")
                    .removeHeader("Pragma")
                    .build();
            return response1;
        }
//        return response;
    }
}
