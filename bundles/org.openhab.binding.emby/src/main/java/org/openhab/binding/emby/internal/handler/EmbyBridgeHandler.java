/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal.handler;

import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONNECTION_CHECK_INTERVAL_MS;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.emby.internal.EmbyBridgeConfiguration;
import org.openhab.binding.emby.internal.EmbyBridgeListener;
import org.openhab.binding.emby.internal.discovery.EmbyClientDiscoveryService;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.openhab.binding.emby.internal.protocol.EmbyConnection;
import org.openhab.binding.emby.internal.protocol.EmbyHTTPUtils;
import org.openhab.binding.emby.internal.protocol.EmbyHttpRetryExceeded;
import org.openhab.core.config.core.validation.ConfigValidationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EmbyBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyBridgeHandler extends BaseBridgeHandler implements EmbyBridgeListener {

    private final Logger logger = LoggerFactory.getLogger(EmbyBridgeHandler.class);

    private volatile @Nullable EmbyConnection connection; // volatile because accessed from multiple threads
    private WebSocketClient webSocketClient;
    private @Nullable ScheduledFuture<?> connectionCheckerFuture;
    private @Nullable String callbackIpAddress = null;
    private @Nullable EmbyClientDiscoveryService clientDiscoveryService;
    private @Nullable EmbyHTTPUtils httputils;
    private EmbyBridgeConfiguration config;
    private int reconnectionCount;

    public EmbyBridgeHandler(Bridge bridge, @Nullable String hostAddress, @Nullable String port,
            WebSocketClient passedWebSocketClient) {
        super(bridge);
        reconnectionCount = 0;
        config = new EmbyBridgeConfiguration();
        checkConfiguration();
        callbackIpAddress = hostAddress + ":" + port;
        logger.debug("The callback ip address is: {}", callbackIpAddress);
        if (config.api != null && !config.api.isEmpty()) {
            httputils = new EmbyHTTPUtils(30, config.api, getServerAddress());
        }
        ;
        this.webSocketClient = passedWebSocketClient;
        connection = new EmbyConnection(this, passedWebSocketClient);
    }

    public void sendCommand(String commandURL) {
        logger.trace("Sending command without payload: {}", commandURL);
        if (httputils != null) {
            try {
                httputils.doPost(commandURL, "", 2);
            } catch (EmbyHttpRetryExceeded e) {
                logger.debug("The number of retry attempts was exceeded", e.getCause());
            }
        }
    }

    public void sendCommand(String commandURL, String payload) {
        logger.trace("Sending command: {} with payload: {}", commandURL, payload);
        if (httputils != null) {
            try {
                httputils.doPost(commandURL, payload, 2);
            } catch (EmbyHttpRetryExceeded e) {
                logger.debug("The number of retry attempts was exceeded", e.getCause());
            }
        }
    }

    private String getServerAddress() {
        return config.ipAddress + ":" + config.port;
    }

    private int getIntConfigParameter(String key, int defaultValue) {
        Object obj = this.getConfig().get(key);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            return Integer.parseInt(obj.toString());
        }
        return defaultValue;
    }

    private void establishConnection() {
        scheduler.execute(() -> {
            try {
                connection.connect(config.ipAddress, config.port, config.api, scheduler, config.refreshInterval,
                        config.bufferSize);

                connectionCheckerFuture = scheduler.scheduleWithFixedDelay(() -> {
                    if (!(connection.checkConnection())) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Connection could not be established");
                    }
                }, CONNECTION_CHECK_INTERVAL_MS, CONNECTION_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        });
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        try {
            checkConfiguration();
        } catch (ConfigValidationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
        establishConnection();
    }

    @Override
    public void handleEvent(EmbyPlayStateModel playstate, String hostname, int embyport) {
        EmbyClientDiscoveryService discoveryService = this.clientDiscoveryService;
        if ((discoveryService != null) && config.discovery) {
            discoveryService.addDeviceIDDiscover(playstate);
        }
        this.getThing().getThings().forEach(thing -> {
            EmbyDeviceHandler handler = (EmbyDeviceHandler) thing.getHandler();
            if (handler != null) {
                handler.handleEvent(playstate, hostname, embyport);
                logger.trace("Handler was found for thing {}", thing.getLabel());
            } else {
                logger.trace("There is no handler for thing {}", thing.getLabel());
            }
        });
    }

    @Override
    public void updateConnectionState(boolean connected) {
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
            reconnectionCount = 0;
        } else {
            reconnectionCount++;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "The connection to the emby server was closed, binding will wait one min and attempt to restablish connection. There have been "
                            + Integer.toString(reconnectionCount) + " attempts to establish a new connection");
            scheduler.schedule(() -> {
                establishConnection();
            }, 60000, TimeUnit.MILLISECONDS);
        }
    }

    public void registerDeviceFoundListener(EmbyClientDiscoveryService embyClientDiscoverySerice) {
        this.clientDiscoveryService = embyClientDiscoverySerice;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command was received for thing {}, no command processed as the bridge handler is read only",
                thing.getLabel());
    }

    private void checkConfiguration() throws ConfigValidationException {
        EmbyBridgeConfiguration embyConfig = getConfigAs(EmbyBridgeConfiguration.class);

        if (embyConfig.api == null || embyConfig.api.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "There is no API key configured for this bridge, please add an API key to enable communication");
        }

        if (embyConfig.ipAddress == null || embyConfig.ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No network address specified, please specify the network address for the EMBY server");
        }

        this.config = embyConfig;

        if (httputils == null && config.api != null && !config.api.isEmpty()) {
            httputils = new EmbyHTTPUtils(30, config.api, getServerAddress());
        }
    }

    @Override
    public void dispose() {
        connection.close();
    }
}
