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
package org.openhab.binding.http.internal.http;

import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;

/**
 * The {@link RateLimitedHttpClient} is a wrapper for a Jetty HTTP client that limits the number of requests by delaying
 * the request creation
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class RateLimitedHttpClient {
    private static final int MAX_QUEUE_SIZE = 1000; // maximum queue size
    private HttpClient httpClient;
    private int delay = 0; // in ms
    private final ScheduledExecutorService scheduler;
    private final LinkedBlockingQueue<RequestQueueEntry> requestQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

    private @Nullable ScheduledFuture<?> processJob;

    public RateLimitedHttpClient(HttpClient httpClient, ScheduledExecutorService scheduler) {
        this.httpClient = httpClient;
        this.scheduler = scheduler;
    }

    /**
     * Stop processing the queue and clear it
     */
    public void shutdown() {
        stopProcessJob();
        requestQueue.forEach(queueEntry -> queueEntry.future.completeExceptionally(new CancellationException()));
    }

    /**
     * Set a new delay
     *
     * @param delay in ms between to requests
     */
    public void setDelay(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Delay needs to be larger or equal to zero");
        }
        this.delay = delay;
        stopProcessJob();
        if (delay != 0) {
            processJob = scheduler.scheduleWithFixedDelay(this::processQueue, 0, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Set the HTTP client
     *
     * @param httpClient secure or insecure Jetty http client
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Create a new request to the given URL respecting rate-limits
     *
     * @param finalUrl the request URL
     * @param method http request method GET/PUT/POST
     * @param content the content (if method PUT/POST)
     * @return a CompletableFuture that completes with the request
     */
    public CompletableFuture<Request> newRequest(URI finalUrl, HttpMethod method, String content) {
        // if no delay is set, return a completed CompletableFuture
        CompletableFuture<Request> future = new CompletableFuture<>();
        RequestQueueEntry queueEntry = new RequestQueueEntry(finalUrl, method, content, future);
        if (delay == 0) {
            queueEntry.completeFuture(httpClient);
        } else {
            if (!requestQueue.offer(queueEntry)) {
                future.completeExceptionally(new RejectedExecutionException("Maximum queue size exceeded."));
            }
        }
        return future;
    }

    /**
     * Get the AuthenticationStore from the wrapped client
     *
     * @return
     */
    public AuthenticationStore getAuthenticationStore() {
        return httpClient.getAuthenticationStore();
    }

    private void stopProcessJob() {
        ScheduledFuture<?> processJob = this.processJob;
        if (processJob != null) {
            processJob.cancel(false);
            this.processJob = null;
        }
    }

    private void processQueue() {
        RequestQueueEntry queueEntry = requestQueue.poll();
        if (queueEntry != null) {
            queueEntry.completeFuture(httpClient);
        }
    }

    private static class RequestQueueEntry {
        private URI finalUrl;
        private HttpMethod method;
        private String content;
        private CompletableFuture<Request> future;

        public RequestQueueEntry(URI finalUrl, HttpMethod method, String content, CompletableFuture<Request> future) {
            this.finalUrl = finalUrl;
            this.method = method;
            this.content = content;
            this.future = future;
        }

        /**
         * complete the future with a request
         *
         * @param httpClient the client to create the request
         */
        public void completeFuture(HttpClient httpClient) {
            Request request = httpClient.newRequest(finalUrl).method(method);
            if (method != HttpMethod.GET && !content.isEmpty()) {
                request.content(new StringContentProvider(content));
            }
            future.complete(request);
        }
    }
}
