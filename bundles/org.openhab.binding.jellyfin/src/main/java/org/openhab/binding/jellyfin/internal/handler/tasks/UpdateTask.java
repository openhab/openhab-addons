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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task for polling and updating data from the Jellyfin server.
 * This is a placeholder for future implementation.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class UpdateTask extends AbstractTask {

    /** Task ID for the update task */
    public static final String TASK_ID = "Update";
    /** Default startup delay for the update task in seconds */
    public static final int DEFAULT_STARTUP_DELAY = 10;
    /** Default interval for the update task in seconds */
    public static final int DEFAULT_INTERVAL = 10;

    private final Logger logger = LoggerFactory.getLogger(UpdateTask.class);
    @SuppressWarnings("unused") // TODO: Will be used when update logic is implemented
    private final ApiClient client;
    private final ExceptionHandlerType exceptionHandler;

    /**
     * Creates a new update task with default startup delay and interval.
     *
     * @param client The API client to use for updates
     * @param exceptionHandler The handler for exceptions
     */
    public UpdateTask(ApiClient client, ExceptionHandlerType exceptionHandler) {
        super(TASK_ID, DEFAULT_STARTUP_DELAY, DEFAULT_INTERVAL);
        this.client = client;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            // TODO: Implement polling/update logic in future
            logger.debug("Update task not implemented yet");
        } catch (Exception e) {
            this.exceptionHandler.handle(e);
        }
    }
}
