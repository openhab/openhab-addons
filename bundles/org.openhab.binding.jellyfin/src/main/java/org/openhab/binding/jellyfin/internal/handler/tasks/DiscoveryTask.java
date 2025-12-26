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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.handler.ServerHandler;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiException;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Task for discovering Jellyfin client devices.
 *
 * This task periodically triggers the discovery service to scan for connected clients.
 * Discovery only runs when the server handler is ONLINE to avoid unnecessary API calls.
 *
 * Additionally, this task now fetches the current users list from the server and invokes
 * the configured users handler before running the discovery. This moves the user-sync
 * responsibility from the ServerSyncTask into discovery which ensures the user list is
 * up-to-date immediately prior to discovery runs.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class DiscoveryTask extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(DiscoveryTask.class);

    public static final String TASK_ID = "discovery";
    private static final int STARTUP_DELAY_SEC = 60;
    private static final int INTERVAL_SEC = 60;

    private final ServerHandler serverHandler;
    private final ClientDiscoveryService discoveryService;
    private final ExceptionHandlerType exceptionHandler;

    // New: API client and users handler used to fetch and process users before discovery
    private final ApiClient client;
    private final Consumer<List<UserDto>> usersHandler;

    /**
     * Creates a new discovery task.
     *
     * @param serverHandler The server handler providing server status
     * @param discoveryService The discovery service to trigger
     * @param client The API client to use for user fetch
     * @param usersHandler The handler to invoke with the retrieved users
     * @param exceptionHandler Handler for exceptions during discovery
     */
    public DiscoveryTask(ServerHandler serverHandler, ClientDiscoveryService discoveryService, ApiClient client,
            Consumer<List<UserDto>> usersHandler, ExceptionHandlerType exceptionHandler) {
        super(TASK_ID, STARTUP_DELAY_SEC, INTERVAL_SEC);
        this.serverHandler = serverHandler;
        this.discoveryService = discoveryService;
        this.client = client;
        this.usersHandler = usersHandler;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            ThingStatus status = serverHandler.getThing().getStatus();

            if (status != ThingStatus.ONLINE) {
                logger.trace("Server not online (status: {}), skipping discovery", status);
                return;
            }

            logger.trace("Running periodic client discovery - fetching users first");

            // Fetch users from /Users endpoint and invoke the handler before discovery
            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(client.getBaseUri() + "/Users")).header("Accept", "application/json").GET();

                Consumer<HttpRequest.Builder> interceptor = client.getRequestInterceptor();
                if (interceptor != null) {
                    interceptor.accept(requestBuilder);
                }

                HttpRequest request = requestBuilder.build();
                HttpResponse<String> response = client.getHttpClient().send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new ApiException(response.statusCode(), "Failed to retrieve users: " + response.body());
                }

                List<UserDto> users = client.getObjectMapper().readValue(response.body(),
                        new TypeReference<ArrayList<UserDto>>() {
                        });

                // Pass the result to the handler (ServerHandler.handleUsersList)
                usersHandler.accept(users);
            } catch (IOException | InterruptedException e) {
                exceptionHandler.handle(e);
                // If we couldn't fetch users, still attempt discovery to avoid complete stall
            } catch (Exception e) {
                exceptionHandler.handle(e);
            }

            // Now perform the normal discovery step
            logger.trace("Triggering discovery service");
            discoveryService.discoverClients();

        } catch (Exception e) {
            exceptionHandler.handle(e);
        }
    }
}
