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
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.exceptions.ExceptionHandler;
import org.openhab.binding.jellyfin.internal.handler.tasks.ConnectionTask;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
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

    Map<String, @Nullable ScheduledFuture<?>> tasks = new HashMap<>();
    private Optional<ApiClient> apiClient = Optional.empty();

    public static class TASKS {
        public static final String CONNECT = "Connect";
        public static final String REGISTER = "Registration";
        public static final String POLL = "Update";

        public static Map<String, Integer> delays = Map.ofEntries(Map.entry(TASKS.CONNECT, 0),
                Map.entry(TASKS.REGISTER, 5), Map.entry(TASKS.POLL, 10));
        public static Map<String, Integer> intervals = Map.ofEntries(Map.entry(TASKS.CONNECT, 10),
                Map.entry(TASKS.REGISTER, 1), Map.entry(TASKS.POLL, 10));
    }

    public ServerHandler(Bridge bridge) {
        super(bridge);

        this.exceptionHandler = new ExceptionHandler();
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
                task = new ConnectionTask(this, instance -> this.handleConnection(instance), this.exceptionHandler);
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

    String getAccessToken() {
        return "123";
    }
}
