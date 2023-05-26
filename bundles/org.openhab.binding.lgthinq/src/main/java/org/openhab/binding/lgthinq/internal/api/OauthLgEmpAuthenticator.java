/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lgthinq.internal.api;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.api.model.GatewayResult;
import org.openhab.binding.lgthinq.internal.errors.RefreshTokenException;
import org.openhab.binding.lgthinq.lgservices.model.ResultCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link OauthLgEmpAuthenticator} main service to authenticate against LG Emp Server via Oauth
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class OauthLgEmpAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(OauthLgEmpAuthenticator.class);
    private static final OauthLgEmpAuthenticator instance;
    private static final Map<String, String> oauthSearchKeyQueryParams = new LinkedHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        instance = new OauthLgEmpAuthenticator();
        oauthSearchKeyQueryParams.put("key_name", "OAUTH_SECRETKEY");
        oauthSearchKeyQueryParams.put("sever_type", "OP");
    }

    public static OauthLgEmpAuthenticator getInstance() {
        return instance;
    }

    private OauthLgEmpAuthenticator() {
    }

    static class PreLoginResult {
        private final String username;
        private final String signature;
        private final String timestamp;
        private final String encryptedPwd;

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

    @NonNullByDefault
    static class LoginAccountResult {
        private final String userIdType;
        private final String userId;
        private final String country;
        private final String loginSessionId;

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
        return Map.ofEntries(new AbstractMap.SimpleEntry<String, String>("Accept", "application/json"),
                new AbstractMap.SimpleEntry<String, String>("x-api-key", API_KEY_V2),
                new AbstractMap.SimpleEntry<String, String>("x-country-code", country),
                new AbstractMap.SimpleEntry<String, String>("x-client-id", CLIENT_ID),
                new AbstractMap.SimpleEntry<String, String>("x-language-code", language),
                new AbstractMap.SimpleEntry<String, String>("x-message-id", MESSAGE_ID),
                new AbstractMap.SimpleEntry<String, String>("x-service-code", SVC_CODE),
                new AbstractMap.SimpleEntry<String, String>("x-service-phase", SVC_PHASE),
                new AbstractMap.SimpleEntry<String, String>("x-thinq-app-level", APP_LEVEL),
                new AbstractMap.SimpleEntry<String, String>("x-thinq-app-os", APP_OS),
                new AbstractMap.SimpleEntry<String, String>("x-thinq-app-type", APP_TYPE),
                new AbstractMap.SimpleEntry<String, String>("x-thinq-app-ver", APP_VER));
    }

    private Map<String, String> getLoginHeader(LGThinqGateway gw) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "keep-alive");
        headers.put("X-Device-Language-Type", "IETF");
        headers.put("X-Application-Key", "6V1V8H2BN5P9ZQGOI5DAQ92YZBDO3EK9");
        headers.put("X-Client-App-Key", "LGAO221A02");
        headers.put("X-Lge-Svccode", "SVC709");
        headers.put("X-Device-Type", "M01");
        headers.put("X-Device-Platform", "ADR");
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

    public LGThinqGateway discoverGatewayConfiguration(String gwUrl, String language, String country,
            String alternativeEmpServer) throws IOException {
        Map<String, String> header = getGatewayRestHeader(language, country);
        RestResult result;
        result = RestUtils.getCall(gwUrl, header, null);

        if (result.getStatusCode() != 200) {
            throw new IllegalStateException(
                    "Expected HTTP OK return, but received result core:" + result.getJsonResponse());
        } else {
            GatewayResult gwResult = LGThinqCanonicalModelUtil.getGatewayResult(result.getJsonResponse());
            ResultCodes resultCode = ResultCodes.fromCode(gwResult.getReturnedCode());
            if (ResultCodes.OK != resultCode) {
                throw new IllegalStateException(String.format(
                        "Result from LGThinq Gateway from Authentication URL was unexpected. ResultCode: %s, with message:%s, Error Description:%s",
                        gwResult.getReturnedCode(), gwResult.getReturnedMessage(), resultCode.getDescription()));
            }

            return new LGThinqGateway(gwResult, language, country, alternativeEmpServer);
        }
    }

    public PreLoginResult preLoginUser(LGThinqGateway gw, String username, String password) throws IOException {
        String encPwd = RestUtils.getPreLoginEncPwd(password);
        Map<String, String> headers = getLoginHeader(gw);
        // 1) Doing preLogin -> getting the password key
        String preLoginUrl = gw.getLoginBaseUri() + PRE_LOGIN_PATH;
        Map<String, String> formData = Map.of("user_auth2", encPwd, "log_param", String.format(
                "login request / user_id : %s / " + "third_party : null / svc_list : SVC202,SVC710 / 3rd_service : ",
                username));
        RestResult resp = RestUtils.postCall(preLoginUrl, headers, formData);
        if (resp == null) {
            logger.error("Error preLogin into account. Null data returned");
            throw new IllegalStateException("Error login into account. Null data returned");
        } else if (resp.getStatusCode() != 200) {
            logger.error("Error preLogin into account. The reason is:{}", resp.getJsonResponse());
            throw new IllegalStateException(String.format("Error login into account:%s", resp.getJsonResponse()));
        }

        Map<String, String> preLoginResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
        });
        logger.debug("encrypted_pw={}, signature={}, tStamp={}", preLoginResult.get("encrypted_pw"),
                preLoginResult.get("signature"), preLoginResult.get("tStamp"));
        return new PreLoginResult(username,
                Objects.requireNonNull(preLoginResult.get("signature"),
                        "Unexpected login json result. Node 'signature' not found"),
                Objects.requireNonNull(preLoginResult.get("tStamp"),
                        "Unexpected login json result. Node 'signature' not found"),
                Objects.requireNonNull(preLoginResult.get("encrypted_pw"),
                        "Unexpected login json result. Node 'signature' not found"));
    }

    public LoginAccountResult loginUser(LGThinqGateway gw, PreLoginResult preLoginResult) throws IOException {
        // 2 - Login with username and hashed password
        Map<String, String> headers = getLoginHeader(gw);
        headers.put("X-Signature", preLoginResult.getSignature());
        headers.put("X-Timestamp", preLoginResult.getTimestamp());
        Map<String, String> formData = Map.of("user_auth2", "" + preLoginResult.getEncryptedPwd(),
                "password_hash_prameter_flag", "Y", "svc_list", "SVC202,SVC710"); // SVC202=LG SmartHome, SVC710=EMP
                                                                                  // OAuth
        String loginUrl = gw.getEmpBaseUri() + V2_SESSION_LOGIN_PATH
                + URLEncoder.encode(preLoginResult.getUsername(), StandardCharsets.UTF_8);
        RestResult resp = RestUtils.postCall(loginUrl, headers, formData);
        if (resp == null) {
            logger.error("Error login into account. Null data returned");
            throw new IllegalStateException("Error loggin into acccount. Null data returned");
        } else if (resp.getStatusCode() != 200) {
            logger.error("Error login into account. The reason is:{}", resp.getJsonResponse());
            throw new IllegalStateException(String.format("Error login into account:%s", resp.getJsonResponse()));
        }
        Map<String, Object> loginResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
        });
        Map<String, Object> accountResult = (Map<String, Object>) loginResult.get("account");
        if (accountResult == null) {
            throw new IllegalStateException("Error getting account from Login");
        }
        return new LoginAccountResult(
                Objects.requireNonNull((String) accountResult.get("userIDType"),
                        "Unexpected account json result. 'userIDType' not found"),
                Objects.requireNonNull((String) accountResult.get("userID"),
                        "Unexpected account json result. 'userID' not found"),
                Objects.requireNonNull((String) accountResult.get("country"),
                        "Unexpected account json result. 'country' not found"),
                Objects.requireNonNull((String) accountResult.get("loginSessionID"),
                        "Unexpected account json result. 'loginSessionID' not found"));
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    public TokenResult getToken(LGThinqGateway gw, LoginAccountResult accountResult) throws IOException {
        // 3 - get secret key from emp signature
        String empSearchKeyUrl = gw.getLoginBaseUri() + OAUTH_SEARCH_KEY_PATH;

        RestResult resp = RestUtils.getCall(empSearchKeyUrl, null, oauthSearchKeyQueryParams);
        if (resp.getStatusCode() != 200) {
            logger.error("Error login into account. The reason is:{}", resp.getJsonResponse());
            throw new IllegalStateException(String.format("Error loggin into acccount:%s", resp.getJsonResponse()));
        }
        Map<String, String> secretResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
        });
        @NonNull
        String secretKey = Objects.requireNonNull(secretResult.get("returnData"),
                "Unexpected json returned. Expected 'returnData' node here");
        logger.debug("Secret found:{}", secretResult.get("returnData"));

        // 4 - get OAuth Token Key from EMP API
        Map<String, String> empData = new LinkedHashMap<>();
        empData.put("account_type", accountResult.getUserIdType());
        empData.put("client_id", CLIENT_ID);
        empData.put("country_code", accountResult.getCountry());
        empData.put("username", "" + accountResult.getUserId());
        String timestamp = getCurrentTimestamp();

        byte[] oauthSig = RestUtils.getTokenSignature(gw.getTokenSessionEmpUrl(), secretKey, empData, timestamp);

        Map<String, String> oauthEmpHeaders = new LinkedHashMap<>();
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
        oauthEmpHeaders.put("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36 Edg/93.0.961.44");
        logger.debug("===> Localized timestamp used: [{}]", timestamp);
        logger.debug("===> signature created: [{}]", new String(oauthSig));
        resp = RestUtils.postCall(gw.getTokenSessionEmpUrl(), oauthEmpHeaders, empData);
        return handleTokenResult(resp);
    }

    public UserInfo getUserInfo(TokenResult token) throws IOException {
        UriBuilder builder = UriBuilder.fromUri(token.getOauthBackendUrl()).path(V2_USER_INFO);
        String oauthUrl = builder.build().toURL().toString();
        String timestamp = getCurrentTimestamp();
        byte[] oauthSig = RestUtils.getTokenSignature(oauthUrl, OAUTH_SECRET_KEY, Collections.EMPTY_MAP, timestamp);
        Map<String, String> headers = Map.of("Accept", "application/json", "Authorization",
                String.format("Bearer %s", token.getAccessToken()), "X-Lge-Svccode", SVC_CODE, "X-Application-Key",
                APPLICATION_KEY, "lgemp-x-app-key", CLIENT_ID, "X-Device-Type", "M01", "X-Device-Platform", "ADR",
                "x-lge-oauth-date", timestamp, "x-lge-oauth-signature", new String(oauthSig));
        RestResult resp = RestUtils.getCall(oauthUrl, headers, null);

        return handleAccountInfoResult(resp);
    }

    private UserInfo handleAccountInfoResult(RestResult resp) throws IOException {
        Map<String, Object> result = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
        });
        if (resp.getStatusCode() != 200) {
            logger.error("LG API returned error when trying to get user account information. The reason is:{}",
                    resp.getJsonResponse());
            throw new IllegalStateException(
                    String.format("LG API returned error when trying to get user account information. The reason is:%s",
                            resp.getJsonResponse()));
        } else if (result.get("account") == null || ((Map) result.get("account")).get("userNo") == null) {
            throw new IllegalStateException(
                    String.format("Error retrieving the account user information from access token"));
        }
        Map<String, String> accountInfo = (Map) result.get("account");

        return new UserInfo(
                Objects.requireNonNullElse(accountInfo.get("userNo"),
                        "Unexpected result. userID must be present in json result"),
                Objects.requireNonNull(accountInfo.get("userID"),
                        "Unexpected result. userID must be present in json result"),
                Objects.requireNonNull(accountInfo.get("userIDType"),
                        "Unexpected result. userIDType must be present in json result"),
                Objects.requireNonNullElse(accountInfo.get("displayUserID"), ""));
    }

    public TokenResult doRefreshToken(TokenResult currentToken) throws IOException, RefreshTokenException {
        UriBuilder builder = UriBuilder.fromUri(currentToken.getOauthBackendUrl()).path(V2_AUTH_PATH);
        String oauthUrl = builder.build().toURL().toString();
        String timestamp = getCurrentTimestamp();

        Map<String, String> formData = new LinkedHashMap<>();
        formData.put("grant_type", "refresh_token");
        formData.put("refresh_token", currentToken.getRefreshToken());

        byte[] oauthSig = RestUtils.getTokenSignature(oauthUrl, OAUTH_SECRET_KEY, formData, timestamp);

        Map<String, String> headers = Map.of("x-lge-appkey", CLIENT_ID, "x-lge-oauth-signature", new String(oauthSig),
                "x-lge-oauth-date", timestamp, "Accept", "application/json");

        RestResult resp = RestUtils.postCall(oauthUrl, headers, formData);
        return handleRefreshTokenResult(resp, currentToken);
    }

    private TokenResult handleTokenResult(@Nullable RestResult resp) throws IOException {
        Map<String, Object> tokenResult;
        if (resp == null) {
            throw new IllegalStateException("Error getting oauth token. Null data returned");
        }
        if (resp.getStatusCode() != 200) {
            logger.error("Error getting oauth token. HTTP Status Code is:{}, The reason is:{}", resp.getStatusCode(),
                    resp.getJsonResponse());
            throw new IllegalStateException(String.format("Error getting oauth token:%s", resp.getJsonResponse()));
        } else {
            tokenResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
            });
            Integer status = (Integer) tokenResult.get("status");
            if ((status != null && !"1".equals("" + status)) || tokenResult.get("expires_in") == null) {
                throw new IllegalStateException(String.format("Status error getting token:%s", tokenResult));
            }
        }

        return new TokenResult(
                Objects.requireNonNull((String) tokenResult.get("access_token"),
                        "Unexpected result. access_token must be present in json result"),
                Objects.requireNonNull((String) tokenResult.get("refresh_token"),
                        "Unexpected result. refresh_token must be present in json result"),
                Integer.parseInt(Objects.requireNonNull((String) tokenResult.get("expires_in"),
                        "Unexpected result. expires_in must be present in json result")),
                new Date(), Objects.requireNonNull((String) tokenResult.get("oauth2_backend_url"),
                        "Unexpected result. oauth2_backend_url must be present in json result"));
    }

    private TokenResult handleRefreshTokenResult(@Nullable RestResult resp, TokenResult currentToken)
            throws IOException, RefreshTokenException {
        Map<String, String> tokenResult;
        if (resp == null) {
            logger.error("Error getting oauth token. Null data returned");
            throw new RefreshTokenException("Error getting oauth token. Null data returned");
        }
        if (resp.getStatusCode() != 200) {
            logger.error("Error getting oauth token. HTTP Status Code is:{}, The reason is:{}", resp.getStatusCode(),
                    resp.getJsonResponse());
            throw new RefreshTokenException(String.format("Error getting oauth token:%s", resp.getJsonResponse()));
        } else {
            tokenResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
            });
            if (tokenResult.get("access_token") == null || tokenResult.get("expires_in") == null) {
                throw new RefreshTokenException(String.format("Status error get refresh token info:%s", tokenResult));
            }
        }

        currentToken.setAccessToken(Objects.requireNonNull(tokenResult.get("access_token"),
                "Unexpected error. Access Token must ever been provided by LG API"));
        currentToken.setGeneratedTime(new Date());
        currentToken.setExpiresIn(Integer.parseInt(Objects.requireNonNull(tokenResult.get("expires_in"),
                "Unexpected error. Access Token must ever been provided by LG API")));
        return currentToken;
    }
}
