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
package org.openhab.binding.siemenshvac.internal.network;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
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
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.siemenshvac.internal.Metadata.RuntimeTypeAdapterFactory;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadata;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataDataPoint;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataMenu;
import org.openhab.binding.siemenshvac.internal.handler.SiemensHvacBridgeBaseThingHandler;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.types.Type;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class SiemensHvacConnectorImpl implements SiemensHvacConnector {

    private static final Logger logger = LoggerFactory.getLogger(SiemensHvacConnectorImpl.class);

    private @Nullable String sessionId = null;
    private String baseUrl = "";
    private String userName = "";
    private String userPassword = "";
    @SuppressWarnings("unused")
    private @Nullable Date lastUpdate;

    protected final HttpClientFactory httpClientFactory;

    protected HttpClient httpClient;
    protected HttpClient httpClientInsecure;

    private static int startedRequest = 0;
    private static int completedRequest = 0;
    private Lock lockObj = new ReentrantLock();

    private Map<String, Type> updateCommand;

    private @Nullable SiemensHvacBridgeBaseThingHandler hvacBridgeBaseThingHandler;

    @Activate
    public SiemensHvacConnectorImpl(@Reference HttpClientFactory httpClientFactory) {
        this.updateCommand = new Hashtable<String, Type>();
        this.httpClientFactory = httpClientFactory;

        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.httpClient.setMaxConnectionsPerDestination(15);
        this.httpClientInsecure = new HttpClient(new SslContextFactory.Client(true));
        this.httpClientInsecure.setRemoveIdleDestinations(true);
        this.httpClientInsecure.setMaxConnectionsPerDestination(15);
        try {
            this.httpClientInsecure.start();
        } catch (Exception e) {
            logger.warn("Failed to start insecure http client: {}", e.getMessage());
        }
    }

    @Override
    public void setSiemensHvacBridgeBaseThingHandler(
            @Nullable SiemensHvacBridgeBaseThingHandler hvacBridgeBaseThingHandler) {
        this.hvacBridgeBaseThingHandler = hvacBridgeBaseThingHandler;
    }

    public void unsetSiemensHvacBridgeBaseThingHandler(SiemensHvacBridgeBaseThingHandler hvacBridgeBaseThingHandler) {
        this.hvacBridgeBaseThingHandler = null;
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
            completedRequest++;
        } finally {
            lockObj.unlock();
        }
    }

    @Override
    public void onError(@Nullable Request request) {
        lockObj.lock();
        try {
            completedRequest++;
        } finally {
            lockObj.unlock();
        }
    }

    private @Nullable ContentResponse executeRequest(final Request request, @Nullable SiemensHvacCallback callback)
            throws Exception {
        request.timeout(60, TimeUnit.SECONDS);

        ContentResponse response = null;

        @Nullable
        SiemensHvacRequestListener requestListener = null;
        if (callback != null) {
            requestListener = new SiemensHvacRequestListener(callback, this);
            request.onResponseSuccess(requestListener);
            request.onResponseFailure(requestListener);
        }

        try {
            if (requestListener != null) {
                lockObj.lock();
                try {
                    startedRequest++;
                } finally {
                    lockObj.unlock();
                }

                request.send(requestListener);
            } else {
                response = request.send();
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new SiemensHvacException("siemensHvac:Exception by executing request: " + request.getQuery() + " ; "
                    + e.getLocalizedMessage());
        }
        return response;
    }

    private void _initConfig() throws Exception {

        Configuration config = null;
        SiemensHvacBridgeBaseThingHandler lcHvacBridgeBaseThingHandler = hvacBridgeBaseThingHandler;

        if (lcHvacBridgeBaseThingHandler != null) {
            Bridge bridge = lcHvacBridgeBaseThingHandler.getThing();
            config = bridge.getConfiguration();
        } else {
            throw new SiemensHvacException(
                    "siemensHvac:Exception unable to get config because hvacBridgeBaseThingHandler is null");
        }

        if (config.containsKey("baseUrl")) {
            baseUrl = (String) config.get("baseUrl");
        }
        if (config.containsKey("userName")) {
            userName = (String) config.get("userName");
        }
        if (config.containsKey("userPassword")) {
            userPassword = (String) config.get("userPassword");
        }
    }

    private void _doAuth() throws Exception {
        logger.debug("siemensHvac:doAuth()");

        _initConfig();
        String baseUri = baseUrl;
        String uri = "api/auth/login.json?user=" + userName + "&pwd=" + userPassword;
        final Request request = httpClientInsecure.newRequest(baseUri + uri);
        request.method(HttpMethod.GET);

        logger.debug("siemensHvac:doAuth:connect()");

        try {
            ContentResponse response = executeRequest(request, null);
            if (response != null) {
                int statusCode = response.getStatus();

                if (statusCode == HttpStatus.OK_200) {
                    String result = response.getContentAsString();

                    if (result != null) {
                        JsonObject resultObj = getGson().fromJson(result, JsonObject.class);

                        if (resultObj != null && resultObj.has("Result")) {
                            JsonElement resultVal = resultObj.get("Result");
                            JsonObject resultObj2 = resultVal.getAsJsonObject();

                            if (resultObj2.has("Success")) {
                                boolean successVal = resultObj2.get("Success").getAsBoolean();

                                if (successVal) {

                                    if (resultObj.has("SessionId")) {
                                        sessionId = resultObj.get("SessionId").getAsString();
                                        logger.debug("Have new SessionId : {} ", sessionId);
                                    }

                                }

                            }
                        }

                        logger.debug("siemensHvac:doAuth:decodeResponse:()");

                    }

                    if (sessionId == null) {
                        logger.debug("Session request auth was unsucessfull in _doAuth()");
                    }
                }
            }

            logger.debug("siemensHvac:doAuth:connect()");

        } catch (Exception ex) {
            logger.debug("siemensHvac:doAuth:error() {}", ex.getLocalizedMessage());
        } finally {
        }
    }

    public @Nullable String DoBasicRequest(String uri) throws Exception {
        return DoBasicRequest(uri, null);
    }

    public @Nullable String DoBasicRequestAsync(String uri, @Nullable SiemensHvacCallback callback) throws Exception {
        return DoBasicRequest(uri, callback);
    }

    public @Nullable String DoBasicRequest(String uri, @Nullable SiemensHvacCallback callback) throws Exception {
        if (sessionId == null) {
            _doAuth();
        }

        try {
            String baseUri = baseUrl;

            String mUri = uri;
            if (!mUri.endsWith("?")) {
                mUri = mUri + "&";
            }
            mUri = mUri + "SessionId=" + sessionId;
            mUri = mUri + "&user=" + userName + "&pwd=" + userPassword;

            final Request request = httpClientInsecure.newRequest(baseUri + mUri);
            request.method(HttpMethod.GET);

            ContentResponse response = executeRequest(request, callback);
            if (callback == null && response != null) {
                int statusCode = response.getStatus();

                if (statusCode == HttpStatus.OK_200) {
                    String result = response.getContentAsString();

                    return result;
                }
            }
        } catch (Exception ex) {
            logger.error("siemensHvac:DoRequest:Exception by executing Request: {} ; {} ", uri,
                    ex.getLocalizedMessage());
        } finally {
        }

        return null;
    }

    @Override
    public @Nullable JsonObject DoRequest(String req, @Nullable SiemensHvacCallback callback) {
        try {
            String response = DoBasicRequest(req, callback);

            if (response != null) {

                JsonObject resultObj = getGson().fromJson(response, JsonObject.class);

                if (resultObj != null && resultObj.has("Result")) {
                    JsonObject subResultObj = resultObj.getAsJsonObject("Result");

                    if (subResultObj.has("Success")) {
                        boolean result = subResultObj.get("Success").getAsBoolean();
                        if (result) {
                            return resultObj;
                        }
                    }

                }

                return null;
            }
        } catch (Exception e) {
            logger.error("siemensHvac:DoRequest:Exception by executing jsonRequest: {} ; {} ", req,
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

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        return gson;
    }

    public static Gson getGsonWithAdapter() {
        RuntimeTypeAdapterFactory<SiemensHvacMetadata> adapter = RuntimeTypeAdapterFactory
                .of(SiemensHvacMetadata.class);
        adapter.registerSubtype(SiemensHvacMetadataMenu.class);
        adapter.registerSubtype(SiemensHvacMetadataDataPoint.class);

        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(adapter).create();
        return gson;
    }

    public void AddDpUpdate(String itemName, Type dp) {
        synchronized (updateCommand) {
            updateCommand.put(itemName, dp);
            lastUpdate = new java.util.Date();
        }
    }
}
