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

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONNECTION_CHECK_INTERVAL_MS;

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
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EmbyBridgeHandler} is responsible for handling commands,
 * managing the connection to Emby and dispatching play‐state events.
 * 
 * @author Zachary Christiansen - Initial Contribution
 */
@NonNullByDefault
public class EmbyBridgeHandler extends BaseBridgeHandler implements EmbyBridgeListener {

    private final Logger logger = LoggerFactory.getLogger(EmbyBridgeHandler.class);

    private volatile @Nullable EmbyConnection connection;
    private final WebSocketClient webSocketClient;
    private @Nullable ScheduledFuture<?> connectionCheckerFuture;
    private @Nullable EmbyClientDiscoveryService clientDiscoveryService;
    private @Nullable EmbyHTTPUtils httputils;
    private @Nullable EmbyBridgeConfiguration config;
    private int reconnectionCount;
    private @Nullable String lastDiscoveryStatus;
    private TranslationProvider i18nProvider;

    public EmbyBridgeHandler(Bridge bridge, WebSocketClient webSocketClient, TranslationProvider i18nProvider) {
        super(bridge);
        this.webSocketClient = requireNonNull(webSocketClient, "webSocketClient must not be null");
        this.i18nProvider = requireNonNull(i18nProvider, "translation provider must not be null");
    }

    public void sendCommand(String commandURL) {
        logger.trace("Sending command without payload: {}", commandURL);
        final EmbyHTTPUtils localHttpUtils = requireNonNull(this.httputils, "HTTP utils not initialized");
        try {
            localHttpUtils.doPost(commandURL, "", 2);
        } catch (EmbyHttpRetryExceeded e) {
            logger.debug("Retry limit exceeded for {}", commandURL, e.getCause());
        }
    }

    public void sendCommand(String commandURL, String payload) {
        logger.trace("Sending command: {} with payload: {}", commandURL, payload);
        final EmbyHTTPUtils localHttpUtils = requireNonNull(this.httputils, "HTTP utils not initialized");
        try {
            localHttpUtils.doPost(commandURL, payload, 2);
        } catch (EmbyHttpRetryExceeded e) {
            logger.debug("Retry limit exceeded for {}", commandURL, e.getCause());
        }
    }

    private void establishConnection() {
        final ScheduledExecutorService exec = requireNonNull(scheduler, "scheduler must not be null");

        exec.execute(() -> {
            try {
                final EmbyConnection conn = requireNonNull(this.connection, "connection must not be null");
                final EmbyBridgeConfiguration cfg = requireNonNull(this.config, "config must not be null");
                final String ipAddress = requireNonNull(cfg.ipAddress, "config.ipAddress must not be null");
                final String apiKey = requireNonNull(cfg.api, "config.api must not be null");
                final Integer refreshInterval = requireNonNull(cfg.refreshInterval,
                        "config.refreshInterval must not be null");
                final Integer port = requireNonNull(cfg.port, "config.port must not be null");

                conn.connect(ipAddress, port, apiKey, exec, refreshInterval);

                this.connectionCheckerFuture = exec.scheduleWithFixedDelay(() -> {
                    if (!conn.checkConnection()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/thing.status.bridge.connectionLost");
                    }
                }, CONNECTION_CHECK_INTERVAL_MS, CONNECTION_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/thing.status.bridge.connectionFailed" + e.getMessage());
            }
        });
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NOT_YET_READY);

        final ScheduledExecutorService exec = requireNonNull(scheduler, "scheduler must not be null");

        exec.execute(() -> {
            this.reconnectionCount = 0;
            try {
                this.config = checkConfiguration();
                this.connection = new EmbyConnection(this, this.webSocketClient);
                establishConnection();
                updateStatus(ThingStatus.ONLINE);
            } catch (ConfigValidationException cve) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/thing.status.bridge.configurationFailed" + cve.getMessage());
            }
        });
    }

    @Override
    public void handleEvent(EmbyPlayStateModel playstate, String hostname, int embyport) {
        final EmbyClientDiscoveryService service = this.clientDiscoveryService;
        final EmbyBridgeConfiguration cfg = requireNonNull(this.config, "config must not be null");

        if (service != null && cfg.discovery) {
            service.addDeviceIDDiscover(playstate);
        }

        getThing().getThings().forEach(thing -> {
            EmbyDeviceHandler handler = (EmbyDeviceHandler) thing.getHandler();
            if (handler != null) {
                handler.handleEvent(playstate, hostname, embyport);
                logger.trace("Dispatched event to {}", thing.getLabel());
            } else {
                logger.trace("No handler for {}", thing.getLabel());
            }
        });
    }

    @Override
    public void updateConnectionState(boolean connected) {
        // Grab current status and the last detail message
        ThingStatusInfo info = getThing().getStatusInfo();
        ThingStatus currentStatus = info.getStatus();

        if (connected) {
            // Only transition to ONLINE if we weren’t already
            if (currentStatus != ThingStatus.ONLINE) {
                reconnectionCount = 0;
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            // We’ve gone offline: increment retry count and build new detail text
            reconnectionCount++;
            logger.debug("@text/thing.status.bridge.connectionRetry{}", reconnectionCount);

            // Only emit a new OFFLINE event if status changed, or the message changed
            boolean statusChanged = currentStatus != ThingStatus.OFFLINE;

            if (statusChanged) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/thing.status.bridge.connectionRetry");
            }

            ScheduledFuture<?> localConnectionCheckerFuture = this.connectionCheckerFuture;
            if (localConnectionCheckerFuture != null) {
                localConnectionCheckerFuture.cancel(false);
                this.connectionCheckerFuture = null;
            }
        }
    }

    public void setClientDiscoveryService(@Nullable EmbyClientDiscoveryService discovery) {
        this.clientDiscoveryService = discovery;
    }

    @Nullable
    public EmbyClientDiscoveryService getClientDiscoveryService() {
        return clientDiscoveryService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(channelUID, pollCurrentValueBridge(channelUID));
        } else {
            logger.trace("Ignored command {} on {}", command, channelUID.getId());
        }
    }

    public void updateDiscoveryStatus(String status) {
        this.lastDiscoveryStatus = status;
        updateState(new ChannelUID(getThing().getUID(), "discoveryStatus"), new StringType(status));
    }

    private EmbyBridgeConfiguration checkConfiguration() throws ConfigValidationException {
        EmbyBridgeConfiguration embyConfig = getConfigAs(EmbyBridgeConfiguration.class);
        if (embyConfig.api.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing.status.bridge.missingAPI");
            throwValidationError("api", "@text/thing.status.bridge.missingAPI");
        }
        if (embyConfig.ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing server address");
            throwValidationError("ipAddress", "@text/thing.status.bridge.missingIP");
        }
        this.httputils = new EmbyHTTPUtils(30, embyConfig.api, embyConfig.ipAddress + ":" + embyConfig.port);
        return embyConfig;
    }

    private void throwValidationError(String parameterName, String errorMessage) throws ConfigValidationException {
        final TranslationProvider provider = requireNonNull(this.i18nProvider, "i18nProvider must not be null");
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        ConfigValidationMessage message = new ConfigValidationMessage(parameterName, "error", errorMessage);
        throw new ConfigValidationException(bundle, provider, Collections.singletonList(message));
    }

    private State pollCurrentValueBridge(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case "serverReachable":
                return (getThing().getStatus() == ThingStatus.ONLINE) ? OnOffType.ON : OnOffType.OFF;
            case "discoveryStatus":
                return (this.lastDiscoveryStatus != null) ? new StringType(this.lastDiscoveryStatus) : UnDefType.UNDEF;
            default:
                return UnDefType.UNDEF;
        }
    }

    @Override
    public void dispose() {
        // cancel checker
        final ScheduledFuture<?> future = this.connectionCheckerFuture;
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
        // close connection
        final EmbyConnection conn = requireNonNull(this.connection, "connection not initialized");
        conn.dispose();
        // detach discovery
        final EmbyClientDiscoveryService service = this.clientDiscoveryService;
        if (service != null) {
            service.clearBridge(this);
            this.clientDiscoveryService = null;
        }
        super.dispose();
    }
}
