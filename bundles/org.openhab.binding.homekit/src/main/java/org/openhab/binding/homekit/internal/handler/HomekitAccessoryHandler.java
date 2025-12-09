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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;
import javax.measure.quantity.Angle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.dto.Characteristic;
import org.openhab.binding.homekit.internal.dto.Service;
import org.openhab.binding.homekit.internal.enums.CharacteristicType;
import org.openhab.binding.homekit.internal.enums.DataFormatType;
import org.openhab.binding.homekit.internal.enums.StatusCode;
import org.openhab.binding.homekit.internal.persistence.HomekitKeyStore;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.binding.homekit.internal.temporary.LightModel;
import org.openhab.binding.homekit.internal.temporary.LightModel.LightCapabilities;
import org.openhab.binding.homekit.internal.temporary.LightModel.RgbDataType;
import org.openhab.binding.homekit.internal.transport.IpTransport;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.service.StartLevelService;
import org.openhab.core.thing.Bridge;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Handler for a HomeKit accessory or bridged accessory.
 * It creates channels based on the accessory's services and characteristics.
 * It handles state updates from the remote device to update channel states.
 * It handles commands sent to the accessory's channels.
 * It also manages a light model for accessories with color capabilities,
 * allowing combined control of hue, saturation, brightness, and color temperature.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitAccessoryHandler extends HomekitBaseAccessoryHandler {

    // Characteristic types relevant for light model management
    private static final Set<CharacteristicType> LIGHT_MODEL_RELEVANT_TYPES = Set.of(CharacteristicType.HUE,
            CharacteristicType.SATURATION, CharacteristicType.BRIGHTNESS, CharacteristicType.COLOR_TEMPERATURE,
            CharacteristicType.ON);

    private final Logger logger = LoggerFactory.getLogger(HomekitAccessoryHandler.class);
    private final ChannelTypeRegistry channelTypeRegistry;
    private final ChannelGroupTypeRegistry channelGroupTypeRegistry;

    /*
     * Light model to manage combined light characteristics (hue, saturation, brightness, color temperature).
     * Used to create a combined HSB channel and handle commands accordingly.
     * This is only initialized if the accessory has relevant light characteristics.
     */
    private volatile @Nullable LightModel lightModel = null;
    private volatile @Nullable ChannelUID lightModelClientHSBTypeChannel = null; // special HSB combined channel

    /*
     * Channel for the stop button (rollershutters)
     */
    private volatile @Nullable Channel stopMoveChannel = null;

    /*
     * Internal record representing a link between an OH channel and a HomeKit characteristic type & iid.
     * Used for light model management.
     */
    private record LightModelLink(Channel channel, CharacteristicType cxxType, Long cxxIid) {
    }

    private final List<LightModelLink> lightModelLinks = new ArrayList<>();

    public HomekitAccessoryHandler(Thing thing, HomekitTypeProvider typeProvider,
            ChannelTypeRegistry channelTypeRegistry, ChannelGroupTypeRegistry channelGroupTypeRegistry,
            HomekitKeyStore keyStore, TranslationProvider i18nProvider, Bundle bundle) {
        super(thing, typeProvider, keyStore, i18nProvider, bundle);
        this.channelTypeRegistry = channelTypeRegistry;
        this.channelGroupTypeRegistry = channelGroupTypeRegistry;
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
                    logger.warn("{} unexpected unit '{}' for channel '{}'", thing.getUID(), channelUnit,
                            channel.getUID());
                }
            }
        }

        // convert StringType enums to integers
        if (object instanceof StringType stringType) {
            if (stateDescription != null && stateDescription.getOptions() instanceof List<StateOption> stateOptions) {
                String commandString = stringType.toString();
                for (StateOption option : stateOptions) {
                    String optionValue = option.getValue();
                    if (commandString.equalsIgnoreCase(optionValue)) {
                        try {
                            object = Integer.parseInt(optionValue);
                            break;
                        } catch (NumberFormatException e) {
                            logger.warn("{} unexpected state option value '{}' for channel '{}'", thing.getUID(),
                                    optionValue, channel.getUID());
                        }
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
            if (channel.getProperties().get(PROPERTY_FORMAT) instanceof String format) {
                object = switch (DataFormatType.from(format)) {
                    case UINT8, UINT16, UINT32, UINT64, INT -> Integer.valueOf(number.intValue());
                    case FLOAT -> Float.valueOf(number.floatValue());
                    case STRING -> String.valueOf(number);
                    case BOOL -> Boolean.valueOf(number.intValue() != 0);
                    default -> object;
                };
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
                    logger.warn("{} channel {} wrong item type 'COLOR'", thing.getUID(), channel.getUID());
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
        Map<Long, Accessory> accessories = getAccessories();
        if (accessories.isEmpty()) {
            return;
        }
        Long accessoryId = getAccessoryId();
        if (accessoryId == null) {
            return;
        }
        Accessory accessory = accessories.get(accessoryId);
        if (accessory == null && !isBridgedAccessory && !accessories.isEmpty()) {
            // fallback to the first accessory if the specific one is not found (should not normally happen)
            accessory = accessories.values().iterator().next();
        }
        if (accessory == null) {
            return;
        }

        lightModelInitialize(accessory);

        // create the channels and properties
        Map<String, Channel> uniqueChannelsMap = new HashMap<>(); // use map to prevent duplicate Channel ID
        Map<String, String> properties = new HashMap<>(thing.getProperties()); // keep existing properties
        accessory.buildAndRegisterChannelGroupDefinitions(thing.getUID(), typeProvider, i18nProvider, bundle)
                .forEach(groupDef -> {
                    logger.trace("{} ChannelGroupDefinition id:{}, typeUID:{}, label:{}, description:{}",
                            thing.getUID(), groupDef.getId(), groupDef.getTypeUID(), groupDef.getLabel(),
                            groupDef.getDescription());

                    ChannelGroupType channelGroupType = channelGroupTypeRegistry
                            .getChannelGroupType(groupDef.getTypeUID());
                    if (channelGroupType == null) {
                        logger.warn("{} fatal error ChannelGroupType '{}' is not registered", thing.getUID(),
                                groupDef.getTypeUID());
                    } else {
                        logger.trace("{}  ChannelGroupType UID:{}, label:{}, category:{}, description:{}",
                                thing.getUID(), channelGroupType.getUID(), channelGroupType.getLabel(),
                                channelGroupType.getCategory(), channelGroupType.getDescription());

                        channelGroupType.getChannelDefinitions().forEach(chanDef -> {
                            logger.trace(
                                    "{}   ChannelDefinition id:{}, label:{}, description:{}, channelTypeUID:{}, autoUpdatePolicy:{}, properties:{}",
                                    thing.getUID(), chanDef.getId(), chanDef.getLabel(), chanDef.getDescription(),
                                    chanDef.getChannelTypeUID(), chanDef.getAutoUpdatePolicy(),
                                    chanDef.getProperties());

                            if (CHANNEL_TYPE_STATIC.equals(chanDef.getChannelTypeUID())) {
                                // static ChannelDefinition: add as a Property (rather than a Channel)
                                Map<String, String> channelProperties = chanDef.getProperties();
                                properties.putAll(channelProperties);
                                logger.trace("{}    Property {}", thing.getUID(), channelProperties);
                            } else {
                                // variable ChannelDefinition: add as a Channel (rather than a Property)
                                ChannelType channelType = channelTypeRegistry
                                        .getChannelType(chanDef.getChannelTypeUID());
                                if (channelType == null) {
                                    logger.warn("{} fatal error ChannelType '{}' is not registered", thing.getUID(),
                                            chanDef.getChannelTypeUID());
                                } else {
                                    logger.trace(
                                            "{}    ChannelType category:{}, description:{}, itemType:{}, label:{}, autoUpdatePolicy:{}, itemType:{}, kind:{}, tags:{}, uid:{}, unitHint:{}",
                                            thing.getUID(), channelType.getCategory(), channelType.getDescription(),
                                            channelType.getItemType(), channelType.getLabel(),
                                            channelType.getAutoUpdatePolicy(), channelType.getItemType(),
                                            channelType.getKind(), channelType.getTags(), channelType.getUID(),
                                            channelType.getUnitHint());

                                    String channelId = chanDef.getId();
                                    if (uniqueChannelsMap.containsKey(channelId)) {
                                        logger.debug("{}     Error duplicate channelId:{}", thing.getUID(), channelId);
                                    } else {
                                        ChannelUID channelUID = new ChannelUID(thing.getUID(), groupDef.getId(),
                                                channelId);
                                        ChannelBuilder builder = ChannelBuilder.create(channelUID)
                                                .withAcceptedItemType(channelType.getItemType())
                                                .withAutoUpdatePolicy(channelType.getAutoUpdatePolicy())
                                                .withDefaultTags(channelType.getTags()).withKind(channelType.getKind())
                                                .withProperties(chanDef.getProperties()).withType(channelType.getUID());
                                        Optional.ofNullable(chanDef.getLabel()).ifPresent(builder::withLabel);
                                        Optional.ofNullable(chanDef.getDescription())
                                                .ifPresent(builder::withDescription);
                                        Channel channel = builder.build();
                                        uniqueChannelsMap.put(channelId, channel);

                                        logger.trace(
                                                "{}     Channel acceptedItemType:{}, defaultTags:{}, description:{}, kind:{}, label:{}, properties:{}, uid:{}",
                                                thing.getUID(), channel.getAcceptedItemType(), channel.getDefaultTags(),
                                                channel.getDescription(), channel.getKind(), channel.getLabel(),
                                                channel.getProperties(), channel.getUID());
                                    }
                                }
                            }
                        });
                    }
                });

        lightModelFinalize(accessory, uniqueChannelsMap);
        stopMoveFinalize(accessory, uniqueChannelsMap);
        eventingPollingFinalize(accessory, uniqueChannelsMap);

        String oldLabel = thing.getLabel();
        String newLabel = oldLabel == null || oldLabel.isEmpty() ? accessory.getAccessoryInstanceLabel() : null;
        List<Channel> newChannels = !uniqueChannelsMap.isEmpty() ? uniqueChannelsMap.values().stream().toList() : null;
        Map<String, String> newProperties = !properties.isEmpty() ? properties : null;
        SemanticTag newEquipmentTag = accessory.getSemanticEquipmentTag();

        if (newLabel != null || newChannels != null || newProperties != null || newEquipmentTag != null) {
            ThingBuilder builder = editThing();
            Optional.ofNullable(newLabel).ifPresent(builder::withLabel);
            Optional.ofNullable(newChannels).ifPresent(builder::withChannels);
            Optional.ofNullable(newProperties).ifPresent(builder::withProperties);
            Optional.ofNullable(newEquipmentTag).ifPresent(builder::withSemanticEquipmentTag);

            updateThing(builder.build());
            logger.debug(
                    "{} updated with {} channels (of which {} polled, {} evented), {} properties, label: '{}', equipment tag: '{}'",
                    thing.getUID(), uniqueChannelsMap.size(), polledCharacteristics.size(),
                    eventedCharacteristics.size(), properties.size(), newLabel, newEquipmentTag);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = thing.getChannel(channelUID);
        if (channel == null) {
            logger.warn("{} received command '{}' for unknown channel '{}'", thing.getUID(), command, channelUID);
            return;
        }
        if (command == RefreshType.REFRESH) {
            requestManualRefresh();
            return;
        }
        try {
            if (command instanceof StopMoveType stopMoveType && StopMoveType.STOP == stopMoveType) {
                if (stopMoveChannel instanceof Channel stopMoveChannel) {
                    writeChannel(stopMoveChannel, OnOffType.ON);
                } else if (readChannel(channel) instanceof Command actualPosition) {
                    writeChannel(channel, actualPosition);
                }
            } else if (channelUID.equals(lightModelClientHSBTypeChannel)) {
                lightModelHandleCommand(command);
                if (lightModel instanceof LightModel lightModel) {
                    lightModelLinks.forEach(link -> {
                        switch (link.cxxType) {
                            case HUE -> {
                                QuantityType<Angle> hue = QuantityType.valueOf(lightModel.getHue(), Units.DEGREE_ANGLE);
                                updateState(link.channel.getUID(), hue);
                            }
                            case SATURATION -> {
                                PercentType sat = new PercentType(BigDecimal.valueOf(lightModel.getSaturation()));
                                updateState(link.channel.getUID(), sat);
                            }
                            case BRIGHTNESS -> {
                                if (lightModel.getBrightness(true) instanceof PercentType bri) {
                                    updateState(link.channel.getUID(), bri);
                                }
                            }
                            case ON -> {
                                if (lightModel.getOnOff(true) instanceof OnOffType onOff) {
                                    updateState(link.channel.getUID(), onOff);
                                }
                            }
                            default -> {
                            }
                        }
                    });
                }
            } else {
                writeChannel(channel, command);
            }
            return; // success
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // shutting down; restore interrupt flag but otherwise do nothing
        } catch (Exception e) {
            if (isCommunicationException(e)) {
                // communication exception; log at debug and try to reconnect
                logger.debug("{} communication error '{}' sending command '{}' to '{}', reconnecting..", thing.getUID(),
                        e.getMessage(), command, channelUID, e);
                scheduleConnectionAttempt();
            } else {
                // other exception; log at warn and don't try to reconnect
                logger.warn("{} unexpected error '{}' sending command '{}' to '{}'", thing.getUID(), e.getMessage(),
                        command, channelUID, e);
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    THING_STATUS_FMT.formatted("error.error-sending-command", e.getMessage()));
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        if (isBridgedAccessory) {
            if (getBridge() instanceof Bridge bridge && bridge.getStatus() == ThingStatus.ONLINE) {
                scheduler.submit(() -> {
                    onConnectedThingAccessoriesLoaded();
                    updateStatus(ThingStatus.ONLINE);
                });
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void dispose() {
        lightModel = null;
        lightModelLinks.clear();
        lightModelClientHSBTypeChannel = null;
        eventedCharacteristics.clear();
        polledCharacteristics.clear();
        super.dispose();
    }

    private @Nullable StateDescription getStateDescription(Channel channel) {
        ChannelTypeUID uid = channel.getChannelTypeUID();
        ChannelType ct = channelTypeRegistry.getChannelType(uid);
        if (ct == null) {
            logger.warn("{} channel '{}' is missing a channel type", thing.getUID(), uid);
            return null;
        }
        StateDescription st = ct.getState();
        if (st == null) {
            logger.warn("{} channel '{}' of type '{}' is missing a state description", thing.getUID(), uid,
                    ct.getUID());
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
     * Refreshes the light model state based on the updated characteristic value.
     *
     * @param cxx the characteristic containing the updated value
     * @return true if the light model was updated, false otherwise
     * @throws IllegalStateException if the light model is not initialized
     */
    private boolean lightModelRefresh(Characteristic cxx) throws IllegalStateException {
        LightModel lightModel = this.lightModel;
        if (lightModel == null) {
            throw new IllegalStateException("Light model is not initialized");
        }
        boolean changed = false;
        Optional<LightModelLink> link = lightModelLinks.stream().filter(e -> e.cxxIid.equals(cxx.iid)).findFirst();
        if (link.isPresent() && cxx.value instanceof JsonPrimitive primitiveValue) {
            CharacteristicType cxxType = link.get().cxxType;
            if (primitiveValue.isNumber()) {
                changed = true;
                switch (cxxType) {
                    case ON -> lightModel.setOnOff(primitiveValue.getAsInt() != 0); // number
                    case HUE -> lightModel.setHue(primitiveValue.getAsDouble());
                    case SATURATION -> lightModel.setSaturation(primitiveValue.getAsDouble());
                    case BRIGHTNESS -> lightModel.setBrightness(primitiveValue.getAsDouble());
                    case COLOR_TEMPERATURE -> lightModel.setMirek(primitiveValue.getAsDouble());
                    default -> changed = false;
                }
            } else {
                switch (cxxType) {
                    case ON -> lightModel.setOnOff(primitiveValue.getAsBoolean()); // string, boolean
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
     * @throws Exception compiler requires us to handle any exception; but actually will be one of the following:
     * @throws ExecutionException if there is an execution error
     * @throws TimeoutException if the operation times out
     * @throws InterruptedException if the operation is interrupted
     * @throws IOException if there is a communication error
     * @throws IllegalStateException if the accessory ID or characteristic IID are not initialized
     */
    private void lightModelHandleCommand(Command command) throws Exception {
        LightModel lightModel = this.lightModel;
        if (lightModel == null) {
            throw new IllegalStateException("Light model is not initialized");
        }
        lightModel.handleCommand(command);
        Optional<LightModelLink> link;
        link = lightModelLinks.stream().filter(e -> CharacteristicType.HUE == e.cxxType).findFirst();
        if (link.isPresent()) {
            QuantityType<Angle> hue = QuantityType.valueOf(lightModel.getHue(), Units.DEGREE_ANGLE);
            writeChannel(link.get().channel, hue);
        }
        link = lightModelLinks.stream().filter(e -> CharacteristicType.SATURATION == e.cxxType).findFirst();
        if (link.isPresent()) {
            PercentType saturation = new PercentType(BigDecimal.valueOf(lightModel.getSaturation()));
            writeChannel(link.get().channel, saturation);
        }
        link = lightModelLinks.stream().filter(e -> CharacteristicType.BRIGHTNESS == e.cxxType).findFirst();
        if (link.isPresent() && lightModel.getBrightness(true) instanceof PercentType brightness) {
            writeChannel(link.get().channel, brightness);
        }
        link = lightModelLinks.stream().filter(e -> CharacteristicType.ON == e.cxxType).findFirst();
        if (link.isPresent() && lightModel.getOnOff(true) instanceof OnOffType onOff) {
            writeChannel(link.get().channel, onOff);
        }
    }

    /**
     * Finalizes the light model channels by mapping the relevant characteristic and channel links
     * and creating a combined HSB channel.
     *
     * @param accessory the accessory containing the characteristics
     * @param channels the list of channels to finalize
     */
    private void lightModelFinalize(Accessory accessory, Map<String, Channel> channels) {
        if (lightModel == null) {
            return;
        }
        // link channels to characteristic types & iids for the light model
        lightModelLinks.clear();
        for (Channel channel : channels.values()) {
            if (channel.getProperties().get(PROPERTY_IID) instanceof String iid) {
                for (Service service : accessory.services) {
                    for (Characteristic cxx : service.characteristics) {
                        if (iid.equals(String.valueOf(cxx.iid))) {
                            CharacteristicType cxxType = cxx.getCharacteristicType();
                            if (LIGHT_MODEL_RELEVANT_TYPES.contains(cxxType)) {
                                lightModelLinks.add(new LightModelLink(channel, cxxType, cxx.iid));
                            }
                        }
                    }
                }
            }
        }
        // create combined HSB channel
        ChannelUID uid = new ChannelUID(thing.getUID(), "hsb-combined-channel");
        Channel channel = ChannelBuilder.create(uid, CoreItemFactory.COLOR)
                .withType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_COLOR).build();
        channels.put(uid.getId(), channel); // add to channels map
        logger.trace(
                "{}     Channel acceptedItemType:{}, defaultTags:{}, description:{}, kind:{}, label:{}, properties:{}, uid:{}",
                thing.getUID(), channel.getAcceptedItemType(), channel.getDefaultTags(), channel.getDescription(),
                channel.getKind(), channel.getLabel(), channel.getProperties(), channel.getUID());
        lightModelClientHSBTypeChannel = uid;
    }

    /**
     * Initializes the stop/move button channel by searching for a characteristic of type POSITION_HOLD.
     *
     * @param accessory the accessory containing the characteristics
     * @param channels the list of channels to search
     */
    private void stopMoveFinalize(Accessory accessory, Map<String, Channel> channels) {
        for (Channel channel : channels.values()) {
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
     * Finalizes the polled and evented characteristics by identifying which characteristics are linked
     * and adding them to the polledCharacteristics list, and which subset of those are evented and adding
     * them also to the eventedCharacteristics list. In case of the special light model HSB channel then we
     * also add the component HUE, SATURATION, BRIGHTNESS, ON, and color temperature characteristsics to
     * the list of polled and evented characteristics.
     *
     * @param accessory the accessory containing the characteristics
     * @param channels the list of channels to check for polled and evented characteristics
     */
    private void eventingPollingFinalize(Accessory accessory, Map<String, Channel> channels) {
        eventedCharacteristics.clear();
        polledCharacteristics.clear();

        final Long aid = getAccessoryId();
        if (aid == null) {
            return;
        }

        for (Channel channel : channels.values()) {
            final ChannelUID channelUID = channel.getUID();
            if (isLinked(channelUID)) {
                Long iid = 0L;
                boolean checkChannelLinkByIID = !channelUID.equals(lightModelClientHSBTypeChannel);
                if (checkChannelLinkByIID && channel.getProperties().get(PROPERTY_IID) instanceof String iidProperty) {
                    try {
                        iid = Long.parseLong(iidProperty);
                    } catch (NumberFormatException e) {
                        continue; // error will already have been logged elsewhere
                    }
                }

                nestedLoops: // break marker for nested loops below
                for (Service service : accessory.services) {
                    for (Characteristic characteristic : service.characteristics) {
                        if ((checkChannelLinkByIID && iid.equals(characteristic.iid))
                                || LIGHT_MODEL_RELEVANT_TYPES.contains(characteristic.getCharacteristicType())) {
                            Characteristic entry = new Characteristic();
                            entry.aid = aid;
                            entry.iid = characteristic.iid;
                            polledCharacteristics.put(AID_IID_FORMAT.formatted(entry.aid, entry.iid), entry);
                            if (characteristic.perms instanceof List<String> perms && perms.contains("ev")) {
                                entry = new Characteristic();
                                entry.aid = aid;
                                entry.iid = characteristic.iid;
                                eventedCharacteristics.put(AID_IID_FORMAT.formatted(entry.aid, entry.iid), entry);
                            }
                            if (checkChannelLinkByIID) {
                                break nestedLoops; // unique match found; continue to next channel
                            }
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
     * @throws Exception compiler requires us to handle any exception; but actually will be one of the following:
     * @throws ExecutionException if there is an execution error
     * @throws TimeoutException if the operation times out
     * @throws InterruptedException if the operation is interrupted
     * @throws IOException if there is a communication error
     * @throws IllegalStateException if the read/write service is not initialized
     */
    private synchronized @Nullable State readChannel(Channel channel) throws Exception {
        Long aid = getAccessoryId();
        String iid = channel.getProperties().get(PROPERTY_IID);
        if (aid == null || iid == null) {
            throw new IllegalStateException(
                    "Missing accessory ID or characteristic IID for channel " + channel.getUID());
        }
        String jsonResponse = readCharacteristics("%s.%s".formatted(aid, iid));
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
     * @throws Exception compiler requires us to handle any exception; but actually will be one of the following:
     * @throws ExecutionException if there is an execution error
     * @throws TimeoutException if the operation times out
     * @throws InterruptedException if the operation is interrupted
     * @throws IOException if there is a communication error
     * @throws IllegalStateException if the accessory ID or characteristic IID are not initialized
     */
    private synchronized void writeChannel(Channel channel, Command command) throws Exception {
        Long aid = getAccessoryId();
        String iid = channel.getProperties().get(PROPERTY_IID);
        if (aid == null || iid == null) {
            throw new IllegalStateException(
                    "Missing accessory ID or characteristic IID for channel " + channel.getUID());
        }
        Service service = new Service();
        Characteristic characteristic = new Characteristic();
        characteristic.aid = aid;
        characteristic.iid = Long.parseLong(iid);
        characteristic.value = commandToJsonPrimitive(command, channel);
        service.characteristics = List.of(characteristic);
        String response = writeCharacteristics(GSON.toJson(service));
        Service serviceResponse = GSON.fromJson(response, Service.class); // check for errors
        if (serviceResponse != null
                && serviceResponse.characteristics instanceof List<Characteristic> characteristics) {
            for (Characteristic cxx : characteristics) {
                if (cxx.getStatusCode() instanceof StatusCode code && code != StatusCode.SUCCESS) {
                    logger.warn("{} error writing to channel '{}': {}", thing.getUID(), channel.getUID(), code);
                }
            }
        }
    }

    /**
     * Updates the channels based on the provided JSON content.
     *
     * @param json the JSON content containing characteristic values
     */
    private void updateChannelsFromJson(String json) {
        Long aid = getAccessoryId();
        ChannelUID hsbChannelUID = null;
        Service service = GSON.fromJson(json, Service.class);
        if (service != null && service.characteristics instanceof List<Characteristic> characteristics) {
            for (Channel channel : thing.getChannels()) {
                ChannelUID channelUID = channel.getUID();
                if (channelUID.equals(lightModelClientHSBTypeChannel)) {
                    for (Characteristic cxx : characteristics) {
                        if (Objects.equals(cxx.aid, aid) && lightModelRefresh(cxx)) {
                            hsbChannelUID = channelUID;
                        }
                    }
                } else if (channel.getProperties().get(PROPERTY_IID) instanceof String iid) {
                    for (Characteristic cxx : characteristics) {
                        if (Objects.equals(cxx.aid, aid) && iid.equals(String.valueOf(cxx.iid))
                                && cxx.value instanceof JsonElement element) {
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
        if (thing.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        if (hsbChannelUID != null) {
            updateState(hsbChannelUID, Objects.requireNonNull(lightModel).getHsb());
        }
    }

    /**
     * Override method to delegate to the bridge IP transport if we are a bridged accessory.
     *
     * @return own IpTransport service or bridge IpTransport service if we are a bridged accessory.
     * @throws IllegalAccessException if access to the transport is denied.
     */
    @Override
    protected IpTransport getIpTransport() throws IllegalAccessException {
        if (isBridgedAccessory) {
            if (getBridge() instanceof Bridge bridge
                    && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
                return bridgeHandler.getIpTransport();
            } else {
                throw new IllegalAccessException("Cannot access bridge IP transport");
            }
        }
        return super.getIpTransport();
    }

    @Override
    protected void onConnectedThingAccessoriesLoaded() {
        createProperties();
        createChannels();
        removeNotReadyThing(thing);
    }

    @Override
    public void onEvent(String json) {
        updateChannelsFromJson(json);
    }

    /**
     * When a channel is linked, check if it corresponds to a characteristic in this accessory.
     * If so, add it to the polledCharacteristics and eventedCharacteristics maps as appropriate.
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        boolean eventedCharacteristicsChanged = false;
        try {
            if (!alreadyAtStartLevelComplete()) {
                return; // item-channel-links not yet fully initialized
            }
            final Channel channel = thing.getChannel(channelUID);
            if (channel == null) {
                return; // OH core ensures this does not happen
            }
            final Long aid = getAccessoryId();
            if (aid == null) {
                return; // error will already have been logged elsewhere
            }
            final Accessory accessory = getAccessories().get(aid);
            if (accessory == null) {
                return; // error will already have been logged elsewhere
            }

            Long iid = 0L;
            boolean checkChannelLinkByIID = !channelUID.equals(lightModelClientHSBTypeChannel);
            if (checkChannelLinkByIID) {
                final String iidProperty = channel.getProperties().get(PROPERTY_IID);
                if (iidProperty == null) {
                    return; // error will already have been logged elsewhere
                }
                try {
                    iid = Long.parseLong(iidProperty);
                } catch (NumberFormatException e) {
                    return; // error will already have been logged elsewhere
                }
            }

            for (Service service : accessory.services) {
                for (Characteristic characteristic : service.characteristics) {
                    if ((checkChannelLinkByIID && iid.equals(characteristic.iid))
                            || LIGHT_MODEL_RELEVANT_TYPES.contains(characteristic.getCharacteristicType())) {
                        Characteristic entry = new Characteristic();
                        entry.aid = aid;
                        entry.iid = iid;
                        polledCharacteristics.put(AID_IID_FORMAT.formatted(entry.aid, entry.iid), entry);
                        if (characteristic.perms instanceof List<String> perms && perms.contains("ev")) {
                            entry = new Characteristic();
                            entry.aid = aid;
                            entry.iid = iid;
                            eventedCharacteristics.put(AID_IID_FORMAT.formatted(entry.aid, entry.iid), entry);
                            eventedCharacteristicsChanged = true;
                        }
                        if (checkChannelLinkByIID) {
                            return; // unique match found; return directly
                        }
                    }
                }
            }
        } finally {
            if (eventedCharacteristicsChanged) { // if evented list changes (re-) enable eventing (using new list)
                scheduler.submit(() -> enableEvents(true));
            }
            super.channelLinked(channelUID);
        }
    }

    @Override
    protected Map<String, Characteristic> getEventedCharacteristics() {
        return eventedCharacteristics;
    }

    @Override
    protected Map<String, Characteristic> getPolledCharacteristics() {
        return polledCharacteristics;
    }

    /**
     * Return true if STARTLEVEL_COMPLETE has already been achieved.
     * <p>
     * Note: STARTLEVEL_COMPLETE means all Thing handlers are instantiated and their initialize() methods have
     * been called, and the registries for item, thing, and item-channel-links have all been loaded.
     */
    private boolean alreadyAtStartLevelComplete() {
        if (bundle.getBundleContext() instanceof BundleContext ctx) {
            if (ctx.getServiceReference(StartLevelService.class) instanceof ServiceReference<StartLevelService> ref) {
                if (ctx.getService(ref) instanceof StartLevelService svc) {
                    try {
                        return svc.getStartLevel() >= StartLevelService.STARTLEVEL_COMPLETE;
                    } finally {
                        ctx.ungetService(ref);
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void initializeNotReadyThings() {
        notReadyThings.clear();
        notReadyThings.add(thing); // a self connected accessory requires only itself to be ready
    }
}
