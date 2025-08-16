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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.exceptions.ExceptionHandler;
import org.openhab.binding.jellyfin.internal.handler.tasks.ConnectionTask;
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

    Map<String, @Nullable ScheduledFuture<?>> tasks = new HashMap<>();

    public static class TASKS {
        public static final String CONNECT = "Connect";
        public static final String REGISTER = "Registration";
        public static final String POLL = "Update";

        public static Map<String, Integer> delays = Map.ofEntries(Map.entry(TASKS.CONNECT, 0),
                Map.entry(TASKS.REGISTER, 5), Map.entry(TASKS.POLL, 10));
        public static Map<String, Integer> intervals = Map.ofEntries(Map.entry(TASKS.CONNECT, 10),
                Map.entry(TASKS.REGISTER, 1), Map.entry(TASKS.POLL, 10));
    }

    public ServerHandler(Bridge bridge, ApiClient apiClient) {
        super(bridge);

        this.exceptionHandler = new ExceptionHandler();
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
                if (this.configurationExists()) {
                    this.stopTasks();
                    this.startTasks();
                } else {
                    logger.warn("Jellyfin configuration is missing or incomplete. Please check your settings.");

                    var description = "";
                    ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.OFFLINE,
                            ThingStatusDetail.CONFIGURATION_PENDING, description);
                    this.getThing().setStatusInfo(statusInfo);
                }
            } catch (Exception e) {
                this.logger.warn("Error during initialization: {}", e.getMessage(), e);

                this.exceptionHandler.handle(e);
            }
        };
    }

    private synchronized void startTasks() {
        String taskId = TASKS.CONNECT;
        Runnable task = null;

        long delay = TASKS.delays.get(taskId);
        long interval = TASKS.intervals.get(taskId);

        this.logger.trace("startTasks - [{}, delay: {}s, interval: {}s]", taskId, delay, interval);

        switch (taskId) {
            case TASKS.CONNECT -> {
                task = new ConnectionTask(this.apiClient, instance -> this.handleConnection(instance),
                        this.exceptionHandler);
                break;
            }
        }

        if (task != null) {
            logger.info("Starting task [{}]", taskId);
            this.tasks.put(taskId, this.executeTask(task, delay, interval));
        }
    }

    private boolean configurationExists() {
        var configuration = this.getConfigAs(Configuration.class);

        return (configuration.token.trim() != "");
    }

    private Object handleConnection(ApiClient instance) {
        try {
            // Get public system information from the Jellyfin server
            var systemApi = new org.openhab.binding.jellyfin.internal.api.generated.current.SystemApi(instance);
            var publicSystemInfo = systemApi.getPublicSystemInfo();

            // Log all available server information at INFO level
            logger.info("Jellyfin Server Information:");
            logger.info("  Server Name: {}", publicSystemInfo.getServerName());
            logger.info("  Local Address: {}", publicSystemInfo.getLocalAddress());
            logger.info("  Version: {}", publicSystemInfo.getVersion());
            logger.info("  Product Name: {}", publicSystemInfo.getProductName());
            // Note: getOperatingSystem() is deprecated but still available for logging
            @SuppressWarnings("deprecation")
            String operatingSystem = publicSystemInfo.getOperatingSystem();
            logger.info("  Operating System: {}", operatingSystem);
            logger.info("  Server ID: {}", publicSystemInfo.getId());
            logger.info("  Startup Wizard Completed: {}", publicSystemInfo.getStartupWizardCompleted());

        } catch (Exception e) {
            logger.warn("Failed to retrieve public system information: {}", e.getMessage(), e);
        }
        return null;
    }

    private synchronized void stopTasks() {
        logger.info("Stopping {} task(s): {}", this.tasks.values().size(), String.join(",", this.tasks.keySet()));

        this.tasks.values().forEach(task -> this.stopTask(task));
        this.tasks.clear();
    }

    private synchronized void stopTask(@Nullable ScheduledFuture<?> task) {
        if (task == null || task.isCancelled() || task.isDone()) {
            return;
        }

        task.cancel(true);
    }

    private @Nullable ScheduledFuture<?> executeTask(Runnable task, long initialDelay, long interval) {
        return scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.SECONDS);
    }
}
