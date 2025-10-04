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
 * A task that establishes a connection to the Jellyfin server and retrieves system information.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ConnectionTask extends AbstractTask {

    /** Task ID for the connection task */
    public static final String TASK_ID = "Connect";
    /** Default startup delay for the connection task in seconds */
    public static final int DEFAULT_STARTUP_DELAY = 0;
    /** Default interval for the connection task in seconds */
    public static final int DEFAULT_INTERVAL = 30;

    private final Consumer<SystemInfo> acceptedHandler;
    private final ExceptionHandlerType exceptionHandler;
    private final ApiClient client;

    /**
     * Creates a new connection task with default startup delay and interval.
     *
     * @param client The API client to use for the connection
     * @param connectionHandler The handler for the retrieved system info
     * @param exceptionHandler The handler for exceptions
     */
    public ConnectionTask(ApiClient client, Consumer<SystemInfo> connectionHandler,
            ExceptionHandlerType exceptionHandler) {
        super(TASK_ID, DEFAULT_STARTUP_DELAY, DEFAULT_INTERVAL);

        this.acceptedHandler = connectionHandler;
        this.exceptionHandler = exceptionHandler;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            var systemApi = new org.openhab.binding.jellyfin.internal.api.generated.current.SystemApi(client);
            var systemInfo = systemApi.getSystemInfo();

            this.acceptedHandler.accept(systemInfo);
        } catch (Exception e) {
            this.exceptionHandler.handle(e);
        }
    }
}
