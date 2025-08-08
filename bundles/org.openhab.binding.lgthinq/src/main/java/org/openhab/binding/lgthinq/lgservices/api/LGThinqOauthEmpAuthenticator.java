/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.api;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.lgservices.api.model.GatewayResult;
import org.openhab.binding.lgthinq.lgservices.errors.RefreshTokenException;
import org.openhab.binding.lgthinq.lgservices.model.ResultCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link LGThinqOauthEmpAuthenticator} main service to authenticate against LG Emp Server via Oauth
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqOauthEmpAuthenticator {

    private static final Map<String, String> OAUTH_SEARCH_KEY_QUERY_PARAMS = new LinkedHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        OAUTH_SEARCH_KEY_QUERY_PARAMS.put("key_name", "OAUTH_SECRETKEY");
        OAUTH_SEARCH_KEY_QUERY_PARAMS.put("sever_type", "OP");
    }

    private final Logger logger = LoggerFactory.getLogger(LGThinqOauthEmpAuthenticator.class);
    private final HttpClient httpClient;

    public LGThinqOauthEmpAuthenticator(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private Map<String, String> getGatewayRestHeader(String language, String country) {
        return Map.ofEntries(new AbstractMap.SimpleEntry<String, String>("Accept", "application/json"),
                new AbstractMap.SimpleEntry<>("x-api-key", LG_API_API_KEY_V2),
                new AbstractMap.SimpleEntry<>("x-country-code", country),
                new AbstractMap.SimpleEntry<>("x-client-id", LG_API_CLIENT_ID),
                new AbstractMap.SimpleEntry<>("x-language-code", language),
                new AbstractMap.SimpleEntry<>("x-message-id", LG_API_MESSAGE_ID),
                new AbstractMap.SimpleEntry<>("x-service-code", LG_API_SVC_CODE),
                new AbstractMap.SimpleEntry<>("x-service-phase", LG_API_SVC_PHASE),
                new AbstractMap.SimpleEntry<>("x-thinq-app-level", LG_API_APP_LEVEL),
                new AbstractMap.SimpleEntry<>("x-thinq-app-os", LG_API_APP_OS),
                new AbstractMap.SimpleEntry<>("x-thinq-app-type", LG_API_APP_TYPE),
                new AbstractMap.SimpleEntry<>("x-thinq-app-ver", LG_API_APP_VER));
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

    LGThinqGateway discoverGatewayConfiguration(String gwUrl, String language, String country,
            String alternativeEmpServer) throws IOException {
        Map<String, String> header = getGatewayRestHeader(language, country);
        RestResult result;
        result = RestUtils.getCall(httpClient, gwUrl, header, null);

        if (result.getStatusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Expected HTTP OK return, but received result core:[%s] - error message:[%s]",
                            result.getJsonResponse(), ResultCodes.getReasonResponse(result.getJsonResponse())));
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

    PreLoginResult preLoginUser(LGThinqGateway gw, String username, String password) throws IOException {
        String encPwd = RestUtils.getPreLoginEncPwd(password);
        Map<String, String> headers = getLoginHeader(gw);
        // 1) Doing preLogin -> getting the password key
        String preLoginUrl = gw.getLoginBaseUri() + LG_API_PRE_LOGIN_PATH;
        Map<String, String> formData = Map.of("user_auth2", encPwd, "log_param", String.format(
                "login request / user_id : %s / " + "third_party : null / svc_list : SVC202,SVC710 / 3rd_service : ",
                username));
        RestResult resp = RestUtils.postCall(httpClient, preLoginUrl, headers, formData);
        if (resp == null) {
            throw new IllegalStateException("Error login into account. Null data returned");
        } else if (resp.getStatusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Error preLogin into account: The reason is: %s", resp.getJsonResponse()));
        }

        Map<String, String> preLoginResult = MAPPER.readValue(resp.getJsonResponse(), new TypeReference<>() {
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

    LoginAccountResult loginUser(LGThinqGateway gw, PreLoginResult preLoginResult) throws IOException {
        // 2 - Login with username and hashed password
        Map<String, String> headers = getLoginHeader(gw);
        headers.put("X-Signature", preLoginResult.signature());
        headers.put("X-Timestamp", preLoginResult.timestamp());
        Map<String, String> formData = Map.of("user_auth2", preLoginResult.encryptedPwd(),
                "password_hash_prameter_flag", "Y", "svc_list", "SVC202,SVC710"); // SVC202=LG SmartHome, SVC710=EMP
        // OAuth
        String loginUrl = gw.getEmpBaseUri() + LG_API_V2_SESSION_LOGIN_PATH
                + URLEncoder.encode(preLoginResult.username(), StandardCharsets.UTF_8);
        RestResult resp = RestUtils.postCall(httpClient, loginUrl, headers, formData);
        if (resp == null) {
            throw new IllegalStateException("Error loggin into acccount. Null data returned");
        } else if (resp.getStatusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Error login into account. The reason is: %s", resp.getJsonResponse()));
        }
        Map<String, Object> loginResult = MAPPER.readValue(resp.getJsonResponse(), new TypeReference<>() {
        });
        @SuppressWarnings("unchecked")
        Map<String, String> accountResult = (Map<String, String>) loginResult.get("account");
        if (accountResult == null) {
            throw new IllegalStateException("Error getting account from Login");
        }
        return new LoginAccountResult(
                Objects.requireNonNull(accountResult.get("userIDType"),
                        "Unexpected account json result. 'userIDType' not found"),
                Objects.requireNonNull(accountResult.get("userID"),
                        "Unexpected account json result. 'userID' not found"),
                Objects.requireNonNull(accountResult.get("country"),
                        "Unexpected account json result. 'country' not found"),
                Objects.requireNonNull(accountResult.get("loginSessionID"),
                        "Unexpected account json result. 'loginSessionID' not found"));
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(LG_API_DATE_FORMAT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    TokenResult getToken(LGThinqGateway gw, LoginAccountResult accountResult) throws IOException {
        // 3 - get secret key from emp signature
        String empSearchKeyUrl = gw.getLoginBaseUri() + LG_API_OAUTH_SEARCH_KEY_PATH;

        RestResult resp = RestUtils.getCall(httpClient, empSearchKeyUrl, null, OAUTH_SEARCH_KEY_QUERY_PARAMS);
        if (resp.getStatusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Error loggin into acccount. The reason is:%s", resp.getJsonResponse()));
        }
        Map<String, String> secretResult = MAPPER.readValue(resp.getJsonResponse(), new TypeReference<>() {
        });
        String secretKey = Objects.requireNonNull(secretResult.get("returnData"),
                "Unexpected json returned. Expected 'returnData' node here");
        logger.debug("Secret found:{}", secretResult.get("returnData"));

        // 4 - get OAuth Token Key from EMP API
        Map<String, String> empData = new LinkedHashMap<>();
        empData.put("account_type", accountResult.userIdType());
        empData.put("client_id", LG_API_CLIENT_ID);
        empData.put("country_code", accountResult.country());
        empData.put("username", accountResult.userId());
        String timestamp = getCurrentTimestamp();

        byte[] oauthSig = RestUtils.getTokenSignature(gw.getTokenSessionEmpUrl(), secretKey, empData, timestamp);

        Map<String, String> oauthEmpHeaders = getOauthEmpHeaders(accountResult, timestamp, oauthSig);
        logger.debug("===> Localized timestamp used: [{}]", timestamp);
        logger.debug("===> signature created: [{}]", new String(oauthSig));
        resp = RestUtils.postCall(httpClient, gw.getTokenSessionEmpUrl(), oauthEmpHeaders, empData);
        return handleTokenResult(resp);
    }

    private Map<String, String> getOauthEmpHeaders(LoginAccountResult accountResult, String timestamp,
            byte[] oauthSig) {
        Map<String, String> oauthEmpHeaders = new LinkedHashMap<>();
        oauthEmpHeaders.put("lgemp-x-app-key", LG_API_OAUTH_CLIENT_KEY);
        oauthEmpHeaders.put("lgemp-x-date", timestamp);
        oauthEmpHeaders.put("lgemp-x-session-key", accountResult.loginSessionId());
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
        return oauthEmpHeaders;
    }

    UserInfo getUserInfo(TokenResult token) throws IOException {
        UriBuilder builder = UriBuilder.fromUri(token.getOauthBackendUrl()).path(LG_API_V2_USER_INFO);
        String oauthUrl = builder.build().toURL().toString();
        String timestamp = getCurrentTimestamp();
        byte[] oauthSig = RestUtils.getTokenSignature(oauthUrl, LG_API_OAUTH_SECRET_KEY, Collections.emptyMap(),
                timestamp);
        Map<String, String> headers = Map.of("Accept", "application/json", "Authorization",
                String.format("Bearer %s", token.getAccessToken()), "X-Lge-Svccode", LG_API_SVC_CODE,
                "X-Application-Key", LG_API_APPLICATION_KEY, "lgemp-x-app-key", LG_API_CLIENT_ID, "X-Device-Type",
                "M01", "X-Device-Platform", "ADR", "x-lge-oauth-date", timestamp, "x-lge-oauth-signature",
                new String(oauthSig));
        RestResult resp = RestUtils.getCall(httpClient, oauthUrl, headers, null);

        return handleAccountInfoResult(resp);
    }

    private UserInfo handleAccountInfoResult(RestResult resp) throws IOException {
        Map<String, Object> result = MAPPER.readValue(resp.getJsonResponse(), new TypeReference<>() {
        });
        if (resp.getStatusCode() != 200) {
            throw new IllegalStateException(
                    String.format("LG API returned error when trying to get user account information. The reason is:%s",
                            resp.getJsonResponse()));
        } else if (result.get("account") == null
                || ((Map<?, ?>) result.getOrDefault("account", Collections.emptyMap())).get("userNo") == null) {
            throw new IllegalStateException("Error retrieving the account user information from access token");
        }
        @SuppressWarnings("unchecked")
        Map<String, String> accountInfo = (Map<String, String>) result.getOrDefault("account", Collections.emptyMap());

        return new UserInfo(
                Objects.requireNonNullElse(accountInfo.get("userNo"),
                        "Unexpected result. userID must be present in json result"),
                Objects.requireNonNull(accountInfo.get("userID"),
                        "Unexpected result. userID must be present in json result"),
                Objects.requireNonNull(accountInfo.get("userIDType"),
                        "Unexpected result. userIDType must be present in json result"),
                Objects.requireNonNullElse(accountInfo.get("displayUserID"), ""));
    }

    TokenResult doRefreshToken(TokenResult currentToken) throws IOException, RefreshTokenException {
        UriBuilder builder = UriBuilder.fromUri(currentToken.getOauthBackendUrl()).path(LG_API_V2_AUTH_PATH);
        String oauthUrl = builder.build().toURL().toString();
        String timestamp = getCurrentTimestamp();

        Map<String, String> formData = new LinkedHashMap<>();
        formData.put("grant_type", "refresh_token");
        formData.put("refresh_token", currentToken.getRefreshToken());

        byte[] oauthSig = RestUtils.getTokenSignature(oauthUrl, LG_API_OAUTH_SECRET_KEY, formData, timestamp);

        Map<String, String> headers = Map.of("x-lge-appkey", LG_API_CLIENT_ID, "x-lge-oauth-signature",
                new String(oauthSig), "x-lge-oauth-date", timestamp, "Accept", "application/json");

        RestResult resp = RestUtils.postCall(httpClient, oauthUrl, headers, formData);
        return handleRefreshTokenResult(resp, currentToken);
    }

    private TokenResult handleTokenResult(@Nullable RestResult resp) throws IOException {
        Map<String, Object> tokenResult;
        if (resp == null) {
            throw new IllegalStateException("Error getting oauth token. Null data returned");
        }
        if (resp.getStatusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Error getting oauth token. HTTP Status Code is:%s, The reason is:%s",
                            resp.getStatusCode(), resp.getJsonResponse()));
        } else {
            tokenResult = MAPPER.readValue(resp.getJsonResponse(), new TypeReference<>() {
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
            throw new RefreshTokenException("Error getting oauth token. Null data returned");
        }
        if (resp.getStatusCode() != 200) {
            throw new RefreshTokenException(
                    String.format("Error getting oauth token. HTTP Status Code is:%s, The reason is:%s",
                            resp.getStatusCode(), resp.getJsonResponse()));
        } else {
            tokenResult = MAPPER.readValue(resp.getJsonResponse(), new TypeReference<>() {
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

    record PreLoginResult(String username, String signature, String timestamp, String encryptedPwd) {
    }

    record LoginAccountResult(String userIdType, String userId, String country, String loginSessionId) {
    }
}
