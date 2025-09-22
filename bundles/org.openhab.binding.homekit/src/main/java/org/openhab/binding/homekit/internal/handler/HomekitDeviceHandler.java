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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.dto.Characteristic;
import org.openhab.binding.homekit.internal.dto.Service;
import org.openhab.binding.homekit.internal.enums.DataFormatType;
import org.openhab.binding.homekit.internal.hap_services.CharacteristicReadWriteService;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

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

    public HomekitDeviceHandler(Thing thing, HomekitTypeProvider typeProvider) {
        super(thing);
        this.typeProvider = typeProvider;
    }

    @Override
    protected void accessoriesLoaded() {
        createChannels(); // create channels based on the fetched accessories
    }

    /**
     * Called when the thing handler has been initialized, the pairing verified, the accessories loaded,
     * and the channels and properties created.
     * Sets up a scheduled task to periodically refresh the state of the accessory.
     */
    private void channelsAndPropertiesLoaded() {
        if (getConfig().get(CONFIG_REFRESH_INTERVAL) instanceof Object refreshInterval) {
            try {
                int refreshIntervalSeconds = Integer.parseInt(refreshInterval.toString());
                if (refreshIntervalSeconds > 0) {
                    scheduler.scheduleWithFixedDelay(this::refresh, 0, refreshIntervalSeconds, TimeUnit.SECONDS);
                    return;
                }
            } catch (NumberFormatException e) {
            }
        }
        logger.warn("Invalid refresh interval configuration, polling disabled");
    }

    /**
     * Converts an openHAB Command to a suitable object for writing to a HomeKit characteristic.
     * It handles various conversions including unit conversion, clamping to min/max values,
     * and converting specific types like OnOffType and OpenClosedType to boolean.
     *
     * @param command the command to convert
     * @param channel the channel for which the command is being converted
     *
     * @return the converted object suitable for HomeKit characteristic
     */
    private Object convertCommandToObject(Command command, Channel channel) {
        Object object = command;
        Map<String, String> properties = channel.getProperties();

        // handle HSBType as not directly supported by HomeKit
        if (object instanceof HSBType) {
            // TODO special handling => TBD
            logger.warn("HSBType command handling is not yet implemented for channel {}", channel.getUID());
        }

        // convert QuantityTypes to the characteristic's unit
        if (object instanceof QuantityType<?> quantity) {
            if (properties.get("unit") instanceof String unit) {
                try {
                    QuantityType<?> temp = quantity.toUnit(normalizedUnitString(unit));
                    object = temp != null ? temp : quantity;
                } catch (MeasurementParseException e) {
                    logger.warn("Unexpected unit {} for channel {}", unit, channel.getUID());
                }
            }
        }

        if (object instanceof Number number) {
            // clamp numbers to characteristic's min/max limits
            Double min = Optional.ofNullable(properties.get("minValue")).map(s -> Double.valueOf(s)).orElse(null);
            if (min != null && number.doubleValue() < min.doubleValue()) {
                object = min;
            }
            Double max = Optional.ofNullable(properties.get("maxValue")).map(s -> Double.valueOf(s)).orElse(null);
            if (max != null && number.doubleValue() > max.doubleValue()) {
                object = max;
            }

            // comply with characteristic's data format
            String format = properties.get("format");
            if (format != null) {
                try {
                    object = switch (DataFormatType.valueOf(format)) {
                        case UINT8, UINT16, UINT32, UINT64, INT -> Integer.valueOf(number.intValue());
                        case FLOAT -> Float.valueOf(number.floatValue());
                        case STRING -> String.valueOf(number);
                        case BOOL -> Boolean.valueOf(number.intValue() != 0);
                        default -> object;
                    };
                } catch (IllegalArgumentException e) {
                    logger.warn("Unexpected format {} for channel {}", format, channel.getUID());
                }
            }
        }

        // convert on/off to boolean
        if (object instanceof OnOffType onOff) {
            object = Boolean.valueOf(onOff == OnOffType.ON);
        }

        // convert open/closed to boolean
        if (object instanceof OpenClosedType openClosed) {
            object = Boolean.valueOf(openClosed == OpenClosedType.OPEN);
        }

        // convert datetime to string
        if (object instanceof DateTimeType dateTime) {
            object = dateTime.toFullString();
        }

        return object;
    }

    /**
     * Converts a Characteristic's 'value' JSON element to an openHAB State based on the channel's accepted item type.
     * Handles various data formats including boolean, string, and number.
     *
     * @param element the JSON element containing the value
     * @param channel the channel for which the state is being converted
     *
     * @return the corresponding openHAB State, or UnDefType.UNDEF if conversion is not possible
     */
    private State convertJsonToState(JsonElement element, Channel channel) {
        if (!element.isJsonPrimitive()) {
            return UnDefType.UNDEF;
        }
        JsonPrimitive value = element.getAsJsonPrimitive();

        String acceptedItemType = (channel.getChannelTypeUID() instanceof ChannelTypeUID uid
                && typeProvider.getChannelType(uid, null) instanceof ChannelType channelType
                && channelType.getItemType() instanceof String itemType) ? itemType : "unknown";

        if (value.isBoolean()) {
            return switch (acceptedItemType) {
                case CoreItemFactory.SWITCH -> OnOffType.from(value.getAsBoolean());
                case CoreItemFactory.CONTACT -> value.getAsBoolean() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                default -> UnDefType.UNDEF;
            };
        } else if (value.isString()) {
            return switch (acceptedItemType) {
                case CoreItemFactory.DATETIME -> DateTimeType.valueOf(value.getAsString());
                default -> StringType.valueOf(value.getAsString());
            };
        } else if (value.isNumber()) {
            return switch (acceptedItemType) {
                case CoreItemFactory.COLOR -> {
                    logger.warn("HSBType command handling is not yet implemented for channel {}", channel.getUID());
                    yield UnDefType.UNDEF;
                }
                case CoreItemFactory.SWITCH -> OnOffType.from(value.getAsInt() != 0);
                case CoreItemFactory.CONTACT -> value.getAsInt() != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                case CoreItemFactory.DIMMER -> new PercentType(value.getAsInt());
                case CoreItemFactory.ROLLERSHUTTER -> new PercentType(value.getAsInt());
                case CoreItemFactory.NUMBER -> new DecimalType(value.getAsNumber());
                default -> {
                    if (acceptedItemType.startsWith(CoreItemFactory.NUMBER)) {
                        int index = acceptedItemType.indexOf(":");
                        if (index > 0) {
                            String targetDimension = acceptedItemType.substring(index + 1);
                            Unit<?> sourceUnit = UnitUtils
                                    .parseUnit(Optional.ofNullable(channel.getProperties().get("unit")).orElse(null));
                            if (sourceUnit != null && targetDimension.equals(UnitUtils.getDimensionName(sourceUnit))) {
                                yield QuantityType.valueOf(value.getAsNumber().doubleValue(), sourceUnit);
                            }
                        }
                        yield new DecimalType(value.getAsNumber());
                    }
                    yield StringType.valueOf(value.getAsString());
                }
            };
        }
        return UnDefType.UNDEF;
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
        Map<String, String> properties = new HashMap<>(thing.getProperties()); // keep existing properties
        accessory.buildAndRegisterChannelGroupDefinitions(typeProvider).forEach(groupDef -> {
            ChannelGroupType groupType = typeProvider.getChannelGroupType(groupDef.getTypeUID(), null);
            if (groupType != null) {
                groupType.getChannelDefinitions().forEach(channelDef -> {
                    logger.info("Creating channels channelDef {}", channelDef.getId());
                    if (FAKE_PROPERTY_CHANNEL_TYPE_UID.equals(channelDef.getChannelTypeUID())) {
                        String name = channelDef.getId();
                        String value = channelDef.getLabel();
                        if (value != null) {
                            properties.put(name, value);
                        }
                    } else {
                        ChannelType channelType = typeProvider.getChannelType(channelDef.getChannelTypeUID(), null);
                        if (channelType != null) {
                            ChannelUID channelUID = new ChannelUID(thing.getUID(), groupDef.getId(),
                                    channelDef.getId());
                            ChannelBuilder builder = ChannelBuilder.create(channelUID).withType(channelType.getUID())
                                    .withProperties(channelDef.getProperties());
                            Optional.ofNullable(channelDef.getLabel()).ifPresent(builder::withLabel);
                            Optional.ofNullable(channelDef.getDescription()).ifPresent(builder::withDescription);
                            channels.add(builder.build());
                        }
                    }
                });
            }
        });

        if (!channels.isEmpty() || !properties.isEmpty()) {
            logger.debug("Updating thing with {} channels, {} properties", channels.size(), properties.size());
            ThingBuilder builder = editThing().withProperties(properties).withChannels(channels);
            Optional.ofNullable(accessory.getSemanticEquipmentTag()).ifPresent(builder::withSemanticEquipmentTag);
            updateThing(builder.build());
            channelsAndPropertiesLoaded();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
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
        Object object = null;
        try {
            Integer aid = getAccessoryId();
            if (aid != null) {
                object = convertCommandToObject(command, channel);
                writer.writeCharacteristic(aid.toString(), channelUID.getId(), object);
            }
        } catch (Exception e) {
            logger.warn("Failed to send command '{}' as object '{}' to accessory for '{}", command, object, channelUID,
                    e);
        }
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    /**
     * Convert a HomeKit formatted unit string to OH format.
     */
    private String normalizedUnitString(String unit) {
        if (unit.isEmpty()) {
            return unit;
        }
        if ("percentage".equals(unit)) { // special case HomeKit "percentage" => "Percent"
            return "Percent";
        }
        return unit.substring(0, 1).toUpperCase() + unit.substring(1).toLowerCase(); // e.g. celsius => Celsius
    }

    /**
     * Polls the accessory for its current state and updates the corresponding channels.
     * This method is called periodically by a scheduled executor.
     */
    private void refresh() {
        CharacteristicReadWriteService rwService = this.rwService;
        if (rwService != null) {
            try {
                Integer aid = getAccessoryId();
                List<String> queries = thing.getChannels().stream()
                        .map(c -> "%s.%s".formatted(aid, Integer.valueOf(c.getUID().getId()))).toList();
                if (queries.isEmpty()) {
                    return;
                }
                String jsonResponse = rwService.readCharacteristic(String.join(",", queries));
                Service service = GSON.fromJson(jsonResponse, Service.class);
                if (service != null && service.characteristics instanceof List<Characteristic> characteristics) {
                    for (Characteristic characteristic : characteristics) {
                        for (Channel channel : thing.getChannels()) {
                            if (channel.getUID().getId().equals(String.valueOf(characteristic.iid))
                                    && characteristic.value instanceof JsonElement element) {
                                updateState(channel.getUID(), convertJsonToState(element, channel));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to poll accessory state", e);
            }
        }
    }
}
