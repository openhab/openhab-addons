/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.concurrent.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.Request;

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
     * @return a CompletableFuture that completes with the request
     */
    public CompletableFuture<Request> newRequest(URI finalUrl) {
        // if no delay is set, return a completed CompletableFuture
        if (delay == 0) {
            return CompletableFuture.completedFuture(httpClient.newRequest(finalUrl));
        }
        CompletableFuture<Request> future = new CompletableFuture<>();
        if (!requestQueue.offer(new RequestQueueEntry(finalUrl, future))) {
            future.completeExceptionally(new RejectedExecutionException("Maximum queue size exceeded."));
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
            queueEntry.future.complete(httpClient.newRequest(queueEntry.finalUrl));
        }
    }

    private static class RequestQueueEntry {
        public URI finalUrl;
        public CompletableFuture<Request> future;

        public RequestQueueEntry(URI finalUrl, CompletableFuture<Request> future) {
            this.finalUrl = finalUrl;
            this.future = future;
        }
    }
}
