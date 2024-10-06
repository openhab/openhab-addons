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
package org.openhab.binding.siemenshvac.internal.network;

import java.io.EOFException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Request.BeginListener;
import org.eclipse.jetty.client.api.Request.QueuedListener;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Response.ContentListener;
import org.eclipse.jetty.client.api.Response.FailureListener;
import org.eclipse.jetty.client.api.Response.SuccessListener;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacRequestListener extends BufferingResponseListener
        implements SuccessListener, FailureListener, ContentListener, CompleteListener, QueuedListener, BeginListener {

    public enum ErrorSource {
        ErrorBridge,
        ErrorThings
    }

    private static int onSuccessCount = 0;
    private static int onBeginCount = 0;
    private static int onQueuedCount = 0;
    private static int onCompleteCount = 0;
    private static int onFailureCount = 0;

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacRequestListener.class);

    private SiemensHvacRequestHandler requestHandler;
    private SiemensHvacConnector hvacConnector;

    /**
     * Callback to execute on complete response
     */
    private final SiemensHvacCallback callback;

    public static int getQueuedCount() {
        return onQueuedCount;
    }

    public static int getStartedCount() {
        return onBeginCount;
    }

    public static int getCompleteCount() {
        return onCompleteCount;
    }

    public static int getFailureCount() {
        return onFailureCount;
    }

    public static int getSuccessCount() {
        return onSuccessCount;
    }

    /**
     * Constructor
     *
     * @param callback Callback which execute method has to be called.
     */
    public SiemensHvacRequestListener(SiemensHvacRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        this.hvacConnector = requestHandler.getHvacConnector();
        this.callback = requestHandler.getCallback();
    }

    @Override
    public void onSuccess(@Nullable Response response) {
        onSuccessCount++;
        requestHandler.setResponse(response);

        if (response != null) {
            logger.debug("{} response: {}", response.getRequest().getURI(), response.getStatus());
        }
    }

    @Override
    public void onFailure(@Nullable Response response, @Nullable Throwable failure) {
        onFailureCount++;
        requestHandler.setResponse(response);

        if (response != null && failure != null) {
            Throwable cause = failure.getCause();
            if (cause == null) {
                cause = failure;
            }

            String msg = cause.getLocalizedMessage();

            if (cause instanceof ConnectException e) {
                logger.debug("ConnectException during request: {} {}", response.getRequest().getURI(), msg, e);
            } else if (cause instanceof SocketException e) {
                logger.debug("SocketException during request: {} {}", response.getRequest().getURI(), msg, e);
            } else if (cause instanceof SocketTimeoutException e) {
                logger.debug("SocketTimeoutException during request: {} {}", response.getRequest().getURI(), msg, e);
            } else if (cause instanceof EOFException e) {
                logger.debug("EOFException during request: {} {}", response.getRequest().getURI(), msg, e);
            } else if (cause instanceof TimeoutException e) {
                logger.debug("TimeoutException during request: {} {}", response.getRequest().getURI(), msg, e);
            } else {
                logger.debug("Response failed: {}  {}", response.getRequest().getURI(), msg, failure);
            }
        }
    }

    @Override
    public void onQueued(@Nullable Request request) {
        onQueuedCount++;
        requestHandler.setRequest(request);
    }

    @Override
    public void onBegin(@Nullable Request request) {
        onBeginCount++;
        requestHandler.setRequest(request);
    }

    @Override
    public void onComplete(@Nullable Result result) {
        onCompleteCount++;
        requestHandler.setResult(result);

        if (result == null) {
            return;
        }

        try {
            String content = getContentAsString();
            logger.trace("response complete: {}", content);
            boolean mayRetry = true;

            if (result.getResponse().getStatus() != 200) {
                logger.debug("Error requesting gateway, non success code: {}", result.getResponse().getStatus());
                hvacConnector.onError(result.getRequest(), requestHandler, ErrorSource.ErrorBridge, mayRetry);
                return;
            }

            if (content != null) {
                if (content.indexOf("<!DOCTYPE html>") >= 0) {
                    hvacConnector.onComplete(result.getRequest(), requestHandler);
                    callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(), content);
                } else {
                    JsonObject resultObj = null;
                    try {
                        Gson gson = hvacConnector.getGson();
                        resultObj = gson.fromJson(content, JsonObject.class);
                    } catch (JsonSyntaxException ex) {
                        logger.debug("error(1): {}", ex.toString());
                    }

                    if (resultObj != null && resultObj.has("Result")) {
                        JsonObject subResultObj = resultObj.getAsJsonObject("Result");

                        if (subResultObj.has("Success")) {
                            boolean resultVal = subResultObj.get("Success").getAsBoolean();
                            JsonObject error = subResultObj.getAsJsonObject("Error");
                            String errorMsg = "";
                            if (error != null) {
                                errorMsg = error.get("Txt").getAsString();
                            }

                            if (errorMsg.indexOf("session") >= 0) {
                                String query = result.getRequest().getURI().getQuery();
                                String sessionId = SiemensHvacConnectorImpl.extractSessionId(query);

                                hvacConnector.resetSessionId(sessionId, false);
                                hvacConnector.resetSessionId(sessionId, true);
                                mayRetry = false;
                            }

                            if (resultVal) {
                                hvacConnector.onComplete(result.getRequest(), requestHandler);
                                callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(),
                                        resultObj);

                                return;
                            } else if (("datatype not supported").equals(errorMsg)) {
                                hvacConnector.onComplete(result.getRequest(), requestHandler);
                                callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(),
                                        resultObj);
                                return;
                            } else if (("read failed").equals(errorMsg)) {
                                logger.debug("error(2): {}", subResultObj);
                                hvacConnector.onError(result.getRequest(), requestHandler, ErrorSource.ErrorThings,
                                        mayRetry);
                            } else {
                                logger.debug("error(3): {}", subResultObj);
                                hvacConnector.onError(result.getRequest(), requestHandler, ErrorSource.ErrorBridge,
                                        mayRetry);
                                return;
                            }
                        } else {
                            logger.debug("error(4): invalid response from gateway, missing subResultObj:Success entry");
                            hvacConnector.onError(result.getRequest(), requestHandler, ErrorSource.ErrorBridge,
                                    mayRetry);
                            return;
                        }
                    } else {
                        logger.debug("error(5): invalid response from gateway, missing Result entry");
                        hvacConnector.onError(result.getRequest(), requestHandler, ErrorSource.ErrorBridge, mayRetry);
                        return;
                    }
                }
            } else {
                logger.debug("error: content == null");
                hvacConnector.onError(result.getRequest(), requestHandler, ErrorSource.ErrorBridge, mayRetry);
                return;
            }
        } catch (SiemensHvacException ex) {
            logger.debug("An error occurred", ex);
        }
    }
}
