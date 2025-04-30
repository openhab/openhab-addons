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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
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
import org.openhab.core.config.core.validation.ConfigValidationMessage;
import org.openhab.core.i18n.TranslationProvider;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Reference;
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
    private @Nullable EmbyBridgeConfiguration config;
    private int reconnectionCount;
    private @Nullable String lastDiscoveryStatus = null;
    private int port;
    @Reference
    private @Nullable TranslationProvider i18nProvider;

    public EmbyBridgeHandler(Bridge bridge, @Nullable String hostAddress, @Nullable String port,
            WebSocketClient webSocketClient) {
        super(bridge);

        if (hostAddress == null || port == null) {
            throw new IllegalArgumentException("Host address and port must not be null");
        }

        // Validate host
        try {
            InetAddress.getByName(hostAddress);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid host address: " + hostAddress);
        }

        // Validate port
        int portNumber;
        try {
            portNumber = Integer.parseInt(port);
            if (portNumber < 1 || portNumber > 65535) {
                throw new IllegalArgumentException("Port number out of range: " + port);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port number: " + port);
        }

        this.callbackIpAddress = hostAddress + ":" + portNumber;
        this.webSocketClient = webSocketClient;
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

    private void establishConnection() {
        // Ensure scheduler is non-null (BaseBridgeHandler always provides one, but it's annotated @Nullable)
        final ScheduledExecutorService exec = java.util.Objects.requireNonNull(scheduler, "scheduler must not be null");

        exec.execute(() -> {
            try {
                // Build a full HTTP URL from host + port
                String httpBaseUrl = String.format("http://%s:%d", config.ipAddress, config.port);

                // Call the instance connect(...) with the 5-arg signature
                connection.connect(config.ipAddress, // String hostName
                        config.port, // int port
                        config.api, // String apiKey
                        exec, // ScheduledExecutorService
                        config.refreshInterval, // int refreshRate
                        config.bufferSize // int bufferSize
                );
                // Schedule periodic connection checks
                connectionCheckerFuture = exec.scheduleWithFixedDelay(() -> {
                    if (!connection.checkConnection()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Connection could not be established");
                    }
                }, CONNECTION_CHECK_INTERVAL_MS, CONNECTION_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Connection attempt failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void initialize() {
        // Indicate that we're trying to connect
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Attempting to connect to Emby server");

        // Background setup
        scheduler.execute(() -> {
            this.reconnectionCount = 0;
            this.config = new EmbyBridgeConfiguration();
            if (callbackIpAddress != null) {
                logger.debug("The callback ip address is: {}", callbackIpAddress);
            } else {
                logger.warn("Callback IP address is not set");
            }
            if (config.api != null && !config.api.isEmpty()) {
                this.httputils = new EmbyHTTPUtils(30, config.api, getServerAddress());
            }
            this.connection = new EmbyConnection(this, this.webSocketClient);
            try {
                EmbyBridgeConfiguration cfg = checkConfiguration();
                this.config = cfg;
                establishConnection();
            } catch (ConfigValidationException cve) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configuration error: " + cve.getMessage());
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
            throwValidationError("api", "Missing value for: api");

        }

        if (embyConfig.ipAddress == null || embyConfig.ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No network address specified, please specify the network address for the EMBY server");
            throwValidationError("ipAddress", "Missing value for: ipAddress");

        }

        if (this.httputils == null && config != null && config.api != null && !config.api.isEmpty()) {
            this.httputils = new EmbyHTTPUtils(30, config.api, getServerAddress());
        }
        return embyConfig;
    }

    private void throwValidationError(String parameterName, String errorMessage) throws ConfigValidationException {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        ConfigValidationMessage message = new ConfigValidationMessage(parameterName, "error", errorMessage);
        throw new ConfigValidationException(bundle, this.i18nProvider, Collections.singletonList(message));
    }

    private State pollCurrentValueBridge(ChannelUID channelUID) {
        String id = channelUID.getId();
        switch (id) {
            case "serverReachable":
                // Reflect the actual ThingStatus of the bridge
                return (getThing().getStatus() == ThingStatus.ONLINE) ? OnOffType.ON : OnOffType.OFF;

            case "discoveryStatus":
                // A simple text status you update as discovery runs
                return (this.lastDiscoveryStatus != null) ? new StringType(this.lastDiscoveryStatus) : UnDefType.UNDEF;
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
