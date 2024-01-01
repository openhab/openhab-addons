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
package org.openhab.binding.solaredge.internal.command;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.SocketTimeoutException;
import java.net.URI;
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
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.connector.CommunicationStatus;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * base class for all commands. common logic should be implemented here
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public abstract class AbstractCommand extends BufferingResponseListener implements SolarEdgeCommand {

    /**
     * logger
     */
    protected final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);

    /**
     * the configuration
     */
    protected final SolarEdgeConfiguration config;

    /**
     * JSON deserializer
     */
    private final Gson gson;

    /**
     * status code of fulfilled request
     */
    private final CommunicationStatus communicationStatus;

    /**
     * listener to provide updates to the WebInterface class
     */
    private final StatusUpdateListener listener;

    /**
     * the constructor
     *
     * @param config
     * @param listener
     *
     */
    public AbstractCommand(SolarEdgeConfiguration config, StatusUpdateListener listener) {
        this.communicationStatus = new CommunicationStatus();
        this.config = config;
        this.listener = listener;
        this.gson = new Gson();
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
    }

    @Override
    public void onContent(@Nullable Response response, @Nullable ByteBuffer content) {
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
        return communicationStatus;
    }

    /**
     * updates status of the registered listener.
     */
    protected final void updateListenerStatus() {
        try {
            listener.update(communicationStatus);
        } catch (Exception ex) {
            // this should not happen
            logger.warn("Exception caught: {}", ex.getMessage(), ex);
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
     * concrete implementation has to provide the URL
     *
     * @return Url
     */
    protected abstract String getURL();

    /**
     * just a wrapper as fromJson could return null. This will avoid warnings as eclipse otherwise assumes unnecessary
     * null checks which are not unnecessary
     *
     * @param <T>
     * @param json
     * @param classOfT
     * @return
     * @throws JsonSyntaxException
     */
    protected <T> @Nullable T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }
}
