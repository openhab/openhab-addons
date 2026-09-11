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
package org.openhab.binding.evcc.internal.api;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link EvccRequestQueue} manages queued Jetty {@link Request} executions
 * and processes them sequentially in a controlled, non-blocking manner.
 * It is used by the EVCC binding to serialize HTTP API calls and ensure
 * predictable request handling independent of WebSocket updates.
 *
 * @author Marcel Goerentz - Initial contribution
 */

@NonNullByDefault
public class EvccRequestQueue {

    private final Logger logger = LoggerFactory.getLogger(EvccRequestQueue.class);
    private final HttpClient httpClient;
    private final Queue<QueuedRequest> requestQueue = new ConcurrentLinkedQueue<>();
    private final java.util.concurrent.ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool("evcc-bridge-handler");

    private @Nullable ScheduledFuture<?> queueWorker;

    public EvccRequestQueue(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void start() {
        queueWorker = scheduler.scheduleWithFixedDelay(() -> {
            try {
                processQueue();
            } catch (Exception e) {
                logger.warn("Error processing evcc request queue", e);
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        Optional.ofNullable(queueWorker).ifPresent(w -> w.cancel(true));
        requestQueue.clear();
    }

    private void processQueue() {
        @Nullable
        QueuedRequest queued = requestQueue.poll();
        if (queued == null) {
            return;
        }
        try {
            ContentResponse response = queued.request().send();
            try {
                queued.onSuccess().accept(response);
            } catch (Exception callbackError) {
                logger.warn("Error in onSuccess callback: ", callbackError);
            }
        } catch (Exception e) {
            try {
                queued.onError().accept(e);
            } catch (Exception callbackError) {
                logger.warn("Error in onError callback: ", callbackError);
            }
        }
    }

    public void enqueueRequest(String url, HttpMethod method, JsonElement payload, Consumer<ContentResponse> onSuccess,
            Consumer<Exception> onError) {
        if (url.isBlank()) {
            onError.accept(new IllegalArgumentException("URL must not be empty"));
            return;
        }
        try {
            Request request = httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS).method(method)
                    .header(HttpHeader.ACCEPT, "application/json");

            if (!payload.isJsonNull()) {
                request.content(new StringContentProvider(payload.toString())).header(HttpHeader.CONTENT_TYPE,
                        "application/json");
            }

            requestQueue.add(new QueuedRequest(request, onSuccess, onError));
        } catch (Exception e) {
            logger.warn("evcc bridge couldn't call the API", e);
            onError.accept(e);
        }
    }
}
