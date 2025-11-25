/**
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
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.handler.ServerHandler;
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task for discovering Jellyfin client devices.
 *
 * This task periodically triggers the discovery service to scan for connected clients.
 * Discovery only runs when the server handler is ONLINE to avoid unnecessary API calls.
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

    /**
     * Creates a new discovery task.
     *
     * @param serverHandler The server handler providing server status
     * @param discoveryService The discovery service to trigger
     * @param exceptionHandler Handler for exceptions during discovery
     */
    public DiscoveryTask(ServerHandler serverHandler, ClientDiscoveryService discoveryService,
            ExceptionHandlerType exceptionHandler) {
        super(TASK_ID, STARTUP_DELAY_SEC, INTERVAL_SEC);
        this.serverHandler = serverHandler;
        this.discoveryService = discoveryService;
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

            logger.trace("Running periodic client discovery");
            discoveryService.discoverClients();

        } catch (Exception e) {
            exceptionHandler.handle(e);
        }
    }
}
