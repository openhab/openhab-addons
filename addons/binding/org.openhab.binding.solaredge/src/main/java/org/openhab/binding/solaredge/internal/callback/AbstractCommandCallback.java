/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.callback;

import static org.openhab.binding.solaredge.SolarEdgeBindingConstants.*;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.solaredge.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.openhab.binding.solaredge.internal.connector.CommunicationStatus;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * base class for all commands. common logic should be implemented here
 *
 * @author Alexander Friese - initial contribution
 */
public abstract class AbstractCommandCallback extends BufferingResponseListener implements SolarEdgeCommand {

    /**
     * logger
     */
    protected final Logger logger = LoggerFactory.getLogger(AbstractCommandCallback.class);

    /**
     * the configuration
     */
    protected final SolarEdgeConfiguration config;

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
    private StatusUpdateListener listener;

    /**
     * the constructor
     *
     * @param config
     */
    public AbstractCommandCallback(SolarEdgeConfiguration config) {
        this.communicationStatus = new CommunicationStatus();
        this.config = config;
        this.gson = new Gson();

    }

    /**
     * the constructor
     *
     * @param config
     */
    public AbstractCommandCallback(SolarEdgeConfiguration config, StatusUpdateListener listener) {
        this(config);
        this.listener = listener;
    }

    /**
     * Log request success
     */
    @Override
    public final void onSuccess(Response response) {
        super.onSuccess(response);
        communicationStatus.setHttpCode(HttpStatus.getCode(response.getStatus()));
        logger.debug("HTTP response {}", response.getStatus());
    }

    /**
     * Log request failure
     */
    @Override
    public final void onFailure(Response response, Throwable failure) {
        super.onFailure(response, failure);
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

    @Override
    public void onContent(Response response, ByteBuffer content) {
        super.onContent(response, content);
        logger.debug("received content, length: {}", getContentAsString().length());
    }

    @Override
    public void performAction(HttpClient asyncclient) {
        Request request = asyncclient.newRequest(getURL()).timeout(config.getAsyncTimeout(), TimeUnit.SECONDS);

        // add authentication data for every request. Handling this here makes it obsolete to implement for each and
        // every command
        if (config.isUsePrivateApi()) {
            // token cookie is only used by private API therefore this can be skipped when using public API
            CookieStore cookieStore = asyncclient.getCookieStore();
            HttpCookie c = new HttpCookie(PRIVATE_API_TOKEN_COOKIE_NAME, config.getTokenOrApiKey());
            c.setDomain(PRIVATE_API_TOKEN_COOKIE_DOMAIN);
            c.setPath(PRIVATE_API_TOKEN_COOKIE_PATH);
            cookieStore.add(URI.create(getURL()), c);
        } else {
            // this is only relevant when using public API
            request.param(PUBLIC_DATA_API_KEY_FIELD, config.getTokenOrApiKey());

        }

        prepareRequest(request).send(this);
    }

    /**
     * @return returns Http Status Code
     */
    public CommunicationStatus getCommunicationStatus() {
        if (communicationStatus.getHttpCode() == null) {
            communicationStatus.setHttpCode(Code.INTERNAL_SERVER_ERROR);
        }
        return communicationStatus;
    }

    /**
     * concrete implementation has to prepare the requests with additional parameters, etc
     *
     * @param requestToPrepare the request to prepare
     * @return prepared Request object
     */
    protected abstract Request prepareRequest(Request requestToPrepare);

    /**
     * concrete implementation has to provide the URL
     *
     * @return Url
     */
    protected abstract String getURL();

    @Override
    public final StatusUpdateListener getListener() {
        return listener;
    }

    @Override
    public final void setListener(StatusUpdateListener listener) {
        this.listener = listener;
    }

}
