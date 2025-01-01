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
package org.openhab.binding.smartthings.internal.api;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.smartthings.internal.dto.ErrorObject;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartthingsNetworkConnectorImpl implements SmartthingsNetworkConnector {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsNetworkConnectorImpl.class);

    private static final @NotNull Gson Gson;

    protected final HttpClientFactory httpClientFactory;

    protected HttpClient httpClient;

    private static int startedRequest = 0;
    private static int completedRequest = 0;
    private Lock lockObj = new ReentrantLock();

    static {
        GsonBuilder builder = new GsonBuilder();
        Gson = builder.setPrettyPrinting().create();
    }

    @Activate
    public SmartthingsNetworkConnectorImpl(HttpClientFactory httpClientFactory, OAuthClientService oAuthClientService) {
        this.httpClientFactory = httpClientFactory;

        SslContextFactory ctxFactory = new SslContextFactory.Client(true);
        ctxFactory.setRenegotiationAllowed(false);
        ctxFactory.setEnableCRLDP(false);
        ctxFactory.setEnableOCSP(false);
        ctxFactory.setTrustAll(true);
        ctxFactory.setValidateCerts(false);
        ctxFactory.setValidatePeerCerts(false);
        ctxFactory.setEndpointIdentificationAlgorithm(null);

        this.httpClient = new HttpClient(ctxFactory);
        this.httpClient.setMaxConnectionsPerDestination(10);
        this.httpClient.setMaxRequestsQueuedPerDestination(1000);
        this.httpClient.setConnectTimeout(10000);
        this.httpClient.setFollowRedirects(false);

        try {
            this.httpClient.start();
        } catch (Exception e) {
            logger.error("Failed to start http client: {}", e.getMessage());
        }
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
    public void onError(@Nullable Request request, @Nullable SmartthingsNetworkCallback cb) throws Exception {
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
                executeRequest(retryRequest, cb);
            }
        } catch (Exception ex) {
            logger.error("exception");
            throw ex;
        }
    }

    private @Nullable ContentResponse executeRequest(final Request request,
            @Nullable SmartthingsNetworkCallback callback) throws SmartthingsException {
        request.timeout(240, TimeUnit.SECONDS);

        ContentResponse response = null;

        @Nullable
        SmartthingsNetworkRequestListener requestListener = null;
        if (callback != null) {
            requestListener = new SmartthingsNetworkRequestListener(callback, this);
            request.onResponseSuccess(requestListener);
            request.onResponseFailure(requestListener);
        }

        try {
            if (requestListener != null) {
                lockObj.lock();
                try {
                    startedRequest++;
                    logger.info("StartedRequest : {}", startedRequest - completedRequest);
                } finally {
                    lockObj.unlock();
                }

                request.send(requestListener);
            } else {
                response = request.send();
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new SmartthingsException(
                    "network:Exception by executing request: " + request.getQuery() + " ; " + e.getLocalizedMessage());
        }
        return response;
    }

    public @Nullable String doBasicRequest(String uri, String accessToken, @Nullable String data, HttpMethod method)
            throws Exception {
        return doBasicRequest(uri, null, accessToken, data, method);
    }

    public @Nullable String doBasicRequestAsync(String uri, @Nullable SmartthingsNetworkCallback callback,
            String accessToken, @Nullable String data, HttpMethod method) throws Exception {
        return doBasicRequest(uri, callback, accessToken, data, method);
    }

    @Override
    public @Nullable String doBasicRequest(String uri, @Nullable SmartthingsNetworkCallback callback,
            String accessToken, @Nullable String data, HttpMethod method) throws SmartthingsException {
        logger.debug("Execute request: {}", uri);
        Request request = httpClient.newRequest(uri).method(method);
        if (!"".equals(accessToken)) {
            request = request.header("Authorization", "Bearer " + accessToken);
        }
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            request = request.content(new StringContentProvider(data), "application/json");
        }

        ContentResponse response = executeRequest(request, callback);
        if (callback == null && response != null) {
            int statusCode = response.getStatus();

            if (statusCode == HttpStatus.OK_200) {
                String result = response.getContentAsString();
                return result;
            } else if (statusCode == HttpStatus.UNPROCESSABLE_ENTITY_422) {
                String result = response.getContentAsString();

                ErrorObject err = Gson.fromJson(result, ErrorObject.class);
                throw new SmartthingsException("Error occured during request:", Objects.requireNonNull(err));
            } else if (statusCode == HttpStatus.TOO_MANY_REQUESTS_429) {
                String result = response.getContentAsString();

                ErrorObject err = Gson.fromJson(result, ErrorObject.class);
                throw new SmartthingsException("Two many request", Objects.requireNonNull(err));
            } else {
                throw new SmartthingsException("Unexepected return code : " + statusCode);
            }
        }
        return null;
    }

    @Override
    public <T> T doRequest(Class<T> resultClass, String req, @Nullable SmartthingsNetworkCallback callback,
            String accessToken, @Nullable String data, HttpMethod method) throws SmartthingsException {
        String response = doBasicRequest(req, callback, accessToken, data, method);

        if (response != null) {
            if (resultClass.isArray()) {
                JsonObject obj = getGson().fromJson(response, JsonObject.class);
                if (obj != null && obj.has("items")) {
                    T resultObj = getGson().fromJson(obj.get("items"), resultClass);
                    return resultObj;
                } else {
                    throw new SmartthingsException(
                            "Requesting a Array result object, but data does not contains array definition");
                }
            } else {
                T resultObj = getGson().fromJson(response, resultClass);
                return resultObj;
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
                    Thread.sleep(1000);
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

    public static Gson getGson() {
        return Gson;
    }

    /*
     * public static Gson getGsonWithAdapter() {
     * return gsonWithAdpter;
     * }
     */
}
