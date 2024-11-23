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
package org.openhab.binding.pushbullet.internal;

import static org.openhab.binding.pushbullet.internal.PushbulletBindingConstants.*;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.pushbullet.internal.exception.PushbulletApiException;
import org.openhab.binding.pushbullet.internal.exception.PushbulletAuthenticationException;
import org.openhab.binding.pushbullet.internal.model.InstantDeserializer;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.RawType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link PushbulletHttpClient} handles requests to Pushbullet API
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class PushbulletHttpClient {

    private static final String USER_AGENT = "openHAB/" + OpenHAB.getVersion();

    private static final int TIMEOUT = 30; // in seconds

    private final Logger logger = LoggerFactory.getLogger(PushbulletHttpClient.class);

    private final Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Instant.class, new InstantDeserializer())
            .create();

    private PushbulletConfiguration config = new PushbulletConfiguration();

    private final HttpClient httpClient;

    public PushbulletHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setConfiguration(PushbulletConfiguration config) {
        this.config = config;
    }

    /**
     * Executes an api request
     *
     * @param apiEndpoint the request api endpoint
     * @param responseType the response type
     * @return the unpacked response
     * @throws PushbulletApiException
     */
    public <T> T executeRequest(String apiEndpoint, Class<T> responseType) throws PushbulletApiException {
        return executeRequest(apiEndpoint, null, responseType);
    }

    /**
     * Executes an api request
     *
     * @param apiEndpoint the request api endpoint
     * @param body the request body object
     * @param responseType the response type
     * @return the unpacked response
     * @throws PushbulletApiException
     */
    public <T> T executeRequest(String apiEndpoint, @Nullable Object body, Class<T> responseType)
            throws PushbulletApiException {
        String url = API_BASE_URL + apiEndpoint;
        String accessToken = config.getAccessToken();

        Request request = newRequest(url).header("Access-Token", accessToken);

        if (body != null) {
            StringContentProvider content = new StringContentProvider(gson.toJson(body));
            String contentType = MimeTypes.Type.APPLICATION_JSON.asString();

            request.method(HttpMethod.POST).content(content, contentType);
        }

        String responseBody = sendRequest(request);

        try {
            T response = Objects.requireNonNull(gson.fromJson(responseBody, responseType));
            logger.debug("Unpacked Response: {}", response);
            return response;
        } catch (JsonSyntaxException e) {
            logger.debug("Failed to unpack response as '{}': {}", responseType.getSimpleName(), e.getMessage());
            throw new PushbulletApiException(e);
        }
    }

    /**
     * Uploads a file
     *
     * @param url the upload url
     * @param data the file data
     * @throws PushbulletApiException
     */
    public void uploadFile(String url, RawType data) throws PushbulletApiException {
        MultiPartContentProvider content = new MultiPartContentProvider();
        content.addFieldPart("file", new BytesContentProvider(data.getMimeType(), data.getBytes()), null);

        Request request = newRequest(url).method(HttpMethod.POST).content(content);

        sendRequest(request);
    }

    /**
     * Creates a new http request
     *
     * @param url the request url
     * @return the new Request object with default parameters
     */
    private Request newRequest(String url) {
        return httpClient.newRequest(url).agent(USER_AGENT).timeout(TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * Sends a http request
     *
     * @param request the request to send
     * @return the response body
     * @throws PushbulletApiException
     */
    private String sendRequest(Request request) throws PushbulletApiException {
        try {
            logger.debug("Request {} {}", request.getMethod(), request.getURI());
            logger.debug("Request Headers: {}", request.getHeaders());

            ContentResponse response = request.send();

            int statusCode = response.getStatus();
            String statusReason = response.getReason();
            String responseBody = response.getContentAsString();

            logger.debug("Got HTTP {} Response: '{}'", statusCode, responseBody);

            switch (statusCode) {
                case HttpStatus.OK_200:
                case HttpStatus.NO_CONTENT_204:
                    return responseBody;
                case HttpStatus.UNAUTHORIZED_401:
                case HttpStatus.FORBIDDEN_403:
                    throw new PushbulletAuthenticationException(statusReason);
                case HttpStatus.TOO_MANY_REQUESTS_429:
                    logger.warn("Rate limited for making too many requests until {}",
                            getRateLimitResetTime(response.getHeaders()));
                default:
                    throw new PushbulletApiException(statusReason);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Failed to send request: {}", e.getMessage());
            throw new PushbulletApiException(e);
        }
    }

    /**
     * Returns the rate limit reset time included in response headers
     *
     * @param headers the response headers
     * @return the rate limit reset time if found in headers, otherwise null
     */
    private @Nullable Instant getRateLimitResetTime(HttpFields headers) {
        try {
            long resetTime = headers.getLongField(HEADER_RATELIMIT_RESET);
            if (resetTime != -1) {
                return Instant.ofEpochSecond(resetTime);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
