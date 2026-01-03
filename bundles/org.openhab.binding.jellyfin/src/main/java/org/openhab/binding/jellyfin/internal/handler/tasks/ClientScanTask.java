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
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.DevicesApi;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.DeviceInfoDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.DeviceInfoDtoQueryResult;
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;

/**
 * Task for scanning connected client devices on the Jellyfin server
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ClientScanTask extends AbstractTask {

    /** Task ID for the client scan task */
    public static final String TASK_ID = "ClientScan";
    /** Default startup delay for the client scan task in seconds */
    public static final int DEFAULT_STARTUP_DELAY = 2;
    /** Default interval for the client scan task in seconds */
    public static final int DEFAULT_INTERVAL = 30;

    private final Consumer<List<DeviceInfoDto>> devicesHandler;
    private final ExceptionHandlerType exceptionHandler;
    private final ApiClient client;
    private final UUID userId;

    /**
     * Create a new ClientScanTask to scan for connected clients
     *
     * @param client The API client to use for the scan
     * @param userId The user ID to filter clients by
     * @param devicesHandler The handler that will process the list of discovered devices
     * @param exceptionHandler The handler that will handle any exceptions that occur
     */
    public ClientScanTask(ApiClient client, UUID userId, Consumer<List<DeviceInfoDto>> devicesHandler,
            ExceptionHandlerType exceptionHandler) {
        super(TASK_ID, DEFAULT_STARTUP_DELAY, DEFAULT_INTERVAL);

        this.devicesHandler = devicesHandler;
        this.exceptionHandler = exceptionHandler;
        this.client = client;
        this.userId = userId;
    }

    @Override
    public void run() {
        try {
            var devicesApi = new DevicesApi(client);
            DeviceInfoDtoQueryResult devices = devicesApi.getDevices(userId);

            this.devicesHandler.accept(devices.getItems());
        } catch (Exception e) {
            this.exceptionHandler.handle(e);
        }
    }
}
