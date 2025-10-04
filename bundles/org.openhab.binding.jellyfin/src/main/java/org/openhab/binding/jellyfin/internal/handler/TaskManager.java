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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.ConnectionTask;
import org.openhab.binding.jellyfin.internal.handler.tasks.UsersListTask;
import org.openhab.binding.jellyfin.internal.types.ServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stateless utility class for managing tasks based on server states.
 * This class handles state-driven task management, automatically starting and stopping
 * tasks based on server state transitions. Individual task control is handled internally
 * through state transitions only.
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public final class TaskManager {

    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TaskManager() {
        // Utility class should not be instantiated
    }

    /**
     * Gets the list of task IDs that should be active for the given server state.
     * 
     * @param serverState The server state to get task IDs for
     * @return List of task IDs that should be running for the given state
     */
    public static List<String> getTaskIdsForState(ServerState serverState) {
        switch (serverState) {
            case CONFIGURED:
                // When configured but not connected, run connection task to establish connection
                return List.of(ConnectionTask.TASK_ID);
            case CONNECTED:
                // When connected, run update tasks to keep data synchronized
                // Note: Connection task stops automatically when successful
                return List.of(UsersListTask.TASK_ID);
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
     * Manages task transitions for a server state change.
     * Stops tasks that shouldn't run in the new state and starts tasks that should run.
     * 
     * @param serverState The new server state
     * @param availableTasks Map of available tasks by their IDs
     * @param scheduledTasks Map of currently scheduled tasks
     * @param scheduler The scheduler service to use for task scheduling
     */
    public static void processStateChange(ServerState serverState, Map<String, AbstractTask> availableTasks,
            Map<String, @Nullable ScheduledFuture<?>> scheduledTasks, ScheduledExecutorService scheduler) {
        List<String> taskIdsToStart = getTaskIdsForState(serverState);

        // Stop any running tasks that are not needed for this state
        for (String runningTaskId : List.copyOf(scheduledTasks.keySet())) {
            if (!taskIdsToStart.contains(runningTaskId)) {
                stopTaskInternal(runningTaskId, scheduledTasks);
            }
        }

        // Start tasks needed for this state
        for (String taskId : taskIdsToStart) {
            if (!scheduledTasks.containsKey(taskId)) {
                startTaskInternal(taskId, availableTasks, scheduledTasks, scheduler);
            }
        }
    }

    /**
     * Stops all currently running tasks.
     * 
     * @param scheduledTasks Map of currently scheduled tasks
     */
    public static void stopAllTasks(Map<String, @Nullable ScheduledFuture<?>> scheduledTasks) {
        logger.info("Stopping {} task(s): {}", scheduledTasks.values().size(),
                String.join(",", scheduledTasks.keySet()));

        scheduledTasks.values().forEach(TaskManager::stopScheduledTask);
        scheduledTasks.clear();
    }

    /**
     * Starts a task by its ID (internal method for state transitions).
     * 
     * @param taskId The ID of the task to start
     * @param availableTasks Map of available tasks by their IDs
     * @param scheduledTasks Map of currently scheduled tasks
     * @param scheduler The scheduler service to use for task scheduling
     */
    private static void startTaskInternal(String taskId, Map<String, AbstractTask> availableTasks,
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
    private static void startTaskInternal(AbstractTask task, Map<String, @Nullable ScheduledFuture<?>> scheduledTasks,
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
    private static void stopTaskInternal(String taskId, Map<String, @Nullable ScheduledFuture<?>> scheduledTasks) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(taskId);
        if (scheduledTask != null) {
            logger.info("Stopping task [{}]", taskId);
            stopScheduledTask(scheduledTask);
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
    private static @Nullable ScheduledFuture<?> scheduleTask(Runnable task, long initialDelay, long interval,
            ScheduledExecutorService scheduler) {
        return scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.SECONDS);
    }

    /**
     * Stops a scheduled task.
     * 
     * @param scheduledTask The scheduled task to stop
     */
    private static void stopScheduledTask(@Nullable ScheduledFuture<?> scheduledTask) {
        if (scheduledTask == null || scheduledTask.isCancelled() || scheduledTask.isDone()) {
            return;
        }

        scheduledTask.cancel(true);
    }
}
