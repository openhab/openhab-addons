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
package org.openhab.binding.jellyfin.internal.handler.tasks;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.handler.ServerHandler;
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;

/**
 * Factory for creating tasks used in the Jellyfin binding.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class TaskFactory implements TaskFactoryInterface {

    /**
     * Creates a connection task with the specified parameters.
     *
     * @param client The API client to use for the connection
     * @param connectionHandler The handler for the retrieved system info
     * @param exceptionHandler The handler for exceptions
     * @return A configured connection task
     */
    @Override
    public ConnectionTask createConnectionTask(ApiClient client, Consumer<SystemInfo> connectionHandler,
            ExceptionHandlerType exceptionHandler) {
        return new ConnectionTask(client, connectionHandler, exceptionHandler);
    }

    /**
     * Creates an update task with the specified parameters.
     * 
     * @param client The API client to use for updates
     * @param exceptionHandler The handler for exceptions
     * @return A configured update task
     */
    @Override
    public UpdateTask createUpdateTask(ApiClient client, ExceptionHandlerType exceptionHandler) {
        return new UpdateTask(client, exceptionHandler);
    }

    /**
     * Create a server sync task to synchronize server state (users and sessions).
     * 
     * @param client The API client to use for the server sync request
     * @param usersHandler The handler for processing the retrieved users list
     * @param exceptionHandler The handler for exceptions
     * @return A configured server sync task
     */
    @Override
    public ServerSyncTask createServerSyncTask(ApiClient client, Consumer<List<UserDto>> usersHandler,
            ExceptionHandlerType exceptionHandler) {
        return new ServerSyncTask(client, usersHandler, exceptionHandler);
    }

    /**
     * Creates a discovery task for client device discovery.
     *
     * @param serverHandler The server handler to check status
     * @param discoveryService The discovery service to trigger
     * @param exceptionHandler The handler for exceptions
     * @return A configured discovery task
     */
    @Override
    public DiscoveryTask createDiscoveryTask(ServerHandler serverHandler, ClientDiscoveryService discoveryService,
            ExceptionHandlerType exceptionHandler) {
        return new DiscoveryTask(serverHandler, discoveryService, exceptionHandler);
    }
}
