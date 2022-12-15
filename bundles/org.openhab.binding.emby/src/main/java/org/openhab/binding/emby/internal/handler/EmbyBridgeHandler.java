/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.emby.internal.EmbyBindingConstants.API_KEY;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.DISCOVERY_ENABLE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.HOST_PARAMETER;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.REFRESH_PARAMETER;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.WS_BUFFER_SIZE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.WS_PORT_PARAMETER;

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
import org.openhab.core.config.core.Configuration;
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
    private @Nullable EmbyClientDiscoveryService clientDiscoverySerivce;
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
        if (config.api != "") {
            httputils = new EmbyHTTPUtils(30, config.api, getServerAddress());
        }
        ;
        this.webSocketClient = passedWebSocketClient;
        connection = new EmbyConnection(this, passedWebSocketClient);
    }

    public void sendCommand(String commandURL, String payload) {
        if (httputils != null) {
            try {
                httputils.doPost(commandURL, payload, 2);
            } catch (EmbyHttpRetryExceeded e) {
                logger.debug("The number of retry attempts was exceeded", e.getCause());
            }
        }
    }

    public void sendCommand(String commandURL) {
        if (httputils != null) {
            try {
                httputils.doPost(commandURL, "", 2);
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
                }, 360000, 360000, TimeUnit.MILLISECONDS);

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
        EmbyClientDiscoveryService discvoeryService = this.clientDiscoverySerivce;
        if ((discvoeryService != null) && config.discovery) {
            discvoeryService.addDeviceIDDiscover(playstate);
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
        this.clientDiscoverySerivce = embyClientDiscoverySerice;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command was received for thing {}, no command processed as the bridge handler is read only",
                thing.getLabel());
    }

    private void checkConfiguration() throws ConfigValidationException {
        // logger.debug("Checking configuration on thing {}", this.getThing().getUID().getAsString());
        Configuration testConfig = this.getConfig();
        String testApi = (String) testConfig.get(API_KEY);
        String testIpAddress = (String) testConfig.get(HOST_PARAMETER);
        Boolean testDiscovery = (Boolean) testConfig.get(DISCOVERY_ENABLE);
        String testPort = (String) testConfig.get(WS_PORT_PARAMETER);
        String testBufferSize = (String) testConfig.get(WS_BUFFER_SIZE);
        String testRefresehInterval = (String) testConfig.get(REFRESH_PARAMETER);

        if (testDiscovery == null) {
            logger.debug(
                    "There is no value set for the discovery switch, setting this to True to enable discovery.  If you would like to disable automatic discovery please set this to false.");
            config.discovery = true;
        } else {
            config.discovery = testDiscovery;
        }

        try {
            config.bufferSize = Integer.parseInt(testBufferSize);
        } catch (NumberFormatException e) {
            logger.debug(
                    "Please check your configuration of the Retry Count as it is not an Integer. It is configured as: {}, will contintue to configure the binding with the default of 10,0000",
                    testBufferSize);
            config.bufferSize = 10000;
        }

        try {
            config.refreshInterval = Integer.parseInt(testRefresehInterval);
        } catch (NumberFormatException e) {
            logger.debug(
                    "Please check your configuration of the Refresh Interval as is not an Integer. It is configured as: {}, will contintue to configure the binding with the default of 10,000",
                    testRefresehInterval);
            config.refreshInterval = 10000;
        }

        try {
            config.port = Integer.parseInt(testPort);
        } catch (NumberFormatException e) {
            logger.debug(
                    "Please check your configuration of the test port parameter as it is not an Integer. It is configured as: {}, will contintue to configure the binding with the default of 8096",
                    testPort);
            config.port = 8096;
        }

        if (testApi == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "There is no API key configured for this bridge, please add an API key to enable communication");
        } else if (testIpAddress == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No network address specified, please specify the network address for the EMBY server");
        } else {
            config.api = testApi;
            config.ipAddress = testIpAddress;
            httputils = new EmbyHTTPUtils(30, testApi, getServerAddress());
        }
    }

    @Override
    public void dispose() {
        connection.close();
        // connectionCheckerFuture.cancel(true);
    }
}
