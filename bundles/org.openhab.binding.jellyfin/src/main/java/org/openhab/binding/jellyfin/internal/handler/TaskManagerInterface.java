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
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.events.ErrorEventBus;
import org.openhab.binding.jellyfin.internal.types.ServerState;

/**
 * Interface for task management operations.
 * This version integrates TaskFactory responsibilities for cleaner architecture.
 * TaskManager becomes the central coordinator for all task-related operations.
 * 
 * @author Patrik Gfeller - Refactoring contribution
 */
@NonNullByDefault
public interface TaskManagerInterface {

    /**
     * Initializes all required tasks for the server handler.
     * Creates tasks using the injected factory and sets up the task registry.
     * 
     * Note: The discoveryService may be null during initial handler initialization because
     * ThingHandlerServices are injected asynchronously by the openHAB framework. The DiscoveryTask
     * will be created later when the ClientDiscoveryService calls back via
     * {@link ServerHandler#onDiscoveryServiceInitialized}.
     * 
     * @param apiClient The API client for task operations
     * @param errorEventBus The error event bus for exception handling
     * @param connectionHandler Handler for connection success events
     * @param usersHandler Handler for users list retrieval events
     * @param serverHandler The server handler for state checking
     * @param discoveryService The discovery service for client discovery (may be null initially)
     * @return Map of initialized tasks by their IDs
     */
    Map<String, org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask> initializeTasks(ApiClient apiClient,
            ErrorEventBus errorEventBus, Consumer<SystemInfo> connectionHandler, Consumer<List<UserDto>> usersHandler,
            ServerHandler serverHandler, @Nullable ClientDiscoveryService discoveryService);

    /**
     * Manages task transitions for a server state change.
     * Automatically starts and stops tasks based on the new state.
     * 
     * @param serverState The new server state
     * @param availableTasks Map of available tasks by their IDs
     * @param scheduledTasks Map of currently scheduled tasks
     * @param scheduler The scheduler service to use for task scheduling
     */
    void processStateChange(ServerState serverState,
            Map<String, org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask> availableTasks,
            Map<String, @Nullable ScheduledFuture<?>> scheduledTasks, ScheduledExecutorService scheduler);

    /**
     * Stops all currently running tasks.
     * 
     * @param scheduledTasks Map of currently scheduled tasks
     */
    void stopAllTasks(Map<String, @Nullable ScheduledFuture<?>> scheduledTasks);

    /**
     * Creates and adds a DiscoveryTask after the discovery service has been injected.
     * 
     * This method is called from {@link ServerHandler#onDiscoveryServiceInitialized} when the
     * ClientDiscoveryService becomes available after async injection by the openHAB framework.
     * 
     * @param serverHandler The server handler for state checking
     * @param discoveryService The discovery service for client discovery
     * @param errorEventBus The error event bus for exception handling
     * @return The created DiscoveryTask
     */
    org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask createDiscoveryTask(ServerHandler serverHandler,
            ClientDiscoveryService discoveryService, ErrorEventBus errorEventBus);
}
