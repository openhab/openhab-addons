/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nuki.internal.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.nuki.internal.constants.NukiBindingConstants;
import org.openhab.binding.nuki.internal.constants.NukiLinkBuilder;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackAddResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackListResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackRemoveResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeInfoResponse;
import org.openhab.binding.nuki.internal.dataexchange.NukiHttpClient;
import org.openhab.binding.nuki.internal.discovery.NukiDeviceDiscoveryService;
import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackListCallbackDto;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NukiBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Katter - Initial contribution
 * @contributer Jan Vybíral - Improved callback handling
 */
@NonNullByDefault
public class NukiBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NukiBridgeHandler.class);
    private static final int JOB_INTERVAL = 600;

    private final HttpClient httpClient;
    @Nullable
    private NukiHttpClient nukiHttpClient;
    @Nullable
    private final String callbackUrl;
    @Nullable
    private ScheduledFuture<?> checkBridgeOnlineJob;
    @Nullable
    private String bridgeIp;
    private boolean manageCallbacks;
    private boolean initializable;

    public NukiBridgeHandler(Bridge bridge, HttpClient httpClient, @Nullable String callbackUrl) {
        super(bridge);
        logger.debug("Instantiating NukiBridgeHandler({}, {}, {})", bridge, httpClient, callbackUrl);
        this.callbackUrl = callbackUrl;
        this.httpClient = httpClient;

        // initialize config from discovered propeties
        initConfigFromProperty(NukiBindingConstants.PROPERTY_BRIDGE_IP, NukiBindingConstants.CONFIG_IP,
                String::toString);
        initConfigFromProperty(NukiBindingConstants.PROPERTY_BRIDGE_PORT, NukiBindingConstants.CONFIG_PORT,
                value -> new BigDecimal(value).intValue());
        initConfigFromProperty(NukiBindingConstants.PROPERTY_BRIDGE_TOKEN, NukiBindingConstants.CONFIG_API_TOKEN,
                String::toString);

        this.initializable = getConfig().get(NukiBindingConstants.CONFIG_IP) != null
                && getConfig().get(NukiBindingConstants.CONFIG_API_TOKEN) != null
                && getConfig().get(NukiBindingConstants.CONFIG_PORT) != null;
    }

    private void initConfigFromProperty(String propertyName, String configName, Function<String, Object> converter) {
        String propertyValue = thing.getProperties().get(propertyName);
        if (propertyValue != null) {
            getConfig().put(configName, converter.apply(propertyValue));
            thing.setProperty(propertyName, null);
        }
    }

    @Nullable
    public NukiHttpClient getNukiHttpClient() {
        return nukiHttpClient;
    }

    public boolean isInitializable() {
        return initializable;
    }

    @Nullable
    private String getStringConfig(String key) {
        Object value = getConfig().get(key);
        return value == null ? null : value.toString();
    }

    @Nullable
    private Integer getIntConfig(String key) {
        Object value = getConfig().get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            return null;
        }
    }

    private boolean getBooleanConfig(String key, boolean defaultValue) {
        Object value = getConfig().get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return defaultValue;
        }
    }

    @Override
    public void initialize() {
        logger.debug("initialize() for Bridge[{}].", getThing().getUID());
        Configuration config = getConfig();
        @Nullable
        String bridgeIpLocal = bridgeIp = getStringConfig(NukiBindingConstants.CONFIG_IP);
        @Nullable
        Integer bridgePort = getIntConfig(NukiBindingConstants.CONFIG_PORT);
        @Nullable
        String token = getStringConfig(NukiBindingConstants.CONFIG_API_TOKEN);
        boolean secureToken = getBooleanConfig(NukiBindingConstants.CONFIG_SECURE_TOKEN, true);

        manageCallbacks = (Boolean) config.get(NukiBindingConstants.CONFIG_MANAGECB);
        if (bridgeIpLocal == null || bridgePort == null) {
            logger.debug("NukiBridgeHandler[{}] is not initializable, IP setting is unset in the configuration!",
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "IP setting is unset");
        } else if (token == null) {
            logger.debug("NukiBridgeHandler[{}] is not initializable, apiToken setting is unset in the configuration!",
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "apiToken setting is unset");
        } else {
            NukiLinkBuilder linkBuilder = new NukiLinkBuilder(bridgeIpLocal, bridgePort, token, secureToken);
            nukiHttpClient = new NukiHttpClient(httpClient, linkBuilder);
            scheduler.execute(this::initializeHandler);
            checkBridgeOnlineJob = scheduler.scheduleWithFixedDelay(this::checkBridgeOnline, JOB_INTERVAL, JOB_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand({}, {}) for Bridge[{}] not implemented!", channelUID, command, bridgeIp);
    }

    @Override
    public void dispose() {
        logger.debug("dispose() for Bridge[{}].", getThing().getUID());
        if (manageCallbacks) {
            unregisterCallback();
        }
        nukiHttpClient = null;
        if (checkBridgeOnlineJob != null && !checkBridgeOnlineJob.isCancelled()) {
            checkBridgeOnlineJob.cancel(true);
        }
        checkBridgeOnlineJob = null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(NukiDeviceDiscoveryService.class);
    }

    private synchronized void initializeHandler() {
        logger.debug("initializeHandler() for Bridge[{}].", bridgeIp);

        BridgeInfoResponse bridgeInfoResponse = getNukiHttpClient().getBridgeInfo();
        if (bridgeInfoResponse.getStatus() == HttpStatus.OK_200) {
            updateProperty(NukiBindingConstants.PROPERTY_FIRMWARE_VERSION, bridgeInfoResponse.getFirmwareVersion());
            updateProperty(NukiBindingConstants.PROPERTY_WIFI_FIRMWARE_VERSION,
                    bridgeInfoResponse.getWifiFirmwareVersion());
            updateProperty(NukiBindingConstants.PROPERTY_HARDWARE_ID,
                    Integer.toString(bridgeInfoResponse.getHardwareId()));
            updateProperty(NukiBindingConstants.PROPERTY_SERVER_ID, Integer.toString(bridgeInfoResponse.getServerId()));
            if (manageCallbacks) {
                manageNukiBridgeCallbacks();
            }
            logger.debug("Bridge[{}] responded with status[{}]. Switching the bridge online.", bridgeIp,
                    bridgeInfoResponse.getStatus());
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Bridge[{}] responded with status[{}]. Switching the bridge offline!", bridgeIp,
                    bridgeInfoResponse.getStatus());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, bridgeInfoResponse.getMessage());
        }
    }

    public void checkBridgeOnline() {
        logger.debug("checkBridgeOnline():bridgeIp[{}] status[{}]", bridgeIp, getThing().getStatus());
        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Requesting BridgeInfo to ensure Bridge[{}] is online.", bridgeIp);
            BridgeInfoResponse bridgeInfoResponse = getNukiHttpClient().getBridgeInfo();
            int status = bridgeInfoResponse.getStatus();
            if (status == HttpStatus.OK_200) {
                logger.debug("Bridge[{}] responded with status[{}]. Bridge is online.", bridgeIp, status);
            } else if (status == HttpStatus.SERVICE_UNAVAILABLE_503) {
                logger.debug(
                        "Bridge[{}] responded with status[{}]. REST service seems to be busy but Bridge is online.",
                        bridgeIp, status);
            } else {
                logger.debug("Bridge[{}] responded with status[{}]. Switching the bridge offline!", bridgeIp, status);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        bridgeInfoResponse.getMessage());
            }
        } else {
            initializeHandler();
        }
    }

    @Nullable
    private List<BridgeApiCallbackListCallbackDto> listCallbacks() {
        BridgeCallbackListResponse bridgeCallbackListResponse = getNukiHttpClient().getBridgeCallbackList();
        if (bridgeCallbackListResponse.isSuccess()) {
            return bridgeCallbackListResponse.getCallbacks();
        } else {
            logger.debug("Failed to list callbacks for Bridge[{}] - status {}, message {}", bridgeIp,
                    bridgeCallbackListResponse.getStatus(), bridgeCallbackListResponse.getMessage());
            return null;
        }
    }

    private void manageNukiBridgeCallbacks() {
        String callback = callbackUrl;
        if (callback == null) {
            logger.debug("Cannot manage callbacks - no URL available");
            return;
        }

        logger.debug("manageNukiBridgeCallbacks() for Bridge[{}].", bridgeIp);

        List<BridgeApiCallbackListCallbackDto> callbacks = listCallbacks();
        if (callbacks == null) {
            return;
        }

        List<Integer> callbacksToRemove = new ArrayList<>(3);

        // callback already registered - do nothing
        if (callbacks.stream().anyMatch(cb -> cb.getUrl().equals(callback))) {
            logger.debug("callbackUrl[{}] already existing on Bridge[{}].", callbackUrl, bridgeIp);
            return;
        }
        // delete callbacks for this bridge registered for different host
        String path = NukiLinkBuilder.callbackPath(getThing().getUID().getId()).build().toString();
        callbacks.stream().filter(cb -> cb.getUrl().endsWith(path)).map(BridgeApiCallbackListCallbackDto::getId)
                .forEach(callbacksToRemove::add);
        // delete callbacks for this bridge registered without bridgeId query (created by previous binding version)
        String urlWithoutQuery = UriBuilder.fromUri(callback).replaceQuery("").build().toString();
        callbacks.stream().filter(cb -> cb.getUrl().equals(urlWithoutQuery))
                .map(BridgeApiCallbackListCallbackDto::getId).forEach(callbacksToRemove::add);

        if (callbacks.size() - callbacksToRemove.size() == 3) {
            logger.debug("Already 3 callback URLs existing on Bridge[{}] - Removing ID 0!", bridgeIp);
            callbacksToRemove.add(0);
        }

        callbacksToRemove.forEach(callbackId -> {
            BridgeCallbackRemoveResponse bridgeCallbackRemoveResponse = getNukiHttpClient()
                    .getBridgeCallbackRemove(callbackId);
            if (bridgeCallbackRemoveResponse.getStatus() == HttpStatus.OK_200) {
                logger.debug("Successfully removed callbackUrl[{}] on Bridge[{}]!", callback, bridgeIp);
            }
        });

        logger.debug("Adding callbackUrl[{}] to Bridge[{}]!", callbackUrl, bridgeIp);
        BridgeCallbackAddResponse bridgeCallbackAddResponse = getNukiHttpClient().getBridgeCallbackAdd(callback);
        if (bridgeCallbackAddResponse.getStatus() == HttpStatus.OK_200) {
            logger.debug("Successfully added callbackUrl[{}] on Bridge[{}]!", callback, bridgeIp);
        }
    }

    private void unregisterCallback() {
        List<BridgeApiCallbackListCallbackDto> callbacks = listCallbacks();
        if (callbacks == null) {
            return;
        }

        callbacks.stream().filter(callback -> callback.getUrl().equals(callbackUrl))
                .map(BridgeApiCallbackListCallbackDto::getId)
                .forEach(callbackId -> getNukiHttpClient().getBridgeCallbackRemove(callbackId));
    }
}
