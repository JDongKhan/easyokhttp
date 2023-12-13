package com.jd.easyokhttp.utils.networks;

/**
 * @author jd
 */
public interface NetStateChangeObserver {
    /**
     * 网络断开连接的回调
     */
    void onNetDisconnected();

    /**
     * 有网络连接的回调
     * @param networkType  网络类型
     */
    void onNetConnected(NetworkType networkType);
}


