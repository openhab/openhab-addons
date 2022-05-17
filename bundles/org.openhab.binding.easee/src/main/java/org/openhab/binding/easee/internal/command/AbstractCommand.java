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
package org.openhab.binding.easee.internal.command;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.WEB_REQUEST_BEARER_TOKEN_PREFIX;

import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.easee.internal.connector.CommunicationStatus;
import org.openhab.binding.easee.internal.connector.StatusUpdateListener;
import org.openhab.binding.easee.internal.handler.EaseeHandler;
import org.openhab.binding.easee.internal.model.GenericErrorResponse;
import org.openhab.binding.easee.internal.model.GenericResponseTransformer;
import org.openhab.binding.easee.internal.model.account.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

/**
 * base class for all commands. common logic should be implemented here
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public abstract class AbstractCommand extends BufferingResponseListener implements EaseeCommand {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);

    /**
     * the configuration
     */
    protected final EaseeHandler handler;

    /**
     * JSON deserializer
     */
    protected final Gson gson;

    /**
     * status code of fulfilled request
     */
    private final CommunicationStatus communicationStatus;

    /**
     * listener to provide updates to the WebInterface class
     */
    private @Nullable StatusUpdateListener listener;

    /**
     * generic transformer which just transfers all values in a plain map.
     */
    private final GenericResponseTransformer transformer;

    /**
     * retry counter.
     */
    private int retries = 0;

    /**
     * retry active
     */
    private final boolean retryOnFailure;

    /**
     * set handler offline on error
     */
    private final boolean updateHandlerOnFailure;

    /**
     * the constructor
     *
     * @param config
     */
    public AbstractCommand(EaseeHandler handler, boolean retryOnFailure, boolean updateHandlerOnFailure) {
        this.communicationStatus = new CommunicationStatus();
        this.transformer = new GenericResponseTransformer(handler);
        this.handler = handler;
        this.updateHandlerOnFailure = updateHandlerOnFailure;
        this.retryOnFailure = retryOnFailure;
        this.gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
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
        } else {
            logger.debug("Request failed");
        }
        if (response != null && response.getStatus() > 0) {
            communicationStatus.setHttpCode(HttpStatus.getCode(response.getStatus()));
        }
    }

    /**
     * just for logging of content
     */
    @Override
    public void onContent(@Nullable Response response, @Nullable ByteBuffer content) {
        super.onContent(response, content);
        logger.debug("received content, length: {}", getContentAsString().length());
    }

    /**
     * default handling of successful requests.
     */
    @Override
    public void onComplete(@Nullable Result result) {
        String json = getContentAsString(StandardCharsets.UTF_8);
        ResultData data = new ResultData();

        logger.debug("JSON String: {}", json);
        switch (getCommunicationStatus().getHttpCode()) {
            case OK:
                if (json != null) {
                    Type genericStringMap = new TypeToken<Map<String, Object>>() {
                    }.getType();
                    Map<String, Object> jsonObject = gson.fromJson(json, genericStringMap);
                    if (jsonObject != null) {
                        logger.info("success");
                        handler.updateChannelStatus(transformer.transform(jsonObject, getChannelGroup()));
                    }
                }
                break;
            default:
                GenericErrorResponse errorResponse = gson.fromJson(json, GenericErrorResponse.class);
                if (updateHandlerOnFailure) {
                    data.setErrorResponse(errorResponse);
                    updateListenerStatus(data);
                } else {
                    logger.info("command failed, url: {} - result: {}", getURL(),
                            errorResponse == null ? "null" : errorResponse.getTitle());
                }

                if (retryOnFailure && retries++ < MAX_RETRIES) {
                    handler.getWebInterface().enqueueCommand(this);
                }
        }
    }

    /**
     * preparation of the request. will call a hook (prepareRequest) that has to be implemented in the subclass to add
     * content to the request.
     */
    @Override
    public void performAction(HttpClient asyncclient) {
        Request request = asyncclient.newRequest(getURL()).timeout(handler.getConfiguration().getAsyncTimeout(),
                TimeUnit.SECONDS);

        // we want to send and receive json only, so explicitely set this!
        request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.CONTENT_TYPE, "application/json");

        // this should be the default for Easee Cloud API
        request.followRedirects(false);

        // add authentication data for every request. Handling this here makes it obsolete to implement for each and
        // every command
        String accessToken = handler.getWebInterface().getAccessToken();
        if (!accessToken.isBlank()) {
            request.header(HttpHeader.AUTHORIZATION, WEB_REQUEST_BEARER_TOKEN_PREFIX + accessToken);
        }

        prepareRequest(request).send(this);
    }

    /**
     * @return returns Http Status Code
     */
    public CommunicationStatus getCommunicationStatus() {
        return communicationStatus;
    }

    @Override
    public void updateListenerStatus() {
        updateListenerStatus(null);
    }

    @Override
    public void updateListenerStatus(@Nullable ResultData data) {
        if (listener != null) {
            listener.update(communicationStatus, data);
        }
    }

    /**
     * concrete implementation has to prepare the requests with additional parameters, etc
     *
     * @param requestToPrepare the request to prepare
     * @return prepared Request object
     */
    protected abstract Request prepareRequest(Request requestToPrepare);

    /**
     * concrete implementation has to provide the channel group.
     *
     * @return
     */
    protected abstract String getChannelGroup();

    /**
     * concrete implementation has to provide the URL
     *
     * @return Url
     */
    protected abstract String getURL();

    @Override
    public final void setListener(StatusUpdateListener listener) {
        this.listener = listener;
    }
}
