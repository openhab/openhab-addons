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
package org.openhab.binding.huesync.internal.handler.tasks;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.connection.HueSyncDeviceConnection;
import org.openhab.binding.huesync.internal.handler.HueSyncHandler;
import org.openhab.binding.huesync.internal.types.HueSyncExceptionHandler;

/**
 * @author Patrik Gfeller - Initial contribution, Issue #18376
 */
@NonNullByDefault
public class HueSyncConnectionTask implements Runnable {
    private final Consumer<HueSyncDeviceConnection> connectedHandler;
    private final HueSyncExceptionHandler exceptionHandler;
    private final HttpClient httpClient;
    private final HueSyncHandler handler;

    public HueSyncConnectionTask(HueSyncHandler handler, HttpClient httpClient,
            Consumer<HueSyncDeviceConnection> connectionHandler, HueSyncExceptionHandler exceptionHandler) {
        this.handler = handler;
        this.httpClient = httpClient;
        this.connectedHandler = connectionHandler;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            var connection = new HueSyncDeviceConnection(this.httpClient,
                    this.handler.getThing().getConfiguration().as(HueSyncConfiguration.class), this.exceptionHandler);

            this.connectedHandler.accept(connection);
        } catch (IOException | URISyntaxException | CertificateException e) {
            this.exceptionHandler.handle(e);
        }
    }
}
