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
 * Task for handling registration with the Jellyfin server.
 * This is a placeholder for future implementation.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class RegistrationTask extends AbstractTask {

    /** Task ID for the registration task */
    public static final String TASK_ID = "Registration";
    /** Default startup delay for the registration task in seconds */
    public static final int DEFAULT_STARTUP_DELAY = 5;
    /** Default interval for the registration task in seconds */
    public static final int DEFAULT_INTERVAL = 1;
    
    private final Logger logger = LoggerFactory.getLogger(RegistrationTask.class);
    private final ApiClient client;
    private final ExceptionHandlerType exceptionHandler;

    /**
     * Creates a new registration task with default startup delay and interval.
     *
     * @param client The API client to use for registration
     * @param exceptionHandler The handler for exceptions
     */
    public RegistrationTask(ApiClient client, ExceptionHandlerType exceptionHandler) {
        super(TASK_ID, DEFAULT_STARTUP_DELAY, DEFAULT_INTERVAL);
        this.client = client;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            // TODO: Implement registration logic in future
            logger.debug("Registration task not implemented yet");
        } catch (Exception e) {
            this.exceptionHandler.handle(e);
        }
    }
}