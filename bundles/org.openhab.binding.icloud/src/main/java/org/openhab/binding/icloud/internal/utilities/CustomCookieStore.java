package org.openhab.binding.icloud.internal.utilities;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

/**
 * TODO simon This type ...
 *
 */
public class CustomCookieStore implements CookieStore {

  private CookieStore cookieStore;

  /**
   * The constructor.
   *
   * @param cookieStore
   */
  public CustomCookieStore(CookieStore cookieStore) {

    this.cookieStore = cookieStore;

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
