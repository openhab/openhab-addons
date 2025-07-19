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
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;

/**
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ConnectionTask implements Runnable {

    private final Consumer<ApiClient> acceptedHandler;
    private final ExceptionHandlerType exceptionHandler;
    private final ApiClient client;

    public ConnectionTask(ApiClient client, Consumer<ApiClient> connectionHandler,
            ExceptionHandlerType exceptionHandler) {

        this.acceptedHandler = connectionHandler;
        this.exceptionHandler = exceptionHandler;

        this.client = client;
    }

    @Override
    public void run() {
        try {
            this.acceptedHandler.accept(client);
        } catch (Exception e) {
            this.exceptionHandler.handle(e);
        }
    }
}
