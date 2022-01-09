package org.openhab.binding.lgthinq.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.openhab.binding.lgthinq.handler.BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;

public class OauthLgEmpAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(BridgeHandler.class);
    private static final OauthLgEmpAuthenticator instance;
    static {
        instance = new OauthLgEmpAuthenticator();
    }

    public static OauthLgEmpAuthenticator getInstance() {
        return instance;
    }

    private OauthLgEmpAuthenticator() {}



    class PreLoginResult {
        private String username;
        private String signature;
        private String timestamp;
        private String encryptedPwd;

        public PreLoginResult(String username, String signature, String timestamp, String encryptedPwd) {
            this.username = username;
            this.signature = signature;
            this.timestamp = timestamp;
            this.encryptedPwd = encryptedPwd;
        }

        public String getUsername() {
            return username;
        }

        public String getSignature() {
            return signature;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getEncryptedPwd() {
            return encryptedPwd;
        }
    }

    class LoginAccountResult {
        private String userIdType;
        private String userId;
        private String country;
        private String loginSessionId;

        public LoginAccountResult(String userIdType, String userId, String country, String loginSessionId) {
            this.userIdType = userIdType;
            this.userId = userId;
            this.country = country;
            this.loginSessionId = loginSessionId;
        }

        public String getUserIdType() {
            return userIdType;
        }

        public String getUserId() {
            return userId;
        }

        public String getCountry() {
            return country;
        }

        public String getLoginSessionId() {
            return loginSessionId;
        }
    }

    private Map<String, String> getGatewayRestHeader(String language, String country) {
        return Map.ofEntries(
                new AbstractMap.SimpleEntry<String, String>("Accept", "application/json"),
                new AbstractMap.SimpleEntry<String, String>("x-api-key", API_KEY),
                new AbstractMap.SimpleEntry<String, String>("x-country-code", country),
                new AbstractMap.SimpleEntry<String, String>("x-client-id", CLIENT_ID),
                new AbstractMap.SimpleEntry<String, String>("x-language-code", language),
                new AbstractMap.SimpleEntry<String, String>("x-message-id", MESSAGE_ID),
                new AbstractMap.SimpleEntry<String, String>("x-service-code", SVC_CODE),
                new AbstractMap.SimpleEntry<String, String>("x-service-phase", SVC_PHASE),
                new AbstractMap.SimpleEntry<String, String>("x-thinq-app-level", APP_LEVEL),
                new AbstractMap.SimpleEntry<String, String>("x-thinq-app-os", APP_OS),
                new AbstractMap.SimpleEntry<String, String>("x-thinq-app-type", APP_TYPE),
                new AbstractMap.SimpleEntry<String, String>("x-thinq-app-ver", APP_VER)
        );
    }

    private Map<String, String> getLoginHeader(Gateway gw) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "keep-alive");
        headers.put("X-Device-Language-Type", "IETF");
        headers.put("X-Application-Key", "6V1V8H2BN5P9ZQGOI5DAQ92YZBDO3EK9");
        headers.put("X-Client-App-Key", "LGAO221A02");
        headers.put("X-Lge-Svccode", "SVC709");
        headers.put("X-Device-Type", "M01");
        headers.put("X-Device-Platform", "ADR");
        headers.put("X-Device-Language-Type", "IETF");
        headers.put("X-Device-Publish-Flag", "Y");
        headers.put("X-Device-Country", gw.getCountry());
        headers.put("X-Device-Language", gw.getLanguage());
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "en-US,en;q=0.9,pt-BR;q=0.8,pt;q=0.7");
        headers.put("Accept", "application/json");
        return headers;
    }

    public Gateway discoverGatewayConfiguration(String gwUrl, String language, String country) throws IOException {
        URL u = new URL(gwUrl);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        Map<String, String> header = getGatewayRestHeader(language, country);
        try {
            con.setRequestMethod("GET");
            header.forEach((k, v) -> con.setRequestProperty(k, v));
            int r = con.getResponseCode();
            if (r != 200) {
                // TODO - Handle error
                logger.error("Result not 200:{}", r);
            } else {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                Map<String, Object> obj = new ObjectMapper().readValue(content.toString(), HashMap.class);
                // get URL to authenticate
                if (!"0000".equals(obj.get("resultCode"))) {
                    logger.error("Result from LGThinq Gateway from Authentication URL was unexpected: {}", obj.get("resultCode"));
                    // TODO - handle error
                    throw new IllegalStateException("...");
                }
                Map result = (Map) obj.get("result");
                if (result == null) {
                    logger.error("the json returned doesn't have \"result\" structure.");
                    // TODO - handle error
                    throw new IllegalStateException("...");
                }
                Gateway gw = new Gateway(result, language, country);
                return gw;
            }
        } finally {
            con.disconnect();
        }
        return null;
    }


    public PreLoginResult preLoginUser(Gateway gw, String username, String password) throws IOException {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Definitively, it is unexpected.", e);
            return null;
        }
        digest.reset();
        digest.update(password.getBytes(StandardCharsets.UTF_8));

        String encPwd = String.format("%0128x", new BigInteger(1, digest.digest()));
        //authUserLogin(gw.getLoginBaseUri(), gw.getEmpBaseUri(), username, toReturn, gw.getCountry(), gw.getLanguage());
        Map<String, String> headers = getLoginHeader(gw);

        // 1) Dong preLogin -> getting the password key
        String preLoginUrl = gw.getLoginBaseUri() + "/preLogin";
        Map<String, String> formData = Map.of("user_auth2", encPwd,
                "log_param", String.format("login request / user_id : %s / " +
                        "third_party : null / svc_list : SVC202,SVC710 / 3rd_service : ", username));
        RestResult resp = RestUtils.postCall(preLoginUrl, headers, formData);
        if (resp.getStatusCode() != 200) {
            logger.error("Error preLogin into account. The reason is:{}", resp.getJsonResponse());
            // TODO - fazer o correto tratamento de erro aqui. Talvez subir uma exceção de validação custom
            throw new IllegalStateException(String.format("Error loggin into acccount:%s", resp.getJsonResponse()));
        }
        Map<String, String> preLoginResult = new ObjectMapper().readValue(resp.getJsonResponse(), HashMap.class);
        logger.debug("encrypted_pw={}, signature={}, tStamp={}",
                preLoginResult.get("encrypted_pw"),
                preLoginResult.get("signature"),
                preLoginResult.get("tStamp"));
        return new PreLoginResult(username, preLoginResult.get("signature"), preLoginResult.get("tStamp"), preLoginResult.get("encrypted_pw"));
    }

    public LoginAccountResult loginUser(Gateway gw, PreLoginResult preLoginResult) throws IOException {
        // 2 - Login with username and hashed password
        Map<String, String> headers = getLoginHeader(gw);
        headers.put("X-Signature", preLoginResult.getSignature());
        headers.put("X-Timestamp", preLoginResult.getTimestamp());
        Map<String, String> formData = Map.of("user_auth2", "" + preLoginResult.getEncryptedPwd(),
                "password_hash_prameter_flag", "Y",
                "svc_list", "SVC202,SVC710"); // SVC202=LG SmartHome, SVC710=EMP OAuth
        String loginUrl = gw.getEmpBaseUri() + "/emp/v2.0/account/session/" + URLEncoder.encode(preLoginResult.getUsername(), StandardCharsets.UTF_8);
        RestResult resp = RestUtils.postCall(loginUrl, headers, formData);
        if (resp.getStatusCode() != 200) {
            logger.error("Error login into account. The reason is:{}", resp.getJsonResponse());
            // TODO - fazer o correto tratamento de erro aqui. Talvez subir uma exceção de validação custom
            throw new IllegalStateException(String.format("Error loggin into acccount:%s", resp.getJsonResponse()));
        }
        Map<String, Object> loginResult = new ObjectMapper().readValue(resp.getJsonResponse(), HashMap.class);
        Map<String, Object> accountResult = (Map<String, Object>) loginResult.get("account");
        if (accountResult == null) {
            throw new IllegalStateException("Error getting account from Login");
        }
        return new LoginAccountResult((String) accountResult.get("userIDType"),
                (String) accountResult.get("userID"),
                (String) accountResult.get("country"),
                (String) accountResult.get("loginSessionID"));
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    public TokenResult getToken(Gateway gw, LoginAccountResult accountResult) throws IOException {
        // 3 - get secret key from emp signature
        String empSearchKeyUrl = gw.getLoginBaseUri() + "/searchKey";
        RestResult resp = RestUtils.getCall(empSearchKeyUrl, null, Map.of("key_name", "OAUTH_SECRETKEY",
                "sever_type", "OP"));
        if (resp.getStatusCode() != 200) {
            logger.error("Error login into account. The reason is:{}", resp.getJsonResponse());
            // TODO - fazer o correto tratamento de erro aqui. Talvez subir uma exceção de validação custom
            throw new IllegalStateException(String.format("Error loggin into acccount:%s", resp.getJsonResponse()));
        }
        Map<String, String> secretResult = new ObjectMapper().readValue(resp.getJsonResponse(), HashMap.class);
        logger.debug("Secret found:{}", secretResult.get("returnData"));
        String secretKey = secretResult.get("returnData");

        // 4 - get OAuth Token Key from EMP API
        Map<String, String> empData = new LinkedHashMap();
        empData.put("account_type", accountResult.getUserIdType());
        empData.put("client_id", CLIENT_ID);
        empData.put("country_code", accountResult.getCountry());
        empData.put("username", "" + accountResult.getUserId());
        String timestamp = getCurrentTimestamp();

        byte[] oauthSig = RestUtils.getTokenSignature(V2_EMP_SESS_URL, secretKey, empData, timestamp);

        Map<String, String> oauthEmpHeaders = new HashMap<>();
        oauthEmpHeaders.put("lgemp-x-app-key", OAUTH_CLIENT_KEY);
        oauthEmpHeaders.put("lgemp-x-date", timestamp);
        oauthEmpHeaders.put("lgemp-x-session-key", accountResult.getLoginSessionId());
        oauthEmpHeaders.put("lgemp-x-signature", new String(oauthSig));
        oauthEmpHeaders.put("Accept", "application/json");
        oauthEmpHeaders.put("X-Device-Type", "M01");
        oauthEmpHeaders.put("X-Device-Platform", "ADR");
        oauthEmpHeaders.put("Content-Type", "application/x-www-form-urlencoded");
        oauthEmpHeaders.put("Access-Control-Allow-Origin", "*");
        oauthEmpHeaders.put("Accept-Encoding", "gzip, deflate, br");
        oauthEmpHeaders.put("Accept-Language", "en-US,en;q=0.9");
        oauthEmpHeaders.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36 Edg/93.0.961.44");

        resp = RestUtils.postCall(V2_EMP_SESS_URL, oauthEmpHeaders, empData);
        return handleTokenResult(resp);
    }

    public UserInfo getUserInfo(TokenResult token) throws IOException {
        UriBuilder builder = UriBuilder.fromUri(token.getOauthBackendUrl()).path(V2_USER_INFO);
        String oauthUrl = builder.build().toURL().toString();
        String timestamp = getCurrentTimestamp();
        byte[] oauthSig = RestUtils.getTokenSignature(oauthUrl, OAUTH_SECRET_KEY, null, timestamp);
        Map<String,String> headers = Map.of(
                "Accept", "application/json",
                "Authorization", String.format("Bearer %s",token.getAccessToken()),
                "X-Lge-Svccode", SVC_CODE,
                "X-Application-Key", APPLICATION_KEY,
                "lgemp-x-app-key", CLIENT_ID,
                "X-Device-Type", "M01",
                "X-Device-Platform", "ADR",
                "x-lge-oauth-date", timestamp,
                "x-lge-oauth-signature", new String(oauthSig));
        RestResult resp = RestUtils.getCall(oauthUrl, headers, null);

        return handleAccountInfoResult(resp);
    }

    private UserInfo handleAccountInfoResult(RestResult resp) throws IOException {
        Map<String, Object> result = new ObjectMapper().readValue(resp.getJsonResponse(), HashMap.class);
        if (resp.getStatusCode() != 200) {
            logger.error("LG API returned error when trying to get user account information. The reason is:{}", resp.getJsonResponse());
            // TODO - fazer o correto tratamento de erro aqui. Talvez subir uma exceção de validação custom
            throw new IllegalStateException(String.format("LG API returned error when trying to get user account information. The reason is:%s", resp.getJsonResponse()));
        } else if (result.get("account") == null || ((Map)result.get("account")).get("userNo") ==null) {
            throw new IllegalStateException(String.format("Error retrieving the account user information from access token"));
        }
        Map<String, String> accountInfo = ((Map) result.get("account"));
        return new UserInfo(accountInfo.get("userNo"),
                accountInfo.get("userID"),
                accountInfo.get("userIDType"),
                accountInfo.get("displayUserID"));
    }

    public TokenResult doRefreshToken(TokenResult currentToken) throws IOException {
        UriBuilder builder = UriBuilder.fromUri(currentToken.getOauthBackendUrl()).path(V2_AUTH_PATH);
        String oauthUrl = builder.build().toURL().toString();
        String timestamp = getCurrentTimestamp();

        Map<String,String> formData = new LinkedHashMap<>();
        formData.put("grant_type","refresh_token");
        formData.put("refresh_token", currentToken.getRefreshToken());

        byte[] oauthSig = RestUtils.getTokenSignature(oauthUrl, OAUTH_SECRET_KEY, formData, timestamp);

        Map<String,String> headers = Map.of("x-lge-appkey", CLIENT_ID,
                "x-lge-oauth-signature", new String(oauthSig),
                "x-lge-oauth-date", timestamp,
                "Accept", "application/json");

        RestResult resp = RestUtils.postCall(oauthUrl, headers, formData);
        return handleRefreshTokenResult(resp, currentToken);
    }

    private TokenResult handleTokenResult(RestResult resp) throws IOException {
        Map<String, String> tokenResult;
        if (resp.getStatusCode() != 200) {
            logger.error("Error getting oauth token. The reason is:{}", resp.getJsonResponse());
            // TODO - fazer o correto tratamento de erro aqui. Talvez subir uma exceção de validação custom
            throw new IllegalStateException(String.format("Error getting oauth token:%s", resp.getJsonResponse()));
        } else {
            tokenResult = new ObjectMapper().readValue(resp.getJsonResponse(), HashMap.class);
            if (!Integer.valueOf(1).equals(tokenResult.get("status")) || tokenResult.get("expires_in") == null) {
                throw new IllegalStateException(String.format("Status error gettinf token:%s", tokenResult));
            }
        }

        return new TokenResult(tokenResult.get("access_token"),
                tokenResult.get("refresh_token"),
                Integer.valueOf("" + tokenResult.get("expires_in")),
                new Date(),
                tokenResult.get("oauth2_backend_url"));
    }

    private TokenResult handleRefreshTokenResult(RestResult resp, TokenResult currentToken) throws IOException {
        Map<String, String> tokenResult;
        if (resp.getStatusCode() != 200) {
            logger.error("Error getting oauth token. The reason is:{}", resp.getJsonResponse());
            // TODO - fazer o correto tratamento de erro aqui. Talvez subir uma exceção de validação custom
            throw new IllegalStateException(String.format("Error getting oauth token:%s", resp.getJsonResponse()));
        } else {
            tokenResult = new ObjectMapper().readValue(resp.getJsonResponse(), HashMap.class);
            if (tokenResult.get("access_token") == null || tokenResult.get("expires_in") == null) {
                throw new IllegalStateException(String.format("Status error get refresh token info:%s", tokenResult));
            }
        }
        TokenResult refreshedToken = SerializationUtils.clone(currentToken);
        refreshedToken.setAccessToken(tokenResult.get("access_token"));
        refreshedToken.setGeneratedTime(new Date());
        refreshedToken.setExpiresIn(Integer.valueOf("" + tokenResult.get("expires_in")));
        return refreshedToken;
    }
}

