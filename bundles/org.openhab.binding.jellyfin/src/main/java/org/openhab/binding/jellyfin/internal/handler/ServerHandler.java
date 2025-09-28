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
import org.openhab.binding.jellyfin.internal.handler.tasks.ConnectionTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.TaskFactory;
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

    Map<String, @Nullable ScheduledFuture<?>> scheduledTasks = new HashMap<>();

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
            this.logger.warn("{}", e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private synchronized Runnable initializeHandler() {
        return () -> {
            try {
                if (Configuration.configurationExists(this.getThing())) {
                    var uriObject = new URI(this.configuration.ssl ? "https" : "http", null, // userInfo
                            this.configuration.hostname, this.configuration.port, this.configuration.path, null, // query
                            null // fragment
                    );

                    var uriString = uriObject.toString();

                    this.apiClient.updateBaseUri(uriString);
                } else {
                    var uriString = thing.getProperties().get(Constants.ServerProperties.SERVER_URI);
                    var uriObject = new URI(uriString);

                    this.configuration.hostname = uriObject.getHost();
                    this.configuration.port = uriObject.getPort();
                    this.configuration.ssl = "https".equalsIgnoreCase(uriObject.getScheme());
                    this.configuration.path = uriObject.getPath();

                    this.apiClient.updateBaseUri(uriString);

                    thing.getProperties().remove(Constants.ServerProperties.SERVER_URI);

                    logger.info("Creating initial configuration for discovered server at {}:{}",
                            this.configuration.hostname, this.configuration.port);
                }
                this.stopTasks();
                this.startTasks();
            } catch (Exception e) {
                this.logger.error("Error during initialization: {}", e.getMessage(), e);
                this.exceptionHandler.handle(e);
            }
        };
    }

    private synchronized void startTasks() {
        Configuration config = this.getConfigAs(Configuration.class);
        this.apiClient.authenticateWithToken(config.token);

        // Create and start the connection task
        AbstractTask connectionTask = TaskFactory.createConnectionTask(
            this.apiClient, 
            systemInfo -> this.handleConnection(systemInfo),
            this.exceptionHandler
        );
        
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

            this.thing.getProperties().put(Constants.ServerProperties.SERVER_VERSION, systemInfo.getVersion());

            logger.info("  Product Name: {}", systemInfo.getProductName());
            // Note: getOperatingSystem() is deprecated but still available for logging
            @SuppressWarnings("deprecation")
            String operatingSystem = systemInfo.getOperatingSystem();
            logger.info("  Operating System: {}", operatingSystem);

            logger.info("  Server ID: {}", systemInfo.getId());
            logger.info("  Startup Wizard Completed: {}", systemInfo.getStartupWizardCompleted());
            logger.info("  Web Socket Port: {}", systemInfo.getWebSocketPortNumber());

            ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "");
            this.getThing().setStatusInfo(statusInfo);

            this.stopTasks();
        } catch (Exception e) {
            logger.warn("Failed to process system information: {}", e.getMessage(), e);
        }
        return null;
    }

    private synchronized void stopTasks() {
        logger.info("Stopping {} task(s): {}", this.scheduledTasks.values().size(), String.join(",", this.scheduledTasks.keySet()));

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
}
