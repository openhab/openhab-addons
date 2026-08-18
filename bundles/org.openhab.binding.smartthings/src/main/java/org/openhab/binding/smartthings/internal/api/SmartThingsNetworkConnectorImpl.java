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
package org.openhab.binding.smartthings.internal.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.smartthings.internal.dto.ErrorObject;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartThingsNetworkConnectorImpl implements SmartThingsNetworkConnector {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsNetworkConnectorImpl.class);

    private final Gson gSon;

    protected HttpClient httpClient;

    private static int startedRequest = 0;
    private static int completedRequest = 0;
    private Lock lockObj = new ReentrantLock();

    public SmartThingsNetworkConnectorImpl(HttpClientFactory httpClientFactory) {
        GsonBuilder builder = new GsonBuilder();
        gSon = builder.setPrettyPrinting().create();

        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public void onComplete(@Nullable Request request) {
        lockObj.lock();
        try {
            logger.debug("OnComplete");
            completedRequest++;
        } finally {
            lockObj.unlock();
        }
    }

    @Override
    public <T> void onError(Class<T> resultClass, @Nullable Request request,
            @Nullable SmartThingsNetworkCallback<T> cb) {
        lockObj.lock();
        try {
            logger.debug("OnError");
            completedRequest++;
        } finally {
            lockObj.unlock();
        }

        if (cb == null || request == null) {
            return;
        }

        try {
            final Request retryRequest = httpClient.newRequest(request.getURI());
            request.method(HttpMethod.GET);

            if (retryRequest != null) {
                executeRequest(resultClass, retryRequest, cb);
            }
        } catch (Exception ex) {
            logger.error("exception: {}", ex.toString(), ex);
        }
    }

    private <T> @Nullable ContentResponse executeRequest(Class<T> resultClass, final Request request,
            @Nullable SmartThingsNetworkCallback<T> callback) throws SmartThingsException {
        request.timeout(240, TimeUnit.SECONDS);

        ContentResponse response = null;

        @Nullable
        SmartThingsNetworkRequestListener<T> requestListener = null;
        if (callback != null) {
            requestListener = new SmartThingsNetworkRequestListener<T>(resultClass, callback, this);
            request.onResponseSuccess(requestListener);
            request.onResponseFailure(requestListener);
        }

        try {
            if (requestListener != null) {
                lockObj.lock();
                try {
                    startedRequest++;
                    logger.trace("StartedRequest : {}", startedRequest - completedRequest);
                } finally {
                    lockObj.unlock();
                }

                request.send(requestListener);
            } else {
                response = request.send();
                logger.trace("Request completed with status {}", response.getStatus());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw createNetworkException(request, e);
        } catch (TimeoutException | ExecutionException e) {
            throw createNetworkException(request, e);
        }
        return response;
    }

    private static SmartThingsException createNetworkException(Request request, Exception e) {
        return new SmartThingsException("network: Exception while executing request " + describeRequest(request) + ": "
                + SmartThingsException.getRootCauseMessage(e), e, true);
    }

    static String describeRequest(Request request) {
        return request.getMethod() + " " + request.getURI();
    }

    public <T> @Nullable String doBasicRequest(Class<T> resultClass, String uri, String accessToken,
            @Nullable String data, HttpMethod method) throws Exception {
        return doBasicRequest(resultClass, uri, null, accessToken, data, method);
    }

    public <T> @Nullable String doBasicRequestAsync(Class<T> resultClass, String uri,
            @Nullable SmartThingsNetworkCallback<T> callback, String accessToken, @Nullable String data,
            HttpMethod method) throws Exception {
        return doBasicRequest(resultClass, uri, callback, accessToken, data, method);
    }

    @Override
    public <T> @Nullable String doBasicRequest(Class<T> resultClass, String uri,
            @Nullable SmartThingsNetworkCallback<T> callback, String accessToken, @Nullable String data,
            HttpMethod method) throws SmartThingsException {
        logger.debug("Execute request: {}", uri);
        Request request = httpClient.newRequest(uri).method(method);
        if (!"".equals(accessToken)) {
            request = request.header("Authorization", "Bearer " + accessToken);
        }
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            request = request.content(new StringContentProvider(data), "application/json");
        }

        if (uri.indexOf("activate") > 0) {
            request = request.header("Accept", "text/event-stream");
            request = request.header("Cache-Control", "no-cache");
        }

        ContentResponse response = executeRequest(resultClass, request, callback);
        if (callback == null && response != null) {
            int statusCode = response.getStatus();

            if (statusCode == HttpStatus.OK_200) {
                String result = response.getContentAsString();
                return result;
            } else if (statusCode == HttpStatus.UNPROCESSABLE_ENTITY_422) {
                String result = response.getContentAsString();

                throw createResponseException("Error occurred during request:", result);
            } else if (statusCode == HttpStatus.TOO_MANY_REQUESTS_429) {
                String result = response.getContentAsString();

                throw createResponseException("Too many requests", result);
            } else {
                throw createResponseException("Unexpected return code: " + statusCode, response.getContentAsString());
            }
        }
        return null;
    }

    private SmartThingsException createResponseException(String message, @Nullable String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return new SmartThingsException(message);
        }

        try {
            ErrorObject err = gSon.fromJson(responseBody, ErrorObject.class);
            if (err != null && (err.requestId != null || err.error != null)) {
                return new SmartThingsException(message, err);
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Unable to parse SmartThings error response.", e);
        }

        return new SmartThingsException(message + ": " + responseBody);
    }

    @Override
    public <T> @Nullable T doRequest(Class<T> resultClass, String req, @Nullable SmartThingsNetworkCallback<T> callback,
            String accessToken, @Nullable String data, HttpMethod method) throws SmartThingsException {
        String response = doBasicRequest(resultClass, req, callback, accessToken, data, method);

        if (response != null) {
            if (resultClass.isArray()) {
                JsonObject obj = gSon.fromJson(response, JsonObject.class);
                if (obj != null && obj.has("items")) {
                    final @Nullable T resultObj = gSon.fromJson(obj.get("items"), resultClass);
                    if (resultObj != null) {
                        return resultObj;
                    }
                } else {
                    throw new SmartThingsException(
                            "Requesting a Array result object, but data does not contains array definition");
                }
            } else {
                final @Nullable T resultObj = gSon.fromJson(response, resultClass);
                if (resultObj != null) {
                    return resultObj;
                }
            }
        }
        return null;
    }

    @Override
    public void waitAllPendingRequest() {
        logger.debug("WaitAllPendingRequest:start");
        try {
            Thread.sleep(1000);
            boolean allRequestDone = false;

            while (!allRequestDone) {
                int idx = 0;

                allRequestDone = true;
                while (idx < 5 && allRequestDone) {
                    logger.debug("WaitAllPendingRequest:waitAllRequestDone {} : {} ({}/{})", idx,
                            (startedRequest - completedRequest), startedRequest, completedRequest);
                    if (startedRequest != completedRequest) {
                        allRequestDone = false;
                    }
                    Thread.sleep(200);
                    idx++;
                }
            }
        } catch (InterruptedException ex) {
            logger.debug("WaitAllPendingRequest:interrupted in WaitAllRequest");
        }

        logger.debug("WaitAllPendingRequest:end WaitAllPendingRequest");
    }

    @Override
    public void waitNoNewRequest() {
        logger.debug("WaitNoNewRequest:start");
        try {
            int lastRequest = startedRequest;
            Thread.sleep(5000);
            while (lastRequest != startedRequest) {
                logger.debug("waitNoNewRequest  {}/{})", startedRequest, lastRequest);
                Thread.sleep(5000);
                lastRequest = startedRequest;
            }
        } catch (InterruptedException ex) {
            logger.debug("WaitAllPendingRequest:interrupted in WaitAllRequest");
        }

        logger.debug("WaitNoNewRequest:end WaitAllStartingRequest");
    }

    @Override
    public Gson getGson() {
        return gSon;
    }
}
