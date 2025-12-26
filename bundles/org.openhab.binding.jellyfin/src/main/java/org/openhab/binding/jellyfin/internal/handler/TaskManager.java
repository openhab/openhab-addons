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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.events.ErrorEventBus;
import org.openhab.binding.jellyfin.internal.exceptions.ContextualExceptionHandler;
import org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.ConnectionTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.DiscoveryTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.ServerSyncTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.TaskFactoryInterface;
import org.openhab.binding.jellyfin.internal.handler.tasks.UpdateTask;
import org.openhab.binding.jellyfin.internal.types.ServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TaskManager that integrates TaskFactory for clean architecture.
 * This class manages the complete lifecycle of tasks and acts as the single point
 * of coordination for all task-related operations.
 *
 * Uses dependency injection for TaskFactory to enable better testability and
 * follows SOLID principles for maintainable code.
 *
 * Key features:
 * - Integrates TaskFactory responsibility
 * - Provides single point of interaction for ServerHandler
 * - Better encapsulation of task management logic
 * - Maintains state-driven task management approach
 * - Instance-based usage with dependency injection
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class TaskManager implements TaskManagerInterface {

    private final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private final @Nullable TaskFactoryInterface taskFactory;

    /**
     * Constructor with dependency injection for TaskFactory (instance-based usage)
     *
     * @param taskFactory The factory for creating tasks
     */
    public TaskManager(TaskFactoryInterface taskFactory) {
        this.taskFactory = taskFactory;
    }

    @Override
    public Map<String, AbstractTask> initializeTasks(ApiClient apiClient, ErrorEventBus errorEventBus,
            Consumer<SystemInfo> connectionHandler, Consumer<List<UserDto>> usersHandler, ServerHandler serverHandler,
            @Nullable ClientDiscoveryService discoveryService) {

        if (taskFactory == null) {
            throw new IllegalStateException("TaskFactory not injected. Use constructor with TaskFactory parameter.");
        }

        Map<String, AbstractTask> tasks = new HashMap<>();

        // Create tasks using the injected factory with context-specific exception handlers
        tasks.put(ConnectionTask.TASK_ID, taskFactory.createConnectionTask(apiClient, connectionHandler,
                new ContextualExceptionHandler(errorEventBus, "ConnectionTask")));

        tasks.put(UpdateTask.TASK_ID,
                taskFactory.createUpdateTask(apiClient, new ContextualExceptionHandler(errorEventBus, "UpdateTask")));

        tasks.put(ServerSyncTask.TASK_ID, taskFactory.createServerSyncTask(apiClient, usersHandler,
                new ContextualExceptionHandler(errorEventBus, "ServerSyncTask")));

        // Note: DiscoveryTask is NOT created here because discoveryService is null during initial
        // handler initialization. It will be added later when the ClientDiscoveryService is injected
        // and calls ServerHandler.onDiscoveryServiceInitialized().

        logger.debug("Initialized {} tasks: {}", tasks.size(), String.join(", ", tasks.keySet()));
        return tasks;
    }

    @Override
    public void processStateChange(ServerState serverState, Map<String, AbstractTask> availableTasks,
            Map<String, @Nullable ScheduledFuture<?>> scheduledTasks, ScheduledExecutorService scheduler) {

        List<String> taskIdsToStart = getTaskIdsForState(serverState, availableTasks);

        // Stop any running tasks that are not needed for this state
        for (String runningTaskId : List.copyOf(scheduledTasks.keySet())) {
            if (!taskIdsToStart.contains(runningTaskId)) {
                stopTaskInternal(runningTaskId, availableTasks, scheduledTasks);
            }
        }

        // Start tasks needed for this state
        for (String taskId : taskIdsToStart) {
            if (!scheduledTasks.containsKey(taskId)) {
                startTaskInternal(taskId, availableTasks, scheduledTasks, scheduler);
            }
        }
    }

    @Override
    public void stopAllTasks(Map<String, @Nullable ScheduledFuture<?>> scheduledTasks) {
        logger.info("Stopping {} task(s): {}", scheduledTasks.values().size(),
                String.join(",", scheduledTasks.keySet()));

        for (ScheduledFuture<?> scheduledTask : scheduledTasks.values()) {
            stopScheduledTask(scheduledTask);
        }
        scheduledTasks.clear();
    }

    /**
     * Gets the list of task IDs that should be active for the given server state.
     *
     * @param serverState The server state to get task IDs for
     * @return List of task IDs that should be running for the given state
     */
    private List<String> getTaskIdsForState(ServerState serverState, Map<String, AbstractTask> availableTasks) {
        switch (serverState) {
            case CONFIGURED:
                // When configured but not connected, run connection task to establish connection
                return List.of(ConnectionTask.TASK_ID);
            case CONNECTED:
                // When connected, run sync task to keep server state (users and sessions) synchronized
                // Also run discovery task to discover Jellyfin clients in the background
                // Note: Connection task stops automatically when successful
                if (availableTasks.containsKey(org.openhab.binding.jellyfin.internal.server.WebSocketTask.TASK_ID)) {
                    return List.of(org.openhab.binding.jellyfin.internal.server.WebSocketTask.TASK_ID,
                            DiscoveryTask.TASK_ID);
                }
                return List.of(ServerSyncTask.TASK_ID, DiscoveryTask.TASK_ID);
            case DISCOVERED:
                // For discovered servers, potentially run registration task in the future
                return List.of();
            case NEEDS_AUTHENTICATION:
                // Could potentially run registration task to help with authentication
                return List.of();
            case INITIALIZING:
            case ERROR:
            case DISPOSED:
            default:
                // No tasks should run in these states
                return List.of();
        }
    }

    /**
     * Starts a task by its ID (internal method for state transitions).
     *
     * @param taskId The ID of the task to start
     * @param availableTasks Map of available tasks by their IDs
     * @param scheduledTasks Map of currently scheduled tasks
     * @param scheduler The scheduler service to use for task scheduling
     */
    private void startTaskInternal(String taskId, Map<String, AbstractTask> availableTasks,
            Map<String, @Nullable ScheduledFuture<?>> scheduledTasks, ScheduledExecutorService scheduler) {
        AbstractTask task = availableTasks.get(taskId);
        if (task != null) {
            startTaskInternal(task, scheduledTasks, scheduler);
        } else {
            logger.warn("Task with ID '{}' not found", taskId);
        }
    }

    /**
     * Starts a specific task (internal method for state transitions).
     *
     * @param task The task to start
     * @param scheduledTasks Map of currently scheduled tasks
     * @param scheduler The scheduler service to use for task scheduling
     */
    private void startTaskInternal(AbstractTask task, Map<String, @Nullable ScheduledFuture<?>> scheduledTasks,
            ScheduledExecutorService scheduler) {
        String taskId = task.getId();
        int delay = task.getStartupDelay();
        int interval = task.getInterval();

        logger.trace("Starting task [{}] with delay: {}s, interval: {}s", taskId, delay, interval);
        logger.info("Starting task [{}]", taskId);

        ScheduledFuture<?> scheduledTask = scheduleTask(task, delay, interval, scheduler);
        scheduledTasks.put(taskId, scheduledTask);
    }

    /**
     * Stops a task by its ID (internal method for state transitions).
     *
     * @param taskId The ID of the task to stop
     * @param scheduledTasks Map of currently scheduled tasks
     */
    private void stopTaskInternal(String taskId, Map<String, AbstractTask> availableTasks,
            Map<String, @Nullable ScheduledFuture<?>> scheduledTasks) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(taskId);
        if (scheduledTask != null) {
            logger.info("Stopping task [{}]", taskId);
            stopScheduledTask(scheduledTask);
            // Dispose resources for tasks that maintain connections
            AbstractTask task = availableTasks.get(taskId);
            if (task instanceof org.openhab.binding.jellyfin.internal.server.WebSocketTask) {
                try {
                    ((org.openhab.binding.jellyfin.internal.server.WebSocketTask) task).dispose();
                } catch (Exception ex) {
                    logger.debug("Error disposing WebSocketTask: {}", ex.getMessage());
                }
            }
        }
    }

    /**
     * Schedules a task for execution.
     *
     * @param task The task to schedule
     * @param initialDelay Initial delay in seconds
     * @param interval Interval between executions in seconds
     * @param scheduler The scheduler service to use
     * @return The scheduled future for the task
     */
    private @Nullable ScheduledFuture<?> scheduleTask(Runnable task, long initialDelay, long interval,
            ScheduledExecutorService scheduler) {
        return scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.SECONDS);
    }

    /**
     * Stops a scheduled task.
     *
     * @param scheduledTask The scheduled task to stop
     */
    private void stopScheduledTask(@Nullable ScheduledFuture<?> scheduledTask) {
        if (scheduledTask == null || scheduledTask.isCancelled() || scheduledTask.isDone()) {
            return;
        }

        scheduledTask.cancel(true);
    }

    @Override
    public AbstractTask createDiscoveryTask(ServerHandler serverHandler, ClientDiscoveryService discoveryService,
            ErrorEventBus errorEventBus) {
        if (taskFactory == null) {
            throw new IllegalStateException("TaskFactory not injected. Use constructor with TaskFactory parameter.");
        }

        return taskFactory.createDiscoveryTask(serverHandler, discoveryService,
                new ContextualExceptionHandler(errorEventBus, "DiscoveryTask"));
    }
}
