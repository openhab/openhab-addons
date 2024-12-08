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

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartthingsNetworkConnectorImpl implements SmartthingsNetworkConnector {

    private static final Logger logger = LoggerFactory.getLogger(SmartthingsNetworkConnectorImpl.class);

    private final static @NotNull Gson gson;
    // private final static @NotNull Gson gsonWithAdpter;

    private @Nullable String sessionId = null;
    private @Nullable String sessionIdHttp = null;
    private String baseUrl = "";
    private String userName = "";
    private String userPassword = "";

    protected final HttpClientFactory httpClientFactory;

    protected HttpClient httpClient;

    private static int startedRequest = 0;
    private static int completedRequest = 0;
    private Lock lockObj = new ReentrantLock();

    static {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.setPrettyPrinting().create();

        /*
         * RuntimeTypeAdapterFactory<networkMetadata> adapter = RuntimeTypeAdapterFactory.of(networkMetadata.class);
         * adapter.registerSubtype(networkMetadataMenu.class);
         * adapter.registerSubtype(networkMetadataDataPoint.class);
         *
         * gsonWithAdpter = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(adapter).create();
         */

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
        this.httpClient.setRemoveIdleDestinations(true);
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

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
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
            @Nullable SmartthingsNetworkCallback callback) throws Exception {
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
            throw new Exception(
                    "network:Exception by executing request: " + request.getQuery() + " ; " + e.getLocalizedMessage());
        }
        return response;
    }

    private void initConfig() throws Exception {
    }

    public @Nullable String DoBasicRequest(String uri, String accessToken, @Nullable String data, HttpMethod method)
            throws Exception {
        return DoBasicRequest(uri, null, accessToken, data, method);
    }

    public @Nullable String DoBasicRequestAsync(String uri, @Nullable SmartthingsNetworkCallback callback,
            String accessToken, @Nullable String data, HttpMethod method) throws Exception {
        return DoBasicRequest(uri, callback, accessToken, data, method);
    }

    public @Nullable String DoBasicRequest(String uri, @Nullable SmartthingsNetworkCallback callback,
            String accessToken, @Nullable String data, HttpMethod method) throws Exception {

        try {
            logger.debug("Execute request: {}", uri);
            Request request = httpClient.newRequest(uri).header("Authorization", "Bearer " + accessToken)
                    .method(method);
            if (method == HttpMethod.POST) {
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

                    ErrorObject err = gson.fromJson(result, ErrorObject.class);
                    if (err != null) {
                        throw new SmartthingsException("Error occured during request:", err);
                    } else {
                        throw new SmartthingsException("Error occured during request");
                    }
                } else {
                    throw new SmartthingsException("Unexepected return code : " + statusCode);
                }
            }
        } catch (Exception ex) {
            logger.error("network:DoRequest:Exception by executing Request: {} ; {} ", uri, ex.getLocalizedMessage());
        } finally {
        }

        return null;
    }

    @Override
    public <T> T DoRequest(Class<T> resultClass, String req, @Nullable SmartthingsNetworkCallback callback,
            String accessToken, @Nullable String data, HttpMethod method) {
        try {
            String response = DoBasicRequest(req, callback, accessToken, data, method);

            if (response != null) {
                T resultObj = getGson().fromJson(response, resultClass);
                return resultObj;

            }
        } catch (Exception e) {
            logger.error("network:DoRequest:Exception by executing jsonRequest: {} ; {} ", req,
                    e.getLocalizedMessage());
        }

        return null;
    }

    @Override
    public void WaitAllPendingRequest() {
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
    public void WaitNoNewRequest() {
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
        return gson;
    }

    /*
     * public static Gson getGsonWithAdapter() {
     * return gsonWithAdpter;
     * }
     */
}
