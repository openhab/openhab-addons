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
package org.openhab.binding.deconz.internal.netutils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.BufferingResponseListener;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.http.HttpMethod;

/**
 * An asynchronous API for HTTP interactions.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class AsyncHttpClient {

    private final HttpClient client;

    public AsyncHttpClient(HttpClient client) {
        this.client = client;
    }

    /**
     * Perform a POST request
     *
     * @param address The address
     * @param jsonString The message body
     * @param timeout A timeout
     * @return The result
     */
    public CompletableFuture<Result> post(String address, @Nullable String jsonString, int timeout) {
        return doNetwork(HttpMethod.POST, address, jsonString, timeout);
    }

    /**
     * Perform a PUT request
     *
     * @param address The address
     * @param jsonString The message body
     * @param timeout A timeout
     * @return The result
     */
    public CompletableFuture<Result> put(String address, @Nullable String jsonString, int timeout) {
        return doNetwork(HttpMethod.PUT, address, jsonString, timeout);
    }

    /**
     * Perform a GET request
     *
     * @param address The address
     * @param timeout A timeout
     * @return The result
     */
    public CompletableFuture<Result> get(String address, int timeout) {
        return doNetwork(HttpMethod.GET, address, null, timeout);
    }

    /**
     * Perform a DELETE request
     *
     * @param address The address
     * @param timeout A timeout
     * @return The result
     */
    public CompletableFuture<Result> delete(String address, int timeout) {
        return doNetwork(HttpMethod.DELETE, address, null, timeout);
    }

    private CompletableFuture<Result> doNetwork(HttpMethod method, String address, @Nullable String body, int timeout) {
        final CompletableFuture<Result> f = new CompletableFuture<>();
        Request request = client.newRequest(URI.create(address));
        if (body != null) {
            request.body(new StringRequestContent("application/json", body, StandardCharsets.UTF_8));
        }

        request.method(method).timeout(timeout, TimeUnit.MILLISECONDS).send(new BufferingResponseListener() {

            @Override
            public void onComplete(@NonNullByDefault({}) org.eclipse.jetty.client.Result result) {
                if (result.getFailure() != null) {
                    f.completeExceptionally(result.getFailure());
                    return;
                }
                String content = getContentAsString();
                f.complete(new Result(content != null ? content : "", result.getResponse().getStatus()));
            }
        });
        return f;
    }

    public static class Result {
        private final String body;
        private final int responseCode;

        public Result(String body, int responseCode) {
            this.body = body;
            this.responseCode = responseCode;
        }

        public String getBody() {
            return body;
        }

        public int getResponseCode() {
            return responseCode;
        }
    }
}
