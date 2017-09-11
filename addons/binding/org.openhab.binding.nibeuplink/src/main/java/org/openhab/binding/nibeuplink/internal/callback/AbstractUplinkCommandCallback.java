/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.callback;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.nibeuplink.config.NibeUplinkConfiguration;
import org.openhab.binding.nibeuplink.internal.command.NibeUplinkCommand;
import org.openhab.binding.nibeuplink.internal.connector.StatusUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base class for all commands. common logic should be implemented here
 *
 * @author afriese
 *
 */
public abstract class AbstractUplinkCommandCallback extends BufferingResponseListener implements NibeUplinkCommand {

    /**
     * logger
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * the configuration
     */
    protected final NibeUplinkConfiguration config;

    /**
     * status code of fulfilled request
     */
    private int httpStatus = HttpStatus.IM_A_TEAPOT_418;

    /**
     * listener to provide updates to the WebInterface class
     */
    private StatusUpdateListener listener;

    /**
     * the constructor
     *
     * @param config
     */
    public AbstractUplinkCommandCallback(NibeUplinkConfiguration config) {
        this.config = config;
    }

    /**
     * Log request success
     */
    @Override
    public final void onSuccess(Response response) {
        super.onSuccess(response);
        httpStatus = response.getStatus();
        logger.debug("HTTP response {}", response.getStatus());
    }

    /**
     * Log request failure
     */
    @Override
    public final void onFailure(Response response, Throwable failure) {
        super.onFailure(response, failure);
        try {
            logger.warn("Request failed: {}", failure.toString());
            httpStatus = HttpStatus.IM_A_TEAPOT_418;

            // as we are not allowed to catch Throwables we must only throw Exceptions!
            if (failure instanceof Exception) {
                throw (Exception) failure;
            }
        } catch (SocketTimeoutException e) {
            httpStatus = HttpStatus.REQUEST_TIMEOUT_408;
        } catch (UnknownHostException e) {
            httpStatus = HttpStatus.BAD_GATEWAY_502;
        } catch (Exception e) {
            httpStatus = HttpStatus.IM_A_TEAPOT_418;
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
        prepareRequest(request).send(this);
    }

    /**
     * returns Http Status Code
     */
    public Code getHttpStatusCode() {
        Code code = HttpStatus.getCode(httpStatus);
        return code == null ? Code.IM_A_TEAPOT : code;
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
    public final StatusUpdateListener getListener() {
        return listener;
    }

    @Override
    public final void setListener(StatusUpdateListener listener) {
        this.listener = listener;
    }

}
