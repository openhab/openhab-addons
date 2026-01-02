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
package org.openhab.binding.blink.internal.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link BaseBlinkApiService} class handles all communication with the Blink API.
 *
 * It provides methods for deserializing JSON responses into the DTOs as well as a method for
 * receiving raw binary data.
 *
 * see https://github.com/MattTW/BlinkMonitorProtocol for the unofficial blink api documentation
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class BaseBlinkApiService {

    public static final String USER_AGENT = "27.0ANDROID_28373244";
    private final Logger logger = LoggerFactory.getLogger(BaseBlinkApiService.class);

    private static final String HEADER_TOKEN_AUTH = "token-auth";
    @SuppressWarnings("FieldCanBeLocal")
    private final String BASE_URL = "https://rest-{tier}.immedia-semi.com";
    static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    final Map<String, Future<?>> cmdStatusJobs = new ConcurrentHashMap<>();

    HttpClient httpClient;
    Gson gson;

    protected BaseBlinkApiService(HttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    protected <T> T apiRequest(String tier, String uri, HttpMethod method, @Nullable String token,
            @Nullable Map<String, String> params, Class<T> classOfT) throws IOException {
        String json = request(tier, uri, method, token, params, null);
        return gson.fromJson(json, classOfT);
    }

    String request(String tier, String uri, HttpMethod method, @Nullable String token,
            @Nullable Map<String, String> params, @Nullable String content) throws IOException {
        String url = createUrl(tier, uri);
        try {
            ContentResponse contentResponse = createRequestAndSend(url, method, token, params, content, true);
            return contentResponse.getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error calling Blink API. Reason: {}", e.getMessage());
            throw new IOException(e);
        }
    }

    public byte[] rawRequest(String tier, String uri, HttpMethod method, @Nullable String token,
            @Nullable Map<String, String> params) throws IOException {
        String url = createUrl(tier, uri);
        try {
            ContentResponse contentResponse = createRequestAndSend(url, method, token, params, null, false);
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
            @Nullable Map<String, String> params, @Nullable String content, boolean json)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        final Request request = httpClient.newRequest(url).method(method.toString());
        if (params != null && !method.equals(HttpMethod.POST))
            params.forEach(request::param);
        else if (params != null) {
            Fields fields = new Fields();
            params.forEach(fields::add);
            request.content(new FormContentProvider(fields));
        }
        if (json)
            request.header(HttpHeader.ACCEPT, CONTENT_TYPE_JSON);
        if (token != null)
            request.header(HEADER_TOKEN_AUTH, token);
        if (content != null)
            request.content(new StringContentProvider(content));
        request.agent(USER_AGENT);
        ContentResponse contentResponse = request.send();
        if (contentResponse.getStatus() != 200) {
            throw new IOException("Blink API Call unsuccessful <Status " + contentResponse.getStatus() + ">");
        }
        return contentResponse;
    }

    public void watchCommandStatus(ScheduledExecutorService scheduler, @Nullable BlinkAccount account, Long networkId,
            Long cmdId, Consumer<Boolean> handler) {
        if (account == null || account.account == null)
            throw new IllegalArgumentException("Cannot call command status api without account");
        watchCommandStatus(scheduler, account, networkId, cmdId, handler, 0);
    }

    void watchCommandStatus(ScheduledExecutorService scheduler, BlinkAccount account, Long networkId, Long cmdId,
            Consumer<Boolean> handler, int numTries) {
        String uri = "/network/" + networkId + "/command/" + cmdId;
        // schedule only once and recurse to avoid having to cancel both a job with fixed delay and a cancellation job
        cmdStatusJobs.put(uri, scheduler.schedule(() -> {
            cmdStatusJobs.remove(uri);
            try {
                logger.debug("Checking for status of async command {} (try {})", cmdId, numTries);
                BlinkCommandResponse status = apiRequest(account.account.tier, uri, HttpMethod.GET, account.auth.token,
                        null, BlinkCommandResponse.class);
                if (status.complete) {
                    logger.debug("Command {} completed with message {}", cmdId, status.status_msg);
                    handler.accept(true);
                } else if (numTries == 15) { // TODO
                    logger.error("Timeout waiting for completion of async command {}", cmdId);
                    handler.accept(false);
                } else {
                    watchCommandStatus(scheduler, account, networkId, cmdId, handler, numTries + 1);
                }
            } catch (IOException e) {
                logger.error("Error waiting for completion of async command {}", cmdId, e);
                handler.accept(false);
            }
        }, 1, TimeUnit.SECONDS));
    }

    public void dispose() {
        for (Future<?> cmdStatusJob : cmdStatusJobs.values()) {
            cmdStatusJob.cancel(true);
        }
        cmdStatusJobs.clear();
    }
}
