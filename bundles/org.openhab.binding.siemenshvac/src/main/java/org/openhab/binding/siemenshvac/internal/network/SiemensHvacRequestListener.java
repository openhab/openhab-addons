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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

/**
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacRequestListener extends BufferingResponseListener
        implements SuccessListener, FailureListener, ContentListener, CompleteListener {

    private static final Logger logger = LoggerFactory.getLogger(SiemensHvacRequestListener.class);
    private SiemensHvacConnector hvacConnector;

    /**
     * Callback to execute on complete response
     */
    private final SiemensHvacCallback callback;

    /**
     * Constructor
     *
     * @param callback Callback which execute method has to be called.
     */
    public SiemensHvacRequestListener(SiemensHvacCallback callback, SiemensHvacConnector hvacConnector) {
        this.callback = callback;
        this.hvacConnector = hvacConnector;
    }

    @Override
    public void onSuccess(@Nullable Response response) {
        // logger.debug("{} response: {}", response.getRequest().getURI(), response.getStatus());
    }

    @Override
    public void onFailure(@Nullable Response response, @Nullable Throwable failure) {
        if (response != null && failure != null) {
            logger.debug("response failed: {}  {}", response.getRequest().getURI(), failure.getLocalizedMessage(),
                    failure);
        }
    }

    @Override
    public void onComplete(@Nullable Result result) {
        if (result == null) {
            return;
        }

        hvacConnector.onComplete(result.getRequest());

        try {
            String content = getContentAsString();
            logger.trace("response complete: {}", content);

            if (result.getResponse().getStatus() != 200) {
                logger.debug("bad gateway !!!");
                return;
            }

            if (content != null) {
                if (content.indexOf("<!DOCTYPE html>") >= 0) {
                    callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(), content);
                } else {
                    JsonObject resultObj = null;
                    try {
                        Gson gson = SiemensHvacConnectorImpl.getGson();
                        resultObj = gson.fromJson(content, JsonObject.class);
                    } catch (Exception ex) {
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

                                callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(),
                                        resultObj);
                                return;
                            } else if (("datatype not supported").equals(errorMsg)) {
                                callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(),
                                        resultObj);
                            } else {
                                logger.debug("error : {}", subResultObj);
                                hvacConnector.onError(result.getRequest());
                            }
                        } else {
                            logger.debug("error");
                            hvacConnector.onError(result.getRequest());
                        }

                    } else {
                        logger.debug("error");
                        hvacConnector.onError(result.getRequest());
                    }

                    return;
                }
            }

            callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(), content);
        } catch (Exception ex) {
            logger.debug("error");
        }
    }
}
