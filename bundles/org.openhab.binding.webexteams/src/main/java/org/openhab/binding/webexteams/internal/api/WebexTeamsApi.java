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
package org.openhab.binding.webexteams.internal.api;

import static org.openhab.binding.webexteams.internal.WebexTeamsBindingConstants.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.webexteams.internal.WebexAuthenticationException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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

    private final OAuthClientService authService;
    private final HttpClient httpClient;

    public WebexTeamsApi(OAuthClientService authService, HttpClient httpClient) {
        this.authService = authService;
        this.httpClient = httpClient;
    }

    /**
     * Get a <code>Person</code> object for the account.
     * 
     * @return a <code>Person</code> object
     * @throws WebexAuthenticationException when authentication fails
     * @throws WebexTeamsApiException for other failures
     */
    public Person getPerson() throws WebexTeamsApiException, WebexAuthenticationException {
        URI url = getUri(WEBEX_API_ENDPOINT + "/people/me");

        return request(url, HttpMethod.GET, Person.class, null);
    }

    private URI getUri(String url) throws WebexTeamsApiException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new WebexTeamsApiException("bad url", e);
        }
        return uri;
    }

    private <I, O> O request(URI url, HttpMethod method, Class<O> clazz, I body)
            throws WebexAuthenticationException, WebexTeamsApiException {
        try {
            // Refresh is handled automatically by this method
            AccessTokenResponse response = this.authService.getAccessTokenResponse();

            String authToken = response == null ? null : response.getAccessToken();
            if (authToken == null) {
                throw new WebexAuthenticationException("Auth token is null");
            } else {
                return doRequest(url, method, authToken, clazz, body);
            }
        } catch (OAuthException | IOException | OAuthResponseException e) {
            throw new WebexAuthenticationException("Not authenticated", e);
        }
    }

    private <I, O> O doRequest(URI url, HttpMethod method, String authToken, Class<O> clazz, I body)
            throws WebexAuthenticationException, WebexTeamsApiException {
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

            ContentResponse response = req.send();

            logger.debug("Response: {} - {}", response.getStatus(), response.getReason());

            if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                throw new WebexAuthenticationException();
            } else if (response.getStatus() == HttpStatus.OK_200) {
                // Obtain the input stream on the response content
                try (InputStream input = new ByteArrayInputStream(response.getContent())) {
                    Reader reader = new InputStreamReader(input);
                    return gson.fromJson(reader, clazz);
                } catch (IOException | JsonIOException | JsonSyntaxException e) {
                    logger.warn("Exception while processing API response: {}", e.getMessage());
                    throw new WebexTeamsApiException("Exception while processing API response", e);
                }
            } else {
                logger.warn("Unexpected response {} - {}", response.getStatus(), response.getReason());
                try (InputStream input = new ByteArrayInputStream(response.getContent())) {
                    String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                            .collect(Collectors.joining("\n"));
                    logger.warn("Content: {}", text);
                } catch (IOException e) {
                    throw new WebexTeamsApiException(
                            String.format("Unexpected response code: {}", response.getStatus()), e);
                }

                throw new WebexTeamsApiException(
                        String.format("Unexpected response {} - {}", response.getStatus(), response.getReason()));
            }
        } catch (TimeoutException e) {
            logger.warn("Request timeout", e);
            throw new WebexTeamsApiException("Request timeout", e);
        } catch (ExecutionException e) {
            logger.warn("Request error", e);
            throw new WebexTeamsApiException("Request error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Request interrupted", e);
            throw new WebexTeamsApiException("Request interrupted", e);
        }
    }

    // sendMessage
    public Message sendMessage(Message msg) throws WebexTeamsApiException, WebexAuthenticationException {
        URI url = getUri(WEBEX_API_ENDPOINT + "/messages");
        Message response = request(url, HttpMethod.POST, Message.class, msg);
        logger.debug("Sent message, id: {}", response.getId());
        return response;
    }
}
