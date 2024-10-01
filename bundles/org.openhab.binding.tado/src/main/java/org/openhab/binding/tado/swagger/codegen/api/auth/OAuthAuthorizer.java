/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tado.swagger.codegen.api.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.tado.swagger.codegen.api.ApiException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class OAuthAuthorizer implements Authorizer {
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final int TOKEN_GRACE_PERIOD = 30;
    private static final HttpClient CLIENT = new HttpClient(new SslContextFactory());

    private final Gson gson = new GsonBuilder().create();

    private String tokenUrl = "https://auth.tado.com/oauth/token";

    private String grantType;
    private String username;
    private String password;

    private String clientId;
    private String clientSecret;
    private String scope;

    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiration;

    public OAuthAuthorizer() {
    }

    public OAuthAuthorizer passwordFlow(String username, String password) {
        this.grantType = "password";
        this.username = username;
        this.password = password;
        return this;
    }

    public OAuthAuthorizer tokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
        return this;
    }

    public OAuthAuthorizer clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public OAuthAuthorizer clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public OAuthAuthorizer scopes(String... scopes) {
        this.scope = String.join(" ", scopes);
        return this;
    }

    private void initializeTokens() throws IOException {
        startHttpClient(CLIENT);

        List<String> queryParams = new ArrayList<>();

        if (this.refreshToken != null) {
            queryParams.add(queryParam("grant_type", "refresh_token"));
            queryParams.add(queryParam("refresh_token", this.refreshToken));
        } else if (GRANT_TYPE_PASSWORD.equals(this.grantType)) {
            queryParams.add(queryParam("grant_type", this.grantType));
            queryParams.add(queryParam("username", this.username));
            queryParams.add(queryParam("password", this.password));
            queryParams.add(queryParam("scope", this.scope));
        }

        if (this.clientId != null) {
            queryParams.add(queryParam("client_id", this.clientId));
        }
        if (this.clientSecret != null) {
            queryParams.add(queryParam("client_secret", this.clientSecret));
        }

        Request request = CLIENT.newRequest(this.tokenUrl + "?" + String.join("&", queryParams)).method(HttpMethod.POST)
                .timeout(5, TimeUnit.SECONDS);
        request.header(HttpHeader.USER_AGENT, "openhab/swagger-java/1.0.0");

        try {
            ContentResponse response = request.send();

            if (response.getStatus() == HttpStatus.OK_200) {
                Map<?, ?> tokenValues = gson.fromJson(response.getContentAsString(), Map.class);
                this.accessToken = (String) tokenValues.get("access_token");
                this.refreshToken = (String) tokenValues.get("refresh_token");
                this.tokenExpiration = LocalDateTime.now().plusSeconds(
                        Double.valueOf(tokenValues.get("expires_in").toString()).longValue() - TOKEN_GRACE_PERIOD);
            } else {
                this.accessToken = null;
                this.refreshToken = null;
                this.tokenExpiration = null;

                throw new ApiException(response, "Error getting access token");
            }
        } catch (Exception e) {
            throw new IOException("Error calling " + this.tokenUrl, e);
        }
    }

    private String queryParam(String key, String value) {
        try {
            return key + "=" + URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return key + "=" + value;
        }
    }

    private boolean isExpired() {
        return this.tokenExpiration == null || this.tokenExpiration.isBefore(LocalDateTime.now());
    }

    public String getToken() throws IOException {
        if (accessToken == null || this.isExpired()) {
            synchronized (this) {
                if (accessToken == null || this.isExpired()) {
                    initializeTokens();
                }
            }
        }

        return this.accessToken;
    }

    @Override
    public void addAuthorization(Request request) throws IOException {
        request.header(HttpHeader.AUTHORIZATION, "Bearer " + getToken());
    }

    private static void startHttpClient(HttpClient client) {
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                // nothing we can do here
            }
        }
    }
}
