/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.api.protocol.klap;

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.ByteUtils.byteArrayToHex;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.JsonUtils.isValidJson;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tapocontrol.internal.api.TapoConnectorInterface;
import org.openhab.binding.tapocontrol.internal.dto.TapoBaseRequestInterface;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TAPO-KLAP-Protocol
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class KlapProtocol implements org.openhab.binding.tapocontrol.internal.api.protocol.TapoProtocolInterface {

    private final Logger logger = LoggerFactory.getLogger(KlapProtocol.class);
    protected final TapoConnectorInterface httpDelegator;
    private KlapSession session;
    private String uid;
    private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /***********************
     * Init Class
     **********************/
    public KlapProtocol(TapoConnectorInterface httpDelegator) {
        this.httpDelegator = httpDelegator;
        session = new KlapSession(this);
        uid = httpDelegator.getThingUID() + " / HTTP-KLAP";
    }

    @Override
    public boolean login(TapoCredentials tapoCredentials) throws TapoErrorHandler {
        logger.trace("({}) login to device", uid);
        session.reset();
        session.login(tapoCredentials);
        return isLoggedIn();
    }

    @Override
    public void logout() {
        session.reset();
    }

    @Override
    public boolean isLoggedIn() {
        return session.isHandshakeComplete() && session.seedIsOkay() && !session.isExpired();
    }

    /***********************
     * Request Sender
     **********************/

    /*
     * send synchron request - response will be handled in [responseReceived()] function
     */
    @Override
    public void sendRequest(TapoRequest tapoRequest) throws TapoErrorHandler {
        String url = getUrl();
        String command = tapoRequest.method();
        logger.trace("({}) sending unencrypted request: '{}' to '{}' ", uid, tapoRequest, url);

        Request httpRequest = httpDelegator.getHttpClient().newRequest(url).method(HttpMethod.POST);

        /* set header */
        httpRequest = setHeaders(httpRequest);
        httpRequest.timeout(TAPO_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        /* add request body */
        httpRequest.content(new StringContentProvider(tapoRequest.toString(), CONTENT_CHARSET), CONTENT_TYPE_JSON);

        try {
            responseReceived(httpRequest.send(), command);
        } catch (Exception e) {
            throw new TapoErrorHandler(e, "error sending content");
        }
    }

    /**
     * handle asynchron request-response
     * pushes (decrypted) TapoResponse to [httpDelegator.handleResponse()]-function
     */
    @Override
    public void sendAsyncRequest(TapoBaseRequestInterface tapoRequest) throws TapoErrorHandler {
        executor.submit(() -> {
            try {
                sendRequestRetryable(tapoRequest);
            } catch (TapoErrorHandler e) {
                String errorMessage = e.getMessage();
                logger.debug("({}) sendAsyncRequest failed'{}'", uid, errorMessage);
                httpDelegator.handleError(new TapoErrorHandler(new Exception(e), errorMessage));
            }
        });
    }

    /**
     * handle synchron request-response
     * pushes (decrypted) TapoResponse to [httpDelegator.handleResponse()]-function
     * retries login+command if Http 403 response indicates klap protocol no longer valid
     */
    private void sendRequestRetryable(TapoBaseRequestInterface tapoRequest) throws TapoErrorHandler {
        /*
         * send request, and retry with re-login if protocol rejected
         * (Protocol can be rejected due to polling from another device e.g. Tapo App, or Tapo H100 Hub)
         * For asynchronous use, call on separate thread
         */
        final int MAX_ATTEMPTS = 3;
        int attemptCount = 0;
        String url = getUrl();
        String command = tapoRequest.method();
        logger.trace("({}) sendRequestRetryable unencrypted request: '{}' to '{}' ", uid, tapoRequest, url);

        while (true) {
            attemptCount++;
            if (attemptCount > 1) {
                try {
                    /* re-login using existing credentials */
                    session.reset();
                    if (!session.login()) {
                        logger.debug("({}) sendRequestRetryable login error'{}'", uid);
                        httpDelegator.handleError(new TapoErrorHandler(ERR_BINDING_LOGIN));
                        return;
                    }
                    logger.trace("({}) sendRequestRetryable re-login successful attempt {}", uid, attemptCount);
                } catch (TapoErrorHandler e) {
                    logger.trace("({}) sendRequestRetryable error1 {}", uid, e.getMessage());
                    if (attemptCount < MAX_ATTEMPTS) {
                        continue;
                    }
                    httpDelegator.handleError(e);
                    return;
                } catch (Exception ex) {
                    logger.trace("({}) sendRequestRetryable error2 {}", uid, ex.getMessage());
                    if (attemptCount < MAX_ATTEMPTS) {
                        continue;
                    }
                    httpDelegator.handleError(new TapoErrorHandler(ERR_BINDING_LOGIN, ex.getMessage()));
                    return;
                }
            }
            try {
                /* encrypt request */
                byte[] encodedBytes = session.encryptRequest(tapoRequest);
                Integer ivSequence = session.getIvSequence();
                if (logger.isTraceEnabled()) {
                    String encryptedString = byteArrayToHex(encodedBytes);
                    logger.trace("({}) encrypted request is '{}' with sequence '{}'", uid, encryptedString, ivSequence);
                }

                Request httpRequest = httpDelegator.getHttpClient().newRequest(url).method(HttpMethod.POST);

                /* set header and params */
                httpRequest = setHeaders(httpRequest);
                httpRequest.param("seq", ivSequence.toString());

                /* add request body */
                httpRequest.content(new BytesContentProvider(encodedBytes));
                ContentResponse response = httpRequest.timeout(TAPO_HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
                switch (response.getStatus()) {
                    case 200:
                        /* request successful */
                        byte[] responseBytes = response.getContent();
                        try {
                            encryptedResponseReceived(responseBytes, ivSequence, command);
                        } catch (TapoErrorHandler tapoError) {
                            httpDelegator.handleError(tapoError);
                        }
                        return;
                    case 403:
                        /*
                         * Forbidden - likely cause Encrption nolonger valid - retry with login unless we have already
                         * retried...
                         */
                        if (attemptCount < MAX_ATTEMPTS) {
                            continue;
                        } else {
                            logger.debug("({}) sendRequestRetryable response error'{}'", uid, response.getStatus());
                            httpDelegator.handleError(
                                    new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, response.getContentAsString()));
                            return;
                        }
                    default:
                        /* throw errors to delegator */
                        logger.debug("({}) sendRequestRetryable response error'{}'", uid, response.getStatus());
                        httpDelegator.handleError(
                                new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, response.getContentAsString()));
                        return;
                }
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                if (e instanceof TimeoutException) {
                    logger.debug("({}) sendRequestRetryable timeout'{}'", uid, errorMessage);
                    httpDelegator.handleError(new TapoErrorHandler(ERR_BINDING_CONNECT_TIMEOUT, errorMessage));
                } else {
                    logger.debug("({}) sendRequestRetryable failed'{}'", uid, errorMessage);
                    httpDelegator.handleError(new TapoErrorHandler(new Exception(e), errorMessage));
                }
                return;
            }
        } /* end of loop */
    }

    /************************
     * RESPONSE HANDLERS
     ************************/

    /**
     * handle synchron request-response
     * pushes (decrypted) TapoResponse to [httpDelegator.handleResponse()]-function
     */
    @Override
    public void responseReceived(ContentResponse response, String command) throws TapoErrorHandler {
        logger.trace("({}) received response content: '{}'", uid, response.getContentAsString());
        TapoResponse tapoResponse = getTapoResponse(response);
        httpDelegator.handleResponse(tapoResponse, command);
        httpDelegator.responsePasstrough(response.getContentAsString(), command);
    }

    /**
     * handle asynchron request-response
     * pushes (decrypted) TapoResponse to [httpDelegator.handleResponse()]-function
     */
    @Override
    public void asyncResponseReceived(String content, String command) throws TapoErrorHandler {
        try {
            TapoResponse tapoResponse = getTapoResponse(content);
            httpDelegator.handleResponse(tapoResponse, command);
        } catch (TapoErrorHandler tapoError) {
            httpDelegator.handleError(tapoError);
        }
    }

    /**
     * handle encrypted response. decrypt it and pass to asyncRequestReceived
     *
     * @param content bytearray with encrypted payload
     * @param ivSeq ivSequence-Number which is incremented each request
     * @param command command was sent to device
     * @throws TapoErrorHandler
     */
    public void encryptedResponseReceived(byte[] content, Integer ivSeq, String command) throws TapoErrorHandler {
        String stringContent = byteArrayToHex(content);
        logger.trace("({}) receivedRespose '{}'", uid, stringContent);
        String decryptedResponse = session.decryptResponse(content, ivSeq);
        logger.trace("({}) decrypted response: '{}'", uid, decryptedResponse);
        asyncResponseReceived(decryptedResponse, command);
    }

    /**
     * Get Tapo-Response from Contentresponse
     * decrypt if is encrypted
     */
    protected TapoResponse getTapoResponse(ContentResponse response) throws TapoErrorHandler {
        if (response.getStatus() == 200) {
            return getTapoResponse(response.getContentAsString());
        } else {
            logger.debug("({}) invalid response received", uid);
            throw new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, "invalid response receicved");
        }
    }

    /**
     * Get Tapo-Response from responsestring
     * decrypt if is encrypted
     */
    protected TapoResponse getTapoResponse(String responseString) throws TapoErrorHandler {
        if (isValidJson(responseString)) {
            TapoResponse tapoResponse = Objects.requireNonNull(GSON.fromJson(responseString, TapoResponse.class));
            if (tapoResponse.hasError()) {
                throw new TapoErrorHandler(tapoResponse.errorCode(), tapoResponse.message());
            }
            return tapoResponse;
        } else {
            logger.debug("({}) invalid response received", uid);
            throw new TapoErrorHandler(ERR_BINDING_HTTP_RESPONSE, "invalid response receicved");
        }
    }

    /************************
     * PRIVATE HELPERS
     ************************/

    protected String getUrl() {
        String baseUrl = String.format(TAPO_DEVICE_URL, httpDelegator.getBaseUrl());
        if (session.isHandshakeComplete()) {
            return baseUrl + "/request";
        } else {
            return baseUrl;
        }
    }

    /*
     * Set HTTP-Headers
     */
    protected Request setHeaders(Request httpRequest) {
        if (!session.isHandshakeComplete()) {
            httpRequest.header("Accept", CONTENT_TYPE_JSON);
        }
        if (!session.getCookie().isBlank()) {
            httpRequest.header(HTTP_AUTH_TYPE_COOKIE, session.getCookie());
        }
        return httpRequest;
    }
}
