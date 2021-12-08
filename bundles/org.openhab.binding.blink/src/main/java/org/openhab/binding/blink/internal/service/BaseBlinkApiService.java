/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.blink.internal.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

/**
 * The {@link BaseBlinkApiService} class handles all communication with the Blink API.
 * <p>
 * It provides methods for deserializing JSON responses into the DTOs as well as a method for
 * receiving raw binary data.
 * <p>
 * see https://github.com/MattTW/BlinkMonitorProtocol for the unofficial blink api documentation
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class BaseBlinkApiService {

    private final Logger logger = LoggerFactory.getLogger(BaseBlinkApiService.class);

    private static final String HEADER_TOKEN_AUTH = "token-auth";
    @SuppressWarnings("FieldCanBeLocal") private final String BASE_URL = "https://rest-{tier}.immedia-semi.com";
    static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    HttpClient httpClient;
    Gson gson;

    protected BaseBlinkApiService(HttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    protected <T> T apiRequest(String tier, String uri, HttpMethod method, @Nullable String token,
            @Nullable Map<String, String> params, Class<T> classOfT) throws IOException {
        String json = request(tier, uri, method, token, params);
        return gson.fromJson(json, classOfT);
    }

    String request(String tier, String uri, HttpMethod method, @Nullable String token,
            @Nullable Map<String, String> params)
            throws IOException {
        String url = createUrl(tier, uri);
        try {
            ContentResponse contentResponse = createRequestAndSend(url, method, token, params, true);
            return contentResponse.getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error calling Blink API. Reason: {}", e.getMessage());
            throw new IOException(e);
        }
    }

    public byte[] rawRequest(String tier, String uri, HttpMethod method, @Nullable String token,
            @Nullable Map<String, String> params)
            throws IOException {
        String url = createUrl(tier, uri);
        try {
            ContentResponse contentResponse = createRequestAndSend(url, method, token, params, false);
            return contentResponse.getContent();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error calling Blink API. Reason: {}", e.getMessage());
            throw new IOException(e);
        }
    }

    private String createUrl(String tier, String uri) {
        String baseUrl = BASE_URL.replace("{tier}", tier);
        return baseUrl + uri;
    }

    private ContentResponse createRequestAndSend(String url, HttpMethod method, @Nullable String token,
            @Nullable Map<String, String> params, boolean json)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        final Request request = httpClient.newRequest(url).method(method.toString());
        if (params != null)
            params.forEach(request::param);
        if (json)
            request.header(HttpHeader.ACCEPT, CONTENT_TYPE_JSON);
        if (token != null)
            request.header(HEADER_TOKEN_AUTH, token);
        ContentResponse contentResponse = request.send();
        if (contentResponse.getStatus() != 200) {
            throw new IOException("Blink API Call unsuccessful <Status " + contentResponse.getStatus() + ">");
        }
        return contentResponse;
    }

}
