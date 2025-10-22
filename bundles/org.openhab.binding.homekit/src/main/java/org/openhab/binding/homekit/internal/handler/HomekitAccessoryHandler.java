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
import java.util.Objects;
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
import org.openhab.binding.homekit.internal.enums.CharacteristicType;
import org.openhab.binding.homekit.internal.enums.DataFormatType;
import org.openhab.binding.homekit.internal.hap_services.CharacteristicReadWriteClient;
import org.openhab.binding.homekit.internal.persistence.HomekitKeyStore;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.binding.homekit.internal.temporary.LightModel;
import org.openhab.binding.homekit.internal.temporary.LightModel.LightCapabilities;
import org.openhab.binding.homekit.internal.temporary.LightModel.RgbDataType;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.osgi.framework.Bundle;
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

    /*
     * Light model to manage combined light characteristics (hue, saturation, brightness, color temperature).
     * Used to create a combined HSB channel and handle commands accordingly.
     * This is only initialized if the accessory has relevant light characteristics.
     */
    private @Nullable LightModel lightModel = null;
    private @Nullable ChannelUID lightModelClientChannel = null; // special HSB combined channel
    private final Map<CharacteristicType, Channel> lightModelServerChannels = new HashMap<>();

    private @Nullable Channel stopMoveChannel = null; // channel for the stop button (rollershutters)
    private @Nullable ScheduledFuture<?> refreshTask;

    public HomekitAccessoryHandler(Thing thing, HomekitTypeProvider typeProvider,
            ChannelTypeRegistry channelTypeRegistry, ChannelGroupTypeRegistry channelGroupTypeRegistry,
            HomekitKeyStore keyStore, TranslationProvider i18nProvider, Bundle bundle) {
        super(thing, typeProvider, keyStore, i18nProvider, bundle);
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
        StateDescription stateDescription = getStateDescription(channel);

        // process Rollershutter commands
        if (CoreItemFactory.ROLLERSHUTTER.equals(channel.getAcceptedItemType())) {
            if (object instanceof PercentType percent) {
                object = new PercentType(100 - percent.intValue());
            } else if (object instanceof OnOffType onOff) {
                object = onOff == OnOffType.ON ? PercentType.HUNDRED : PercentType.ZERO;
            } else if (object instanceof OpenClosedType openClosed) {
                object = openClosed == OpenClosedType.OPEN ? PercentType.HUNDRED : PercentType.ZERO;
            } else if (object instanceof UpDownType upDown) {
                object = upDown == UpDownType.UP ? PercentType.HUNDRED : PercentType.ZERO;
            }
        }

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

        // convert StringType enums to integers
        if (object instanceof StringType stringType) {
            if (stateDescription != null && stateDescription.getOptions() instanceof List<StateOption> stateOptions) {
                for (StateOption option : stateOptions) {
                    if (stringType.toString().equals(option.getLabel())) {
                        String val = option.getValue();
                        try {
                            object = Integer.parseInt(val);
                        } catch (NumberFormatException e) {
                            logger.warn("Unexpected state option value {} for channel {}", val, channel.getUID(), e);
                        }
                        break;
                    }
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

        // comply with the characteristic's data type
        if (object instanceof Boolean bool
                && channel.getProperties().get(PROPERTY_DATA_TYPE) instanceof String dataType) {
            switch (dataType) {
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
                // convert HomeKit open percent to roller shutter closed percent
                case CoreItemFactory.ROLLERSHUTTER -> new PercentType(100 - value.getAsInt());
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

        lightModelInitialize(accessory);

        // create the channels and properties
        List<Channel> channels = new ArrayList<>();
        Map<String, String> properties = new HashMap<>(thing.getProperties()); // keep existing properties
        accessory.buildAndRegisterChannelGroupDefinitions(thing.getUID(), typeProvider, i18nProvider, bundle)
                .forEach(groupDef -> {
                    logger.trace("+ChannelGroupDefinition id:{}, typeUID:{}, label:{}, description:{}",
                            groupDef.getId(), groupDef.getTypeUID(), groupDef.getLabel(), groupDef.getDescription());

                    ChannelGroupType channelGroupType = channelGroupTypeRegistry
                            .getChannelGroupType(groupDef.getTypeUID());
                    if (channelGroupType == null) {
                        logger.warn("Fatal Error: ChannelGroupType {} is not registered", groupDef.getTypeUID());
                    } else {
                        logger.trace("++ChannelGroupType UID:{}, label:{}, category:{}, description:{}",
                                channelGroupType.getUID(), channelGroupType.getLabel(), channelGroupType.getCategory(),
                                channelGroupType.getDescription());

                        channelGroupType.getChannelDefinitions().forEach(chanDef -> {
                            logger.trace(
                                    "+++ChannelDefinition id:{}, label:{}, description:{}, channelTypeUID:{}, autoUpdatePolicy:{}, properties:{}",
                                    chanDef.getId(), chanDef.getLabel(), chanDef.getDescription(),
                                    chanDef.getChannelTypeUID(), chanDef.getAutoUpdatePolicy(),
                                    chanDef.getProperties());

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
                                ChannelType channelType = channelTypeRegistry
                                        .getChannelType(chanDef.getChannelTypeUID());
                                if (channelType == null) {
                                    logger.warn("Fatal Error: ChannelType {} is not registered",
                                            chanDef.getChannelTypeUID());
                                } else {
                                    logger.trace(
                                            "++++ChannelType category:{}, description:{}, itemType:{}, label:{}, autoUpdatePolicy:{}, itemType:{}, kind:{}, tags:{}, uid:{}, unitHint:{}",
                                            channelType.getCategory(), channelType.getDescription(),
                                            channelType.getItemType(), channelType.getLabel(),
                                            channelType.getAutoUpdatePolicy(), channelType.getItemType(),
                                            channelType.getKind(), channelType.getTags(), channelType.getUID(),
                                            channelType.getUnitHint());

                                    ChannelUID channelUID = new ChannelUID(thing.getUID(), groupDef.getId(),
                                            chanDef.getId());
                                    ChannelBuilder builder = ChannelBuilder.create(channelUID)
                                            .withAcceptedItemType(channelType.getItemType())
                                            .withAutoUpdatePolicy(channelType.getAutoUpdatePolicy())
                                            .withDefaultTags(channelType.getTags()).withKind(channelType.getKind())
                                            .withProperties(chanDef.getProperties()).withType(channelType.getUID());
                                    Optional.ofNullable(chanDef.getLabel()).ifPresent(builder::withLabel);
                                    Optional.ofNullable(chanDef.getDescription()).ifPresent(builder::withDescription);
                                    Channel channel = builder.build();
                                    channels.add(channel);

                                    logger.trace(
                                            "+++++Channel acceptedItemType:{}, defaultTags:{}, description:{}, kind:{}, label:{}, properties:{}, uid:{}",
                                            channel.getAcceptedItemType(), channel.getDefaultTags(),
                                            channel.getDescription(), channel.getKind(), channel.getLabel(),
                                            channel.getProperties(), channel.getUID());

                                }
                            }
                        });
                    }
                });

        lightModelFinalize(accessory, channels);
        stopMoveFinalize(accessory, channels);

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
            logger.warn("Received command for unknown channel '{}'", channelUID);
            return;
        }
        if (command == RefreshType.REFRESH) {
            return;
        }
        CharacteristicReadWriteClient readerWriter = this.rwService;
        if (readerWriter == null) {
            logger.warn("No reader/writer service available to handle command for '{}'", channelUID);
            return;
        }
        try {
            if (command instanceof HSBType) {
                logger.warn("Forbidden to send command '{}' directly to '{}'", command, channelUID);
            } else if (command instanceof StopMoveType stopMoveType && StopMoveType.STOP == stopMoveType) {
                if (stopMoveChannel instanceof Channel stopMoveChannel) {
                    writeChannel(stopMoveChannel, OnOffType.ON, readerWriter);
                } else if (readChannel(channel, readerWriter) instanceof Command actualPosition) {
                    writeChannel(channel, actualPosition, readerWriter);
                }
            } else if (channelUID.equals(lightModelClientChannel)) {
                lightModelHandleCommand(command, readerWriter);
            } else {
                writeChannel(channel, command, readerWriter);
            }
        } catch (Exception e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Failed to send command '{}' to '{}', reconnecting", command, channelUID, e);
            } else {
                logger.debug("Failed to send command '{}' to '{}', reconnecting: {}", command, channelUID,
                        e.getMessage());
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    i18nProvider.getText(bundle, "error.error-sending-command", "Error sending command", null));
            startConnectionTask();
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
        cancelRefreshTask();
        lightModel = null;
        lightModelServerChannels.clear();
        lightModelClientChannel = null;
        super.dispose();
    }

    /**
     * Polls the accessory for its current state and updates the corresponding channels.
     * This method is called periodically by a scheduled executor.
     */
    private void refresh() {
        CharacteristicReadWriteClient reader = this.rwService;
        if (reader != null) {
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
                String jsonResponse = reader.readCharacteristic(String.join(",", queries));
                Service service = GSON.fromJson(jsonResponse, Service.class);
                if (service != null && service.characteristics instanceof List<Characteristic> characteristics) {
                    for (Channel channel : thing.getChannels()) {
                        ChannelUID channelUID = channel.getUID();
                        if (channelUID.equals(lightModelClientChannel)) {
                            for (Characteristic cxx : characteristics) {
                                if (lightModelRefresh(cxx)) {
                                    updateState(channelUID, Objects.requireNonNull(lightModel).getHsb());
                                }
                            }
                        } else if (channel.getProperties().get(PROPERTY_IID) instanceof String iid) {
                            for (Characteristic cxx : characteristics) {
                                if (iid.equals(String.valueOf(cxx.iid)) && cxx.value instanceof JsonElement element) {
                                    State state = convertJsonToState(element, channel);
                                    switch (channel.getKind()) {
                                        case STATE -> updateState(channelUID, state);
                                        case TRIGGER -> triggerChannel(channelUID, state.toFullString());
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Failed to poll accessory state, reconnecting", e);
                } else {
                    logger.debug("Failed to poll accessory state, reconnecting: {}", e.getMessage());
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        i18nProvider.getText(bundle, "error.polling-error", "Polling error", null));
                startConnectionTask();
            }
        }
    }

    private void cancelRefreshTask() {
        ScheduledFuture<?> task = refreshTask;
        if (task != null) {
            task.cancel(true);
        }
        refreshTask = null;
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

    /**
     * Determines if a light model is required for the accessory based on its characteristics.
     * If the accessory has color or color temperature characteristics, a LightModel is created and configured.
     *
     * @param accessory the accessory to check
     */
    private void lightModelInitialize(Accessory accessory) {
        boolean isColor = false;
        boolean isColorTemp = false;
        Double minMirek = null;
        Double maxMirek = null;

        for (Service service : accessory.services) {
            for (Characteristic cxx : service.characteristics) {
                CharacteristicType cxxType = cxx.getCharacteristicType();
                if (CharacteristicType.HUE == cxxType || CharacteristicType.SATURATION == cxxType) {
                    isColor = true;
                } else if (CharacteristicType.COLOR_TEMPERATURE == cxxType) {
                    isColorTemp = true;
                    maxMirek = cxx.maxValue;
                    minMirek = cxx.minValue;
                }
            }
        }

        if (!isColor) {
            return;
        }

        LightCapabilities caps = isColorTemp ? LightCapabilities.COLOR_WITH_COLOR_TEMPERATURE : LightCapabilities.COLOR;
        LightModel lightModel = new LightModel(caps, RgbDataType.DEFAULT, null, null, null, null, null, null);
        if (minMirek != null) {
            lightModel.configSetMirekControlCoolest(minMirek);
        }
        if (maxMirek != null) {
            lightModel.configSetMirekControlWarmest(maxMirek);
        }
        this.lightModel = lightModel;
    }

    /**
     * Checks if a characteristic is relevant to the light model.
     *
     * @param cxx the characteristic to check
     * @return true if the characteristic is part of the light model, false otherwise
     */
    private boolean lightModelRelevantCharacteristic(Characteristic cxx) {
        CharacteristicType cxxType = cxx.getCharacteristicType();
        return CharacteristicType.HUE == cxxType || CharacteristicType.SATURATION == cxxType
                || CharacteristicType.BRIGHTNESS == cxxType || CharacteristicType.COLOR_TEMPERATURE == cxxType
                || CharacteristicType.ON == cxxType;
    }

    /**
     * Refreshes the light model state based on the updated characteristic value.
     *
     * @param cxx the characteristic containing the updated value
     * @return true if the light model was updated, false otherwise
     */
    private boolean lightModelRefresh(Characteristic cxx) {
        LightModel lightModel = this.lightModel;
        if (lightModel == null) {
            throw new IllegalStateException("Light model is not initialized");
        }
        boolean changed = false;
        if (lightModelRelevantCharacteristic(cxx) && cxx.value instanceof JsonPrimitive primitiveValue) {
            CharacteristicType cxxType = cxx.getCharacteristicType();
            if (primitiveValue.isNumber()) {
                changed = true;
                switch (cxxType) {
                    case HUE -> lightModel.setHue(primitiveValue.getAsDouble());
                    case SATURATION -> lightModel.setSaturation(primitiveValue.getAsDouble());
                    case BRIGHTNESS -> lightModel.setBrightness(primitiveValue.getAsDouble());
                    case COLOR_TEMPERATURE -> lightModel.setMirek(primitiveValue.getAsDouble());
                    default -> changed = false;
                }
            } else if (primitiveValue.isBoolean()) {
                changed = true;
                switch (cxxType) {
                    case ON -> lightModel.setOnOff(primitiveValue.getAsBoolean());
                    default -> changed = false;
                }
            }
        }
        return changed;
    }

    /**
     * Sends a command to update the light model based on an HSBType command.
     *
     * @param hsbCommand the HSBType command containing hue, saturation, and brightness
     * @param writer the CharacteristicReadWriteClient to send the command
     * @throws Exception
     */
    private void lightModelHandleCommand(Command command, CharacteristicReadWriteClient writer) throws Exception {
        LightModel lightModel = this.lightModel;
        if (lightModel == null) {
            throw new IllegalStateException("Light model is not initialized");
        }
        lightModel.handleCommand(command);
        if (lightModelServerChannels.get(CharacteristicType.HUE) instanceof Channel channel) {
            writeChannel(channel, QuantityType.valueOf(lightModel.getHue(), Units.DEGREE_ANGLE), writer);
        }
        if (lightModelServerChannels.get(CharacteristicType.SATURATION) instanceof Channel channel) {
            writeChannel(channel, new PercentType(BigDecimal.valueOf(lightModel.getSaturation())), writer);
        }
        if (lightModelServerChannels.get(CharacteristicType.BRIGHTNESS) instanceof Channel channel
                && lightModel.getBrightness() instanceof PercentType percentType) {
            writeChannel(channel, percentType, writer);
        }
        if (lightModelServerChannels.get(CharacteristicType.ON) instanceof Channel channel
                && lightModel.getOnOff() instanceof OnOffType onOff) {
            writeChannel(channel, onOff, writer);
        }
    }

    /**
     * Finalizes the light model channels by mapping the relevant characteristic channels
     * and creating a combined HSB channel.
     *
     * @param accessory the accessory containing the characteristics
     * @param channels the list of channels to finalize
     */
    private void lightModelFinalize(Accessory accessory, List<Channel> channels) {
        if (lightModel == null) {
            return;
        }
        // map characteristic channels to light model
        lightModelServerChannels.clear();
        for (Channel channel : channels) {
            if (channel.getProperties().get(PROPERTY_IID) instanceof String iid) {
                for (Service service : accessory.services) {
                    for (Characteristic cxx : service.characteristics) {
                        if (iid.equals(String.valueOf(cxx.iid)) && lightModelRelevantCharacteristic(cxx)) {
                            CharacteristicType cxxType = cxx.getCharacteristicType();
                            lightModelServerChannels.put(cxxType, channel);
                        }
                    }
                }
            }
        }
        // create combined HSB channel
        ChannelUID uid = new ChannelUID(thing.getUID(), "hsb-combined-channel");
        Channel channel = ChannelBuilder.create(uid, CoreItemFactory.COLOR)
                .withType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_COLOR).build();
        channels.add(channel);
        logger.trace(
                "+++++Channel acceptedItemType:{}, defaultTags:{}, description:{}, kind:{}, label:{}, properties:{}, uid:{}",
                channel.getAcceptedItemType(), channel.getDefaultTags(), channel.getDescription(), channel.getKind(),
                channel.getLabel(), channel.getProperties(), channel.getUID());
        lightModelClientChannel = uid;
    }

    /**
     * Initializes the stop/move button channel by searching for a characteristic of type POSITION_HOLD.
     *
     * @param accessory the accessory containing the characteristics
     * @param channels the list of channels to search
     */
    private void stopMoveFinalize(Accessory accessory, List<Channel> channels) {
        for (Channel channel : channels) {
            if (channel.getProperties().get(PROPERTY_IID) instanceof String iid) {
                for (Service service : accessory.services) {
                    for (Characteristic cxx : service.characteristics) {
                        if (iid.equals(String.valueOf(cxx.iid))
                                && CharacteristicType.POSITION_HOLD == cxx.getCharacteristicType()) {
                            stopMoveChannel = channel;
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Reads the state of a specific channel by querying the accessory for the characteristic value.
     *
     * @param channel the channel to read
     * @return the current state of the channel, or null if not found
     * @throws Exception
     */
    private synchronized @Nullable State readChannel(Channel channel, CharacteristicReadWriteClient reader)
            throws Exception {
        Integer aid = getAccessoryId();
        String iid = channel.getProperties().get(PROPERTY_IID);
        if (aid == null || iid == null) {
            throw new IllegalStateException(
                    "Missing accessory ID or characteristic IID for channel " + channel.getUID());
        }
        String jsonResponse = reader.readCharacteristic("%s.%s".formatted(aid, iid));
        Service service = GSON.fromJson(jsonResponse, Service.class);
        if (service != null && service.characteristics instanceof List<Characteristic> characteristics) {
            for (Characteristic cxx : characteristics) {
                if (iid.equals(String.valueOf(cxx.iid)) && cxx.value instanceof JsonElement element) {
                    return convertJsonToState(element, channel);
                }
            }
        }
        return null;
    }

    /**
     * Writes a command to a specific channel by constructing a Service and embedded Characteristic object.
     *
     * @param channel the channel to which the command is sent
     * @param command the command to send
     * @param writer the CharacteristicReadWriteClient to send the command
     *
     * @throws Exception
     */
    private synchronized void writeChannel(Channel channel, Command command, CharacteristicReadWriteClient writer)
            throws Exception {
        Integer aid = getAccessoryId();
        String iid = channel.getProperties().get(PROPERTY_IID);
        if (aid == null || iid == null) {
            throw new IllegalStateException(
                    "Missing accessory ID or characteristic IID for channel " + channel.getUID());
        }
        Service service = new Service();
        Characteristic characteristic = new Characteristic();
        characteristic.aid = aid;
        characteristic.iid = Integer.parseInt(iid);
        characteristic.value = commandToJsonPrimitive(command, channel);
        service.characteristics = List.of(characteristic);
        writer.writeCharacteristic(GSON.toJson(service));
    }

    @Override
    protected void startConnectionTask() {
        cancelRefreshTask();
        super.startConnectionTask();
    }

    @Override
    public void onEvent(String jsonContent) {
        Service service = GSON.fromJson(jsonContent, Service.class);
        if (service != null && service.characteristics instanceof List<Characteristic> characteristics) {
            for (Channel channel : thing.getChannels()) {
                String iid = channel.getProperties().get(PROPERTY_IID);
                if (iid != null) {
                    for (Characteristic cxx : characteristics) {
                        if (iid.equals(String.valueOf(cxx.iid)) && cxx.value instanceof JsonElement element) {
                            State state = convertJsonToState(element, channel);
                            switch (channel.getKind()) {
                                case STATE -> updateState(channel.getUID(), state);
                                case TRIGGER -> triggerChannel(channel.getUID(), state.toFullString());
                            }
                        }
                    }
                }
            }
        }
    }
}
