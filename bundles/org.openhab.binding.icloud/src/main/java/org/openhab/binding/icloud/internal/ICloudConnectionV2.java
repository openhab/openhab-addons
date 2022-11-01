package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;

import org.openhab.binding.icloud.internal.json.request.ICloudAuthDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * TODO simon This type ...
 *
 */
public class ICloudConnectionV2 {

  private final static Logger LOGGER = LoggerFactory.getLogger(ICloudConnectionV2.class);

  private final static String IDMSA_ENDPOINT = "https://idmsa.apple.com/appleauth/auth";

  private final HttpClient client;

  private final CookieManager cookieManager;

  private final Gson gson = new GsonBuilder().create();

  private String authToken;

  private String sessionId;

  private String scnt;

  // private final String oAuthState = "auth-" + UUID.randomUUID().toString();
  private final String oAuthState = "auth-34792a3-3333-3333-3633-3377333333";

  private final String clientID = "d39ba9916b7251055b22c7f910e2ea796ee65e98b2ddecea8f5dde8d9d1a815d";

  public ICloudConnectionV2() {

    this.cookieManager = new CookieManager();
    this.client = HttpClient.newBuilder().version(Version.HTTP_1_1).followRedirects(Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(20)).cookieHandler(this.cookieManager).build();
  }

  public void authenticate(String username, String password) throws IOException, InterruptedException {

    LOGGER.debug("Authenticate");
    HttpRequest authRequest = HttpRequest.newBuilder()
        .uri(URI.create(IDMSA_ENDPOINT + "/signin?isRememberMeEnabled=true"))
        // FIXME: What Client-ID
        .header("X-Apple-OAuth-Client-Id", this.clientID).header("X-Apple-OAuth-Client-Type", "firstPartyAuth")
        .header("X-Apple-OAuth-Redirect-URI", "https://www.icloud.com")
        .header("X-Apple-OAuth-Require-Grant-Code", "true").header("X-Apple-OAuth-Response-Mode", "web_message")
        .header("X-Apple-OAuth-Response-Type", "code")
        // FIXME: Generate Id
        .header("X-Apple-OAuth-State", this.oAuthState).header("X-Apple-Widget-Key", this.clientID)
        .header("Content-Type", "application/json").header("Accept", "application/json")
        .POST(BodyPublishers.ofString(this.gson.toJson(new ICloudAuthDataRequest(username, password, "true", null))))
        .build();
    HttpResponse authResponse = this.client.send(authRequest, BodyHandlers.ofString());
    LOGGER.debug("Auth Code: " + authResponse.statusCode());
    LOGGER.debug("Auth Body: " + authResponse.body());
    LOGGER.debug("Auth Headers: " + authResponse.headers());

    Map<String, Object> authResponseMap = this.gson.fromJson((String) authResponse.body(), Map.class);

    this.authToken = authResponse.headers().firstValue("X-Apple-Session-Token").orElse(null);
    this.sessionId = authResponse.headers().firstValue("X-Apple-ID-Session-Id").orElse(null);
    this.scnt = authResponse.headers().firstValue("scnt").orElse(null);

    LOGGER.debug("Auth Token: " + this.authToken);
    LOGGER.debug("Auth SesionId: " + this.sessionId);
    LOGGER.debug("Auth Scnt: " + this.scnt);

  }

  public void listServices() throws IOException, InterruptedException {

    Map<String, Object> params = Map.of("accountCountryCode", "DEU", "dsWebAuthToken", this.authToken, "extended_login",
        true, "trustToken", "");
    LOGGER.debug("listServices: " + this.gson.toJson(params));
    HttpRequest authRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://setup.icloud.com/setup/ws/1/accountLogin")).header("Origin", "https://www.icloud.com")
        .header("Referer", "https://www.icloud.com").POST(BodyPublishers.ofString(this.gson.toJson(params))).build();
    HttpResponse authResponse = this.client.send(authRequest, BodyHandlers.ofString());
    LOGGER.debug("Auth Code: " + authResponse.statusCode());
    LOGGER.debug("Auth Body: " + authResponse.body());
    LOGGER.debug("Auth Headers: " + authResponse.headers());

  }

  public void restoreSession(String authToken, String sessionId, String scnt) {

    this.authToken = authToken;
    this.sessionId = sessionId;
    this.scnt = scnt;
  }

  public void sendSecurityCode(String code) throws IOException, InterruptedException {

    LOGGER.debug("sendSecurityCode");
    // FIXME GSON
    String json = String.format("{\"securityCode\":{\"code\": %s}}", code);

    HttpRequest authRequest = HttpRequest.newBuilder()
        .uri(URI.create(IDMSA_ENDPOINT + "/verify/trusteddevice/securitycode"))
        // FIXME: What Client-ID
        .header("X-Apple-OAuth-Client-Id", this.clientID).header("X-Apple-OAuth-Client-Type", "firstPartyAuth")
        .header("X-Apple-OAuth-Redirect-URI", "https://www.icloud.com")
        .header("X-Apple-OAuth-Require-Grant-Code", "true").header("X-Apple-OAuth-Response-Mode", "web_message")
        .header("X-Apple-OAuth-Response-Type", "code")
        // FIXME: Generate Id
        .header("X-Apple-OAuth-State", this.oAuthState).header("X-Apple-Widget-Key", this.clientID)
        .header("X-Apple-ID-Session-Id", this.sessionId).header("scnt", this.scnt)
        .header("Origin", "https://www.icloud.com").header("Referer", "https://www.icloud.com")
        .header("Content-Type", "application/json").header("Accept", "application/json")
        .POST(BodyPublishers.ofString(json)).build();

    HttpResponse authResponse = this.client.send(authRequest, BodyHandlers.ofString());
    LOGGER.debug("Auth Code: " + authResponse.statusCode());
    LOGGER.debug("Auth Body: " + authResponse.body());
    LOGGER.debug("Auth Headers: " + authResponse.headers());

    this.authToken = authResponse.headers().firstValue("X-Apple-Session-Token").orElse(null);
    this.sessionId = authResponse.headers().firstValue("X-Apple-ID-Session-Id").orElse(null);
    this.scnt = authResponse.headers().firstValue("scnt").orElse(null);

    LOGGER.debug("Auth Token: " + this.authToken);
    LOGGER.debug("Auth SesionId: " + this.sessionId);
    LOGGER.debug("Auth Scnt: " + this.scnt);

    /*
     * if (response.getStatusLine().getStatusCode() >= 300) { throw new IllegalStateException("Failed to verify code: "
     * + response.getStatusLine()); }
     */

  }

  public void sendSound(String deviceId) throws IOException, InterruptedException {

    // FIXME GSON
    String json = "{\"device\": \"" + deviceId
        + "\", \"subject\": \"Find My iPhone Alert\", \"clientContext\": {\"fmly\": true}}";
    LOGGER.debug("sendSound: " + json);
    HttpRequest authRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://p61-fmipweb.icloud.com:443/fmipservice/client/web/playSound"))
        // FIXME: What Client-ID
        .header("Accept", "application/json").header("X-Apple-OAuth-Client-Id", this.clientID)
        .header("X-Apple-OAuth-Client-Type", "firstPartyAuth")
        .header("X-Apple-OAuth-Redirect-URI", "https://www.icloud.com")
        .header("X-Apple-OAuth-Require-Grant-Code", "true").header("X-Apple-OAuth-Response-Mode", "web_message")
        .header("X-Apple-OAuth-Response-Type", "code")
        // FIXME: Generate Id
        .header("X-Apple-OAuth-State", this.oAuthState).header("X-Apple-Widget-Key", this.clientID)
        .header("X-Apple-ID-Session-Id", this.sessionId).header("scnt", this.scnt)
        .header("X-Apple-Account-Country", "DEU").header("X-Apple-Session-Token", this.authToken)
        .header("Origin", "https://www.icloud.com").header("Referer", "https://www.icloud.com")
        .POST(BodyPublishers.ofString(json)).build();

    HttpResponse authResponse = this.client.send(authRequest, BodyHandlers.ofString());
    LOGGER.debug("Auth Code: " + authResponse.statusCode());
    LOGGER.debug("Auth Body: " + authResponse.body());
    LOGGER.debug("Auth Headers: " + authResponse.headers());
    /*
     * if (response.getStatusLine().getStatusCode() >= 300) { throw new IllegalStateException("Failed to verify code: "
     * + response.getStatusLine()); }
     */

  }

}
