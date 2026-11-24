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
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.JsonUtils.isValidJson;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.ContentResponse;
import org.openhab.binding.tapocontrol.internal.api.TapoConnectorInterface;
import org.openhab.binding.tapocontrol.internal.api.protocol.TapoProtocolInterface;
import org.openhab.binding.tapocontrol.internal.discovery.TapoUdpDiscovery;
import org.openhab.binding.tapocontrol.internal.dto.TapoBaseRequestInterface;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;
import org.openhab.binding.tplinkbridge.klap.KlapAuthenticationException;
import org.openhab.binding.tplinkbridge.klap.KlapTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TAPO-KLAP-Protocol
 *
 * @author Christian Wild - Initial contribution
 * @author David Henshaw - Improvements to handle tapo device error states in send
 * @author Christopher Smith - Use the shared KLAP transport of the TP-Link bridge binding
 */
@NonNullByDefault
public class KlapProtocol implements TapoProtocolInterface {

    private final Logger logger = LoggerFactory.getLogger(KlapProtocol.class);
    protected final TapoConnectorInterface httpDelegator;
    private final KlapTransport transport;
    private final String uid;
    private volatile List<TPLinkCredentials> credentials = List.of();
    /** Keeps each response handled before the next request is sent, so an older response can't overwrite a newer. */
    private final Object requestLock = new Object();
    private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /***********************
     * Init Class
     **********************/
    public KlapProtocol(TapoConnectorInterface httpDelegator) {
        this.httpDelegator = httpDelegator;
        this.uid = httpDelegator.getThingUID() + " / HTTP-KLAP";
        this.transport = new KlapTransport(httpDelegator.getHttpClient(), httpDelegator.getBaseUrl(),
                () -> credentials);
    }

    KlapProtocol(TapoConnectorInterface httpDelegator, KlapTransport transport) {
        this.httpDelegator = httpDelegator;
        this.uid = httpDelegator.getThingUID() + " / HTTP-KLAP";
        this.transport = transport;
    }

    @Override
    public boolean login(TapoCredentials tapoCredentials) throws TapoErrorHandler {
        logger.trace("({}) login to device", uid);
        TPLinkCredentials account = new TPLinkCredentials(tapoCredentials.username(), tapoCredentials.password());
        credentials = account.areSet() ? List.of(account) : List.of();
        transport.setHost(httpDelegator.getBaseUrl());
        transport.close();
        try {
            try {
                transport.login();
            } catch (KlapAuthenticationException | InterruptedIOException e) {
                throw e;
            } catch (IOException e) {
                // After a power outage some devices only accept a handshake once they received a discovery message
                logger.trace("({}) handshake failed, retrying after UDP wake-up: {}", uid, e.getMessage());
                if (!sendUdpDiscoveryMessage()) {
                    throw e;
                }
                transport.login();
            }
        } catch (IOException e) {
            throw toTapoError(e);
        }
        return isLoggedIn();
    }

    @Override
    public void logout() {
        transport.close();
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    @Override
    public boolean isLoggedIn() {
        return transport.isLoggedIn();
    }

    /***********************
     * Request Sender
     **********************/

    /*
     * send synchron request - response is pushed to [httpDelegator.handleResponse()]
     */
    @Override
    public void sendRequest(TapoRequest tapoRequest) throws TapoErrorHandler {
        exchangeAndHandle(tapoRequest);
    }

    /**
     * handle asynchron request-response
     * pushes (decrypted) TapoResponse to [httpDelegator.handleResponse()]-function
     */
    @Override
    public void sendAsyncRequest(TapoBaseRequestInterface tapoRequest) throws TapoErrorHandler {
        if (executor.isShutdown()) {
            executor = Executors.newVirtualThreadPerTaskExecutor();
        }
        executor.submit(() -> {
            try {
                exchangeAndHandle(tapoRequest);
            } catch (TapoErrorHandler e) {
                logger.debug("({}) sendAsyncRequest failed '{}'", uid, e.getMessage());
                httpDelegator.handleError(e);
            }
        });
    }

    private void exchangeAndHandle(TapoBaseRequestInterface tapoRequest) throws TapoErrorHandler {
        synchronized (requestLock) {
            asyncResponseReceived(exchange(tapoRequest), tapoRequest.method());
        }
    }

    /**
     * Sends the request through the KLAP session and returns the decrypted response. The transport logs in again
     * once if the device rejects the session, for example after the Tapo app or an H100 hub polled the device.
     */
    private String exchange(TapoBaseRequestInterface tapoRequest) throws TapoErrorHandler {
        logger.trace("({}) sending unencrypted request: '{}'", uid, tapoRequest);
        try {
            transport.setHost(httpDelegator.getBaseUrl());
            String response = transport.exchange(tapoRequest.toString());
            logger.trace("({}) decrypted response: '{}'", uid, response);
            return response;
        } catch (IOException e) {
            throw toTapoError(e);
        }
    }

    private TapoErrorHandler toTapoError(IOException e) {
        if (e instanceof KlapAuthenticationException) {
            return new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED, e.getMessage());
        }
        if (e instanceof InterruptedIOException) {
            Thread.currentThread().interrupt();
            return new TapoErrorHandler(ERR_BINDING_SEND_REQUEST, e.getMessage());
        }
        if (e.getCause() instanceof TimeoutException) {
            return new TapoErrorHandler(ERR_BINDING_CONNECT_TIMEOUT, e.getMessage());
        }
        return new TapoErrorHandler(ERR_BINDING_SEND_REQUEST, e.getMessage());
    }

    /**
     * Send the UDP discovery message to the device - sometimes needed after a power outage, prior to login
     *
     * @return success or failed
     */
    private boolean sendUdpDiscoveryMessage() {
        try {
            // Without a bridge the reply only wakes the device and isn't added to discovery
            var udpSender = new TapoUdpDiscovery(httpDelegator.getThingUID());
            udpSender.sendAllPorts(InetAddress.getByName(hostWithoutPort(httpDelegator.getBaseUrl())));
            return true;
        } catch (Exception e) {
            logger.trace("({}) UDP wake-up failed: {}", uid, e.getMessage());
            return false;
        }
    }

    /**
     * @param baseUrl the address followed by {@code :} and the port, as {@code TapoDeviceConnector.getBaseUrl()}
     *            builds it, such as {@code 192.168.1.20:80}, {@code fe80::1:80} or {@code [fe80::1]:80}
     * @return the address without the port and without the brackets of an IPv6 literal
     */
    static String hostWithoutPort(String baseUrl) {
        int colon = baseUrl.lastIndexOf(':');
        String host = colon > 0 ? baseUrl.substring(0, colon) : baseUrl;
        return host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
    }

    /************************
     * RESPONSE HANDLERS
     ************************/

    /**
     * handle synchron request-response
     * pushes TapoResponse to [httpDelegator.handleResponse()]-function
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
     * Get Tapo-Response from Contentresponse
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
}
