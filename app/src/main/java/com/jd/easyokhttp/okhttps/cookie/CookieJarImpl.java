package com.jd.easyokhttp.okhttps.cookie;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;


/**
 * @author jd
 */
public class CookieJarImpl implements CookieJar {
  private CustomCookieStore cookieStore;
  /**
   * 用户手动添加的Cookie
   */
  private Map<String, Set<Cookie>> userCookies = new HashMap<String, Set<Cookie>>();
  /**
   * 用来临时保存cookie,并原来还原
   */
  private List<Cookie> tempCookie = new ArrayList<>();
  private HttpUrl tempHttpUrl;

  public CookieJarImpl(CustomCookieStore cookieStore) {
    if (cookieStore == null) {
      throw new IllegalArgumentException("cookieStore can not be null!");
    }
    this.cookieStore = cookieStore;
  }

  public void addCookies(List<Cookie> cookies) {
    for (Cookie cookie : cookies) {
      String domain = cookie.domain();
      Set<Cookie> domainCookies = userCookies.get(domain);
      if (domainCookies == null) {
        domainCookies = new HashSet<Cookie>();
        userCookies.put(domain, domainCookies);
      }
      domainCookies.add(cookie);
    }
  }

  @Override
  public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
    cookieStore.saveCookies(url, cookies);
  }

  @Override
  public synchronized List<Cookie> loadForRequest(HttpUrl url) {
    // 暂时解决登录问题。带上全部cookie，不根据url携带cookie。
    List<Cookie> requestUrlCookies = cookieStore.getAllCookieNotExpired(url);
    Set<Cookie> userUrlCookies = userCookies.get(url.host());
    Set<Cookie> cookieSet = new HashSet<Cookie>();
    if (requestUrlCookies != null) {
      cookieSet.addAll(requestUrlCookies);
    }
    if (userUrlCookies != null) {
      cookieSet.addAll(userUrlCookies);
    }
    // LogUtil.i("OkHttp", "loadForRequest : " + url.toString() + "\n " + cookieSet.toString());
    return new ArrayList<Cookie>(cookieSet);
  }

  public CustomCookieStore getCookieStore() {
    return cookieStore;
  }

  /**
   * 保存Cookie
   * @param url
   */
  public void saveCookies(HttpUrl url){
    tempCookie.clear();
    List<Cookie> cookies = loadForRequest(url);
    if(cookies!=null){
      tempHttpUrl = url;
      tempCookie.addAll(cookies);
    }
  }

  /**
   * 还原Cookie
   */
  public void restoreCookies(){
    if(tempCookie!=null&&
            !tempCookie.isEmpty()&&tempHttpUrl!=null){
      try {
        saveFromResponse(tempHttpUrl, tempCookie);
      }catch (Exception e){
        e.printStackTrace();
        Log.w("Sonar exception",e);
      }
    }
  }

}
