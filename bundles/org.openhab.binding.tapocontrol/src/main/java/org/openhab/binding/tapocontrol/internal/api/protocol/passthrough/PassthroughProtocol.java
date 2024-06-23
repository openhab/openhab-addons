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
package org.openhab.binding.tapocontrol.internal.api.protocol.passthrough;

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.JsonUtils.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TapoUtils.*;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tapocontrol.internal.api.TapoConnectorInterface;
import org.openhab.binding.tapocontrol.internal.api.protocol.TapoProtocolInterface;
import org.openhab.binding.tapocontrol.internal.dto.TapoBaseRequestInterface;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TAPO-PASSTHROUGH-Protocol
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class PassthroughProtocol implements TapoProtocolInterface {
    private final Logger logger = LoggerFactory.getLogger(PassthroughProtocol.class);
    private final TapoConnectorInterface httpDelegator;
    private final String uid;

    /***********************
     * Init Class
     **********************/

    public PassthroughProtocol(TapoConnectorInterface httpDelegator) {
        this.httpDelegator = httpDelegator;
        uid = httpDelegator.getThingUID() + " / HTTP-Passtrhough";
    }

    /***********************
     * Login Handling
     **********************/

    @Override
    public boolean login(TapoCredentials tapoCredentials) throws TapoErrorHandler {
        logger.debug("({}) login not implemented", uid);
        throw new TapoErrorHandler(ERR_BINDING_NOT_IMPLEMENTED, "NOT NEEDED");
    }

    @Override
    public void logout() {
        logger.trace("({}) logout not implemented", uid);
    }

    @Override
    public boolean isLoggedIn() {
        logger.debug("({}) isLoggedIn not implemented", uid);
        return false;
    }

    /***********************
     * Request Sender
     **********************/

    /*
     * send synchronous request - response will be handled in [responseReceived()] function
     */
    @Override
    public void sendRequest(TapoRequest tapoRequest) throws TapoErrorHandler {
        String url = getUrl();
        logger.trace("({}) sending encrypted request to '{}' ", uid, url);
        logger.trace("({}) unencrypted request: '{}'", uid, tapoRequest);

        Request httpRequest = httpDelegator.getHttpClient().newRequest(url).method(HttpMethod.POST);

        /* set header */
        httpRequest = setHeaders(httpRequest);
        httpRequest.timeout(TAPO_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        /* add request body */
        httpRequest.content(new StringContentProvider(tapoRequest.toString(), CONTENT_CHARSET), CONTENT_TYPE_JSON);

        try {
            responseReceived(httpRequest.send(), tapoRequest.method());
        } catch (TapoErrorHandler tapoError) {
            logger.debug("({}) sendRequest exception'{}'", uid, tapoError.toString());
            throw tapoError;
        } catch (TimeoutException e) {
            logger.debug("({}) sendRequest timeout'{}'", uid, e.getMessage());
            throw new TapoErrorHandler(ERR_BINDING_CONNECT_TIMEOUT, getValueOrDefault(e.getMessage(), ""));
        } catch (InterruptedException e) {
            logger.debug("({}) sendRequest interrupted'{}'", uid, e.getMessage());
            throw new TapoErrorHandler(ERR_BINDING_SEND_REQUEST, getValueOrDefault(e.getMessage(), ""));
        } catch (ExecutionException e) {
            logger.debug("({}) sendRequest exception'{}'", uid, e.getMessage());
            throw new TapoErrorHandler(ERR_BINDING_SEND_REQUEST, getValueOrDefault(e.getMessage(), ""));
        }
    }

    /*
     * send asynchronous request - response will be handled in [asyncResponseReceived()] function
     */
    @Override
    public void sendAsyncRequest(TapoBaseRequestInterface tapoRequest) throws TapoErrorHandler {
        String url = getUrl();
        String command = tapoRequest.method();
        logger.trace("({}) sendAsncRequest to '{}'", uid, url);
        logger.trace("({}) command/payload: '{}''{}'", uid, command, tapoRequest.params());

        Request httpRequest = httpDelegator.getHttpClient().newRequest(url).method(HttpMethod.POST);

        /* set header */
        httpRequest = setHeaders(httpRequest);

        /* add request body */
        httpRequest.content(new StringContentProvider(tapoRequest.toString(), CONTENT_CHARSET), CONTENT_TYPE_JSON);

        httpRequest.timeout(TAPO_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS).send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                final HttpResponse response = (HttpResponse) result.getResponse();
                if (result.getFailure() != null) {
                    /* handle result errors */
                    Throwable e = result.getFailure();
                    String errorMessage = getValueOrDefault(e.getMessage(), "");
                    if (e instanceof TimeoutException) {
                        logger.debug("({}) sendAsyncRequest timeout'{}'", uid, errorMessage);
                        httpDelegator.handleError(new TapoErrorHandler(ERR_BINDING_CONNECT_TIMEOUT, errorMessage));
                    } else {
                        logger.debug("({}) sendAsyncRequest failed'{}'", uid, errorMessage);
                        httpDelegator.handleError(new TapoErrorHandler(new Exception(e), errorMessage));
                    }
                } else if (response.getStatus() != 200) {
                    logger.debug("({}) sendAsyncRequest response error'{}'", uid, response.getStatus());
                    httpDelegator.handleError(new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, getContentAsString()));
                } else {
                    /* request successful */
                    String rBody = Objects.requireNonNull(getContentAsString());
                    try {
                        asyncResponseReceived(rBody, command);
                    } catch (TapoErrorHandler tapoError) {
                        httpDelegator.handleError(tapoError);
                    }
                }
            }
        });
    }

    /************************
     * RESPONSE HANDLERS
     ************************/

    /**
     * handle synchronous request-response - pushes TapoResponse to [httpDelegator.handleResponse()]-function
     */
    @Override
    public void responseReceived(ContentResponse response, String command) throws TapoErrorHandler {
        logger.trace("({}) received response: {}", uid, response.getContentAsString());
        TapoResponse tapoResponse = getTapoResponse(response);
        if (!tapoResponse.hasError()) {
            switch (command) {
                default:
                    httpDelegator.handleResponse(tapoResponse, command);
                    httpDelegator.responsePasstrough(response.getContentAsString(), command);
            }
        } else {
            logger.debug("({}) response returned error: {} ({})", uid, tapoResponse.message(),
                    tapoResponse.errorCode());
            httpDelegator.handleError(new TapoErrorHandler(tapoResponse.errorCode()));
        }
    }

    /**
     * handle asynchronous request-response - pushes TapoResponse to [httpDelegator.handleResponse()]-function
     */
    @Override
    public void asyncResponseReceived(String responseBody, String command) throws TapoErrorHandler {
        logger.trace("({}) asyncResponseReceived '{}'", uid, responseBody);
        TapoResponse tapoResponse = getTapoResponse(responseBody);
        if (!tapoResponse.hasError()) {
            httpDelegator.handleResponse(tapoResponse, command);
        } else {
            httpDelegator.handleError(new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, command));
        }
    }

    /************************
     * 
     * PRIVATE HELPERS
     * 
     ************************/

    /**
     * Get Tapo-Response from Contentresponse
     */
    private TapoResponse getTapoResponse(ContentResponse response) throws TapoErrorHandler {
        if (response.getStatus() == 200) {
            return getTapoResponse(response.getContentAsString());
        } else {
            String reason = response.getStatus() + " " + response.getReason();
            logger.debug("({}) invalid response received - {}", uid, reason);
            throw new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, reason);
        }
    }

    /**
     * Get Tapo-Response from responsestring
     */
    private TapoResponse getTapoResponse(String responseString) throws TapoErrorHandler {
        if (isValidJson(responseString)) {
            return Objects.requireNonNull(GSON.fromJson(responseString, TapoResponse.class));
        } else {
            logger.debug("({}) invalid response received", uid);
            throw new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, "invalid response receicved");
        }
    }

    /**
     * Get Url requests are sent to
     */
    public String getUrl() {
        return httpDelegator.getBaseUrl();
    }

    /*
     * Set HTTP-Headers
     */
    public Request setHeaders(Request httpRequest) {
        httpRequest.header("Accept", CONTENT_TYPE_JSON);
        return httpRequest;
    }
}
