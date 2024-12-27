/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sbus.internal.config.SbusBridgeConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.facade.SbusAdapter;

/**
 * The {@link SbusBridgeHandler} is responsible for handling communication with the SBUS bridge.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusBridgeHandler.class);

    private @Nullable SbusAdapter sbusConnection;

    /**
     * Constructs a new SBUSBridgeHandler.
     *
     * @param bridge the bridge
     */
    public SbusBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Initializes the SBUS bridge handler by establishing a connection to the SBUS network.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing SBUS bridge handler for bridge {}", getThing().getUID());

        try {
            // Get configuration using the config class
            SbusBridgeConfig config = getConfigAs(SbusBridgeConfig.class);
            if (config.host.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host address not configured");
                return;
            }

            // Initialize SBUS connection with the configuration parameters
            sbusConnection = new SbusAdapter(config.host, config.port);

            updateStatus(ThingStatus.ONLINE);
            logger.debug("SBUS bridge handler initialized successfully");

        } catch (Exception e) {
            logger.error("Error initializing SBUS bridge", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Gets the SBUS adapter connection.
     *
     * @return the SBUS adapter
     */
    public @Nullable SbusAdapter getSbusConnection() {
        return sbusConnection;
    }

    /**
     * Disposes the handler by closing the SBUS connection.
     */
    @Override
    public void dispose() {
        logger.debug("Disposing SBUS bridge handler");
        final SbusAdapter connection = sbusConnection;
        if (connection != null) {
            connection.close();
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Bridge doesn't handle commands directly
    }
}
