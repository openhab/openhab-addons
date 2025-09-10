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
package org.openhab.binding.homekit.internal.handler;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.SecureClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a single HomeKit accessory.
 * It provides a polling mechanism to regularly update the state of the accessory.
 * It also handles commands sent to the accessory's channels.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitDeviceHandler extends HomekitBaseServerHandler {

    private final Logger logger = LoggerFactory.getLogger(HomekitDeviceHandler.class);

    public HomekitDeviceHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing, httpClientFactory);
    }

    @Override
    public void initialize() {
        super.initialize();
        String interval = getConfig().get("pollingInterval").toString();
        try {
            int intervalSeconds = Integer.parseInt(interval);
            if (intervalSeconds > 0) {
                scheduler.scheduleAtFixedRate(this::poll, 0, intervalSeconds, TimeUnit.SECONDS);
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid polling interval configuration: {}", interval);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SecureClient accessoryClient = this.client;
        if (accessoryClient != null) {
            String channelId = channelUID.getId();
            try {
                switch (channelId) {
                    case "power":
                        boolean value = command.equals(OnOffType.ON);
                        accessoryClient.writeCharacteristic("1", "10", value); // Example AID/IID
                        break;
                    // TODO Add more channels here
                    default:
                        logger.warn("Unhandled channel: {}", channelId);
                }
            } catch (Exception e) {
                logger.error("Failed to send command to accessory", e);
            }
        }
    }

    /**
     * Polls the accessory for its current state and updates the corresponding channels.
     * This method is called periodically by a scheduled executor.
     */
    private void poll() {
        SecureClient accessoryClient = this.client;
        if (accessoryClient != null) {
            try {
                String power = accessoryClient.readCharacteristic("1", "10"); // TODO example AID/IID
                // Parse powerState and update channel state accordingly
                if ("true".equals(power)) {
                    updateState(new ChannelUID(getThing().getUID(), "power"), OnOffType.ON);
                } else {
                    updateState(new ChannelUID(getThing().getUID(), "power"), OnOffType.OFF);
                }
            } catch (Exception e) {
                logger.error("Failed to poll accessory state", e);
            }
        }
    }
}
