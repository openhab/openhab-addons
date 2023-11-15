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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private static Lock lockObj = new ReentrantLock();
    private static int requestListenerCount = 0;
    private static int onSuccessCount = 0;
    private static int onBeginCount = 0;
    private static int onQueuedCount = 0;
    private static int onCompleteCount = 0;
    private static int onFailureCount = 0;

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacRequestListener.class);

    private SiemensHvacConnector hvacConnector;

    /**
     * Callback to execute on complete response
     */
    private final SiemensHvacCallback callback;

    public static int getQueuedCount() {
        return onQueuedCount;
    }

    public static int getRequestListenerCount() {
        return requestListenerCount;
    }

    public static int getCompleteCount() {
        return onCompleteCount;
    }

    public static int getCurrentRunning() {
        return requestListenerCount - onCompleteCount;
    }

    /**
     * Constructor
     *
     * @param callback Callback which execute method has to be called.
     */
    public SiemensHvacRequestListener(SiemensHvacCallback callback, SiemensHvacConnector hvacConnector) {
        this.callback = callback;
        this.hvacConnector = hvacConnector;

        lockObj.lock();
        try {
            requestListenerCount++;
        } finally {
            lockObj.unlock();
        }
    }

    @Override
    public void onSuccess(@Nullable Response response) {
        lockObj.lock();
        try {
            onSuccessCount++;
        } finally {
            lockObj.unlock();
        }
        if (response != null) {
            logger.debug("{} response: {}", response.getRequest().getURI(), response.getStatus());
        }
    }

    @Override
    public void onFailure(@Nullable Response response, @Nullable Throwable failure) {
        lockObj.lock();
        try {
            onFailureCount++;
        } finally {
            lockObj.unlock();
        }
        if (response != null && failure != null) {
            logger.debug("response failed: {}  {}", response.getRequest().getURI(), failure.getLocalizedMessage(),
                    failure);
        }
    }

    @Override
    public void onQueued(@Nullable Request request) {
        lockObj.lock();
        try {
            onQueuedCount++;
        } finally {
            lockObj.unlock();
        }
    }

    @Override
    public void onBegin(@Nullable Request request) {
        lockObj.lock();
        try {
            onBeginCount++;
        } finally {
            lockObj.unlock();
        }
    }

    public void displayStats() {
        logger.info("DisplayStats :");
        logger.info("     requestListenerCount : {}", requestListenerCount);
        logger.info("     onSuccessCount       : {}", onSuccessCount);
        logger.info("     onBeginCount         : {}", onBeginCount);
        logger.info("     onQueuedCount        : {}", onQueuedCount);
        logger.info("     onCompleteCount      : {}", onCompleteCount);
        logger.info("     onFailureCount       : {}", onFailureCount);
    }

    @Override
    public void onComplete(@Nullable Result result) {
        lockObj.lock();
        try {
            onCompleteCount++;
        } finally {
            lockObj.unlock();
        }

        if (result == null) {
            return;
        }

        try {
            String content = getContentAsString();
            logger.trace("response complete: {}", content);

            if (result.getResponse().getStatus() != 200) {
                logger.debug("Error requesting gateway, non success code: {}", result.getResponse().getStatus());
                hvacConnector.onError(result.getRequest(), callback);
                return;
            }

            if (content != null) {
                if (content.indexOf("<!DOCTYPE html>") >= 0) {
                    hvacConnector.onComplete(result.getRequest());
                    callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(), content);
                } else {
                    JsonObject resultObj = null;
                    try {
                        Gson gson = hvacConnector.getGson();
                        resultObj = gson.fromJson(content, JsonObject.class);
                    } catch (JsonSyntaxException ex) {
                        logger.debug("error: {}", ex.toString());
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

                            if (resultVal) {
                                hvacConnector.onComplete(result.getRequest());
                                callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(),
                                        resultObj);

                                return;
                            } else if (("datatype not supported").equals(errorMsg)) {
                                hvacConnector.onComplete(result.getRequest());
                                callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(),
                                        resultObj);
                                return;
                            } else {
                                logger.debug("error: {}", subResultObj);
                                hvacConnector.onError(result.getRequest(), callback);
                                return;
                            }
                        } else {
                            logger.debug("error: invalid response from gateway, missing subResultObj:Success entry");
                            hvacConnector.onError(result.getRequest(), callback);
                            return;
                        }

                    } else {
                        logger.debug("error: invalid response from gateway, missing Result entry");
                        hvacConnector.onError(result.getRequest(), callback);
                        return;
                    }
                }
            } else {
                logger.debug("error: content == null");
                hvacConnector.onError(result.getRequest(), callback);
                return;
            }
        } catch (Exception ex) {
            logger.debug("An error occurred", ex);
        }
    }
}
