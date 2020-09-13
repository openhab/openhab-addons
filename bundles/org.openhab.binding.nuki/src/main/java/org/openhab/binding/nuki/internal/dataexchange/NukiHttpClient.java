/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.nuki.internal.dataexchange;

import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.net.SocketException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.core.Configuration;
import org.openhab.binding.nuki.internal.NukiBindingConstants;
import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackAddDto;
import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackListDto;
import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackRemoveDto;
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

    public NukiHttpClient(HttpClient httpClient, Configuration configuration) {
        logger.debug("Instantiating NukiHttpClient({})", configuration);
        this.configuration = configuration;
        this.httpClient = httpClient;
        gson = new Gson();
    }

    private String prepareUri(String uriTemplate, String... additionalArguments) {
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
        logger.trace("prepareUri(...):URI[{}]", uri);
        return uri;
    }

    private synchronized ContentResponse executeRequest(String uri)
            throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("executeRequest({})", uri);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        ContentResponse contentResponse = httpClient.GET(uri);
        logger.debug("contentResponseAsString[{}]", contentResponse.getContentAsString());
        return contentResponse;
    }

    private NukiBaseResponse handleException(Exception e) {
        if (e instanceof ExecutionException) {
            if (e.getCause() instanceof HttpResponseException) {
                HttpResponseException cause = (HttpResponseException) e.getCause();
                int status = cause.getResponse().getStatus();
                String reason = cause.getResponse().getReason();
                logger.debug("HTTP Response Exception! Status[{}] - Reason[{}]! Check your API Token!", status, reason);
                return new NukiBaseResponse(status, reason);
            } else if (e.getCause() instanceof InterruptedIOException) {
                logger.debug(
                        "InterruptedIOException! Exception[{}]! Check IP/Port configuration and if Nuki Bridge is powered on!",
                        e.getMessage());
                return new NukiBaseResponse(HttpStatus.REQUEST_TIMEOUT_408,
                        "InterruptedIOException! Check IP/Port configuration and if Nuki Bridge is powered on!");
            } else if (e.getCause() instanceof SocketException) {
                logger.debug(
                        "SocketException! Exception[{}]! Check IP/Port configuration and if Nuki Bridge is powered on!",
                        e.getMessage());
                return new NukiBaseResponse(HttpStatus.NOT_FOUND_404,
                        "SocketException! Check IP/Port configuration and if Nuki Bridge is powered on!");
            }
        }
        logger.error("Could not handle Exception! Exception[{}]", e.getMessage(), e);
        return new NukiBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage());
    }

    public BridgeInfoResponse getBridgeInfo() {
        logger.debug("getBridgeInfo() in thread {}", Thread.currentThread().getId());
        String uri = prepareUri(NukiBindingConstants.URI_INFO);
        try {
            ContentResponse contentResponse = executeRequest(uri);
            int status = contentResponse.getStatus();
            String response = contentResponse.getContentAsString();
            logger.debug("getBridgeInfo status[{}] response[{}]", status, response);
            if (status == HttpStatus.OK_200) {
                BridgeApiInfoDto bridgeApiInfoDto = gson.fromJson(response, BridgeApiInfoDto.class);
                logger.debug("getBridgeInfo OK");
                return new BridgeInfoResponse(status, contentResponse.getReason(), bridgeApiInfoDto);
            } else {
                logger.debug("Could not get Bridge Info! Status[{}] - Response[{}]", status, response);
                return new BridgeInfoResponse(status, contentResponse.getReason(), null);
            }
        } catch (Exception e) {
            logger.debug("Could not get Bridge Info! Exception[{}]", e.getMessage());
            return new BridgeInfoResponse(handleException(e));
        }
    }

    public BridgeLockStateResponse getBridgeLockState(String nukiId) {
        logger.debug("getBridgeLockState({}) in thread {}", nukiId, Thread.currentThread().getId());
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
            logger.debug("getBridgeLockState status[{}] response[{}]", status, response);
            if (status == HttpStatus.OK_200) {
                BridgeApiLockStateDto bridgeApiLockStateDto = gson.fromJson(response, BridgeApiLockStateDto.class);
                logger.debug("getBridgeLockState OK");
                return bridgeLockStateResponseCache = new BridgeLockStateResponse(status, contentResponse.getReason(),
                        bridgeApiLockStateDto);
            } else {
                logger.debug("Could not get Lock State! Status[{}] - Response[{}]", status, response);
                return new BridgeLockStateResponse(status, contentResponse.getReason(), null);
            }
        } catch (Exception e) {
            logger.debug("Could not get Bridge Lock State! Exception[{}]", e.getMessage());
            return new BridgeLockStateResponse(handleException(e));
        }
    }

    public BridgeLockActionResponse getBridgeLockAction(String nukiId, int lockAction) {
        logger.debug("getBridgeLockAction({}, {}) in thread {}", nukiId, lockAction, Thread.currentThread().getId());
        String uri = prepareUri(NukiBindingConstants.URI_LOCKACTION, nukiId, Integer.toString(lockAction));
        try {
            ContentResponse contentResponse = executeRequest(uri);
            int status = contentResponse.getStatus();
            String response = contentResponse.getContentAsString();
            logger.debug("getBridgeLockAction status[{}] response[{}]", status, response);
            if (status == HttpStatus.OK_200) {
                BridgeApiLockActionDto bridgeApiLockActionDto = gson.fromJson(response, BridgeApiLockActionDto.class);
                logger.debug("getBridgeLockAction OK");
                return new BridgeLockActionResponse(status, contentResponse.getReason(), bridgeApiLockActionDto);
            } else {
                logger.debug("Could not execute Lock Action! Status[{}] - Response[{}]", status, response);
                return new BridgeLockActionResponse(status, contentResponse.getReason(), null);
            }
        } catch (Exception e) {
            logger.debug("Could not execute Lock Action! Exception[{}]", e.getMessage());
            return new BridgeLockActionResponse(handleException(e));
        }
    }

    public BridgeCallbackAddResponse getBridgeCallbackAdd(String callbackUrl) {
        logger.debug("getBridgeCallbackAdd({}) in thread {}", callbackUrl, Thread.currentThread().getId());
        try {
            String uri = prepareUri(NukiBindingConstants.URI_CBADD, URLEncoder.encode(callbackUrl, "UTF-8"));
            ContentResponse contentResponse = executeRequest(uri);
            int status = contentResponse.getStatus();
            String response = contentResponse.getContentAsString();
            logger.debug("getBridgeCallbackAdd status[{}] response[{}]", status, response);
            if (status == HttpStatus.OK_200) {
                BridgeApiCallbackAddDto bridgeApiCallbackAddDto = gson.fromJson(response,
                        BridgeApiCallbackAddDto.class);
                logger.debug("getBridgeCallbackAdd OK");
                return new BridgeCallbackAddResponse(status, contentResponse.getReason(), bridgeApiCallbackAddDto);
            } else {
                logger.debug("Could not execute Callback Add! Status[{}] - Response[{}]", status, response);
                return new BridgeCallbackAddResponse(status, contentResponse.getReason(), null);
            }
        } catch (Exception e) {
            logger.debug("Could not execute Callback Add! Exception[{}]", e.getMessage());
            return new BridgeCallbackAddResponse(handleException(e));
        }
    }

    public BridgeCallbackListResponse getBridgeCallbackList() {
        logger.debug("getBridgeCallbackList() in thread {}", Thread.currentThread().getId());
        String uri = prepareUri(NukiBindingConstants.URI_CBLIST);
        try {
            ContentResponse contentResponse = executeRequest(uri);
            int status = contentResponse.getStatus();
            String response = contentResponse.getContentAsString();
            logger.debug("getBridgeCallbackList status[{}] response[{}]", status, response);
            if (status == HttpStatus.OK_200) {
                BridgeApiCallbackListDto bridgeApiCallbackListDto = gson.fromJson(response,
                        BridgeApiCallbackListDto.class);
                logger.debug("getBridgeCallbackList OK");
                return new BridgeCallbackListResponse(status, contentResponse.getReason(), bridgeApiCallbackListDto);
            } else {
                logger.debug("Could not execute Callback List! Status[{}] - Response[{}]", status, response);
                return new BridgeCallbackListResponse(status, contentResponse.getReason(), null);
            }
        } catch (Exception e) {
            logger.debug("Could not execute Callback List! Exception[{}]", e.getMessage());
            return new BridgeCallbackListResponse(handleException(e));
        }
    }

    public BridgeCallbackRemoveResponse getBridgeCallbackRemove(int id) {
        logger.debug("getBridgeCallbackRemove({}) in thread {}", id, Thread.currentThread().getId());
        try {
            String uri = prepareUri(NukiBindingConstants.URI_CBREMOVE, Integer.toString(id));
            ContentResponse contentResponse = executeRequest(uri);
            int status = contentResponse.getStatus();
            String response = contentResponse.getContentAsString();
            logger.debug("getBridgeCallbackRemove status[{}] response[{}]", status, response);
            if (status == HttpStatus.OK_200) {
                BridgeApiCallbackRemoveDto bridgeApiCallbackRemoveDto = gson.fromJson(response,
                        BridgeApiCallbackRemoveDto.class);
                logger.debug("getBridgeCallbackRemove OK");
                return new BridgeCallbackRemoveResponse(status, contentResponse.getReason(),
                        bridgeApiCallbackRemoveDto);
            } else {
                logger.debug("Could not execute Callback Remove! Status[{}] - Response[{}]", status, response);
                return new BridgeCallbackRemoveResponse(status, contentResponse.getReason(), null);
            }
        } catch (Exception e) {
            logger.debug("Could not execute Callback Remove! Exception[{}]", e.getMessage());
            return new BridgeCallbackRemoveResponse(handleException(e));
        }
    }
}
