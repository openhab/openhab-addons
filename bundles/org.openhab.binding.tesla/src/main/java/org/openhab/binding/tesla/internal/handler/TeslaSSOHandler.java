/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal.handler;

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.Fields.Field;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.tesla.internal.protocol.sso.AuthorizationCodeExchangeRequest;
import org.openhab.binding.tesla.internal.protocol.sso.AuthorizationCodeExchangeResponse;
import org.openhab.binding.tesla.internal.protocol.sso.RefreshTokenRequest;
import org.openhab.binding.tesla.internal.protocol.sso.TokenExchangeRequest;
import org.openhab.binding.tesla.internal.protocol.sso.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link TeslaSSOHandler} is responsible for authenticating with the Tesla SSO service.
 *
 * @author Christian Güdel - Initial contribution
 */
@NonNullByDefault
public class TeslaSSOHandler {

    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(TeslaSSOHandler.class);

    public TeslaSSOHandler(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Nullable
    public TokenResponse getAccessToken(String refreshToken) {
        logger.debug("Exchanging SSO refresh token for API access token");

        // get a new access token for the owner API token endpoint
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
        String refreshTokenPayload = gson.toJson(refreshRequest);

        final org.eclipse.jetty.client.api.Request request = httpClient.newRequest(URI_SSO + "/" + PATH_TOKEN);
        request.content(new StringContentProvider(refreshTokenPayload));
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.method(HttpMethod.POST);

        ContentResponse refreshResponse = executeHttpRequest(request);

        if (refreshResponse != null && refreshResponse.getStatus() == 200) {
            String refreshTokenResponse = refreshResponse.getContentAsString();
            TokenResponse tokenResponse = gson.fromJson(refreshTokenResponse.trim(), TokenResponse.class);

            if (tokenResponse != null && tokenResponse.access_token != null && !tokenResponse.access_token.isEmpty()) {
                TokenExchangeRequest token = new TokenExchangeRequest();
                String tokenPayload = gson.toJson(token);

                final org.eclipse.jetty.client.api.Request logonRequest = httpClient
                        .newRequest(URI_OWNERS + "/" + PATH_ACCESS_TOKEN);
                logonRequest.content(new StringContentProvider(tokenPayload));
                logonRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
                logonRequest.header(HttpHeader.AUTHORIZATION, "Bearer " + tokenResponse.access_token);
                logonRequest.method(HttpMethod.POST);

                ContentResponse logonTokenResponse = executeHttpRequest(logonRequest);

                if (logonTokenResponse != null && logonTokenResponse.getStatus() == 200) {
                    String tokenResponsePayload = logonTokenResponse.getContentAsString();
                    TokenResponse tr = gson.fromJson(tokenResponsePayload.trim(), TokenResponse.class);

                    if (tr != null && tr.token_type != null && !tr.access_token.isEmpty()) {
                        return tr;
                    }
                } else {
                    logger.debug("An error occurred while exchanging SSO access token for API access token: {}",
                            (logonTokenResponse != null ? logonTokenResponse.getStatus() : "no response"));
                }
            }
        } else {
            logger.debug("An error occurred during refresh of SSO token: {}",
                    (refreshResponse != null ? refreshResponse.getStatus() : "no response"));
        }

        return null;
    }

    /**
     * Authenticates using username/password against Tesla SSO endpoints.
     *
     * @param username Username
     * @param password Password
     * @return Refresh token for use with {@link getAccessToken}
     */
    @Nullable
    public String authenticate(String username, String password) {
        String codeVerifier = generateRandomString(86);
        String codeChallenge = null;
        String state = generateRandomString(10);

        try {
            codeChallenge = getCodeChallenge(codeVerifier);
        } catch (NoSuchAlgorithmException e) {
            logger.error("An exception occurred while building login page request: '{}'", e.getMessage());
            return null;
        }

        final org.eclipse.jetty.client.api.Request loginPageRequest = httpClient
                .newRequest(URI_SSO + "/" + PATH_AUTHORIZE);
        loginPageRequest.method(HttpMethod.GET);
        loginPageRequest.followRedirects(false);

        addQueryParameters(loginPageRequest, codeChallenge, state);

        ContentResponse loginPageResponse = executeHttpRequest(loginPageRequest);
        if (loginPageResponse == null
                || (loginPageResponse.getStatus() != 200 && loginPageResponse.getStatus() != 302)) {
            logger.debug("Failed to obtain SSO login page, response status code: {}",
                    (loginPageResponse != null ? loginPageResponse.getStatus() : "no response"));
            return null;
        }

        logger.debug("Obtained SSO login page");

        String authorizationCode = null;

        if (loginPageResponse.getStatus() == 302) {
            String redirectLocation = loginPageResponse.getHeaders().get(HttpHeader.LOCATION);
            if (isValidRedirectLocation(redirectLocation)) {
                authorizationCode = extractAuthorizationCodeFromUri(redirectLocation);
            } else {
                logger.debug("Unexpected redirect location received when fetching login page: {}", redirectLocation);
                return null;
            }
        } else {
            Fields postData = new Fields();

            try {
                Document doc = Jsoup.parse(loginPageResponse.getContentAsString());
                logger.trace("{}", doc.toString());
                Element loginForm = doc.getElementsByTag("form").first();

                Iterator<Element> elIt = loginForm.getElementsByTag("input").iterator();
                while (elIt.hasNext()) {
                    Element input = elIt.next();
                    if (input.attr("type").equalsIgnoreCase("hidden")) {
                        postData.add(input.attr("name"), input.attr("value"));
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to parse login page: {}", e.getMessage());
                logger.debug("login page response {}", loginPageResponse.getContentAsString());
                return null;
            }

            postData.add("identity", username);
            postData.add("credential", password);

            final org.eclipse.jetty.client.api.Request formSubmitRequest = httpClient
                    .newRequest(URI_SSO + "/" + PATH_AUTHORIZE);
            formSubmitRequest.method(HttpMethod.POST);
            formSubmitRequest.content(new FormContentProvider(postData));
            formSubmitRequest.followRedirects(false); // this should return a 302 ideally, but that location doesn't
                                                      // exist
            addQueryParameters(formSubmitRequest, codeChallenge, state);

            ContentResponse formSubmitResponse = executeHttpRequest(formSubmitRequest);
            if (formSubmitResponse == null || formSubmitResponse.getStatus() != 302) {
                logger.debug("Failed to obtain code from SSO login page when submitting form, response status code: {}",
                        (formSubmitResponse != null ? formSubmitResponse.getStatus() : "no response"));
                return null;
            }

            String redirectLocation = formSubmitResponse.getHeaders().get(HttpHeader.LOCATION);
            if (!isValidRedirectLocation(redirectLocation)) {
                logger.debug("Redirect location not set or doesn't match expected callback URI {}: {}", URI_CALLBACK,
                        redirectLocation);
                return null;
            }

            logger.debug("Obtained valid redirect location");
            authorizationCode = extractAuthorizationCodeFromUri(redirectLocation);
        }

        if (authorizationCode == null) {
            logger.debug("Did not receive an authorization code");
            return null;
        }

        // exchange authorization code for SSO access + refresh token
        AuthorizationCodeExchangeRequest request = new AuthorizationCodeExchangeRequest(authorizationCode,
                codeVerifier);
        String payload = gson.toJson(request);

        final org.eclipse.jetty.client.api.Request tokenExchangeRequest = httpClient
                .newRequest(URI_SSO + "/" + PATH_TOKEN);
        tokenExchangeRequest.content(new StringContentProvider(payload));
        tokenExchangeRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        tokenExchangeRequest.method(HttpMethod.POST);

        ContentResponse response = executeHttpRequest(tokenExchangeRequest);
        if (response != null && response.getStatus() == 200) {
            String responsePayload = response.getContentAsString();
            AuthorizationCodeExchangeResponse ssoTokenResponse = gson.fromJson(responsePayload.trim(),
                    AuthorizationCodeExchangeResponse.class);
            if (ssoTokenResponse != null && ssoTokenResponse.token_type != null
                    && !ssoTokenResponse.access_token.isEmpty()) {
                logger.debug("Obtained valid SSO refresh token");
                return ssoTokenResponse.refresh_token;
            }
        } else {
            logger.debug("An error occurred while exchanging authorization code for SSO refresh token: {}",
                    (response != null ? response.getStatus() : "no response"));
        }

        return null;
    }

    private Boolean isValidRedirectLocation(@Nullable String redirectLocation) {
        return redirectLocation != null && redirectLocation.startsWith(URI_CALLBACK);
    }

    @Nullable
    private String extractAuthorizationCodeFromUri(String uri) {
        Field code = httpClient.newRequest(uri).getParams().get("code");
        return code != null ? code.getValue() : null;
    }

    private String getCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes());

        StringBuilder hashStr = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            hashStr.append(String.format("%02x", b));
        }

        return Base64.getUrlEncoder().encodeToString(hashStr.toString().getBytes());
    }

    private String generateRandomString(int length) {
        Random random = new Random();

        String generatedString = random.ints('a', 'z' + 1).limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();

        return generatedString;
    }

    private void addQueryParameters(org.eclipse.jetty.client.api.Request request, String codeChallenge, String state) {
        request.param("client_id", CLIENT_ID);
        request.param("code_challenge", codeChallenge);
        request.param("code_challenge_method", "S256");
        request.param("redirect_uri", URI_CALLBACK);
        request.param("response_type", "code");
        request.param("scope", SSO_SCOPES);
        request.param("state", state);
    }

    @Nullable
    private ContentResponse executeHttpRequest(org.eclipse.jetty.client.api.Request request) {
        request.timeout(10, TimeUnit.SECONDS);

        ContentResponse response;
        try {
            response = request.send();
            return response;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("An exception occurred while invoking a HTTP request: '{}'", e.getMessage());
            return null;
        }
    }
}
