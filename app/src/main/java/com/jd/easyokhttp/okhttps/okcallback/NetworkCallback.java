package com.jd.easyokhttp.okhttps.okcallback;

import com.google.gson.internal.$Gson$Types;
import com.jd.easyokhttp.utils.ToastUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class NetworkCallback<T> {

    //请求网络之前，一般展示loading
    public void onBefore() {}

    //请求网络结束，消失loading
    public void onAfter() {}

    //监听上传图片的进度(目前支持图片上传,其他重写这个方法无效)
    public void inProgress(long total,float progress) {}

    //错误信息
    public void onError(String errorMessage) {
        ToastUtils.showToast(errorMessage);
    }

    public abstract void onSuccess(T response);

    public Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            return null;
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }
    public Type getType() {
        return getSuperclassTypeParameter(getClass());
    }
}
