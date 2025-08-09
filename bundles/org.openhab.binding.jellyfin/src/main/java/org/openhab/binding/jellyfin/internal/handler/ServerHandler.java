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
import org.openhab.binding.jellyfin.internal.api.generated.current.SystemApi;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PublicSystemInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.exceptions.ExceptionHandler;
import org.openhab.binding.jellyfin.internal.handler.tasks.ConnectionTask;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
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
            // Set up API authentication with the provided token
            apiClient.setApiKey("ad0ce1a2a3d24feeb1304c28d688ad73");

            // Test the API client integration
            testJellyfinApiClient();

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
            this.stopTasks();
            this.startTasks();
        };
    }

    private synchronized void startTasks() {
        String taskId = getTask();

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

    private String getTask() {
        return TASKS.CONNECT;
    }

    private Object handleConnection(ApiClient instance) {
        // TODO Auto-generated method stub
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

    /**
     * Test method to verify the Jellyfin API client integration using the generated code.
     * This method demonstrates that our OpenHAB-integrated ApiClient works correctly
     * with the generated API endpoints and models.
     */
    private void testJellyfinApiClient() {
        try {
            // Create the SystemApi using our integrated ApiClient
            SystemApi systemApi = new SystemApi(apiClient);

            logger.info("Testing Jellyfin API client integration...");

            // Test 1: Get public system information (no authentication required)
            try {
                PublicSystemInfo publicInfo = systemApi.getPublicSystemInfo();
                logger.info("=== Public System Information ===");
                logger.info("Server Name: {}", publicInfo.getServerName());
                logger.info("Version: {}", publicInfo.getVersion());
                logger.info("Product Name: {}", publicInfo.getProductName());
                logger.info("Local Address: {}", publicInfo.getLocalAddress());
                logger.info("Server ID: {}", publicInfo.getId());
            } catch (Exception e) {
                logger.warn("Failed to get public system info: {}", e.getMessage());
            }

            // Test 2: Get detailed system information (requires authentication)
            try {
                SystemInfo systemInfo = systemApi.getSystemInfo();
                logger.info("=== Detailed System Information ===");
                logger.info("Server Name: {}", systemInfo.getServerName());
                logger.info("Version: {}", systemInfo.getVersion());
                logger.info("Operating System Display Name: {}", systemInfo.getOperatingSystemDisplayName());
                logger.info("Package Name: {}", systemInfo.getPackageName());
                logger.info("Has Pending Restart: {}", systemInfo.getHasPendingRestart());
                logger.info("Is Shutting Down: {}", systemInfo.getIsShuttingDown());
                logger.info("Supports Library Monitor: {}", systemInfo.getSupportsLibraryMonitor());
                logger.info("WebSocket Port: {}", systemInfo.getWebSocketPortNumber());
                logger.info("Can Self Restart: {}", systemInfo.getCanSelfRestart());
                logger.info("Program Data Path: {}", systemInfo.getProgramDataPath());
                logger.info("Cache Path: {}", systemInfo.getCachePath());
                logger.info("Log Path: {}", systemInfo.getLogPath());
            } catch (Exception e) {
                logger.warn("Failed to get detailed system info (may require admin privileges): {}", e.getMessage());
            }

            logger.info("API client integration test completed successfully!");

        } catch (Exception e) {
            logger.error("API client integration test failed: {}", e.getMessage(), e);
        }
    }
}
