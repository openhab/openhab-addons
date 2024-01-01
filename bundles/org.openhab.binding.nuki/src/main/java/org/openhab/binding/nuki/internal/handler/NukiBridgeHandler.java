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
package org.openhab.binding.nuki.internal.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.nuki.internal.configuration.NukiBridgeConfiguration;
import org.openhab.binding.nuki.internal.constants.NukiBindingConstants;
import org.openhab.binding.nuki.internal.constants.NukiLinkBuilder;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackAddResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackListResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackRemoveResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeInfoResponse;
import org.openhab.binding.nuki.internal.dataexchange.NukiHttpClient;
import org.openhab.binding.nuki.internal.discovery.NukiDeviceDiscoveryService;
import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackListCallbackDto;
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
 * @author Jan Vybíral - Improved callback handling
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
    private NukiBridgeConfiguration config = new NukiBridgeConfiguration();

    public NukiBridgeHandler(Bridge bridge, HttpClient httpClient, @Nullable String callbackUrl) {
        super(bridge);
        logger.debug("Instantiating NukiBridgeHandler({}, {}, {})", bridge, httpClient, callbackUrl);
        this.callbackUrl = callbackUrl;
        this.httpClient = httpClient;
    }

    public @Nullable NukiHttpClient getNukiHttpClient() {
        return this.nukiHttpClient;
    }

    public void withHttpClient(Consumer<NukiHttpClient> consumer) {
        withHttpClient(client -> {
            consumer.accept(client);
            return null;
        }, null);
    }

    protected <@Nullable U> @Nullable U withHttpClient(Function<NukiHttpClient, U> consumer, U defaultValue) {
        NukiHttpClient client = this.nukiHttpClient;
        if (client == null) {
            logger.warn("Nuki HTTP client is null. This is a bug in Nuki Binding, please report it",
                    new IllegalStateException());
            return defaultValue;
        } else {
            return consumer.apply(client);
        }
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(NukiBridgeConfiguration.class);
        String ip = config.ip;
        Integer port = config.port;
        String apiToken = config.apiToken;

        if (ip == null || port == null) {
            logger.debug("NukiBridgeHandler[{}] is not initializable, IP setting is unset in the configuration!",
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "IP setting is unset");
        } else if (apiToken == null || apiToken.isBlank()) {
            logger.debug("NukiBridgeHandler[{}] is not initializable, apiToken setting is unset in the configuration!",
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "apiToken setting is unset");
        } else {
            NukiLinkBuilder linkBuilder = new NukiLinkBuilder(ip, port, apiToken, this.config.secureToken);
            nukiHttpClient = new NukiHttpClient(httpClient, linkBuilder);
            scheduler.execute(this::initializeHandler);
            checkBridgeOnlineJob = scheduler.scheduleWithFixedDelay(this::checkBridgeOnline, JOB_INTERVAL, JOB_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand({}, {}) for Bridge[{}] not implemented!", channelUID, command, this.config.ip);
    }

    @Override
    public void dispose() {
        logger.debug("dispose() for Bridge[{}].", getThing().getUID());
        if (this.config.manageCallbacks) {
            unregisterCallback();
        }
        nukiHttpClient = null;
        ScheduledFuture<?> job = checkBridgeOnlineJob;
        if (job != null) {
            job.cancel(true);
        }
        checkBridgeOnlineJob = null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(NukiDeviceDiscoveryService.class);
    }

    private synchronized void initializeHandler() {
        withHttpClient(client -> {
            BridgeInfoResponse bridgeInfoResponse = client.getBridgeInfo();
            if (bridgeInfoResponse.getStatus() == HttpStatus.OK_200) {
                updateProperty(NukiBindingConstants.PROPERTY_FIRMWARE_VERSION, bridgeInfoResponse.getFirmwareVersion());
                updateProperty(NukiBindingConstants.PROPERTY_WIFI_FIRMWARE_VERSION,
                        bridgeInfoResponse.getWifiFirmwareVersion());
                updateProperty(NukiBindingConstants.PROPERTY_HARDWARE_ID,
                        Integer.toString(bridgeInfoResponse.getHardwareId()));
                updateProperty(NukiBindingConstants.PROPERTY_SERVER_ID,
                        Integer.toString(bridgeInfoResponse.getServerId()));
                if (this.config.manageCallbacks) {
                    manageNukiBridgeCallbacks();
                }
                logger.debug("Bridge[{}] responded with status[{}]. Switching the bridge online.", this.config.ip,
                        bridgeInfoResponse.getStatus());
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Bridge[{}] responded with status[{}]. Switching the bridge offline!", this.config.ip,
                        bridgeInfoResponse.getStatus());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        bridgeInfoResponse.getMessage());
            }
        });
    }

    public void checkBridgeOnline() {
        logger.debug("checkBridgeOnline():bridgeIp[{}] status[{}]", this.config.ip, getThing().getStatus());
        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
            withHttpClient(client -> {
                logger.debug("Requesting BridgeInfo to ensure Bridge[{}] is online.", this.config.ip);
                BridgeInfoResponse bridgeInfoResponse = client.getBridgeInfo();
                int status = bridgeInfoResponse.getStatus();
                if (status == HttpStatus.OK_200) {
                    logger.debug("Bridge[{}] responded with status[{}]. Bridge is online.", this.config.ip, status);
                } else if (status == HttpStatus.SERVICE_UNAVAILABLE_503) {
                    logger.debug(
                            "Bridge[{}] responded with status[{}]. REST service seems to be busy but Bridge is online.",
                            this.config.ip, status);
                } else {
                    logger.debug("Bridge[{}] responded with status[{}]. Switching the bridge offline!", this.config.ip,
                            status);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            bridgeInfoResponse.getMessage());
                }
            });
        } else {
            initializeHandler();
        }
    }

    private boolean isHttpClientNull() {
        NukiHttpClient httpClient = getNukiHttpClient();
        if (httpClient == null) {
            logger.debug("HTTP Client not configured, switching bridge to OFFLINE");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "HTTP Client not configured");
            return true;
        } else {
            return false;
        }
    }

    private @Nullable List<BridgeApiCallbackListCallbackDto> listCallbacks() {
        if (isHttpClientNull()) {
            return Collections.emptyList();
        }

        return withHttpClient(client -> {
            BridgeCallbackListResponse bridgeCallbackListResponse = client.getBridgeCallbackList();
            if (bridgeCallbackListResponse.isSuccess()) {
                return bridgeCallbackListResponse.getCallbacks();
            } else {
                logger.debug("Failed to list callbacks for Bridge[{}] - status {}, message {}", this.config.ip,
                        bridgeCallbackListResponse.getStatus(), bridgeCallbackListResponse.getMessage());
                return null;
            }
        }, null);
    }

    private void manageNukiBridgeCallbacks() {
        String callback = callbackUrl;
        if (callback == null) {
            logger.debug("Cannot manage callbacks - no URL available");
            return;
        }

        logger.debug("manageNukiBridgeCallbacks() for Bridge[{}].", this.config.ip);

        List<BridgeApiCallbackListCallbackDto> callbacks = listCallbacks();
        if (callbacks == null) {
            return;
        }

        List<Integer> callbacksToRemove = new ArrayList<>(3);

        // callback already registered - do nothing
        if (callbacks.stream().anyMatch(cb -> cb.getUrl().equals(callback))) {
            logger.debug("callbackUrl[{}] already existing on Bridge[{}].", callbackUrl, this.config.ip);
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
            logger.debug("Already 3 callback URLs existing on Bridge[{}] - Removing ID 0!", this.config.ip);
            callbacksToRemove.add(0);
        }

        callbacksToRemove.forEach(callbackId -> {
            withHttpClient(client -> {
                BridgeCallbackRemoveResponse bridgeCallbackRemoveResponse = client.getBridgeCallbackRemove(callbackId);
                if (bridgeCallbackRemoveResponse.getStatus() == HttpStatus.OK_200) {
                    logger.debug("Successfully removed callbackUrl[{}] on Bridge[{}]!", callback, this.config.ip);
                }
            });
        });

        withHttpClient(client -> {
            logger.debug("Adding callbackUrl[{}] to Bridge[{}]!", callbackUrl, this.config.ip);
            BridgeCallbackAddResponse bridgeCallbackAddResponse = client.getBridgeCallbackAdd(callback);
            if (bridgeCallbackAddResponse.getStatus() == HttpStatus.OK_200) {
                logger.debug("Successfully added callbackUrl[{}] on Bridge[{}]!", callback, this.config.ip);
            }
        });
    }

    private void unregisterCallback() {
        List<BridgeApiCallbackListCallbackDto> callbacks = listCallbacks();
        if (callbacks == null) {
            return;
        }

        callbacks.stream().filter(callback -> callback.getUrl().equals(callbackUrl))
                .map(BridgeApiCallbackListCallbackDto::getId)
                .forEach(callbackId -> withHttpClient(client -> client.getBridgeCallbackRemove(callbackId)));
    }
}
