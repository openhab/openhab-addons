/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.util;

import static org.eclipse.jetty.http.HttpHeader.*;
import static org.eclipse.jetty.http.HttpMethod.*;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON_UTF_8;
import static org.eclipse.jetty.http.MimeTypes.Type.FORM_ENCODED;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.API_VERSION;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.DI_OS_VERSION;
import static org.openhab.binding.amazonechocontrol.internal.util.HttpRequestBuilder.FailMode.*;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link HttpRequestBuilder} creates customized requests for Alexa API requests
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HttpRequestBuilder {
    private static final String DEFAULT_USER_AGENT = "AmazonWebView/Amazon Alexa/" + API_VERSION + "/iOS/"
            + DI_OS_VERSION + "/iPhone";

    private final Logger logger = LoggerFactory.getLogger(HttpRequestBuilder.class);

    private final CookieManager cookieManager;
    private final HttpClient httpClient;
    private final Gson gson;
    private final Lock lock = new ReentrantLock();
    private final Semaphore semaphore = new Semaphore(2, true);

    public HttpRequestBuilder(HttpClient httpClient, CookieManager cookieManager, Gson gson) {
        this.httpClient = httpClient;
        this.cookieManager = cookieManager;
        this.gson = gson;
    }

    public Builder get(String uriString) {
        return new Builder(GET, uriString);
    }

    public Builder post(String uriString) {
        return new Builder(POST, uriString);
    }

    public Builder put(String uriString) {
        return new Builder(PUT, uriString);
    }

    public Builder delete(String uriString) {
        return new Builder(DELETE, uriString);
    }

    public Builder builder(HttpMethod httpMethod, String uriString) {
        return new Builder(httpMethod, uriString);
    }

    private void createRequest(URI uri, RequestParams params, HttpResponseListener responseListener) {
        Request request = httpClient.newRequest(uri).method(params.method());
        request.header(ACCEPT_LANGUAGE, "en-US");
        request.header("DNT", "1");
        request.header("Upgrade-Insecure-Requests", "1");
        if (!params.customHeaders().containsKey(USER_AGENT.toString())) {
            request.agent(DEFAULT_USER_AGENT);
        }
        params.customHeaders().entrySet().stream().filter(h -> !h.getValue().isBlank())
                .forEach(h -> request.header(h.getKey(), h.getValue()));

        // handle re-directs in response listener manually
        request.followRedirects(false);

        // add cookies

        if (!params.customHeaders().containsKey(COOKIE.toString())) {
            for (HttpCookie cookie : cookieManager.getCookieStore().get(uri)) {
                request.cookie(cookie);
                if (cookie.getName().equals("csrf")) {
                    request.header("csrf", cookie.getValue());
                }
            }
        }

        if (params.requestContent() != null) {
            byte[] contentBytes = params.requestContent().getBytes(StandardCharsets.UTF_8);
            request.header(CONTENT_TYPE, params.json() ? APPLICATION_JSON_UTF_8.asString() : FORM_ENCODED.asString());
            request.header(CONTENT_LENGTH, Integer.toString(contentBytes.length));
            if (POST.equals(params.method())) {
                request.header(EXPECT, "100-continue");
            }
            request.content(new BytesContentProvider(contentBytes));
        }

        if (logger.isTraceEnabled()) {
            logger.trace("> {} to {}, headers = {}, cookies = {}, content = {}", params.method(), uri,
                    HttpUtil.logToString(request.getHeaders()), request.getCookies(), params.requestContent());
        }

        request.send(responseListener);
    }

    /**
     * The {@link Builder} is used to build HTTP requests to remote servers, including managed cookies
     */
    public class Builder {
        private final HttpMethod httpMethod;
        private final URI uri;
        private final Map<String, String> headers = new HashMap<>();
        private boolean retry = true;
        private boolean redirect = true;
        private boolean isJson = false;
        private @Nullable String body;

        private Builder(HttpMethod httpMethod, String uriString) {
            this.httpMethod = httpMethod;
            this.uri = URI.create(uriString);
        }

        /**
         * Adds a single header to this request
         *
         * @param field the field name
         * @param value the value
         * @return the request builder
         */
        public Builder withHeader(String field, String value) {
            this.headers.put(field, value);
            return this;
        }

        /**
         * Add multiple headers to this request
         *
         * @param headers a {@link Map} containing the headers
         * @return the request builder
         */
        public Builder withHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * Set the retry flag
         *
         * @param retry {@code true} allows up to 3 retries (default), {@code false} fails immediately
         * @return the request builder
         */
        public Builder retry(boolean retry) {
            this.retry = retry;
            return this;
        }

        /**
         * Set the redirect flag
         *
         * @param redirect {@code true} allows up to 30 redirects (default), {@code false} fails on
         *            redirection
         * @return the request builder
         */
        public Builder redirect(boolean redirect) {
            this.redirect = redirect;
            return this;
        }

        public Builder withContent(@Nullable Object content) {
            if (content == null || content instanceof String) {
                this.body = (String) content;
                this.isJson = false;
            } else if (content instanceof JsonObject) {
                this.body = content.toString();
                this.isJson = true;
            } else {
                this.body = gson.toJson(content);
                this.isJson = true;
            }
            return this;
        }

        /**
         * Override the autodetected type
         * <p />
         * This needs to be called AFTER the content has been set
         *
         * @param isJson if the request content should be considered as JSON
         * @return the request builder
         */
        public Builder withJson(boolean isJson) {
            this.isJson = isJson;
            return this;
        }

        public CompletableFuture<HttpResponse> send() {
            RequestParams params = new RequestParams(httpMethod, body, isJson, headers);
            CompletableFuture<HttpResponse> httpResponse = new CompletableFuture<>();
            HttpResponseListener responseListener = new HttpResponseListener(httpResponse, params, redirect,
                    retry ? RETRY : EXCEPTION);
            createRequest(uri, params, responseListener);
            return httpResponse;
        }

        public <T> CompletableFuture<T> send(Class<T> returnType) {
            return send(TypeToken.get(returnType));
        }

        @SuppressWarnings("unchecked")
        public <T> CompletableFuture<T> send(TypeToken<T> returnType) {
            return send().thenApply(response -> {
                if (returnType.getRawType().equals(String.class)) {
                    return (T) response.content;
                }
                if (returnType.getRawType().equals(HttpRequestBuilder.HttpResponse.class)) {
                    return (T) response;
                }
                try {
                    String contentType = response.headers.get(CONTENT_TYPE);
                    if (contentType == null) {
                        throw new JsonParseException("Response Content-Type header is missing");
                    }
                    T returnValue = gson.fromJson(response.content(), returnType);
                    // gson.fromJson is non-null if json is non-null and not empty
                    if (returnValue == null) {
                        if (!contentType.startsWith(MediaType.APPLICATION_JSON)) {
                            throw new JsonParseException("Response Content-Type is not JSON: " + contentType);
                        }
                        throw new JsonParseException("Empty result");
                    }
                    return returnValue;
                } catch (JsonParseException e) {
                    logger.warn("Parsing json failed, exception: {}", e.getMessage());
                    throw e;
                }
            });
        }

        public HttpResponse syncSend() throws ConnectionException {
            return syncSend(HttpResponse.class);
        }

        public <T> T syncSend(Class<T> returnType) throws ConnectionException {
            return syncSend(TypeToken.get(returnType));
        }

        public <T> T syncSend(TypeToken<T> returnType) throws ConnectionException {
            try {
                logger.debug("> {}: {} (available: {})", httpMethod, uri, semaphore.availablePermits());
                semaphore.acquire();
                return send(returnType).get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof ConnectionException connectionException) {
                    throw connectionException;
                } else {
                    throw new ConnectionException("Request failed", e);
                }
            } catch (RuntimeException | InterruptedException e) {
                logger.debug("Request to uri '{}' failed:", uri, e);
                throw new ConnectionException("Request failed", e);
            } finally {
                semaphore.release();
            }
        }
    }

    private class HttpResponseListener extends BufferingResponseListener {
        private static final int MAX_REDIRECTS = 30;
        private static final int MAX_RETRIES = 3;

        private final Logger logger = LoggerFactory.getLogger(HttpResponseListener.class);
        private final CompletableFuture<HttpResponse> httpResponse;
        private final RequestParams params;
        private final boolean autoRedirect;
        private final FailMode failMode;
        private int redirectCounter = MAX_REDIRECTS;
        private int retryCounter = MAX_RETRIES;

        public HttpResponseListener(CompletableFuture<HttpResponse> httpResponse, RequestParams requestParams,
                boolean autoRedirect, FailMode failMode) {
            this.httpResponse = httpResponse;
            this.params = requestParams;
            this.autoRedirect = autoRedirect;
            this.failMode = failMode;
        }

        private HttpResponseListener(HttpResponseListener other, int retryCounter, int redirectCounter) {
            this.httpResponse = other.httpResponse;
            this.params = other.params;
            this.autoRedirect = other.autoRedirect;
            this.failMode = other.failMode;
            this.retryCounter = retryCounter;
            this.redirectCounter = redirectCounter;
        }

        @Override
        public void onComplete(Result result) {
            Response response = result.getResponse();
            URI requestUri = response.getRequest().getURI();
            int responseStatus = response.getStatus();
            HttpFields headers = Objects.requireNonNull(response.getHeaders());
            String content = Objects.requireNonNullElse(getContentAsString(), "");

            if (logger.isTraceEnabled()) {
                logger.trace("< {} to {}: {}, headers = {}, content = {}", params.method(), requestUri, responseStatus,
                        HttpUtil.logToString(response.getHeaders()), content);
            } else {
                logger.debug("< {} to {}: {}", params.method, requestUri, responseStatus);
            }

            headers.getFields(SET_COOKIE).forEach(cookieHeader -> HttpCookie.parse(cookieHeader.getValue())
                    .forEach(cookie -> cookieManager.getCookieStore().add(requestUri, cookie)));

            String location = headers.get(LOCATION);
            if (location != null && !location.isBlank()) {
                location = requestUri.resolve(location).toString();
                if (location.toLowerCase().startsWith("http://")) {
                    // always use https
                    location = "https://" + location.substring(7);
                    logger.debug("Redirect corrected to {}", location);
                }
            }

            if (HttpStatus.isSuccess(responseStatus)) {
                httpResponse.complete(new HttpResponse(responseStatus, headers, content));
            } else if (isRedirection(responseStatus) && location != null) {
                logger.debug("Redirected to {}", location);
                if (!autoRedirect) {
                    httpResponse.complete(new HttpResponse(responseStatus, headers, content));
                }
                if (redirectCounter == 0) {
                    httpResponse.completeExceptionally(new ConnectionException("Too many redirects"));
                }
                createRequest(URI.create(location), params,
                        new HttpResponseListener(this, retryCounter, redirectCounter - 1));
            } else if (responseStatus == BAD_REQUEST_400
                    && "QUEUE_EXPIRED".equals(response.getHeaders().get("x-amzn-error"))) {
                // handle queue expired
                httpResponse.completeExceptionally(new ConnectionException("Queue expired"));
            } else {
                if (failMode == EXCEPTION || retryCounter == 0) {
                    if (responseStatus == 0) {
                        httpResponse.completeExceptionally(new ConnectionException("Request aborted."));
                    }
                    httpResponse.completeExceptionally(new ConnectionException(
                            requestUri + " failed with code " + responseStatus + ": " + response.getReason()));
                } else if (failMode == NORMAL) {
                    httpResponse.complete(new HttpResponse(responseStatus, headers, content));
                } else {
                    logger.debug("Retrying call to {}", requestUri);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        httpResponse.completeExceptionally(new ConnectionException("Interrupted", e));
                    }
                    createRequest(requestUri, params,
                            new HttpResponseListener(this, retryCounter - 1, redirectCounter));
                }
            }
        }
    }

    private record RequestParams(HttpMethod method, @Nullable String requestContent, boolean json,
            Map<String, String> customHeaders) {
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RequestParams that)) {
                return false;
            }
            return json == that.json && method == that.method && Objects.equals(requestContent, that.requestContent)
                    && Objects.equals(customHeaders, that.customHeaders);
        }

        public int hashCode() {
            return Objects.hash(method, requestContent, json, customHeaders);
        }
    }

    public record HttpResponse(int statusCode, HttpFields headers, String content) {
        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HttpResponse response = (HttpResponse) o;
            return statusCode == response.statusCode && Objects.equals(headers, response.headers)
                    && Objects.equals(content, response.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(statusCode, headers, content);
        }
    }

    public enum FailMode {
        NORMAL,
        EXCEPTION,
        RETRY
    }
}
