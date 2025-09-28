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

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;

/**
 * Factory for creating tasks used in the Jellyfin binding.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class TaskFactory {

    /**
     * Creates a connection task with the specified parameters.
     *
     * @param client The API client to use for the connection
     * @param connectionHandler The handler for the retrieved system info
     * @param exceptionHandler The handler for exceptions
     * @return A configured connection task
     */
    public static ConnectionTask createConnectionTask(ApiClient client, Consumer<SystemInfo> connectionHandler,
            ExceptionHandlerType exceptionHandler) {
        return new ConnectionTask(client, connectionHandler, exceptionHandler);
    }
    
    /**
     * Creates a registration task with the specified parameters.
     * 
     * @param client The API client to use for registration
     * @param exceptionHandler The handler for exceptions
     * @return A configured registration task
     */
    public static RegistrationTask createRegistrationTask(ApiClient client, ExceptionHandlerType exceptionHandler) {
        return new RegistrationTask(client, exceptionHandler);
    }
    
    /**
     * Creates an update task with the specified parameters.
     * 
     * @param client The API client to use for updates
     * @param exceptionHandler The handler for exceptions
     * @return A configured update task
     */
    public static UpdateTask createUpdateTask(ApiClient client, ExceptionHandlerType exceptionHandler) {
        return new UpdateTask(client, exceptionHandler);
    }
}