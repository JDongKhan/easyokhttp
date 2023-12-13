package com.jd.easyokhttp.okhttps.builder;

import android.os.Handler;


import com.jd.easyokhttp.okhttps.okcallback.NetworkCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.FileChannel;
import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author jd
 * 这里应该先生成临时文件
 */
public class DownloadBuilder extends HttpBuilder {
    /**
     * 断点续传的长度
     */
    private long currentLength;
    /**
     * 文件路径(不包括文件名)
     */
    private String path;
    /**
     * 文件名
     */
    private String fileName;
    private String fileNameTemp;
    /**
     * 是否开启断点续传
     */
    private boolean resume;

    public DownloadBuilder(OkHttpClient okHttpClient, Handler delivery) {
        super(okHttpClient, delivery);
    }

    @Override
    protected Request.Builder createBuilder() {
        Request.Builder mBuilder = new Request.Builder();
        mBuilder.url(url);
        //这里只要断点上传，总会走缓存。。所以强制网络下载
        mBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        if (resume) {
            File exFile = new File(path, fileNameTemp);
            if (exFile.exists()) {
                currentLength = exFile.length();
                mBuilder.header("RANGE", "bytes=" + currentLength + "-");
            }
        }
        return mBuilder;
    }

    @Override
    protected void doSuccessCallback(Call call, Response response, NetworkCallback resultMyCall, Callback callback) throws IOException {
        removeOnceTag();
        saveFile(call,response,resultMyCall);
    }

    @Override
    protected void doFailureCallback(Call call, IOException e, NetworkCallback resultMyCall, Callback callback) {
        removeOnceTag();
        //下载失败监听回调
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                String errorMsg;
                if (e instanceof SocketException) {
                } else {
                    if (e instanceof ConnectException) {
                        errorMsg = "网络不可用,请检查网络";
                    } else if (e instanceof SocketTimeoutException) {
                        errorMsg = "请求超时,请稍后再试";
                    } else {
                        errorMsg = "服务器异常,请稍后再试";
                    }
                    resultMyCall.onError(errorMsg);
                }
            }
        });
    }

    private void saveFile(Call call, Response response,final NetworkCallback listener){
        InputStream is = null;
        byte[] buf = new byte[1024];
        int len = 0;
        FileOutputStream fos = null;
        //储存下载文件的目录
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final File tempFile = new File(dir, fileNameTemp);
        try {
            is = response.body().byteStream();
            //总长度
            final long total;
            if (resume) {
                total = response.body().contentLength() + currentLength;
            } else {
                total = response.body().contentLength();
            }
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    listener.inProgress(total,0);
                }
            });
            if (resume) {
                //这个方法是文件开始拼接
                fos = new FileOutputStream(tempFile, true);
            } else {
                //这个是不拼接，从头开始
                fos = new FileOutputStream(tempFile);
            }
            long sum;
            if (resume) {
                sum = currentLength;
            } else {
                sum = 0;
            }
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
                sum += len;
                final int progress = (int) (sum * 1.0f / total * 100);
                //下载中更新进度条
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.inProgress(total,progress);
                    }
                });
            }
            fos.flush();
            final File file = new File(dir, fileName);
            //从临时文件拷贝到目标地址
            copyFile(tempFile,file);
            //下载完成
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(file);
                }
            });

        } catch (final Exception e) {
            mDelivery.post(new Runnable() {
                @Override
                public void run() {
                    listener.onError("文件下载异常");
                }
            });
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
            }
        }
    }

    //拷贝文件
    private void copyFile(File sourceFile,File targetFile) throws FileNotFoundException,IOException {
        FileChannel inputStream = null;
        FileChannel outputStream = null;
        try {
            inputStream = new FileInputStream(sourceFile).getChannel();
            outputStream = new FileOutputStream(targetFile).getChannel();
            if (outputStream != null) {
                outputStream.transferFrom(inputStream,0,inputStream.size());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
           if (inputStream != null) {
               inputStream.close();
           }
           if (outputStream != null) {
               outputStream.close();
           }
        }

    }

    public DownloadBuilder path(String path) {
        this.path = path;
        return this;
    }

    public DownloadBuilder fileName(String fileName) {
        this.fileName = fileName;
        this.fileNameTemp = "temp_" + fileName;
        return this;
    }

    public DownloadBuilder resume(boolean resume) {
        this.resume = resume;
        return this;
    }

    @Override
    public DownloadBuilder url(String url) {
        super.url(url);
        return this;
    }

    @Override
    public DownloadBuilder params(Map<String, String> params) {
        super.params(params);
        return this;
    }

    @Override
    public DownloadBuilder once(boolean once) {
        super.once(once);
        return this;
    }

    @Override
    public DownloadBuilder tag(String tag) {
        super.tag(tag);
        return this;
    }

    @Override
    public DownloadBuilder headers(Map<String, String> headers) {
        super.headers(headers);
        return this;
    }

}
