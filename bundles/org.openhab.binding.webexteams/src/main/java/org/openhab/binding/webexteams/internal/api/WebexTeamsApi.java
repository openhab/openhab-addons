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
package org.openhab.binding.webexteams.internal.api;

import static org.openhab.binding.webexteams.internal.WebexTeamsBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.webexteams.internal.WebexTeamsConfiguration;
import org.openhab.binding.webexteams.internal.WebexTeamsHandler;
import org.openhab.core.config.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * WebexTeamsApi implements API integration with Webex Teams.
 * 
 * Not using webex-java-sdk since it's not in a public maven repo, and it doesn't easily
 * support caching refresh tokens between openhab restarts, etc..
 * 
 * @author Tom Deckers - Initial contribution
 * 
 */
@NonNullByDefault
public class WebexTeamsApi {

    private final Logger logger = LoggerFactory.getLogger(WebexTeamsApi.class);

    private final WebexTeamsHandler handler;
    private final HttpClient httpClient;

    // OAuth members
    private @Nullable String authToken;
    private @Nullable String clientId;
    private @Nullable String clientSecret;
    private @Nullable String authCode;
    private @Nullable String redirectUrl;
    private @Nullable String refreshToken;

    /**
     * Constructor.
     * 
     * @param WebexTeamsHandler hander interact with handler context.
     */
    public WebexTeamsApi(WebexTeamsHandler handler, HttpClient httpClient) {
        this.handler = handler;
        this.httpClient = httpClient;

        WebexTeamsConfiguration config = handler.getConfigAs(WebexTeamsConfiguration.class);
        this.authToken = config.token;
        this.clientId = config.clientId;
        this.clientSecret = config.clientSecret;
        this.authCode = config.authCode;
        this.redirectUrl = config.redirectUrl;
        this.refreshToken = config.refreshToken;
    }

    /**
     * authenticate against the webex service.
     * 
     * @return <code>boolean</code> representing success of failure.
     */
    public boolean authenticate() {
        logger.debug("Authenticating...");
        if (this.clientId != null && !this.clientId.isEmpty() && this.clientSecret != null
                && !this.clientSecret.isEmpty()) {

            logger.debug("Client id and secret configured");
            if (this.authCode != null && !this.authCode.isEmpty() && this.redirectUrl != null
                    && !this.redirectUrl.isEmpty()) {
                logger.debug("Auth code and redirect url configured");
                URI url = this.getUrl(OAUTH_TOKEN_URL);
                AccessTokenRequest request = new AccessTokenRequest();
                request.setGrantType("authorization_code");
                request.setClientId(this.clientId);
                request.setClientSecret(this.clientSecret);
                request.setCode(this.authCode);
                request.setRedirectUri(getUrl(this.redirectUrl));

                AccessTokenResponse response = doRequest(url, HttpMethod.POST, AccessTokenResponse.class, request);
                // TODO: remove this line:
                logger.debug("Access: {}, refresh: {}", response.getAccessToken(), response.getRefreshToken());

                logger.debug("Updating tokens");
                Configuration localConfig = handler.editConfiguration();
                // add access token
                this.authToken = response.getAccessToken();
                localConfig.put(WebexTeamsConfiguration.TOKEN, this.authToken);
                // remove authCode - single use
                this.authCode = "";
                localConfig.put(WebexTeamsConfiguration.AUTH_CODE, "");

                // set refresh token
                this.refreshToken = response.getRefreshToken();
                localConfig.put(WebexTeamsConfiguration.REFRESH_TOKEN, this.refreshToken);

                handler.updateConfiguration(localConfig);

                return true;

            } else if (this.refreshToken != null && this.refreshToken.isEmpty()) {
                logger.debug("Refreshing token.");
                URI url = this.getUrl(OAUTH_TOKEN_URL);
                AccessTokenRequest request = new AccessTokenRequest();
                request.setGrantType("authorization_code");
                request.setClientId(this.clientId);
                request.setClientSecret(this.clientSecret);
                request.setRefreshToken(this.refreshToken);
                AccessTokenResponse response = doRequest(url, HttpMethod.POST, AccessTokenResponse.class, request);

                logger.debug("Updating auth token after refresh");
                Configuration localConfig = handler.editConfiguration();
                // add access token
                this.authToken = response.getAccessToken();
                localConfig.put(WebexTeamsConfiguration.TOKEN, this.authToken);
                handler.updateConfiguration(localConfig);

                return true;
            }
        }
        return false;
    }

    public Person getPerson() {
        URI url = getUrl(WEBEX_API_ENDPOINT + "/people/me");

        Person person = request(url, HttpMethod.GET, Person.class, null);
        return person;
    }

    private URI getUrl(@Nullable String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new WebexTeamsApiException("bad url", e);
        }
        return uri;
    }

    private <I, O> O request(URI url, HttpMethod method, Class<O> clazz, I body) {
        // If we don't have an auth token, get it first.
        if (this.authToken == null || this.authToken.isEmpty()) {
            if (!authenticate()) {
                logger.warn("Failed to authenticate");
                throw new NotAuthenticatedException();
            }
        }

        try {
            // try a request
            return doRequest(url, method, clazz, body);
        } catch (NotAuthenticatedException e) {
            // possible we need to refresh the token
            logger.debug("Authenticating and retrying...");
            if (authenticate()) {
                return doRequest(url, method, clazz, body);
            } else {
                // something else is going on
                logger.error("Failed to request", e);
                throw e;
            }
        }
    }

    private <I, O> O doRequest(URI url, HttpMethod method, Class<O> clazz, I body) {
        Gson gson = new Gson();
        try {
            Request req = httpClient.newRequest(url).method(method);
            req.header("Authorization", "Bearer " + this.authToken);
            logger.debug("Requesting {} with ({}, {})", url, clazz, body);

            if (body != null) {
                String bodyString = gson.toJson(body, body.getClass());
                req.content(new StringContentProvider(bodyString));
                req.header("Content-type", "application/json");
            }

            InputStreamResponseListener listener = new InputStreamResponseListener();
            req.send(listener);

            // Wait for the response headers to arrive
            Response response = listener.get(5, TimeUnit.SECONDS);
            logger.debug("Response: {} - {}", response.getStatus(), response.getReason());

            if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                throw new NotAuthenticatedException();
            } else if (response.getStatus() == HttpStatus.OK_200) {
                // Obtain the input stream on the response content
                try (InputStream input = listener.getInputStream()) {
                    Reader reader = new InputStreamReader(input);
                    O entity = gson.fromJson(reader, clazz);
                    return entity;
                } catch (IOException e) {
                    throw new WebexTeamsApiException("ioexception", e);
                }
            } else {
                logger.warn("Unexpected response {} - {}", response.getStatus(), response.getReason());
                try (InputStream input = listener.getInputStream()) {
                    String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                            .collect(Collectors.joining("\n"));
                    logger.warn("Content: {}", text);

                } catch (IOException e) {
                    throw new WebexTeamsApiException("ioexception", e);
                }

                throw new WebexTeamsApiException(
                        String.format("Unexpected response {} - {}", response.getStatus(), response.getReason()));
            }
        } catch (TimeoutException e) {
            logger.error("Request timeout", e);
            throw new WebexTeamsApiException("Request timeout", e);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Request error", e);
            throw new WebexTeamsApiException("Request error", e);
        }
    }

    // sendMessage
    public Message sendMessage(Message msg) {
        URI url = getUrl(WEBEX_API_ENDPOINT + "/messages");
        Message response = request(url, HttpMethod.POST, Message.class, msg);
        logger.debug("Sent message, id: {}", response.getId());
        return response;
    }
}
