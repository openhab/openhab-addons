/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dataexchange;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.core.Configuration;
import org.openhab.binding.nuki.NukiBindingConstants;
import org.openhab.binding.nuki.internal.dto.BridgeApiInfoDto;
import org.openhab.binding.nuki.internal.dto.BridgeApiLockActionDto;
import org.openhab.binding.nuki.internal.dto.BridgeApiLockStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link NukiHttpClient} class is responsible for getting data from the Nuki Bridge.
 *
 * @author Markus Katter - Initial contribution
 */
public class NukiHttpClient {

    private final Logger logger = LoggerFactory.getLogger(NukiHttpClient.class);
    private static final long CACHE_PERIOD = 5;

    private HttpClient httpClient;
    private Configuration configuration;
    private Gson gson;
    private BridgeLockStateResponse bridgeLockStateResponseCache;

    public NukiHttpClient(Configuration configuration) {
        logger.trace("Instantiating NukiHttpClient({})", configuration);
        this.configuration = configuration;
        this.httpClient = new HttpClient();
        long connectTimeout = NukiBindingConstants.CLIENT_CONNECT_TIMEOUT;
        httpClient.setConnectTimeout(connectTimeout);
        // startClient();
        gson = new Gson();
    }

    private void startClient() {
        logger.trace("Starting HttpClient...");
        if (httpClient.isStarted()) {
            logger.trace("Cancel starting HttpClient as it is already started!");
            return;
        }
        try {
            httpClient.start();
            logger.trace("Started httpClient[{}]", httpClient);
        } catch (Exception e) {
            logger.error("Could not start NukiHttpClient! ERROR: {}", e.getMessage(), e);
        }
    }

    public void stopClient() {
        logger.trace("Stopping HttpClient...");
        try {
            if (httpClient.isStarted()) {
                httpClient.stop();
                logger.trace("Stopped NukiHttpClient[{}]", httpClient);
            }
        } catch (Exception e) {
            logger.error("Could not stop NukiHttpClient! ERROR: {}", e.getMessage(), e);
        }
    }

    private synchronized String prepareUri(String uriTemplate, String... additionalArguments) {
        String configIp = (String) configuration.get(NukiBindingConstants.CONFIG_IP);
        BigDecimal configPort = (BigDecimal) configuration.get(NukiBindingConstants.CONFIG_PORT);
        String configApiToken = (String) configuration.get(NukiBindingConstants.CONFIG_API_TOKEN);
        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(configIp);
        parameters.add(configPort.toString());
        parameters.add(configApiToken);
        if (additionalArguments != null) {
            for (String argument : additionalArguments) {
                parameters.add(argument);
            }
        }
        String uri = String.format(uriTemplate, parameters.toArray());
        logger.trace("URI[{}]", uri);
        return uri;
    }

    private synchronized ContentResponse executeRequest(String uri)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (httpClient.isStarting()) {
            logger.trace("httpClient is starting! waiting...");
            wait();
        }
        if (httpClient.isStopped()) {
            startClient();
        }
        ContentResponse contentResponse = httpClient.GET(uri);
        logger.trace("contentResponseAsString[{}]", contentResponse.getContentAsString());
        return contentResponse;
    }

    private NukiBaseResponse handleException(Exception e) {
        if (e instanceof ExecutionException) {
            if (e.getCause() instanceof HttpResponseException) {
                HttpResponseException cause = (HttpResponseException) e.getCause();
                int status = cause.getResponse().getStatus();
                String reason = cause.getResponse().getReason();
                logger.warn("HTTP Response Exception! Status[{}] - Message[{}]! Check your API Token!", status, reason);
                return new NukiBaseResponse(status, reason);
            } else if (e.getCause() instanceof SocketTimeoutException) {
                logger.warn("Timeout Exception! Message[{}]! Check your IP and Port configuration!", e.getMessage());
                return new NukiBaseResponse(HttpStatus.REQUEST_TIMEOUT_408,
                        "Timeout Exception! Check your IP and Port configuration!");
            } else if (e.getCause() instanceof ConnectException) {
                logger.warn("Connect Exception! Message[{}]! Check your IP and Port configuration!", e.getMessage());
                return new NukiBaseResponse(HttpStatus.NOT_FOUND_404,
                        "Connect Exception! Check your IP and Port configuration!");
            }
        }
        logger.error("Could not handle Exception! Message[{}]", e.getMessage(), e);
        return new NukiBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage());
    }

    public synchronized BridgeInfoResponse getBridgeInfo() {
        logger.debug("NukiHttpClient:getBridgeInfo()");
        String uri = prepareUri(NukiBindingConstants.URI_INFO);
        try {
            ContentResponse contentResponse = executeRequest(uri);
            int status = contentResponse.getStatus();
            String response = contentResponse.getContentAsString();
            if (status == HttpStatus.OK_200) {
                BridgeApiInfoDto bridgeApiInfoDto = gson.fromJson(response, BridgeApiInfoDto.class);
                BridgeInfoResponse bridgeInfoResponse = new BridgeInfoResponse(status, contentResponse.getReason());
                bridgeInfoResponse.setBridgeInfo(bridgeApiInfoDto);
                return bridgeInfoResponse;
            } else {
                logger.warn("Could not get Bridge Info! Status[{}] - Response[{}]", status, response);
                return new BridgeInfoResponse(status, response);
            }
        } catch (Exception e) {
            logger.debug("Could not get Bridge Info! Message[{}]", e.getMessage());
            return new BridgeInfoResponse(handleException(e));
        }
    }

    public synchronized BridgeLockStateResponse getBridgeLockState(String nukiId) {
        logger.debug("NukiHttpClient:getBridgeLockState({})", nukiId);
        long timestampSecs = Instant.now().getEpochSecond();
        if (this.bridgeLockStateResponseCache != null
                && timestampSecs < this.bridgeLockStateResponseCache.getCreated().getEpochSecond() + CACHE_PERIOD) {
            logger.debug("Returning LockState from cache - now[{}]<created[{}]+cachePeriod[{}]", timestampSecs,
                    this.bridgeLockStateResponseCache.getCreated().getEpochSecond(), CACHE_PERIOD);
            return bridgeLockStateResponseCache;
        } else {
            logger.debug("Requesting LockState from Bridge.");
        }
        String uri = prepareUri(NukiBindingConstants.URI_LOCKSTATE, nukiId);
        try {
            ContentResponse contentResponse = executeRequest(uri);
            int status = contentResponse.getStatus();
            String response = contentResponse.getContentAsString();
            if (status == HttpStatus.OK_200) {
                BridgeApiLockStateDto bridgeApiLockStateDto = gson.fromJson(response, BridgeApiLockStateDto.class);
                BridgeLockStateResponse bridgeLockStateResponse = new BridgeLockStateResponse(status,
                        contentResponse.getReason());
                bridgeLockStateResponse.setBridgeLockState(bridgeApiLockStateDto);
                bridgeLockStateResponseCache = bridgeLockStateResponse;
                return bridgeLockStateResponse;
            } else {
                logger.warn("Could not get Lock State! Status[{}] - Response[{}]", status, response);
                return new BridgeLockStateResponse(status, response);
            }
        } catch (Exception e) {
            logger.debug("Could not get Bridge Lock State! Message[{}]", e.getMessage());
            return new BridgeLockStateResponse(handleException(e));
        }
    }

    public synchronized BridgeLockActionResponse getBridgeLockAction(String nukiId, int lockAction) {
        logger.debug("NukiHttpClient:getBridgeLockAction({}, {})", nukiId, lockAction);
        String uri = prepareUri(NukiBindingConstants.URI_LOCKACTION, nukiId, Integer.toString(lockAction));
        try {
            ContentResponse contentResponse = executeRequest(uri);
            int status = contentResponse.getStatus();
            String response = contentResponse.getContentAsString();
            if (status == HttpStatus.OK_200) {
                BridgeApiLockActionDto bridgeApiLockActionDto = gson.fromJson(response, BridgeApiLockActionDto.class);
                BridgeLockActionResponse bridgeLockActionResponse = new BridgeLockActionResponse(status,
                        contentResponse.getReason());
                bridgeLockActionResponse.setBridgeLockAction(bridgeApiLockActionDto);
                return bridgeLockActionResponse;
            } else {
                logger.warn("Could not execute Lock Action! Status[{}] - Response[{}]", status, response);
                return new BridgeLockActionResponse(status, response);
            }
        } catch (Exception e) {
            logger.debug("Could not execute Lock Action! Message[{}]", e.getMessage());
            return new BridgeLockActionResponse(handleException(e));
        }
    }

}
