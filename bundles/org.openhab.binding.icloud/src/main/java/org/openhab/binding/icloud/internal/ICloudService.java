package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.icloud.internal.utilities.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * TODO simon This type ...
 *
 */
public class ICloudService {

  private final static Logger LOGGER = LoggerFactory.getLogger(ICloudService.class);

  private final static String AUTH_ENDPOINT = "https://idmsa.apple.com/appleauth/auth";

  private final static String HOME_ENDPOINT = "https://www.icloud.com";

  private final static String SETUP_ENDPOINT = "https://setup.icloud.com/setup/ws/1";

  private final Gson gson = new GsonBuilder().create();

  // private final String oAuthState = "auth-" + UUID.randomUUID().toString();
  // private final String oAuthState = "auth-34792a3-3333-3333-3633-3377333333";

  // private final String clientId = "d39ba9916b7251055b22c7f910e2ea796ee65e98b2ddecea8f5dde8d9d1a815d";

  private String appleId;

  private String password;

  private String clientId;

  private String accountCountry = null;

  /**
   * @return sessionId
   */
  public String getSessionId() {

    return this.sessionId;
  }

  /**
   * @param sessionId new value of {@link #getsessionId}.
   */
  public void setSessionId(String sessionId) {

    this.sessionId = sessionId;
  }

  /**
   * @return sessionToken
   */
  public String getSessionToken() {

    return this.sessionToken;
  }

  /**
   * @param sessionToken new value of {@link #getsessionToken}.
   */
  public void setSessionToken(String sessionToken) {

    this.sessionToken = sessionToken;
  }

  /**
   * @return trustToken
   */
  public String getTrustToken() {

    return this.trustToken;
  }

  /**
   * @param trustToken new value of {@link #gettrustToken}.
   */
  public void setTrustToken(String trustToken) {

    this.trustToken = trustToken;
  }

  /**
   * @return scnt
   */
  public String getScnt() {

    return this.scnt;
  }

  /**
   * @param scnt new value of {@link #getscnt}.
   */
  public void setScnt(String scnt) {

    this.scnt = scnt;
  }

  private String sessionId = null;

  private String sessionToken = null;

  private String trustToken = null;

  private String scnt = null;

  private boolean verify;

  private boolean withFamily;

  private ICloudSession session;

  private Map<String, Object> data = new HashMap();

  // TODO why this pyicloud
  private Object params;

  // private Map<String, String> sessionData = new HashMap();

  private Map<String, Object> webservices;

  public ICloudService(String appleId, String password) throws IOException, InterruptedException {

    this(appleId, password, "auth-" + UUID.randomUUID().toString().toLowerCase(), true, true);
  }

  public ICloudService(String appleId, String password, String clientId) throws IOException, InterruptedException {

    this(appleId, password, clientId, true, true);
  }

  public ICloudService(String appleId, String password, String clientId, boolean verify, boolean withFamily)
      throws IOException, InterruptedException {

    this.appleId = appleId;
    this.password = password;
    this.clientId = clientId;

    this.session = new ICloudSession(this);
    this.session.updateHeaders(Pair.of("Accept", "*/*"), Pair.of("Origin", HOME_ENDPOINT),
        Pair.of("Referer", HOME_ENDPOINT + "/"));

    // loadCookies();
    // set ClientId from stored session base.py L 228-253;

    // FIXME refactor do not do it in constructor
    authenticate(false, null);
  }

  private void authenticate(boolean forceRefresh, String service) throws IOException, InterruptedException {

    boolean loginSuccessful = false;
    // pyicloud 286
    if (hasToken() && !forceRefresh) {
      this.data = validateToken();
      loginSuccessful = true;
    }

    if (!loginSuccessful && service != null) {
      // TODO work with maps?
      Map<String, Object> app = (Map<String, Object>) ((Map<String, Object>) this.data.get("apps")).get(service);
      if (app.containsKey("canLaunchWithOneFactor") && app.get("canLaunchWithOneFactor").equals(Boolean.TRUE)) {
        try {
          authenticateWithCredentialsService(service);
          loginSuccessful = true;
        } catch (Exception ex) {
          LOGGER.debug("Cannot log into service. Attemping new login.");
        }
      }
    }

    if (!loginSuccessful) {
      LOGGER.debug("Authenticating as {}...", this.appleId);

      // TODO use TO here?
      HashMap localdata = new HashMap();
      localdata.put("accountName", this.appleId);
      localdata.put("password", this.password);
      localdata.put("rememberMe", true);
      if (hasToken()) {
        localdata.put("trustTokens", getTrustToken());
      } else {
        localdata.put("trustTokens", new String[0]);
      }

      // TODO why this pycloud 318?

      List<Pair<String, String>> headers = getAuthHeaders(null);
      /*
       * if (this.sessionData.containsKey("scnt") && !this.sessionData.get("scnt").isEmpty()) {
       * headers.add(Pair.of("scnt", this.sessionData.get("scnt"))); } if (this.sessionData.containsKey("session_id") &&
       * !this.sessionData.get("session_id").isEmpty()) { headers.add(Pair.of("X-Apple-ID-Session-Id",
       * this.sessionData.get("session_id"))); }
       *
       * if (this.getScnt() != null && !this.getScnt().isEmpty()) { headers.add(Pair.of("scnt", this.getSessionId())); }
       * if (this.getSessionId() != null && !this.getSessionId().isEmpty()) {
       * headers.add(Pair.of("X-Apple-ID-Session-Id", this.getSessionId())); }
       */
      try {
        this.session.post(AUTH_ENDPOINT + "/signin?isRememberMeEnabled=true", this.gson.toJson(localdata), headers);
      } catch (ICloudAPIResponseException ex) {
        throw new RuntimeException("Invalid username/password.");
      }

      authenticateWithToken();

      this.webservices = (Map<String, Object>) this.data.get("webservices");
    }
  }

  /**
   * @throws InterruptedException
   * @throws IOException
   *
   */
  private void authenticateWithToken() throws IOException, InterruptedException {

    // TODO use TO here?
    HashMap localdata = new HashMap();
    localdata.put("accountCountryCode", getAccountCountry());
    localdata.put("dsWebAuthToken", getSessionToken());
    localdata.put("extended_login", true);
    if (hasToken()) {
      localdata.put("trustToken", getTrustToken());
    } else {
      localdata.put("trustToken", "");
    }

    try {
      this.data = this.gson
          .fromJson(this.session.post(SETUP_ENDPOINT + "/accountLogin", this.gson.toJson(localdata), null), Map.class);
    } catch (ICloudAPIResponseException ex) {
      throw new RuntimeException("Invalid authentication");
    }

  }

  /**
   * @param pair
   * @return
   */
  private List<Pair<String, String>> getAuthHeaders(Pair<String, String>... replacement) {

    List<Pair<String, String>> result = new ArrayList(
        List.of(Pair.of("Accept", "*/*"), Pair.of("Content-Type", "application/json"),
            Pair.of("X-Apple-OAuth-Client-Id", "d39ba9916b7251055b22c7f910e2ea796ee65e98b2ddecea8f5dde8d9d1a815d"),
            Pair.of("X-Apple-OAuth-Client-Type", "firstPartyAuth"),
            Pair.of("X-Apple-OAuth-Redirect-URI", "https://www.icloud.com"),
            Pair.of("X-Apple-OAuth-Require-Grant-Code", "true"), Pair.of("X-Apple-OAuth-Response-Mode", "web_message"),
            Pair.of("X-Apple-OAuth-Response-Type", "code"), Pair.of("X-Apple-OAuth-State", this.clientId),
            Pair.of("X-Apple-Widget-Key", "d39ba9916b7251055b22c7f910e2ea796ee65e98b2ddecea8f5dde8d9d1a815d")));

    if (replacement != null) {
      ICloudSession.updateList(result, replacement);
    }
    return result;
  }

  /**
   * @param service
   */
  private void authenticateWithCredentialsService(String service) {

    throw new RuntimeException("Not implemented!");

  }

  /**
   * @throws InterruptedException
   * @throws IOException
   *
   */
  private Map<String, Object> validateToken() throws IOException, InterruptedException {

    LOGGER.debug("Checking session token validity");
    try {
      String result = this.session.post(SETUP_ENDPOINT + "/validate", null, null);
      LOGGER.debug("Session token is still valid");
      return this.gson.fromJson(result, Map.class);
    } catch (ICloudAPIResponseException ex) {
      // FIXME log + throw, bad practice?!
      LOGGER.debug("Invalid authentication token");
      throw ex;
    }
  }

  public boolean requires2fa() {

    if (this.data.containsKey("dsInfo")) {
      Map<String, Object> dsInfo = (Map<String, Object>) this.data.get("dsInfo");
      if (((Double) dsInfo.getOrDefault("hsaVersion", "0")) == 2.0) {
        return (this.data.containsKey("hsaChallengeRequired")
            && ((Boolean) this.data.getOrDefault("hsaChallengeRequired", Boolean.FALSE) || !isTrustedSession()));
      }
    }
    return false;
  }

  /**
   * @return
   */
  public boolean isTrustedSession() {

    return (boolean) this.data.getOrDefault("hsaTrustedBrowser", Boolean.FALSE);
  }

  /**
   * @param code
   * @return
   * @throws InterruptedException
   * @throws IOException
   */
  public boolean validate2faCode(String code) throws IOException, InterruptedException {

    // TODO use TO here?
    HashMap localdata = new HashMap();
    localdata.put("securityCode", Map.of("code", code));

    // TODO why pyicloud session_data

    List<Pair<String, String>> headers = getAuthHeaders(Pair.of("Accept", "application/json"));

    /*
     * if (this.sessionData.containsKey("scnt") && !this.sessionData.get("scnt").isEmpty()) {
     * headers.add(Pair.of("scnt", this.sessionData.get("scnt"))); } if (this.sessionData.containsKey("session_id") &&
     * !this.sessionData.get("session_id").isEmpty()) { headers.add(Pair.of("X-Apple-ID-Session-Id",
     * this.sessionData.get("session_id"))); }
     */

    if (getScnt() != null && !getScnt().isEmpty()) {
      headers.add(Pair.of("scnt", getScnt()));
    }
    if (getSessionId() != null && !getSessionId().isEmpty()) {
      headers.add(Pair.of("X-Apple-ID-Session-Id", getSessionId()));
    }

    try {
      this.session.post(AUTH_ENDPOINT + "/verify/trusteddevice/securitycode", this.gson.toJson(localdata), headers);
    } catch (ICloudAPIResponseException ex) {
      // TODO
      // if error.code == -21669:
      // # Wrong verification code
      // LOGGER.error("Code verification failed.")
      return false;
    }

    LOGGER.debug("Code verification successful.");

    trustSession();
    return true;
    // return not self.requires_2sa
  }

  private String getWebserviceUrl(String wsKey) {

    return (String) ((Map) this.webservices.get(wsKey)).get("url");
  }

  /**
   * @throws InterruptedException
   * @throws IOException
   *
   */
  public void trustSession() throws IOException, InterruptedException {

    List<Pair<String, String>> headers = getAuthHeaders();

    if (getScnt() != null && !getScnt().isEmpty()) {
      headers.add(Pair.of("scnt", getScnt()));
    }
    if (getSessionId() != null && !getSessionId().isEmpty()) {
      headers.add(Pair.of("X-Apple-ID-Session-Id", getSessionId()));
    }
    this.session.get(AUTH_ENDPOINT + "/2sv/trust", null, headers);
    authenticateWithToken();

  }

  public FindMyIPhoneServiceManager getDevices() throws IOException, InterruptedException {

    return new FindMyIPhoneServiceManager(this.session, getWebserviceUrl("findme"), this.params, this.withFamily);
  }

  /**
   * @return
   */
  public boolean hasToken() {

    return this.sessionToken != null && !this.sessionToken.isEmpty();
  }

  /**
   * @return accountCountry
   */
  public String getAccountCountry() {

    return this.accountCountry;
  }

  /**
   * @param accountCountry new value of {@link #getaccountCountry}.
   */
  public void setAccountCountry(String accountCountry) {

    this.accountCountry = accountCountry;
  }

}
