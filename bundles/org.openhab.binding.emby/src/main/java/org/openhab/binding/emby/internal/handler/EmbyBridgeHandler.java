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
import static org.openhab.binding.emby.internal.EmbyBindingConstants.HOST_PARAMETER;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.REFRESH_PARAMETER;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.WS_BUFFER_SIZE;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.WS_PORT_PARAMETER;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.emby.internal.EmbyBridgeListener;
import org.openhab.binding.emby.internal.discovery.EmbyClientDiscoveryService;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.openhab.binding.emby.internal.protocol.EmbyConnection;
import org.openhab.binding.emby.internal.protocol.EmbyHTTPUtils;
import org.openhab.binding.emby.internal.protocol.EmbyHttpRetryExceeded;
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

    private volatile EmbyConnection connection; // volatile because accessed from multiple threads
    private @Nullable ScheduledFuture<?> connectionCheckerFuture;
    private @Nullable String callbackIpAddress = null;
    private @Nullable EmbyClientDiscoveryService clientDiscoverySerivce;
    private EmbyHTTPUtils httputils;

    public EmbyBridgeHandler(Bridge bridge, @Nullable String hostAddress, @Nullable String port,
            WebSocketClient WebSocketClient) {
        super(bridge);
        callbackIpAddress = hostAddress + ":" + port;
        logger.debug("The callback ip address is: {}", callbackIpAddress);
        httputils = new EmbyHTTPUtils(30, (String) this.getConfig().get(API_KEY), getServerAddress());
        connection = new EmbyConnection(this, WebSocketClient);
    }

    public void sendCommand(String commandURL, String payload) {
        try {
            httputils.doPost(commandURL, payload, 2);
        } catch (EmbyHttpRetryExceeded e) {
            logger.debug("The number of retry attempts was exceeded", e.getCause());
        }
    }

    public void sendCommand(String commandURL) {
        try {
            httputils.doPost(commandURL, "", 2);
        } catch (EmbyHttpRetryExceeded e) {
            logger.debug("The number of retry attempts was exceeded", e.getCause());
        }
    }

    private String getServerAddress() {
        String host = getConfig().get(HOST_PARAMETER).toString();
        String port = Integer.toString(getIntConfigParameter(WS_PORT_PARAMETER, 8096));

        return host + ":" + port;
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
                String host = getConfig().get(HOST_PARAMETER).toString();
                if (getConfig().get(HOST_PARAMETER) == null || host.isEmpty()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No network address specified");
                } else {
                    connection.connect(host, getIntConfigParameter(WS_PORT_PARAMETER, 8096),
                            (String) this.getConfig().get(API_KEY), scheduler,
                            getIntConfigParameter(REFRESH_PARAMETER, 10000),
                            getIntConfigParameter(WS_BUFFER_SIZE, 100000));

                    connectionCheckerFuture = scheduler.scheduleWithFixedDelay(() -> {
                        if (!(connection.checkConnection())) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Connection could not be established");
                        }
                    }, 1, getIntConfigParameter(REFRESH_PARAMETER, 10), TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        });
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        establishConnection();
    }

    @Override
    public void handleEvent(EmbyPlayStateModel playstate, String hostname, int embyport) {
        EmbyClientDiscoveryService discvoeryService = this.clientDiscoverySerivce;
        if (discvoeryService != null) {
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
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "The connection to the emby server was closed, binding will attempt to restablish connection.");
                    establishConnection();
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
}
