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

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.CONFIG_POLLING_INTERVAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.enums.DataFormatType;
import org.openhab.binding.homekit.internal.hap_services.CharacteristicReadWriteService;
import org.openhab.binding.homekit.internal.persistance.HomekitTypeProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.Command;
import org.openhab.core.types.util.UnitUtils;
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
    public void handleCommand(ChannelUID channelUID, Command commandArg) {
        Channel channel = thing.getChannel(channelUID);
        if (channel == null) {
            logger.warn("Received command for unknown channel: {}", channelUID);
            return;
        }
        CharacteristicReadWriteService writer = this.rwService;
        if (writer == null) {
            logger.warn("No writer service available to handle command for channel: {}", channelUID);
            return;
        }

        Object command = commandArg;
        Map<String, String> properties = channel.getProperties();

        // convert QuantityTypes to the characteristic's unit
        if (command instanceof QuantityType<?> quantity) {
            Unit<?> unit = UnitUtils.parseUnit(Optional.ofNullable(properties.get("unit")).orElse(null));
            if (unit != null && !unit.equals(quantity.getUnit()) && quantity.getUnit().isCompatible(unit)) {
                command = quantity.toUnit(unit);
            }
        }

        if (command instanceof Number number) {
            // clamp numbers to characteristic's min/max limits
            Double min = Optional.ofNullable(properties.get("minValue")).map(s -> Double.valueOf(s)).orElse(null);
            if (min != null && number.doubleValue() < min.doubleValue()) {
                command = min;
            }
            Double max = Optional.ofNullable(properties.get("maxValue")).map(s -> Double.valueOf(s)).orElse(null);
            if (max != null && number.doubleValue() > max.doubleValue()) {
                command = max;
            }

            // comply with characteristic's data format
            String format = properties.get("format");
            if (format != null) {
                try {
                    command = switch (DataFormatType.valueOf(format)) {
                        case UINT8, UINT16, UINT32, UINT64, INT -> Integer.valueOf(number.intValue());
                        case FLOAT -> Float.valueOf(number.floatValue());
                        case STRING -> String.valueOf(number);
                        case BOOL -> Boolean.valueOf(number.intValue() != 0);
                        default -> command;
                    };
                } catch (IllegalArgumentException e) {
                    logger.warn("Unexpected format for channel {}: {}", channelUID, properties.get("format"));
                }
            }
        }

        // convert on/off to boolean
        if (command instanceof OnOffType onOff) {
            command = Boolean.valueOf(onOff == OnOffType.ON);
        }

        // convert open/closed to boolean
        if (command instanceof OpenClosedType openClosed) {
            command = Boolean.valueOf(openClosed == OpenClosedType.OPEN);
        }

        try {
            writer.writeCharacteristic(thing.getUID().getId(), channelUID.getId(), Objects.requireNonNull(command));
        } catch (Exception e) {
            logger.warn("Failed to send command '{}' as '{}' to accessory", commandArg, command, e);
        }
    }

    /**
     * Polls the accessory for its current state and updates the corresponding channels.
     * This method is called periodically by a scheduled executor.
     */
    private void poll() {
        CharacteristicReadWriteService charactersticsManager = this.rwService;
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
        Integer accessoryId = getAccessoryId();
        if (accessoryId == null) {
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
                        ChannelBuilder builder = ChannelBuilder.create(channelUID).withType(channelType.getUID())
                                .withProperties(channelDef.getProperties());
                        Optional.ofNullable(channelDef.getLabel()).ifPresent(builder::withLabel);
                        Optional.ofNullable(channelDef.getDescription()).ifPresent(builder::withDescription);
                        channels.add(builder.build());
                    }
                });
            }
        });

        // update thing with the new channels
        ThingBuilder builder = editThing().withChannels(channels);
        Optional.ofNullable(accessory.getSemanticEquipmentTag()).ifPresent(builder::withSemanticEquipmentTag);
        updateThing(builder.build());
    }
}
