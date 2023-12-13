/*
 * Copyright (C), 2002-2016, �����׹������������޹�˾
 * FileName: CookieStoreManager.java
 * Author:   14074533
 * Date:     2016-3-30 ����9:26:51
 * Description: //ģ��Ŀ�ġ���������
 * History: //�޸ļ�¼
 * <author>      <time>      <version>    <desc>
 * �޸�������             �޸�ʱ��            �汾��                  ����
 */
package com.jd.easyokhttp.okhttps.cookie;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.jd.easyokhttp.okhttps.AppUtil;

import java.util.Locale;

/**
 * Cookie管理
 *
 * @author jd
 */
public class CookieJarManager {

  private static volatile CookieJarManager instance;

  private Application mApplication;

  private CookieJarImpl cookieJar;

  private String source;

  private TokenGenerate tokenGenerate;

  private String sToken;

  private CookieJarManager() {}

  public static CookieJarManager getInstance() {
    if (instance == null) {
      synchronized (CookieJarManager.class) {
        if (instance == null) {
          instance = new CookieJarManager();
        }
      }
    }
    return instance;
  }

  public void init(Application app) {
    init(app, "");
  }

  public void init(Application app, String source_from) {
    mApplication = app;
    source = source_from;
    cookieJar = new CookieJarImpl(new PersistentCookieStore());
  }

  public Context getContext() {
    return mApplication;
  }

  /**
   * 获取全局的cookie实例
   */
  public CookieJarImpl getCookieJar() {
    return cookieJar;
  }

  /**
   * 获取来源系统，店+:StorePlus，独立Pos:PosApp
   *
   * @return
   */
  public String getSource() {
    return TextUtils.isEmpty(source) ? "SmartHealth" : source;
  }

  /**
   * 获取User-Agent
   *
   * @return
   */
  public String getUserAgent() {
    try {
      return "Mozilla/5.0(Linux; U; Android " + Build.VERSION.RELEASE + "; "
        + Locale.getDefault().getLanguage() + "; deviceId:"
        + AppUtil.getDeviceId() + "; " + Build.MODEL
        + ") AppleWebKit/533.0 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"
        + "; version:" + AppUtil.getAppVersionName()
        + "; AppClient:" + getSource()
        + ";";
    } catch (Exception e) {
      Log.w("Sonar Catch Exception", e);
      e.printStackTrace();
      return "";
    }
  }

  /**
   * 清空Cookie
   */
  public void clearCookieStore() {
    if (cookieJar != null && cookieJar.getCookieStore() != null) {
      cookieJar.getCookieStore().removeAllCookie();
    }
  }

  public void setToken(String token) {
    sToken = token;
  }

  public void setTokenGenerate(TokenGenerate tokenGenerate){
    this.tokenGenerate = tokenGenerate;
  }

  public String getToken() {
    if (tokenGenerate != null) {
      setToken(tokenGenerate.generate());
    }
    return sToken ;
  }

  public interface  TokenGenerate {
    /**
     * 生成token
     * @return
     */
    String generate();
  }
}
