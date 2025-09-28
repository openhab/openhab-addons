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
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Task for retrieving available users from the Jellyfin server
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class UsersListTask implements Runnable {

    private final Consumer<List<UserDto>> usersHandler;
    private final ExceptionHandlerType exceptionHandler;
    private final ApiClient client;

    /**
     * Create a new UsersListTask to retrieve information about available users
     *
     * @param client The API client to use for the request
     * @param usersHandler The handler that will process the list of retrieved users
     * @param exceptionHandler The handler that will handle any exceptions that occur
     */
    public UsersListTask(ApiClient client, Consumer<List<UserDto>> usersHandler,
            ExceptionHandlerType exceptionHandler) {
        this.usersHandler = usersHandler;
        this.exceptionHandler = exceptionHandler;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            // Since we couldn't find a direct method in the API, we'll use a direct HTTP request
            // to the /Users endpoint which returns a list of users
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(client.getBaseUri() + "/Users"))
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = client.getHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
                
            if (response.statusCode() != 200) {
                throw new ApiException(response.statusCode(), "Failed to retrieve users: " + response.body());
            }
            
            // Parse the response into a list of UserDto objects
            List<UserDto> users = client.getObjectMapper().readValue(response.body(), 
                new TypeReference<ArrayList<UserDto>>() {});
                
            // Pass the result to the handler
            this.usersHandler.accept(users);
        } catch (IOException | InterruptedException e) {
            this.exceptionHandler.handle(e);
        } catch (Exception e) {
            this.exceptionHandler.handle(e);
        }
    }
}