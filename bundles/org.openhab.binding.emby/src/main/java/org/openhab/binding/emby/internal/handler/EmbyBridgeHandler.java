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
package org.openhab.binding.emby.internal.handler;

import static org.openhab.binding.emby.internal.EmbyBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.emby.internal.EmbyBridgeListener;
import org.openhab.binding.emby.internal.discovery.EmbyClientDiscoveryService;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.openhab.binding.emby.internal.protocol.EmbyConnection;
import org.openhab.binding.emby.internal.protocol.EmbyHTTPUtils;
import org.openhab.binding.emby.internal.protocol.EmbyHttpRetryExceeded;
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

    public EmbyBridgeHandler(Bridge bridge, @Nullable String hostAddress, @Nullable String port) {
        super(bridge);
        callbackIpAddress = hostAddress + ":" + port;
        logger.debug("The callback ip address is: {}", callbackIpAddress);
        httputils = new EmbyHTTPUtils(30, (String) this.getConfig().get(API_KEY), getServerAddress());
        connection = new EmbyConnection(this);
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

    @Nullable
    public String sendCommandWithReturn(String commandURL) {
        try {
            return httputils.doPost(commandURL, "", 2);
        } catch (EmbyHttpRetryExceeded e) {
            logger.debug("The number of retry attempts was exceeded", e.getCause());
            return null;
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

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        // Example for background initialization:
        scheduler.execute(() -> {
            try {
                String host = getConfig().get(HOST_PARAMETER).toString();
                if (host == null || host.isEmpty()) {
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
                logger.debug("error during opening connection: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        });
    }

    @Override
    public void handleEvent(EmbyPlayStateModel playstate, String hostname, int embyport) {
        logger.debug("Received event from emby server");
        this.clientDiscoverySerivce.addDeviceIDDiscover(playstate);
        this.getThing().getThings().forEach(thing -> {
            EmbyDeviceHandler handler = (EmbyDeviceHandler) thing.getHandler();
            logger.debug("Checking to see if handler is null");
            if (!(handler == null)) {
                handler.handleEvent(playstate, hostname, embyport);
            } else {
                logger.debug("There is no handler for thing {}", thing.getLabel());
            }
        });
    }

    @Override
    public void updateConnectionState(boolean connected) {
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No connection established");
        }
    }

    public void registerDeviceFoundListener(EmbyClientDiscoveryService embyClientDiscoverySerice) {
        this.clientDiscoverySerivce = embyClientDiscoverySerice;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("The bridge handler is read only");
    }
}
