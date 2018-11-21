/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.util;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.smarthome.core.library.types.RawType;
import org.openhab.binding.http.HttpBindingConstants;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for making HTTP requests.
 *
 * @author Brian J. Tarricone
 */
public class HttpUtil {
    /**
     * Encapsulation of a returned HTTP response, suitable for handling Thing State.
     *
     * @author Brian J. Tarricone
     */
    public static class HttpResponse {
        private static final Set<String> SUPPORTED_TEXT_TYPES = new HashSet<>(Arrays.asList(
                "application/json",
                "application/xhtml",
                "application/xhtml+xml",
                "application/xml"
        ));

        private final Response response;
        private final ByteBuffer responseBody;
        private final String mimeType;
        private final Optional<Charset> charset;

        HttpResponse(final Response response, final ByteBuffer responseBody) {
            this.response = response;
            this.responseBody = responseBody;

            final String contentType = Optional.ofNullable(response.getHeaders().get("content-type"))
                    .orElse(HttpBindingConstants.DEFAULT_CONTENT_TYPE);
            final String[] parts = contentType.split(";");
            this.mimeType = parts[0];
            this.charset = Arrays.stream(parts, 1, parts.length)
                    .map(String::trim)
                    .filter(s -> s.toLowerCase(Locale.US).startsWith("charset="))
                    .findFirst()
                    .map(s -> Charset.forName(s.substring(8)));
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
            return new RawType(asByteArray(), this.mimeType);
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
                return new String(asByteArray(), this.charset.orElse(StandardCharsets.UTF_8));
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
            return this.mimeType.startsWith("text/") || SUPPORTED_TEXT_TYPES.contains(this.mimeType);
        }

        private byte[] asByteArray() {
            if (this.responseBody.hasArray()) {
                return this.responseBody.array();
            } else {
                this.responseBody.rewind();
                final byte[] array = new byte[this.responseBody.remaining()];
                this.responseBody.get(array);
                return array;
            }
        }
    }

    /**
     * A Jetty HTTP response listener allows us to deal with the async response as a {@link CompletionStage}.
     *
     * @author Brian J. Tarricone
     */
    private static class CompletionStageResponseListener extends Response.Listener.Adapter {
        private final long maxResponseBodyLen;
        private final CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        private final ByteArrayOutputStream responseBodyAccum = new ByteArrayOutputStream();

        CompletionStageResponseListener(final long maxResponseBodyLen) {
            this.maxResponseBodyLen = maxResponseBodyLen;
        }

        @Override
        public void onContent(final Response response, final ByteBuffer byteBuffer) {
            if (responseBodyAccum.size() + byteBuffer.remaining() >= this.maxResponseBodyLen) {
                response.abort(new IllegalArgumentException("Response body is larger than the max allowed length (" + maxResponseBodyLen + ")"));
            } else if (!future.isDone()) {
                if (byteBuffer.hasArray()) {
                    responseBodyAccum.write(byteBuffer.array(), 0, byteBuffer.remaining());
                } else {
                    final int len = byteBuffer.remaining();
                    final byte[] buf = new byte[len];
                    byteBuffer.get(buf);
                    responseBodyAccum.write(buf, 0, len);
                }
            }
        }

        @Override
        public void onSuccess(final Response response) {
            future.complete(new HttpResponse(response, ByteBuffer.wrap(responseBodyAccum.toByteArray())));
        }

        @Override
        public void onFailure(final Response response, final Throwable throwable) {
            future.completeExceptionally(throwable);
        }

        void setOn(final Request request) {
            request.onResponseContent(this);
            request.onResponseSuccess(this);
            request.onResponseFailure(this);
        }

        CompletionStage<HttpResponse> getCompletionStage() {
            return future;
        }
    }

    private static <T> CompletionStage<T> failedFuture(final Throwable ex) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
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
                                                            final String contentType,
                                                            final Optional<String> lastEtag,
                                                            final Optional<String> body,
                                                            final Duration connectTimeout,
                                                            final Duration requestTimeout,
                                                            final int maxResponseBodyLen)
    {
        try {
            final Request request = httpClient
                    .newRequest(url.toURI())
                    .timeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .idleTimeout(requestTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .method(method)
                    .header("content-type", contentType);
            lastEtag.ifPresent(le -> request.header("if-none-match", le));
            body.ifPresent(b -> request.content(new StringContentProvider(b)));
            final CompletionStageResponseListener listener = new CompletionStageResponseListener(maxResponseBodyLen);
            listener.setOn(request);
            request.send();
            return listener.getCompletionStage();
        } catch (final ExecutionException e) {
            return failedFuture(e.getCause() != null ? e.getCause() : e);
        } catch (final Exception e) {
            return failedFuture(e);
        }
    }
}
