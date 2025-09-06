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
package org.openhab.binding.mercedesme.internal.api;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.dto.TokenResponse;
import org.openhab.binding.mercedesme.internal.exception.MercedesMeApiException;
import org.openhab.binding.mercedesme.internal.exception.MercedesMeAuthException;
import org.openhab.binding.mercedesme.internal.exception.MercedesMeBindingException;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.storage.Storage;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * {@link Authorization} for handling the authentication process
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Authorization {
    private static final int EXPIRATION_BUFFER = 5;
    private final Logger logger = LoggerFactory.getLogger(Authorization.class);

    private AccessTokenRefreshListener listener;
    private AccessTokenResponse token = Utils.INVALID_TOKEN;
    private String identifier;

    protected static final String CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded";
    protected static final String CONTENT_TYPE_JSON = "application/json";

    protected LocaleProvider localeProvider;
    protected AccountConfiguration config;
    protected Storage<String> storage;
    protected HttpClient httpClient;
    protected String baseUrl;

    public Authorization(AccessTokenRefreshListener atrl, HttpClient hc, AccountConfiguration ac, LocaleProvider l,
            Storage<String> store) {
        listener = atrl;
        httpClient = hc;
        config = ac;
        identifier = config.email;
        localeProvider = l;
        storage = store;

        baseUrl = Utils.getLoginServer(config.region);
        // restore token from persistence if available
        String storedToken = storage.get(identifier);
        if (storedToken != null) {
            try {
                TokenResponse tokenResponseJson = Utils.GSON.fromJson(storedToken, TokenResponse.class);
                token = decodeToken(tokenResponseJson);
            } catch (JsonSyntaxException e) {
                handleInvalidToken();
                logger.warn("Stored token {} for {} not parsable: {}", storedToken, config.email, e.getMessage());
            }
            if (!authTokenIsValid()) {
                handleInvalidToken();
                logger.trace("Invalid token for {}", config.email);
            } else {
                atrl.onAccessTokenResponse(token);
            }
        } else {
            logger.trace("No token for {} stored, stay on invalid token", config.email);
        }
    }

    protected synchronized String getToken() {
        if (token.isExpired(Instant.now(), EXPIRATION_BUFFER)) {
            if (authTokenIsValid()) {
                refreshToken();
            }
        }
        return token.getAccessToken();
    }

    private void refreshToken() {
        logger.trace("RefreshToken");
        try {
            String url = Utils.getTokenUrl(config.region);
            Request req = httpClient.POST(url);
            req.header("X-Device-Id", UUID.randomUUID().toString());
            req.header("X-Request-Id", UUID.randomUUID().toString());

            String grantAttribute = "grant_type=refresh_token";
            String refreshTokenAttribute = "refresh_token="
                    + URLEncoder.encode(token.getRefreshToken(), StandardCharsets.UTF_8.toString());
            String content = grantAttribute + "&" + refreshTokenAttribute;
            req.content(new StringContentProvider(content), CONTENT_TYPE_URL_ENCODED);

            ContentResponse cr = req.timeout(Constants.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
            int tokenResponseStatus = cr.getStatus();
            String tokenResponse = cr.getContentAsString();
            if (tokenResponseStatus == HttpStatus.OK_200) {
                storeToken(tokenResponse);
            } else {
                handleInvalidToken();
                logger.warn("Failed to refresh token {} {}", tokenResponseStatus, tokenResponse);
            }
            listener.onAccessTokenResponse(token);
        } catch (InterruptedException | TimeoutException | ExecutionException | UnsupportedEncodingException
                | JsonSyntaxException e) {
            logger.info("Failed to refresh token {}", e.getMessage());
        }
    }

    private void storeToken(String tokenResponse) {
        try {
            TokenResponse tokenResponseJson = Utils.GSON.fromJson(tokenResponse, TokenResponse.class);
            if (tokenResponseJson == null) {
                handleInvalidToken();
                logger.warn("Token response is null");
                return;
            }
            // response doesn't contain creation date time so set it manually
            tokenResponseJson.createdOn = Instant.now().toString();
            // A refresh token is delivered optional. If not set in response take old one
            if (Constants.NOT_SET.equals(tokenResponseJson.refreshToken)) {
                tokenResponseJson.refreshToken = token.getRefreshToken();
            }
            token = decodeToken(tokenResponseJson);
            if (authTokenIsValid()) {
                String tokenStore = Utils.GSON.toJson(tokenResponseJson);
                logger.debug("Token result {}", token.toString());
                storage.put(identifier, tokenStore);
            } else {
                handleInvalidToken();
                logger.warn("Refresh token delivered invalid result {}", tokenResponse);
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Token response {} not parsable: {}", tokenResponse, e.getMessage());
            handleInvalidToken();
            return;
        }
    }

    private AccessTokenResponse decodeToken(@Nullable TokenResponse tokenJson) {
        if (tokenJson != null) {
            AccessTokenResponse atr = new AccessTokenResponse();
            atr.setCreatedOn(Instant.parse(tokenJson.createdOn));
            atr.setExpiresIn(tokenJson.expiresIn);
            atr.setAccessToken(tokenJson.accessToken);
            if (!Constants.NOT_SET.equals(tokenJson.refreshToken)) {
                atr.setRefreshToken(tokenJson.refreshToken);
            } else {
                // Preserve refresh token if available
                if (!Constants.NOT_SET.equals(token.getRefreshToken())) {
                    atr.setRefreshToken(token.getRefreshToken());
                } else {
                    logger.debug("Neither new nor old refresh token available");
                    return Utils.INVALID_TOKEN;
                }
            }
            atr.setTokenType("Bearer");
            atr.setScope(Constants.AUTH_SCOPE);
            return atr;
        } else {
            logger.debug("Token Response is null");
        }
        return Utils.INVALID_TOKEN;
    }

    public boolean authTokenIsValid() {
        return !Constants.NOT_SET.equals(token.getAccessToken()) && !Constants.NOT_SET.equals(token.getRefreshToken());
    }

    /**
     * Login to MercedesMe using the resume authentication process. After successful login, the access token is updated.
     *
     * @throws MercedesMeAuthException if response status isn't correct or needed parameters not delivered
     * @throws MercedesMeApiException if an error occurs during API calls
     */
    public void login() throws MercedesMeAuthException, MercedesMeApiException, MercedesMeBindingException {
        logger.trace("Start login");
        HttpClient loginHttpClient = new HttpClient(new SslContextFactory.Client());
        /**
         * I need to start an extra client
         * Using common HttpClient causes problems with
         * 2025-07-15 21:48:39.212 [INFO ] [rcedesme.internal.server.Authorization] - [a@b.c] Start resume
         * login
         * 2025-07-15 21:48:39.212 [TRACE] [rcedesme.internal.server.Authorization] - Step 1: Resume headers
         * Accept-Encoding: gzip
         * User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 15_8_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko)
         * Version/15.6.6 Mobile/15E148 Safari/604.1
         * Accept-Language: de-DE,de;q=0.9
         * Accept: text/html,application/xhtml+xml,application/xml;q=0.9,;q=0.8
         *
         *
         * 2025-07-15 21:48:39.577 [WARN ] [rcedesme.internal.server.Authorization] - Failed request
         * /as/authorization.oauth2client_id=62778dc4-1de3-44f4-af95-115f06a3a008&code_challenge_method=S256&redirect_uri=rismycar%3A%2F%2Flogin-callback&response_type=code&scope=email+profile+ciam-uid+phone+openid+offline_access&code_challenge=q-xTU0kQx3fLkhAO89qo_4shExS7wa6XVoG1DXAYoZ4
         * - org.eclipse.jetty.http.BadMessageException: 500: Request header too large
         **/
        try {
            loginHttpClient.start();
            loginHttpClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
            String codeVerifier = generateCodeVerifier(32);
            String codeChallenge = generateCodeChallenge(codeVerifier);
            String resumeUrl = getResumeUrl(loginHttpClient, codeChallenge);
            sendUserAgent(loginHttpClient);
            sendUsername(loginHttpClient);
            String preLoginToken = performPasswordLogin(loginHttpClient);
            String authCode = resumeAuthentication(loginHttpClient, resumeUrl, preLoginToken);
            requestAccessToken(loginHttpClient, authCode, codeVerifier);
        } catch (MercedesMeAuthException | MercedesMeApiException e) {
            throw e;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            String message = e.getMessage();
            throw new MercedesMeBindingException(message == null ? "unknown" : message);
        } catch (Exception e) {
            String message = e.getMessage();
            throw new MercedesMeApiException(message == null ? "unknown" : message);
        } finally {
            try {
                loginHttpClient.stop();
            } catch (Exception e) {
                logger.warn("Failed to stop HttpClient {}", e.getMessage());
            }
        }
    }

    /**
     * Get URL for resume authentication.
     *
     * @throws MercedesMeAuthException if response status isn't correct or URL parameter not delivered
     * @throws MercedesMeApiException if an error occurs during API call
     */
    private String getResumeUrl(HttpClient loginHttpClient, String codeChallenge)
            throws MercedesMeAuthException, MercedesMeApiException, UnsupportedEncodingException {
        Fields resumeContent = new Fields();
        resumeContent.put("client_id", Constants.AUTH_CLIENT_ID);
        resumeContent.put("code_challenge_method", "S256");
        resumeContent.put("redirect_uri", Constants.AUTH_REDIRECT_URI);
        resumeContent.put("response_type", "code");
        resumeContent.put("scope", Constants.AUTH_SCOPE);
        resumeContent.put("code_challenge", codeChallenge);

        String resumeUrl = null;
        Request resumeRequest = loginHttpClient
                .newRequest(baseUrl + "/as/authorization.oauth2?" + FormContentProvider.convert(resumeContent))
                .followRedirects(true);
        resumeRequest.agent(Constants.AUTH_USER_AGENT);
        resumeRequest.header(HttpHeader.ACCEPT_LANGUAGE, Constants.AUTH_LANGUAGE);
        resumeRequest.header(HttpHeader.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        ContentResponse resumeResponse = send(resumeRequest);
        logger.trace("Step 1: Get resume code {} - {}", resumeResponse.getStatus(),
                resumeResponse.getRequest().getURI());
        if (resumeResponse.getStatus() == HttpStatus.OK_200) {
            String response = resumeResponse.getRequest().getURI().getQuery();
            Map<String, String> params = Utils.getQueryParams(response);
            resumeUrl = params.get("resume");
        }
        if (resumeUrl != null) {
            return resumeUrl;
        } else {
            throw new MercedesMeAuthException("Failed to get resume URL. HTTP " + resumeResponse.getStatus());
        }
    }

    /**
     * Send user agent.
     *
     * @throws MercedesMeAuthException if response status isn't correct
     * @throws MercedesMeApiException if an error occurs during API call
     */
    private void sendUserAgent(HttpClient loginHttpClient) throws MercedesMeAuthException, MercedesMeApiException {
        Request agentRequest = loginHttpClient.POST(baseUrl + "/ciam/auth/ua");
        agentRequest.agent(Constants.AUTH_USER_AGENT);
        agentRequest.header(HttpHeader.ACCEPT_LANGUAGE, Constants.AUTH_LANGUAGE);
        agentRequest.header(HttpHeader.ACCEPT, "*/*");
        agentRequest.header(HttpHeader.ORIGIN, baseUrl);

        JSONObject agentContent = new JSONObject();
        agentContent.put("browserName", "Mobile Safari");
        agentContent.put("browserVersion", "15.6.6");
        agentContent.put("osName", "iOS");
        agentRequest.content(new StringContentProvider(agentContent.toString(), "utf-8"), CONTENT_TYPE_JSON);

        ContentResponse agentResponse = send(agentRequest);
        logger.trace("Step 2: Post Agent {} - {}", agentResponse.getStatus(), agentResponse.getContentAsString());
        if (agentResponse.getStatus() != HttpStatus.OK_200) {
            throw new MercedesMeAuthException("Failed to post user agent. HTTP " + agentResponse.getStatus());
        }
    }

    /**
     * Send user name.
     *
     * @throws MercedesMeAuthException if response status isn't correct
     * @throws MercedesMeApiException if an error occurs during API call
     */
    private void sendUsername(HttpClient loginHttpClient) throws MercedesMeAuthException, MercedesMeApiException {
        Request userRequest = loginHttpClient.POST(baseUrl + "/ciam/auth/login/user");
        userRequest.agent(Constants.AUTH_USER_AGENT);
        userRequest.header(HttpHeader.ACCEPT_LANGUAGE, Constants.AUTH_LANGUAGE);
        userRequest.header(HttpHeader.ACCEPT, CONTENT_TYPE_JSON + ", text/plain, */*");
        userRequest.header(HttpHeader.ORIGIN, baseUrl);
        userRequest.header(HttpHeader.REFERER, baseUrl + "/ciam/auth/login");

        JSONObject userContent = new JSONObject();
        userContent.put("username", config.email);
        userRequest.content(new StringContentProvider(userContent.toString(), "utf-8"), CONTENT_TYPE_JSON);

        ContentResponse userResponse = send(userRequest);
        int status = userResponse.getStatus();
        logger.trace("Step 3: Post username {} - {}", status, userResponse.getContentAsString());
        if (status != HttpStatus.OK_200) {
            throw new MercedesMeAuthException("Failed to post username " + config.email + ". HTTP " + status);
        }
    }

    /**
     * Perform login with user name and password to get pre-login token.
     *
     * @throws MercedesMeAuthException if response status isn't correct or token not delivered
     * @throws MercedesMeApiException if an error occurs during API call
     */
    private String performPasswordLogin(HttpClient loginHttpClient)
            throws MercedesMeAuthException, MercedesMeApiException {
        Request loginRequest = loginHttpClient.POST(baseUrl + "/ciam/auth/login/pass");
        loginRequest.agent(Constants.AUTH_USER_AGENT);
        loginRequest.header(HttpHeader.ACCEPT_LANGUAGE, Constants.AUTH_LANGUAGE);
        loginRequest.header(HttpHeader.ACCEPT, CONTENT_TYPE_JSON + ", text/plain, */*");
        loginRequest.header(HttpHeader.ORIGIN, baseUrl);
        loginRequest.header(HttpHeader.REFERER, baseUrl + "/ciam/auth/login");

        String rid = generateCodeVerifier(24);
        JSONObject loginContent = new JSONObject();
        loginContent.put("username", config.email);
        loginContent.put("password", config.password);
        loginContent.put("rememberMe", false);
        loginContent.put("rid", rid);
        loginRequest.content(new StringContentProvider(loginContent.toString(), "utf-8"), CONTENT_TYPE_JSON);

        String preLoginToken = null;
        ContentResponse loginResponse = send(loginRequest);
        int status = loginResponse.getStatus();
        String loginResponseString = loginResponse.getContentAsString();
        logger.trace("Step 4: Login {} - {}", status, loginResponseString);
        if (status == HttpStatus.OK_200) {
            JSONObject loginResponseJSON = new JSONObject(loginResponseString);
            preLoginToken = loginResponseJSON.optString("token", null);
        }
        if (preLoginToken != null) {
            return preLoginToken;
        } else {
            throw new MercedesMeAuthException("Failed to login. HTTP " + status + " " + loginResponseString);
        }
    }

    /**
     * Perform login with token to receive authorization code.
     *
     * @throws MercedesMeAuthException if response status isn't correct or code not delivered
     * @throws MercedesMeApiException if an error occurs during API call
     */
    private String resumeAuthentication(HttpClient loginHttpClient, String resumeUrl, String preLoginToken)
            throws MercedesMeAuthException, MercedesMeApiException, UnsupportedEncodingException {
        String code = null;
        MultiMap<@Nullable String> authParams = new MultiMap<>();
        authParams.add("token", preLoginToken);

        Request authRequest = loginHttpClient.POST(baseUrl + resumeUrl).followRedirects(false);
        authRequest.agent(Constants.AUTH_USER_AGENT);
        authRequest.header(HttpHeader.ACCEPT_LANGUAGE, Constants.AUTH_LANGUAGE);
        authRequest.header(HttpHeader.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        authRequest.header(HttpHeader.ORIGIN, baseUrl);
        authRequest.header(HttpHeader.REFERER, baseUrl + "/ciam/auth/login");
        authRequest.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED,
                UrlEncoded.encode(authParams, StandardCharsets.UTF_8, false), StandardCharsets.UTF_8));

        ContentResponse authResponse = send(authRequest);
        int status = authResponse.getStatus();
        logger.trace("Step 5: Resume auth {} - {}", status, authResponse.getContentAsString());
        if (HttpStatus.isRedirection(status)) {
            String location = authResponse.getHeaders().get(HttpHeader.LOCATION);
            Map<String, String> params = Utils.getQueryParams(URI.create(location).getQuery());
            code = params.get("code");
        }
        if (code != null) {
            return code;
        } else {
            throw new MercedesMeAuthException(
                    "Failed to resume auth HTTP " + status + "  " + authResponse.getContentAsString());
        }
    }

    /**
     * Exchange authorization code for access and refresh tokens
     *
     * @throws MercedesMeAuthException if response status isn't correct
     * @throws MercedesMeApiException if an error occurs during API call
     */
    private void requestAccessToken(HttpClient loginHttpClient, String authCode, String codeVerifier)
            throws MercedesMeAuthException, MercedesMeApiException {
        Fields tokenParams = new Fields();
        tokenParams.put("client_id", Constants.AUTH_CLIENT_ID);
        tokenParams.put("code", authCode);
        tokenParams.put("code_verifier", codeVerifier);
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("redirect_uri", Constants.AUTH_REDIRECT_URI);

        Request tokenRequest = loginHttpClient.POST(baseUrl + "/as/token.oauth2");
        addBasicHeaders(tokenRequest);
        tokenRequest.content(new FormContentProvider(tokenParams));

        ContentResponse tokenResponse = send(tokenRequest);
        int status = tokenResponse.getStatus();
        String tokenResponseString = tokenResponse.getContentAsString();
        if (status == HttpStatus.OK_200) {
            storeToken(tokenResponseString);
            logger.info("Successfully resumed login");
        } else {
            handleInvalidToken();
            logger.info("Failed resume login {} {}", status, tokenResponse.getContentAsString());
        }
    }

    private void handleInvalidToken() {
        token = Utils.INVALID_TOKEN;
        storage.remove(identifier);
    }

    private String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private String generateCodeVerifier(int size) {
        String verifierBytes = StringUtils.getRandomAlphanumeric(size);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes.getBytes());
    }

    public void addBasicHeaders(Request req) {
        req.agent(Utils.getApplication(config.region));
        req.header("Ris-Os-Name", Constants.RIS_OS_NAME);
        req.header("Ris-Os-Version", Constants.RIS_OS_VERSION);
        req.header("Ris-Sdk-Version", Utils.getRisSDKVersion(config.region));
        req.header("X-Locale",
                localeProvider.getLocale().getLanguage() + "-" + localeProvider.getLocale().getCountry()); // de-DE
        req.header("X-Applicationname", Utils.getUserAgent(config.region));
        req.header("Ris-Application-Version", Utils.getRisApplicationVersion(config.region));
    }

    protected void addAuthHeaders(Request request) {
        request.header("X-SessionId", UUID.randomUUID().toString());
        request.header("X-TrackingId", UUID.randomUUID().toString());
        request.header("Authorization", getToken());
    }

    /**
     * Send the request and return the response.
     *
     * @param request HTTP request with all necessary headers and content
     * @return {@link ContentResponse} with the response from the server
     * @throws MercedesMeApiException if an error occurs during the request
     */
    protected ContentResponse send(Request request) throws MercedesMeApiException {
        try {
            return request.timeout(Constants.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed request {}{} - {}", request.getPath(), request.getQuery(), e.getMessage());
            throw new MercedesMeApiException(request.getPath() + request.getQuery() + " - " + e.getMessage());
        }
    }
}
