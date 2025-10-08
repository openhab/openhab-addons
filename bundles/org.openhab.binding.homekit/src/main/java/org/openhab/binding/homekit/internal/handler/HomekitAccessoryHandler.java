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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.dto.Characteristic;
import org.openhab.binding.homekit.internal.dto.Service;
import org.openhab.binding.homekit.internal.enums.DataFormatType;
import org.openhab.binding.homekit.internal.hap_services.CharacteristicReadWriteClient;
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
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeRegistry;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragment;
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
public class HomekitAccessoryHandler extends HomekitBaseAccessoryHandler {

    private static final int INITIAL_DELAY_SECONDS = 2;

    private final Logger logger = LoggerFactory.getLogger(HomekitAccessoryHandler.class);
    private final ChannelTypeRegistry channelTypeRegistry;
    private final ChannelGroupTypeRegistry channelGroupTypeRegistry;

    private @Nullable ScheduledFuture<?> refreshTask;

    public HomekitAccessoryHandler(Thing thing, HomekitTypeProvider typeProvider,
            ChannelTypeRegistry channelTypeRegistry, ChannelGroupTypeRegistry channelGroupTypeRegistry) {
        super(thing, typeProvider);
        this.channelTypeRegistry = channelTypeRegistry;
        this.channelGroupTypeRegistry = channelGroupTypeRegistry;
    }

    @Override
    protected void accessoriesLoaded() {
        logger.debug("Thing accessories loaded {}", accessories.size());
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
                    ScheduledFuture<?> task = refreshTask;
                    if (task == null || task.isCancelled() || task.isDone()) {
                        refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, INITIAL_DELAY_SECONDS,
                                refreshIntervalSeconds, TimeUnit.SECONDS);
                    }
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
    private JsonPrimitive commandToJsonPrimitive(Command command, Channel channel) {
        Object object = command;

        // handle HSBType as not directly supported by HomeKit
        if (object instanceof HSBType) {
            // TODO special handling => TBD
            logger.warn("HSBType command handling is not yet implemented for channel {}", channel.getUID());
        }

        StateDescription stateDescription = getStateDescription(channel);

        // convert QuantityTypes to the characteristic's unit
        if (object instanceof QuantityType<?> quantity) {
            if (stateDescription != null
                    && UnitUtils.parseUnit(stateDescription.getPattern()) instanceof Unit<?> channelUnit) {
                try {
                    QuantityType<?> temp = quantity.toUnit(channelUnit);
                    object = temp != null ? temp : quantity;
                } catch (MeasurementParseException e) {
                    logger.warn("Unexpected unit {} for channel {}", channelUnit, channel.getUID());
                }
            }
        }

        if (object instanceof Number number) {
            // clamp numbers to characteristic's min/max limits
            if (stateDescription != null && stateDescription.getMinimum() instanceof BigDecimal min
                    && min.doubleValue() > number.doubleValue()) {
                object = min;
            }
            if (stateDescription != null && stateDescription.getMaximum() instanceof BigDecimal max
                    && max.doubleValue() < number.doubleValue()) {
                object = max;
            }

            // comply with characteristic's data format
            String format = channel.getProperties().get(PROPERTY_FORMAT);
            if (format != null) {
                try {
                    object = switch (DataFormatType.from(format)) {
                        case UINT8, UINT16, UINT32, UINT64, INT -> Integer.valueOf(number.intValue());
                        case FLOAT -> Float.valueOf(number.floatValue());
                        case STRING -> String.valueOf(number);
                        case BOOL -> Boolean.valueOf(number.intValue() != 0);
                        default -> object;
                    };
                } catch (IllegalArgumentException e) {
                    logger.warn("Unexpected format {} for channel {}", format, channel.getUID(), e);
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

        // comply with the characteristic's boolean data type
        if (object instanceof Boolean bool
                && channel.getProperties().get(PROPERTY_BOOL_TYPE) instanceof String booleanDataType) {
            switch (booleanDataType) {
                case "number" -> object = Integer.valueOf(bool ? 1 : 0);
                case "string" -> object = bool ? "true" : "false";
            }
        }

        return object instanceof Number num ? new JsonPrimitive(num)
                : object instanceof Boolean bool ? new JsonPrimitive(bool) : new JsonPrimitive(object.toString());
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
                        String[] itemTypeParts = acceptedItemType.split(":");
                        if (itemTypeParts.length > 1
                                && getStateDescription(channel) instanceof StateDescriptionFragment stateDescription
                                && UnitUtils.parseUnit(stateDescription.getPattern()) instanceof Unit<?> channelUnit
                                && itemTypeParts[1].equalsIgnoreCase(UnitUtils.getDimensionName(channelUnit))) {
                            yield QuantityType.valueOf(value.getAsNumber().doubleValue(), channelUnit);
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
        if (accessory == null && !isChildAccessory && !accessories.isEmpty()) {
            // fallback to the first accessory if the specific one is not found (should not normally happen)
            accessory = accessories.values().iterator().next();
        }
        if (accessory == null) {
            return;
        }

        // create the channels and properties
        List<Channel> channels = new ArrayList<>();
        Map<String, String> properties = new HashMap<>(thing.getProperties()); // keep existing properties
        accessory.buildAndRegisterChannelGroupDefinitions(thing.getUID(), typeProvider).forEach(groupDef -> {
            logger.trace("+ChannelGroupDefinition id:{}, typeUID:{}, label:{}, description:{}", groupDef.getId(),
                    groupDef.getTypeUID(), groupDef.getLabel(), groupDef.getDescription());

            ChannelGroupType channelGroupType = channelGroupTypeRegistry.getChannelGroupType(groupDef.getTypeUID());
            if (channelGroupType == null) {
                logger.warn("Fata Error: ChannelGroupType {} is not registered", groupDef.getTypeUID());
            } else {
                logger.trace("++ChannelGroupType UID:{}, label:{}, category:{}, description:{}",
                        channelGroupType.getUID(), channelGroupType.getLabel(), channelGroupType.getCategory(),
                        channelGroupType.getDescription());

                channelGroupType.getChannelDefinitions().forEach(chanDef -> {
                    logger.trace(
                            "+++ChannelDefinition id:{}, label:{}, description:{}, channelTypeUID:{}, autoUpdatePolicy:{}, properties:{}",
                            chanDef.getId(), chanDef.getLabel(), chanDef.getDescription(), chanDef.getChannelTypeUID(),
                            chanDef.getAutoUpdatePolicy(), chanDef.getProperties());

                    if (FAKE_PROPERTY_CHANNEL_TYPE_UID.equals(chanDef.getChannelTypeUID())) {
                        // this is a property, not a channel
                        String name = chanDef.getId();
                        String value = chanDef.getLabel();
                        if (value != null) {
                            properties.put(name, value);
                            logger.trace("++++Property '{}:{}'", name, value);
                        }
                    } else {
                        // this is a real channel
                        ChannelType channelType = channelTypeRegistry.getChannelType(chanDef.getChannelTypeUID());
                        if (channelType == null) {
                            logger.warn("Fatal Error: ChannelType {} is not registered", chanDef.getChannelTypeUID());
                        } else {
                            logger.trace(
                                    "++++ChannelType category:{}, description:{}, itemType:{}, label:{}, autoUpdatePolicy:{}, itemType:{}, kind:{}, tags:{}, uid:{}, unitHint:{}",
                                    channelType.getCategory(), channelType.getDescription(), channelType.getItemType(),
                                    channelType.getLabel(), channelType.getAutoUpdatePolicy(),
                                    channelType.getItemType(), channelType.getKind(), channelType.getTags(),
                                    channelType.getUID(), channelType.getUnitHint());

                            ChannelUID channelUID = new ChannelUID(thing.getUID(), groupDef.getId(), chanDef.getId());
                            ChannelBuilder builder = ChannelBuilder.create(channelUID).withType(channelType.getUID())
                                    .withProperties(chanDef.getProperties());
                            Optional.ofNullable(chanDef.getLabel()).ifPresent(builder::withLabel);
                            Optional.ofNullable(chanDef.getDescription()).ifPresent(builder::withDescription);
                            Channel channel = builder.build();
                            channels.add(channel);

                            logger.trace(
                                    "+++++Channel acceptedItemType:{}, defaultTags:{}, description:{}, kind:{}, label:{}, properties:{}, uid:{}",
                                    channel.getAcceptedItemType(), channel.getDefaultTags(), channel.getDescription(),
                                    channel.getKind(), channel.getLabel(), channel.getProperties(), channel.getUID());
                        }
                    }
                });
            }
        });

        String oldLabel = thing.getLabel();
        String newLabel = oldLabel == null || oldLabel.isEmpty() ? accessory.getAccessoryInstanceLabel() : null;
        List<Channel> newChannels = !channels.isEmpty() ? channels : null;
        Map<String, String> newProperties = !properties.isEmpty() ? properties : null;
        SemanticTag newTag = accessory.getSemanticEquipmentTag();

        if (newLabel != null || newChannels != null || newProperties != null || newTag != null) {
            ThingBuilder builder = editThing().withProperties(properties).withChannels(channels);
            Optional.ofNullable(newLabel).ifPresent(builder::withLabel);
            Optional.ofNullable(newChannels).ifPresent(builder::withChannels);
            Optional.ofNullable(newProperties).ifPresent(builder::withProperties);
            Optional.ofNullable(newTag).ifPresent(builder::withSemanticEquipmentTag);

            updateThing(builder.build());
            logger.debug("Updated thing {} channels, {} properties, label {}, tag {}", channels.size(),
                    properties.size(), newLabel, newTag);

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
        if (command == RefreshType.REFRESH) {
            return;
        }
        CharacteristicReadWriteClient writer = this.rwService;
        if (writer == null) {
            logger.warn("No writer service available to handle command for channel: {}", channelUID);
            return;
        }
        try {
            Integer aid = getAccessoryId();
            String iid = channel.getProperties().get(PROPERTY_IID);
            if (aid != null && iid != null) {
                Service service = new Service();
                Characteristic characteristic = new Characteristic();
                characteristic.aid = aid;
                characteristic.iid = Integer.parseInt(iid);
                characteristic.value = commandToJsonPrimitive(command, channel);
                service.characteristics = List.of(characteristic);
                writer.writeCharacteristic(GSON.toJson(service));
            }
        } catch (Exception e) {
            logger.warn("Failed to send command '{}' to '{}'", command, channelUID, e);
        }
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void handleRemoval() {
        ScheduledFuture<?> task = refreshTask;
        if (task != null) {
            task.cancel(true);
        }
        refreshTask = null;
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> task = refreshTask;
        if (task != null) {
            task.cancel(true);
        }
        refreshTask = null;
        super.dispose();
    }

    /**
     * Polls the accessory for its current state and updates the corresponding channels.
     * This method is called periodically by a scheduled executor.
     */
    private void refresh() {
        CharacteristicReadWriteClient rwService = this.rwService;
        if (rwService != null) {
            try {
                Integer aid = getAccessoryId();
                List<String> queries = new ArrayList<>();
                thing.getChannels().stream().forEach(c -> {
                    String iid = c.getProperties().get(PROPERTY_IID);
                    if (iid != null) {
                        queries.add("%s.%s".formatted(aid, iid));
                    }
                });
                if (queries.isEmpty()) {
                    return;
                }
                String jsonResponse = rwService.readCharacteristic(String.join(",", queries));
                Service service = GSON.fromJson(jsonResponse, Service.class);
                if (service != null && service.characteristics instanceof List<Characteristic> characteristics) {
                    for (Channel channel : thing.getChannels()) {
                        String iid = channel.getProperties().get(PROPERTY_IID);
                        if (iid == null) {
                            continue;
                        }
                        for (Characteristic characteristic : characteristics) {
                            if (iid.equals(String.valueOf(characteristic.iid))
                                    && characteristic.value instanceof JsonElement element) {
                                updateState(channel.getUID(), convertJsonToState(element, channel));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to poll accessory state", e);
            }
        }
    }

    private @Nullable StateDescription getStateDescription(Channel channel) {
        ChannelTypeUID uid = channel.getChannelTypeUID();
        ChannelType ct = channelTypeRegistry.getChannelType(uid);
        if (ct == null) {
            logger.warn("Channel {} is missing a channel type", uid);
            return null;
        }
        StateDescription st = ct.getState();
        if (st == null) {
            logger.warn("Channel {} of type {} is missing a state description", uid, ct.getUID());
            return null;
        }
        return st;
    }
}
