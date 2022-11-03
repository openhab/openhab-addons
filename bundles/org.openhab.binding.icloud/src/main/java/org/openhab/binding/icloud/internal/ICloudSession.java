package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openhab.binding.icloud.internal.utilities.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * TODO simon This type ...
 *
 */
public class ICloudSession {

  /**
   * @return sessionId
   */
  public String getSessionId() {

    return this.sessionId;
  }

  /**
   * @return scnt
   */
  public String getScnt() {

    return this.scnt;
  }

  /**
   * @return sessionToken
   */
  public String getSessionToken() {

    return this.sessionToken;
  }

  private final static Logger LOGGER = LoggerFactory.getLogger(ICloudService.class);

  private final ICloudService iCloudService;

  private final HttpClient client;

  private final CookieManager cookieManager;

  private final List<Pair<String, String>> headers = new ArrayList();

  private String accountCountry = null;

  private String sessionId = null;

  private String sessionToken = null;

  private String trustToken = null;

  private String scnt = null;

  private final Gson gson = new GsonBuilder().create();

  public ICloudSession(ICloudService iCloudService) {

    this.iCloudService = iCloudService;

    this.cookieManager = new CookieManager();
    this.client = HttpClient.newBuilder().version(Version.HTTP_1_1).followRedirects(Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(20)).cookieHandler(this.cookieManager).build();
  }

  public String post(String url, String kwargs, List<Pair<String, String>> overrideHeaders)
      throws IOException, InterruptedException {

    return request("POST", url, kwargs, overrideHeaders);
  }

  public String get(String url, String kwargs, List<Pair<String, String>> overrideHeaders)
      throws IOException, InterruptedException {

    return request("GET", url, kwargs, overrideHeaders);
  }

  private String request(String method, String url, String kwargs, List<Pair<String, String>> overrideHeaders)
      throws IOException, InterruptedException {

    Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

    List<Pair<String, String>> requestHeaders = this.headers;
    if (overrideHeaders != null) {
      requestHeaders = overrideHeaders;
    } else {
      if (this.accountCountry != null) {
        requestHeaders.add(Pair.of("X-Apple-ID-Account-Country", this.accountCountry));
      }
      if (this.sessionId != null) {
        requestHeaders.add(Pair.of("X-Apple-ID-Session-Id", this.sessionId));
      }
      if (this.sessionToken != null) {
        requestHeaders.add(Pair.of("X-Apple-Session-Token", this.sessionToken));
      }
      if (this.trustToken != null) {
        requestHeaders.add(Pair.of("X-Apple-TwoSV-Trust-Token", this.trustToken));
      }
      if (this.scnt != null) {
        requestHeaders.add(Pair.of("scnt", this.scnt));
      }
    }

    for (Pair<String, String> header : requestHeaders) {
      builder.header(header.key, header.value);
    }

    builder.method(method, BodyPublishers.ofString(kwargs));

    HttpRequest request = builder.build();

    LOGGER.debug("Calling {}\nHeaders -----\n{}\nBody -----\n{}\n------\n", url, request.headers(), kwargs);

    HttpResponse response = this.client.send(request, BodyHandlers.ofString());
    LOGGER.debug("Result {} {}\nHeaders -----\n{}\nBody -----\n{}\n------\n", url, response.statusCode(),
        response.headers().toString(), response.body().toString());

    // TODO Error Handling pyicloud 99-162
    if (response.statusCode() != 200) {
      throw new ICloudAPIResponseException();
      /*
       * 826 if (response.statusCode() == 421 || response.statusCode() == 450 || response.statusCode() == 500) { throw
       * new ICloudAPIResponseException();
       */
    }

    this.accountCountry = response.headers().firstValue("X-Apple-ID-Account-Country").orElse(this.accountCountry);
    this.sessionId = response.headers().firstValue("X-Apple-ID-Session-Id").orElse(this.sessionId);
    this.sessionToken = response.headers().firstValue("X-Apple-Session-Token").orElse(this.sessionToken);
    this.trustToken = response.headers().firstValue("X-Apple-TwoSV-Trust-Token").orElse(this.trustToken);
    this.scnt = response.headers().firstValue("scnt").orElse(this.scnt);

    return response.body().toString();

  }

  /**
   * Update headers, remove existing keys and set given
   *
   * @return headers
   */
  public void updateHeaders(Pair<String, String>... headers) {

    updateList(this.headers, headers);

  }

  // TODO refactor this
  public static void updateList(List<Pair<String, String>> originalList, Pair<String, String>... replacements) {

    for (Pair<String, String> newHeader : replacements) {
      Iterator<Pair<String, String>> it = originalList.iterator();
      boolean found = false;
      while (it.hasNext()) {
        Pair<String, String> existingHeader = it.next();
        if (existingHeader.key.equals(newHeader.key)) {
          if (found) {
            it.remove();
          } else {
            existingHeader.value = newHeader.value;
            found = true;
          }
        }
      }
      if (!found) {
        originalList.add(newHeader);
      }
    }
  }

  /**
   * @return
   */
  public boolean hasToken() {

    return this.sessionToken != null && !this.sessionToken.isEmpty();
  }

  /**
   * @return
   */
  public String getTrustToken() {

    return this.trustToken;
  }

  /**
   * @return
   */
  public String getAccountCountry() {

    return this.accountCountry;
  }
}
