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
import org.openhab.binding.homekit.internal.SecureAccessoryClient;
import org.openhab.binding.homekit.internal.discovery.HomekitAccessoryDiscoveryService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomekitAccessoryHandler} is an instance of a {@link BaseHomekitServerHandler} that
 * handles a single HomeKit accessory.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitAccessoryHandler extends BaseHomekitServerHandler {

    private final Logger logger = LoggerFactory.getLogger(HomekitAccessoryHandler.class);

    public HomekitAccessoryHandler(Thing thing, HomekitAccessoryDiscoveryService discoveryService) {
        super(thing, discoveryService);
    }

    @Override
    public void initialize() {
        super.initialize();
        scheduler.scheduleAtFixedRate(this::poll, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SecureAccessoryClient accessoryClient = this.accessoryClient;
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

    private void poll() {
        SecureAccessoryClient accessoryClient = this.accessoryClient;
        if (accessoryClient != null) {
            try {
                String power = accessoryClient.readCharacteristic("1", "10"); // Example AID/IID
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
