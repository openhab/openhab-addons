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
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RateLimitedHttpClient} is a wrapper for a Jetty HTTP client that limits the number of requests by delaying
 * the request creation
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class RateLimitedHttpClient {
    private static final int MAX_QUEUE_SIZE = 1000; // maximum queue size
    private final Logger logger = LoggerFactory.getLogger(RateLimitedHttpClient.class);

    private HttpClient httpClient;
    private int delay = 0; // in ms
    private final ScheduledExecutorService scheduler;
    private final LinkedBlockingQueue<RequestQueueEntry> requestQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private final LinkedBlockingQueue<RequestQueueEntry> priorityRequestQueue = new LinkedBlockingQueue<>(
            MAX_QUEUE_SIZE);

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
        requestQueue.forEach(RequestQueueEntry::cancel);
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
     * @param httpClient secure or insecure {@link HttpClient}
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
     * @return a {@link CompletableFuture} that completes with the request
     */
    public CompletableFuture<Request> newRequest(URI finalUrl, HttpMethod method, String content,
            @Nullable String contentType) {
        return queueRequest(finalUrl, method, content, contentType, requestQueue);
    }

    /**
     * Create a new priority request (executed as next request) to the given URL respecting rate-limits
     *
     * @param finalUrl the request URL
     * @param method http request method GET/PUT/POST
     * @param content the content (if method PUT/POST)
     * @return a {@link CompletableFuture} that completes with the request
     */
    public CompletableFuture<Request> newPriorityRequest(URI finalUrl, HttpMethod method, String content,
            @Nullable String contentType) {
        return queueRequest(finalUrl, method, content, contentType, priorityRequestQueue);
    }

    private CompletableFuture<Request> queueRequest(URI finalUrl, HttpMethod method, String content,
            @Nullable String contentType, LinkedBlockingQueue<RequestQueueEntry> queue) {
        // if no delay is set, return a completed CompletableFuture
        CompletableFuture<Request> future = new CompletableFuture<>();
        RequestQueueEntry queueEntry = new RequestQueueEntry(finalUrl, method, content, contentType, future);
        if (delay == 0) {
            queueEntry.completeFuture(httpClient);
        } else {
            if (!queue.offer(queueEntry)) {
                future.completeExceptionally(new RejectedExecutionException("Maximum queue size exceeded."));
            }

        }
        return future;
    }

    /**
     * Get the {@link AuthenticationStore} from the wrapped {@link HttpClient}
     *
     * @return the AuthenticationStore of the client
     */
    public AuthenticationStore getAuthenticationStore() {
        return httpClient.getAuthenticationStore();
    }

    /**
     * Remove authentication result from the wrapped {@link HttpClient} and force re-auth
     *
     * @param uri the {@link URI} associated with the authentication result
     * @return true if a result was found and cleared, false if not authenticated at all
     */
    public boolean reAuth(URI uri) {
        AuthenticationStore authStore = httpClient.getAuthenticationStore();
        Authentication.Result authResult = authStore.findAuthenticationResult(uri);
        if (authResult != null) {
            authStore.removeAuthenticationResult(authResult);
            logger.debug("Cleared authentication result for '{}', retrying immediately", uri);
            return true;
        } else {
            logger.warn("Could not find authentication result for '{}', failing here", uri);
            return false;
        }
    }

    private void stopProcessJob() {
        ScheduledFuture<?> processJob = this.processJob;
        if (processJob != null) {
            processJob.cancel(false);
            this.processJob = null;
        }
    }

    /**
     * Gets a request from either the priority queue or tge regular queue and creates the request
     */
    private void processQueue() {
        RequestQueueEntry queueEntry = priorityRequestQueue.poll();
        if (queueEntry == null) {
            // no entry in priorityRequestQueue, try the regular queue
            queueEntry = requestQueue.poll();
        }
        if (queueEntry != null) {
            queueEntry.completeFuture(httpClient);
        }
    }

    private static class RequestQueueEntry {
        private final URI finalUrl;
        private final HttpMethod method;
        private final String content;
        private final @Nullable String contentType;
        private final CompletableFuture<Request> future;

        public RequestQueueEntry(URI finalUrl, HttpMethod method, String content, @Nullable String contentType,
                CompletableFuture<Request> future) {
            this.finalUrl = finalUrl;
            this.method = method;
            this.content = content;
            this.contentType = contentType;
            this.future = future;
        }

        /**
         * complete the future with a request
         *
         * @param httpClient the client to create the request
         */
        public void completeFuture(HttpClient httpClient) {
            Request request = httpClient.newRequest(finalUrl).method(method);
            if ((method == HttpMethod.POST || method == HttpMethod.PUT) && !content.isEmpty()) {
                if (contentType == null) {
                    request.content(new StringContentProvider(content));
                } else {
                    request.content(new StringContentProvider(content), contentType);
                }
            }
            future.complete(request);
        }

        /**
         * cancel this request and complete the future with a {@link CancellationException}
         */
        public void cancel() {
            future.completeExceptionally(new CancellationException());
        }
    }
}
