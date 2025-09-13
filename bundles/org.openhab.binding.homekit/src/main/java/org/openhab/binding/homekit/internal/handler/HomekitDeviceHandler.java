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

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.network.CharacteristicsManager;
import org.openhab.binding.homekit.internal.provider.HomekitTypeProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelType;
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
    private final HomekitTypeProvider typeProvider;

    public HomekitDeviceHandler(Thing thing, HttpClientFactory httpClientFactory, HomekitTypeProvider typeProvider) {
        super(thing, httpClientFactory);
        this.typeProvider = typeProvider;
    }

    @Override
    public void initialize() {
        super.initialize();
        String interval = getConfig().get(CONFIG_POLLING_INTERVAL).toString();
        try {
            int intervalSeconds = Integer.parseInt(interval);
            if (intervalSeconds > 0) {
                scheduler.scheduleWithFixedDelay(this::poll, 0, intervalSeconds, TimeUnit.SECONDS);
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid polling interval configuration: {}", interval);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        CharacteristicsManager charactersticsManager = this.charactersticsManager;
        if (charactersticsManager != null) {
            String channelId = channelUID.getId();
            try {
                switch (channelId) {
                    case "power":
                        // boolean value = command.equals(OnOffType.ON);
                        // accessoryClient.writeCharacteristic("1", "10", value); // Example AID/IID
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
        CharacteristicsManager charactersticsManager = this.charactersticsManager;
        if (charactersticsManager != null) {
            try {
                // String power = accessoryClient.readCharacteristic("1", "10"); // TODO example AID/IID
                // Parse powerState and update channel state accordingly
                // if ("true".equals(power)) {
                // updateState(new ChannelUID(getThing().getUID(), "power"), OnOffType.ON);
                // } else {
                // updateState(new ChannelUID(getThing().getUID(), "power"), OnOffType.OFF);
                // }
            } catch (Exception e) {
                logger.error("Failed to poll accessory state", e);
            }
        }
    }

    @Override
    protected void getAccessories() {
        if (!isChildAccessory) {
            // child accessories shall not fetch accessories again
            super.getAccessories();
        }
        createChannels();
    }

    /**
     * Creates channels for the accessory based on its services and characteristics.
     * Only parses the one relevant accessory in the list, as each handler is for a single accessory.
     * Iterates through that accessory's services and characteristics to create appropriate channels.
     * Each service creates a channel group, and each characteristic creates a channel within it.
     */
    private void createChannels() {
        if (accessories.isEmpty()) {
            return;
        }
        String uidProperty = thing.getProperties().get(PROPERTY_UID);
        if (uidProperty == null) {
            return;
        }
        int accessoryIdIndex = uidProperty.lastIndexOf("-");
        if (accessoryIdIndex < 0) {
            return;
        }
        Integer accessoryId;
        try {
            accessoryId = Integer.parseInt(uidProperty.substring(accessoryIdIndex + 1));
        } catch (NumberFormatException e) {
            return;
        }
        Accessory accessory = accessories.get(accessoryId);
        if (accessory == null) {
            return;
        }

        // create the channels
        List<Channel> channels = new ArrayList<>();
        accessory.buildAndRegisterChannelGroupDefinitions(typeProvider).forEach(groupDef -> {
            ChannelGroupType groupType = typeProvider.getChannelGroupType(groupDef.getTypeUID(), null);
            if (groupType != null) {
                groupType.getChannelDefinitions().forEach(channelDef -> {
                    ChannelType channelType = typeProvider.getChannelType(channelDef.getChannelTypeUID(), null);
                    if (channelType != null) {
                        ChannelUID channelUID = new ChannelUID(thing.getUID(), groupDef.getId(), channelDef.getId());
                        ChannelBuilder builder = ChannelBuilder.create(channelUID).withType(channelType.getUID());
                        Optional.ofNullable(channelDef.getLabel()).ifPresent(builder::withLabel);
                        Optional.ofNullable(channelDef.getDescription()).ifPresent(builder::withDescription);
                        channels.add(builder.build());
                    }
                });
            }
        });

        // update thing with new channels
        ThingBuilder builder = editThing().withChannels(channels);
        Optional.ofNullable(accessory.getSemanticEquipmentTag()).ifPresent(builder::withSemanticEquipmentTag);
        updateThing(builder.build());
    }
}
