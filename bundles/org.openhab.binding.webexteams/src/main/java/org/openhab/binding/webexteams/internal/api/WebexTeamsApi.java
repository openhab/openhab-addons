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
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
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
public class WebexTeamsApi implements AccessTokenRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(WebexTeamsApi.class);

    private final WebexTeamsHandler handler;
    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    private @NonNullByDefault({}) OAuthClientService authService;

    // Config variables.
    private String authToken;
    private String clientId;
    private String clientSecret;
    private String authCode;
    private String refreshToken;

    /**
     * Constructor.
     * 
     * @param WebexTeamsHandler hander interact with handler context.
     */
    public WebexTeamsApi(WebexTeamsHandler handler, OAuthFactory oAuthFactory, HttpClient httpClient) {
        this.handler = handler;

        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;

        WebexTeamsConfiguration config = handler.getConfigAs(WebexTeamsConfiguration.class);
        this.authToken = config.token;
        this.clientId = config.clientId;
        this.clientSecret = config.clientSecret;
        this.authCode = config.authCode;
        this.refreshToken = config.refreshToken;

        // If clientId is provided, we're using a webex integration and we'll need to run OAuth
        if (!this.clientId.isBlank()) {
            createIntegrationOAuthClientService();
            // if we have a refresh token, try it.
            if (!this.refreshToken.isBlank()) {
                String token = refreshToken();
                if (token.isBlank()) {
                    final String msg = "Failed to use refresh token.  Set new auth code.";
                    logger.error(msg);
                    throw new NotAuthenticatedException(msg);
                }
            } else { // use authCode to get initial authCode
                getInitialToken();
            }
        } else { // otherwise use the configured bot token
            createBotOAuthClientService();
        }
    }

    public void createIntegrationOAuthClientService() {
        String thingUID = handler.getThing().getUID().getAsString();
        logger.debug("Creating OAuth Client Service for {}", thingUID);
        OAuthClientService service = oAuthFactory.createOAuthClientService(thingUID, OAUTH_TOKEN_URL, OAUTH_AUTH_URL,
                this.clientId, this.clientSecret, "", false);
        service.addAccessTokenRefreshListener(this);
        this.authService = service;
    }

    public String refreshToken() {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setRefreshToken(this.refreshToken);
        try {
            this.authService.importAccessTokenResponse(response);
            response = this.authService.refreshToken();
            logger.debug("Initialized from refreshToken");
            logger.debug("Token {} of type {} created on {} expiring after {} seconds", response.getAccessToken(),
                    response.getTokenType(), response.getCreatedOn(), response.getExpiresIn());
        } catch (OAuthException | OAuthResponseException | IOException e) {
            logger.error("Failed to import refreshToken", e);
            return "";
        }
        return response.getAccessToken();
    }

    public String getInitialToken() {
        try {
            AccessTokenResponse response = authService.getAccessTokenResponseByAuthorizationCode(this.authCode,
                    OAUTH_REDIRECT_URL);
            logger.debug("Token {} of type {} created on {} expiring after {} seconds", response.getAccessToken(),
                    response.getTokenType(), response.getCreatedOn(), response.getExpiresIn());

            // Need to call this manually here. The authService callback only fires on actual refreshes.
            onAccessTokenResponse(response);
            return response.getAccessToken();
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.error("Failed to get initial token: {}", e.getMessage());
            throw new NotAuthenticatedException("@text/confErrorInitial", e);
        }
    }

    public void createBotOAuthClientService() {
        String thingUID = handler.getThing().getUID().getAsString();
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(this.authToken);
        response.setScope(OAUTH_SCOPE);
        response.setTokenType("Bearer");
        response.setExpiresIn(Long.MAX_VALUE); // Bot access tokens don't expire
        OAuthClientService service = oAuthFactory.createOAuthClientService(thingUID, OAUTH_TOKEN_URL,
                OAUTH_AUTHORIZATION_URL, "not used", null, OAUTH_SCOPE, false);
        try {
            service.importAccessTokenResponse(response);
        } catch (OAuthException e) {
            throw new WebexTeamsApiException("Failed to create oauth client with bot token", e);
        }
        this.authService = service;
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        // Update access token and refreshToken in config
        logger.debug("Updating refreshToken");
        String refreshToken = tokenResponse.getRefreshToken();
        Configuration configuration = handler.editConfiguration();
        configuration.put("refreshToken", refreshToken);
        handler.updateConfiguration(configuration);
    }

    public void dispose() {
        String thingUID = handler.getThing().getUID().getAsString();
        this.oAuthFactory.ungetOAuthService(thingUID);
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
        try {
            // Refresh is handled automatically by this method
            AccessTokenResponse response = this.authService.getAccessTokenResponse();

            String authToken = response.getAccessToken();
            return doRequest(url, method, authToken, clazz, body);

        } catch (OAuthException | IOException | OAuthResponseException e) {
            throw new NotAuthenticatedException("Not authenticated", e);
        }
    }

    private <I, O> O doRequest(URI url, HttpMethod method, String authToken, Class<O> clazz, I body) {
        Gson gson = new Gson();
        try {
            Request req = httpClient.newRequest(url).method(method);
            req.header("Authorization", "Bearer " + authToken);
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
