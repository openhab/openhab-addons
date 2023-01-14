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
package org.openhab.binding.homeconnect.internal.client;

import static io.github.bucket4j.Bandwidth.classic;
import static io.github.bucket4j.Refill.intervally;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;

/**
 * okHttp helper.
 *
 * @author Jonas Brüstel - Initial contribution
 * @author Laurent Garnier - Removed okhttp
 *
 */
@NonNullByDefault
public class HttpHelper {
    private static final String BEARER = "Bearer ";
    private static final int OAUTH_EXPIRE_BUFFER = 10;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Map<String, Bucket> BUCKET_MAP = new HashMap<>();
    private static final Object AUTHORIZATION_HEADER_MONITOR = new Object();
    private static final Object BUCKET_MONITOR = new Object();

    private static @Nullable String lastAccessToken = null;

    public static ContentResponse sendRequest(Request request, String clientId)
            throws InterruptedException, TimeoutException, ExecutionException {
        if (HttpMethod.GET.name().equals(request.getMethod())) {
            try {
                getBucket(clientId).asScheduler().consume(1);
            } catch (InterruptedException e) {
                LoggerFactory.getLogger(HttpHelper.class).debug("Could not consume from bucket! clientId={}, error={}",
                        clientId, e.getMessage());
            }
        }
        return request.send();
    }

    public static String formatJsonBody(@Nullable String jsonString) {
        if (jsonString == null) {
            return "";
        }
        try {
            JsonObject json = parseString(jsonString).getAsJsonObject();
            return GSON.toJson(json);
        } catch (Exception e) {
            return jsonString;
        }
    }

    public static String getAuthorizationHeader(OAuthClientService oAuthClientService)
            throws AuthorizationException, CommunicationException {
        synchronized (AUTHORIZATION_HEADER_MONITOR) {
            try {
                AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
                // refresh the token if it's about to expire
                if (accessTokenResponse != null && accessTokenResponse.isExpired(Instant.now(), OAUTH_EXPIRE_BUFFER)) {
                    LoggerFactory.getLogger(HttpHelper.class).debug("Requesting a refresh of the access token.");
                    accessTokenResponse = oAuthClientService.refreshToken();
                }

                if (accessTokenResponse != null) {
                    String lastToken = lastAccessToken;
                    if (lastToken == null) {
                        LoggerFactory.getLogger(HttpHelper.class).debug("The used access token was created at {}",
                                LocalDateTime.ofInstant(accessTokenResponse.getCreatedOn(), ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    } else if (!lastToken.equals(accessTokenResponse.getAccessToken())) {
                        LoggerFactory.getLogger(HttpHelper.class)
                                .debug("The access token changed. New one created at {}",
                                        LocalDateTime
                                                .ofInstant(accessTokenResponse.getCreatedOn(), ZoneId.systemDefault())
                                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                    lastAccessToken = accessTokenResponse.getAccessToken();

                    LoggerFactory.getLogger(HttpHelper.class).debug("Current access token: {}",
                            accessTokenResponse.getAccessToken());
                    return BEARER + accessTokenResponse.getAccessToken();
                } else {
                    LoggerFactory.getLogger(HttpHelper.class).error("No access token available! Fatal error.");
                    throw new AuthorizationException("No access token available!");
                }
            } catch (IOException e) {
                String errorMessage = e.getMessage();
                throw new CommunicationException(errorMessage != null ? errorMessage : "IOException", e);
            } catch (OAuthException | OAuthResponseException e) {
                String errorMessage = e.getMessage();
                throw new AuthorizationException(errorMessage != null ? errorMessage : "oAuth exception", e);
            }
        }
    }

    public static JsonElement parseString(String json) {
        return JSON_PARSER.parse(json);
    }

    private static Bucket getBucket(String clientId) {
        synchronized (BUCKET_MONITOR) {
            Bucket bucket = null;
            if (BUCKET_MAP.containsKey(clientId)) {
                bucket = BUCKET_MAP.get(clientId);
            }

            if (bucket == null) {
                bucket = Bucket4j.builder()
                        // allows 50 tokens per minute (added 10 second buffer)
                        .addLimit(classic(50, intervally(50, Duration.ofSeconds(70))).withInitialTokens(40))
                        // but not often then 50 tokens per second
                        .addLimit(classic(10, intervally(10, Duration.ofSeconds(1)))).build();
                BUCKET_MAP.put(clientId, bucket);
            }
            return bucket;
        }
    }
}
