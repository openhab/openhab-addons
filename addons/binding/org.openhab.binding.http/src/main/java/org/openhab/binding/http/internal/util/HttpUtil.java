/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.smarthome.core.library.types.RawType;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for making HTTP requests.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
@NonNullByDefault
public class HttpUtil {
    /**
     * Encapsulation of a returned HTTP response, suitable for handling Thing State.
     */
    public static class HttpResponse {
        private static final Set<String> SUPPORTED_TEXT_TYPES = new HashSet<>(Arrays.asList(
                "application/json",
                "application/xhtml",
                "application/xhtml+xml",
                "application/xml"
        ));

        private final Response response;
        private final BufferingResponseListener listener;

        HttpResponse(final Response response, final BufferingResponseListener listener) {
            this.response = response;
            this.listener = listener;
        }

        /**
         * Returns the underlying HTTP response object.
         *
         * @return the {@link Response}
         */
        public Response getResponse() {
            return response;
        }

        /**
         * Returns the body of the response as a {@link RawType}.
         *
         * @return the {@link RawType}
         */
        public RawType asRawType() {
            final String mediaType = Optional.ofNullable(listener.getMediaType()).orElse("application/octet-stream");
            return new RawType(listener.getContent(), mediaType);
        }

        /**
         * Returns the body of the request as a string, if possible.
         *
         * @return a string representation of the response
         * @throws IllegalStateException if the response is binary data and not text
         */
        public String asString() throws IllegalStateException {
            if (!canBeString()) {
                throw new IllegalStateException("Response is not convertible to text");
            } else {
                return listener.getContentAsString();
            }
        }

        @Override
        public String toString() {
            return "HttpResponse[" +
                    getResponse().getStatus() + ", " +
                    (canBeString() ? asString() : "{binary data}") +
                    "]";
        }

        private boolean canBeString() {
            final String mediaType = listener.getMediaType();
            return mediaType != null && mediaType.startsWith("text/") || SUPPORTED_TEXT_TYPES.contains(mediaType);
        }
    }

    /**
     * Make a HTTP request and return an async response.
     *
     * @param httpClient HTTP client to use to make the request
     * @param method HTTP method
     * @param url URL to request
     * @param contentType Content type of the request body, if any
     * @param lastEtag Last E-Tag header received for this request, if any
     * @param body Request body to send, if any
     * @param connectTimeout HTTP connect timeout
     * @param requestTimeout HTTP request timeout
     * @param maxResponseBodyLen Maximum response body length, in bytes
     * @return A {@link CompletionStage} pointing to a {@link HttpResponse} on success.
     */
    public static CompletionStage<HttpResponse> makeRequest(final HttpClient httpClient,
                                                            final String method,
                                                            final URL url,
                                                            final Optional<String> username,
                                                            final Optional<String> password,
                                                            final String contentType,
                                                            final Optional<String> lastEtag,
                                                            final Optional<String> body,
                                                            final Duration connectTimeout,
                                                            final Duration requestTimeout,
                                                            final int maxResponseBodyLen)
    {
        final CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        try {
            final Request request = httpClient
                    .newRequest(url.toURI())
                    .timeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .idleTimeout(requestTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .method(method)
                    .header("content-type", contentType);
            buildBasicAuthHeader(username, password).ifPresent(hdr -> request.header("authorization", hdr));
            lastEtag.ifPresent(le -> request.header("if-none-match", le));
            body.ifPresent(b -> request.content(new StringContentProvider(b)));
            final BufferingResponseListener listener = new BufferingResponseListener(maxResponseBodyLen) {
                @NonNullByDefault({})
                @Override
                public void onComplete(final Result result) {
                    if (result.getFailure() != null) {
                        future.completeExceptionally(result.getFailure());
                    } else {
                        future.complete(new HttpResponse(result.getResponse(), this));
                    }
                }
            };
            request.send(listener);
        } catch (final Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    private static Optional<String> buildBasicAuthHeader(final Optional<String> username, final Optional<String> password) {
        if (username.isPresent() || password.isPresent()) {
            final StringBuilder sb = new StringBuilder();
            username.ifPresent(sb::append);
            sb.append(':');
            password.ifPresent(sb::append);
            return Optional.of("Basic " + Base64.getEncoder().encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8)));
        } else {
            return Optional.empty();
        }
    }
}
