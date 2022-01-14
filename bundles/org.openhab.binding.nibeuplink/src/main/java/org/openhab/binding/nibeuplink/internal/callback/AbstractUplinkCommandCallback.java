/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nibeuplink.internal.callback;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.nibeuplink.internal.command.NibeUplinkCommand;
import org.openhab.binding.nibeuplink.internal.config.NibeUplinkConfiguration;
import org.openhab.binding.nibeuplink.internal.connector.CommunicationStatus;
import org.openhab.binding.nibeuplink.internal.connector.StatusUpdateListener;
import org.openhab.binding.nibeuplink.internal.model.GenericDataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * base class for all commands. common logic should be implemented here
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public abstract class AbstractUplinkCommandCallback extends BufferingResponseListener implements NibeUplinkCommand {

    /**
     * logger
     */
    protected final Logger logger = LoggerFactory.getLogger(AbstractUplinkCommandCallback.class);

    /**
     * the configuration
     */
    protected final NibeUplinkConfiguration config;

    /**
     * status code of fulfilled request
     */
    private CommunicationStatus communicationStatus;

    /**
     * listener to provide updates to the WebInterface class
     */
    private @Nullable StatusUpdateListener listener;

    /**
     * JSON deserializer
     */
    private final Gson gson;

    /**
     * the constructor
     *
     * @param config
     */
    public AbstractUplinkCommandCallback(NibeUplinkConfiguration config) {
        this.communicationStatus = new CommunicationStatus();
        this.config = config;
        this.gson = new Gson();
    }

    /**
     * the constructor
     *
     * @param config
     */
    public AbstractUplinkCommandCallback(NibeUplinkConfiguration config, StatusUpdateListener listener) {
        this(config);
        this.listener = listener;
    }

    /**
     * Log request success
     */
    @Override
    public final void onSuccess(@Nullable Response response) {
        super.onSuccess(response);
        if (response != null) {
            communicationStatus.setHttpCode(HttpStatus.getCode(response.getStatus()));
            logger.debug("HTTP response {}", response.getStatus());
        }
    }

    /**
     * Log request failure
     */
    @Override
    public final void onFailure(@Nullable Response response, @Nullable Throwable failure) {
        super.onFailure(response, failure);
        if (failure != null) {
            logger.debug("Request failed: {}", failure.toString());
            communicationStatus.setError((Exception) failure);

            if (failure instanceof SocketTimeoutException || failure instanceof TimeoutException) {
                communicationStatus.setHttpCode(Code.REQUEST_TIMEOUT);
            } else if (failure instanceof UnknownHostException) {
                communicationStatus.setHttpCode(Code.BAD_GATEWAY);
            } else {
                communicationStatus.setHttpCode(Code.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    public void onContent(@Nullable Response response, @Nullable ByteBuffer content) {
        super.onContent(response, content);
        logger.debug("received content, length: {}", getContentAsString().length());
    }

    @Override
    public void performAction(HttpClient asyncclient) {
        Request request = asyncclient.newRequest(getURL()).timeout(config.getAsyncTimeout(), TimeUnit.SECONDS);
        prepareRequest(request).send(this);
    }

    /**
     * @Nullable wrapper of gson which does not 'understand' nonnull annotations
     *
     * @param json
     * @return
     */
    protected @Nullable GenericDataResponse fromJson(String json) {
        // gson is not able to handle @NonNull annotation, thus the return value can be null.
        return gson.fromJson(json, GenericDataResponse.class);
    }

    /**
     * returns Http Status Code
     */
    public CommunicationStatus getCommunicationStatus() {
        return communicationStatus;
    }

    /**
     * concrete implementation has to prepare the requests with additional parameters, etc
     *
     * @return
     */
    protected abstract Request prepareRequest(Request requestToPrepare);

    /**
     * concrete implementation has to provide the URL
     *
     * @return
     */
    protected abstract String getURL();

    @Override
    public final @Nullable StatusUpdateListener getListener() {
        return listener;
    }

    @Override
    public final void setListener(StatusUpdateListener listener) {
        this.listener = listener;
    }
}
