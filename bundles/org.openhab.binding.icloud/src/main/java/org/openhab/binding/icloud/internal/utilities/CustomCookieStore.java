package org.openhab.binding.icloud.internal.utilities;

import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * TODO simon This type ...
 *
 */
public class CustomCookieStore implements CookieStore {

  private CookieStore cookieStore;

  private final static String COOKIES_KEY = "COOKIES";

  private final Gson gson = new GsonBuilder().create();

  private Storage<String> stateStorage;

  private final static Logger LOGGER = LoggerFactory.getLogger(CustomCookieStore.class);

  /**
   * The constructor.
   *
   * @param cookieStore
   */
  public CustomCookieStore(Storage<String> stateStorage) {

    this.cookieStore = new CookieManager().getCookieStore();
    this.stateStorage = stateStorage;
    restoreCookies();

  }

  /**
   * @param cookieData
   *
   */
  private void restoreCookies() {

    String cookieData = this.stateStorage.get(COOKIES_KEY);
    if (cookieData != null) {
      Type type = new TypeToken<List<HttpCookie>>() {
      }.getType();
      try {
        List<HttpCookie> cookies = this.gson.fromJson(cookieData, type);
        for (HttpCookie cookie : cookies) {
          add(URI.create(cookie.getDomain()), cookie);
        }
      } catch (Exception ex) {
        LOGGER.warn("Cannot restore cookies.", ex);
      }
    } else {
      LOGGER.info("No cookie data found. Start with fresh cookies store.");
    }

  }

  public void saveState() {

    try {
      String cookieData = this.gson.toJson(getCookies());
      this.stateStorage.put(COOKIES_KEY, cookieData);
    } catch (Exception ex) {
      LOGGER.warn("Cannot save cookies.", ex);
    }
  }

  @Override
  public void add(URI uri, HttpCookie cookie) {

    this.cookieStore.add(uri, cookie);
  }

  @Override
  public List<HttpCookie> get(URI uri) {

    List<HttpCookie> result = this.cookieStore.get(uri);
    filterCookies(result);
    return result;
  }

  @Override
  public List<HttpCookie> getCookies() {

    List<HttpCookie> result = this.cookieStore.getCookies();
    filterCookies(result);
    return result;
  }

  @Override
  public List<URI> getURIs() {

    return this.cookieStore.getURIs();
  }

  @Override
  public boolean remove(URI uri, HttpCookie cookie) {

    return this.cookieStore.remove(uri, cookie);
  }

  @Override
  public boolean removeAll() {

    return this.cookieStore.removeAll();
  }

  /**
   * Add quotes add beginning and end of all cookie values
   *
   * @param cookieList
   */
  private void filterCookies(List<HttpCookie> cookieList) {

    for (HttpCookie cookie : cookieList) {
      if (!cookie.getValue().startsWith("\"")) {
        cookie.setValue("\"" + cookie.getValue());
      }
      if (!cookie.getValue().endsWith("\"")) {
        cookie.setValue(cookie.getValue() + "\"");
      }
    }

  }

}
