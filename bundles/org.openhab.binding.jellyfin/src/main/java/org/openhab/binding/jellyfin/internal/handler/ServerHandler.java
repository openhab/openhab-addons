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
package org.openhab.binding.jellyfin.internal.handler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.exceptions.ExceptionHandler;
import org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.TaskFactory;
import org.openhab.binding.jellyfin.internal.i18n.ResourceHelper;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miguel Álvarez - Initial contribution
 * @author Patrik Gfeller - Adjustments to work independently of the Android SDK
 *         and respective runtime
 * 
 */
@NonNullByDefault
public class ServerHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final ExceptionHandler exceptionHandler;
    private final ApiClient apiClient;
    private final Configuration configuration;

    private Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    public ServerHandler(Bridge bridge, ApiClient apiClient) {
        super(bridge);

        this.exceptionHandler = new ExceptionHandler();
        this.configuration = this.getConfigAs(Configuration.class);
        this.apiClient = apiClient;
    }

    @Override
    public void initialize() {
        try {
            scheduler.execute(initializeHandler());
        } catch (Exception e) {
            this.logger.warn("Exception during initialization: {}", e.getMessage());
            this.exceptionHandler.handle(e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels on the server bridge require command handling.
        // This method is intentionally left blank.
    }

    @Override
    public void dispose() {
        // No additional cleanup required
        super.dispose();
    }

    private synchronized Runnable initializeHandler() {
        return () -> {
            try {
                URI serverUri = this.configuration.getServerURI();

                // Initialize discovered server
                if (thing.getProperties().containsKey(Constants.ServerProperties.SERVER_URI)) {
                    serverUri = new URI(thing.getProperties().get(Constants.ServerProperties.SERVER_URI));
                    updateConfiguration(serverUri);
                } else {
                    // Add the server URI to the properties for non-discovery results
                    updateThingProperty(Constants.ServerProperties.SERVER_URI, serverUri.toString());
                }

                this.apiClient.updateBaseUri(serverUri.toString());

                if (this.configuration.token != null && !this.configuration.token.isEmpty()) {
                    this.apiClient.authenticateWithToken(this.configuration.token);
                    this.startTasks();
                } else {
                    ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.OFFLINE,
                            ThingStatusDetail.CONFIGURATION_ERROR,
                            ResourceHelper.getResourceString("error.configuration.no-access-token"));
                    this.getThing().setStatusInfo(statusInfo);
                }
            } catch (Exception e) {
                this.logger.error("Error during initialization: {}", e.getMessage(), e);
                this.exceptionHandler.handle(e);
            }
        };
    }

    private synchronized void startTasks() {
        // Create and start the connection task
        AbstractTask connectionTask = TaskFactory.createConnectionTask(this.apiClient,
                systemInfo -> this.handleConnection(systemInfo), this.exceptionHandler);

        startTask(connectionTask);

        // Additional tasks can be started here in the future
    }

    private synchronized void startTask(AbstractTask task) {
        String taskId = task.getId();
        int delay = task.getStartupDelay();
        int interval = task.getInterval();

        this.logger.trace("Starting task [{}] with delay: {}s, interval: {}s", taskId, delay, interval);
        logger.info("Starting task [{}]", taskId);

        this.scheduledTasks.put(taskId, this.scheduleTask(task, delay, interval));
    }

    private Object handleConnection(SystemInfo systemInfo) {
        try {
            // Log all available server information at INFO level
            logger.info("Jellyfin Server Information:");
            logger.info("  Server Name: {}", systemInfo.getServerName());
            logger.info("  Local Address: {}", systemInfo.getLocalAddress());
            logger.info("  Version: {}", systemInfo.getVersion());

            // Update properties with server version
            updateThingProperty(Constants.ServerProperties.SERVER_VERSION, systemInfo.getVersion());

            logger.info("  Product Name: {}", systemInfo.getProductName());
            logger.info("  Server ID: {}", systemInfo.getId());
            logger.info("  Startup Wizard Completed: {}", systemInfo.getStartupWizardCompleted());
            logger.info("  Web Socket Port: {}", systemInfo.getWebSocketPortNumber());

            // Update configuration with systemInfo data if available
            this.updateConfiguration(systemInfo);

            ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
            this.getThing().setStatusInfo(statusInfo);

            this.stopTasks();
        } catch (Exception e) {
            logger.warn("Failed to process system information: {}", e.getMessage(), e);
        }
        return null;
    }

    private synchronized void stopTasks() {
        logger.info("Stopping {} task(s): {}", this.scheduledTasks.values().size(),
                String.join(",", this.scheduledTasks.keySet()));

        this.scheduledTasks.values().forEach(this::stopScheduledTask);
        this.scheduledTasks.clear();
    }

    private synchronized void stopScheduledTask(@Nullable ScheduledFuture<?> scheduledTask) {
        if (scheduledTask == null || scheduledTask.isCancelled() || scheduledTask.isDone()) {
            return;
        }

        scheduledTask.cancel(true);
    }

    private @Nullable ScheduledFuture<?> scheduleTask(Runnable task, long initialDelay, long interval) {
        return scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.SECONDS);
    }

    /**
     * Updates configuration from a URI
     * 
     * @param uri The URI containing server information
     */
    private void updateConfiguration(URI uri) {
        // Track if any config value has changed
        boolean configChanged = false;

        // Only update values if they differ from current configuration
        if (uri.getHost() != null && !uri.getHost().equals(this.configuration.hostname)) {
            this.configuration.hostname = uri.getHost();
            configChanged = true;
        }

        if (uri.getPort() > 0 && uri.getPort() != this.configuration.port) {
            this.configuration.port = uri.getPort();
            configChanged = true;
        }

        if (uri.getScheme() != null) {
            boolean newSslValue = "https".equalsIgnoreCase(uri.getScheme());
            if (newSslValue != this.configuration.ssl) {
                this.configuration.ssl = newSslValue;
                configChanged = true;
            }
        }

        if (uri.getPath() != null && !uri.getPath().isEmpty() && !uri.getPath().equals(this.configuration.path)) {
            this.configuration.path = uri.getPath();
            configChanged = true;
        }

        // Only save if something has changed
        if (configChanged) {
            logger.info("Configuration changed, updating Thing configuration");

            org.openhab.core.config.core.Configuration config = editConfiguration();

            config.put("hostname", this.configuration.hostname);
            config.put("port", this.configuration.port);
            config.put("ssl", this.configuration.ssl);
            config.put("path", this.configuration.path);

            updateConfiguration(config);
        } else {
            logger.debug("No configuration changes needed");
        }
    }

    private void updateConfiguration(SystemInfo systemInfo) {
        var localAddress = systemInfo.getLocalAddress();

        if (localAddress != null && !localAddress.isEmpty()) {
            try {
                updateConfiguration(new URI(localAddress));
            } catch (Exception e) {
                logger.debug("Failed to parse local address URI: {}", e.getMessage());
                // Don't use exception handler for debug-level issues
            }
        }
    }

    /**
     * Helper method to update a single Thing property.
     * Creates a new properties map with the updated property and calls updateProperties.
     * 
     * @param key The property key
     * @param value The property value
     */
    private void updateThingProperty(String key, String value) {
        Map<String, String> properties = new HashMap<>(thing.getProperties());
        properties.put(key, value);
        updateProperties(properties);
    }
}
