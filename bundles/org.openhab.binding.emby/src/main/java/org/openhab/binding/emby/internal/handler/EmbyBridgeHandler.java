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
import org.openhab.binding.emby.internal.protocol.EmbyConnection;
import org.openhab.binding.emby.internal.protocol.EmbyHTTPUtils;
import org.openhab.binding.emby.internal.protocol.EmbyHttpRetryExceeded;
import org.openhab.core.config.core.validation.ConfigValidationException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
    private String lastDiscoveryStatus = null;

    public EmbyBridgeHandler(Bridge bridge, @Nullable String hostAddress, @Nullable String port,
            WebSocketClient passedWebSocketClient) {
        super(bridge);
        this.callbackIpAddress = hostAddress + ":" + port;
        this.webSocketClient = passedWebSocketClient;
    }

    public void sendCommand(String commandURL) {
        logger.debug("Sending command without payload: {}", commandURL);
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
            } catch (InterruptedException ie) {
                // Preserve interrupt and stop trying
                Thread.currentThread().interrupt();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection init interrupted");
            } catch (IOException | URISyntaxException io) {
                // Expected I/O or URI parsing failures
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to connect: " + io.getMessage());
            } catch (EmbyHttpRetryExceeded retryEx) {
                // Too many retries contacting server
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Retry limit exceeded");
            }
        });
    }

    @Override
    public void initialize() {
        // Show initializing
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Initializing Emby bridge");

        // Background setup
        scheduler.execute(() -> {
            this.reconnectionCount = 0;
            this.config = new EmbyBridgeConfiguration();
            this.callbackIpAddress = hostAddress + ":" + port;
            logger.debug("The callback ip address is: {}", callbackIpAddress);
            if (config.api != null && !config.api.isEmpty()) {
                this.httputils = new EmbyHTTPUtils(30, config.api, getServerAddress());
            }
            this.webSocketClient = passedWebSocketClient;
            this.connection = new EmbyConnection(this, passedWebSocketClient);
            try {
                EmbyBridgeConfiguration cfg = checkConfiguration();
                this.config = cfg;
                establishConnection();
                updateStatus(ThingStatus.ONLINE);
            } catch (ConfigValidationException cve) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configuration error: " + cve.getMessage());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Initialization interrupted");
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Initialization failed: " + e.getMessage());
            }
        });
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
        // 1) Handle REFRESH immediately on any bridge channel
        if (command instanceof RefreshType) {
            updateState(channelUID, pollCurrentValueBridge(channelUID));
            return;
        }

        // 2) Everything else is read-only
        logger.trace("Bridge received command '{}' on {}, but no write operations are supported here", command,
                channelUID.getId());
    }

    /**
     * Called by the discovery service to update the status string
     * that backs the “discoveryStatus” channel.
     */
    public void updateDiscoveryStatus(String status) {
        this.lastDiscoveryStatus = status;
        // optionally push it immediately so rules/UI see it
        updateState(new ChannelUID(getThing().getUID(), "discoveryStatus"), new StringType(status));
    }

    private EmbyBridgeConfiguration checkConfiguration() throws ConfigValidationException {
        EmbyBridgeConfiguration embyConfig = getConfigAs(EmbyBridgeConfiguration.class);

        if (embyConfig.api == null || embyConfig.api.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "There is no API key configured for this bridge, please add an API key to enable communication");
            throw new ConfigValidationException("There is no API key configured for this bridge.");
        }

        if (embyConfig.ipAddress == null || embyConfig.ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No network address specified, please specify the network address for the EMBY server");
            throw new ConfigValidationException("No network address specified, please specify the network address for the EMBY server");
        }

        if (this.httputils == null && config.api != null && !config.api.isEmpty()) {
            this.httputils = new EmbyHTTPUtils(30, config.api, getServerAddress());
        }
        return embyConfig;
    }

    private State pollCurrentValueBridge(ChannelUID channelUID) {
        String id = channelUID.getId();
        switch (id) {
            case "serverReachable":
                // Reflect the actual ThingStatus of the bridge
                return (getThing().getStatus() == ThingStatus.ONLINE) ? OnOffType.ON : OnOffType.OFF;

            case "discoveryStatus":
                // A simple text status you update as discovery runs
                return (lastDiscoveryStatus != null) ? new StringType(lastDiscoveryStatus) : UnDefType.UNDEF;
            default:
                return UnDefType.UNDEF;
        }
    }

    @Override
    public void dispose() {
        // 1) Cancel the periodic connection check
        if (connectionCheckerFuture != null && !connectionCheckerFuture.isCancelled()) {
            connectionCheckerFuture.cancel(true);
        }
        // 2) Close the Emby connection
        if (connection != null) {
            connection.close();
        }
        // 3) Let the super class do its cleanup (threads, etc.)
        super.dispose();
    }
}
