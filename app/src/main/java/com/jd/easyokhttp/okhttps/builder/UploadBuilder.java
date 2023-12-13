package com.jd.easyokhttp.okhttps.builder;

import android.os.Handler;
import android.util.Pair;

import com.jd.easyokhttp.okhttps.request.CountingRequestBody;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author jd
 */
public class UploadBuilder extends  HttpBuilder {
    private Pair<String, File>[] files;

    public UploadBuilder(OkHttpClient okHttpClient, Handler delivery) {
        super(okHttpClient, delivery);
    }

    @Override
    protected Request.Builder createBuilder() {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                bodyBuilder.addFormDataPart(key, params.get(key));
            }
        }
        addFiles(bodyBuilder);
        RequestBody requestBody = bodyBuilder.build();
        //这是正常的请求头
        Request.Builder mBuilder = new Request.Builder();
        mBuilder.url(url);
        //进项这部操作才能监听进度，来自鸿洋okHttpUtils
        RequestBody requestBodyProgress = new CountingRequestBody(requestBody, (bytesWritten, contentLength) -> mDelivery.post(() -> callback.inProgress(contentLength,bytesWritten * 1.0f / contentLength)));
        mBuilder.post(requestBodyProgress);
        return mBuilder;
    }

    public UploadBuilder files(Pair<String, File>... files) {
        this.files = files;
        return this;
    }

    @Override
    public UploadBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public UploadBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }


    @Override
    public UploadBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }


    public void addFiles(MultipartBody.Builder mBuilder) {
        if (files != null) {
            RequestBody fileBody = null;
            for (int i = 0; i < files.length; i++) {
                if (files[i] != null) {
                    Pair<String, File> filePair = files[i];
                    String fileKeyName = filePair.first;
                    File file = filePair.second;
                    String fileName = file.getName();
                    fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                    mBuilder.addPart(Headers.of("Content-Disposition",
                            "form-data; name=\"" + fileKeyName + "\"; filename=\"" + fileName + "\""),
                            fileBody);
                }
            }
        }
    }


    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

}
