/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.openhab.binding.jellyfin.internal.api.ApiClientWrapper;
import org.openhab.binding.jellyfin.internal.gen.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;

/**
 * Task for synchronizing server state (sessions) with the Jellyfin server.
 * Note: User synchronization was moved to {@link DiscoveryTask} to ensure user updates are
 * performed immediately prior to discovery runs.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ServerSyncTask extends AbstractTask {

    /** Task ID for the server sync task */
    public static final String TASK_ID = "ServerSync";
    /** Default startup delay for the server sync task in seconds */
    public static final int DEFAULT_STARTUP_DELAY = 5;
    /** Default interval for the server sync task in seconds */
    public static final int DEFAULT_INTERVAL = 60;

    private final ExceptionHandlerType exceptionHandler;

    /**
     * Create a new ServerSyncTask to synchronize server state (users and sessions).
     *
     * @param client The API client to use for the request
     * @param usersHandler The handler that will process the list of retrieved users
     * @param exceptionHandler The handler that will handle any exceptions that occur
     */
    public ServerSyncTask(ApiClientWrapper client, Consumer<List<UserDto>> usersHandler,
            ExceptionHandlerType exceptionHandler) {
        super(TASK_ID, DEFAULT_STARTUP_DELAY, DEFAULT_INTERVAL);

        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            // User synchronization has been moved to DiscoveryTask which fetches users
            // immediately prior to discovery. No action required here.
        } catch (Exception e) {
            this.exceptionHandler.handle(e);
        }
    }
}
