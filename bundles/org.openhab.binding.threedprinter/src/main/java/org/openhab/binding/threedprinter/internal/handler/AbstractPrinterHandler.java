/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.threedprinter.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Base class for all 3D printer handlers providing shared HTTP and scheduling logic.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractPrinterHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final HttpClient httpClient;
    protected final Gson gson = new Gson();

    private @Nullable ScheduledFuture<?> refreshJob;

    protected AbstractPrinterHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        int interval = getRefreshInterval();
        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, interval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    protected abstract int getRefreshInterval();

    protected abstract void refresh();

    /**
     * Sends an HTTP GET and returns the response body, or null on failure.
     */
    protected @Nullable String httpGet(String url, String apiKey) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(10, TimeUnit.SECONDS);
            if (!apiKey.isBlank()) {
                request.header("X-Api-Key", apiKey);
            }
            ContentResponse response = request.send();
            int status = response.getStatus();
            if (status == 200) {
                return response.getContentAsString();
            }
            logger.debug("GET {} returned HTTP {}", url, status);
            return null;
        } catch (Exception e) {
            logger.debug("GET {} failed: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * Sends an HTTP POST with a JSON body and returns the HTTP status code, or -1 on exception.
     */
    protected int httpPost(String url, String apiKey, String jsonBody) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.POST).timeout(10, TimeUnit.SECONDS);
            if (!apiKey.isBlank()) {
                request.header("X-Api-Key", apiKey);
            }
            if (!jsonBody.isEmpty()) {
                request.content(new StringContentProvider(jsonBody), "application/json");
            }
            return request.send().getStatus();
        } catch (Exception e) {
            logger.debug("POST {} failed: {}", url, e.getMessage());
            return -1;
        }
    }

    /**
     * Sends an HTTP DELETE and returns the HTTP status code, or -1 on exception.
     */
    protected int httpDelete(String url, String apiKey) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.DELETE).timeout(10, TimeUnit.SECONDS);
            if (!apiKey.isBlank()) {
                request.header("X-Api-Key", apiKey);
            }
            return request.send().getStatus();
        } catch (Exception e) {
            logger.debug("DELETE {} failed: {}", url, e.getMessage());
            return -1;
        }
    }

    /**
     * Sends an HTTP PUT with a JSON body and returns the HTTP status code, or -1 on exception.
     */
    protected int httpPut(String url, String apiKey, String jsonBody) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.PUT).timeout(10, TimeUnit.SECONDS);
            if (!apiKey.isBlank()) {
                request.header("X-Api-Key", apiKey);
            }
            if (!jsonBody.isEmpty()) {
                request.content(new StringContentProvider(jsonBody), "application/json");
            }
            return request.send().getStatus();
        } catch (Exception e) {
            logger.debug("PUT {} failed: {}", url, e.getMessage());
            return -1;
        }
    }

    /**
     * Sends an HTTP GET and returns the raw response bytes, or null on failure.
     */
    protected byte @Nullable [] httpGetBytes(String url, String apiKey) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(10, TimeUnit.SECONDS);
            if (!apiKey.isBlank()) {
                request.header("X-Api-Key", apiKey);
            }
            ContentResponse response = request.send();
            if (response.getStatus() == 200) {
                return response.getContent();
            }
            logger.debug("GET bytes {} returned HTTP {}", url, response.getStatus());
            return null;
        } catch (Exception e) {
            logger.debug("GET bytes {} failed: {}", url, e.getMessage());
            return null;
        }
    }

    protected void markOffline(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }

    /**
     * Parses a JSON string into the given type, returning null on malformed input instead of throwing. This prevents a
     * {@link JsonSyntaxException} from propagating out of the scheduled refresh task and silently stopping all future
     * polling.
     */
    protected <T> @Nullable T fromJson(@Nullable String json, Class<T> type) {
        if (json == null) {
            return null;
        }
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            logger.debug("Failed to parse JSON response: {}", e.getMessage());
            return null;
        }
    }
}
