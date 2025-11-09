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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.openhab.binding.homekit.internal.hap_services.CharacteristicReadWriteClient;
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
    private @Nullable LightModel lightModel = null;
    private @Nullable ChannelUID lightModelClientHSBTypeChannel = null; // special HSB combined channel

    /*
     * Internal record representing a link between an OH channel and a HomeKit characteristic type & iid.
     * Used for light model management.
     */
    private record LightModelLink(Channel channel, CharacteristicType cxxType, Long cxxIid) {
    }

    private final List<LightModelLink> lightModelLinks = new ArrayList<>();

    private @Nullable Channel stopMoveChannel = null; // channel for the stop button (rollershutters)
    private @Nullable ScheduledFuture<?> refreshTask;

    public HomekitAccessoryHandler(Thing thing, HomekitTypeProvider typeProvider,
            ChannelTypeRegistry channelTypeRegistry, ChannelGroupTypeRegistry channelGroupTypeRegistry,
            HomekitKeyStore keyStore, TranslationProvider i18nProvider, Bundle bundle) {
        super(thing, typeProvider, keyStore, i18nProvider, bundle);
        this.channelTypeRegistry = channelTypeRegistry;
        this.channelGroupTypeRegistry = channelGroupTypeRegistry;
    }

    /**
     * Called when the thing handler has been initialized, the pairing verified, the accessories loaded,
     * and the channels and properties created. Sets up a scheduled task to periodically refresh the state
     * of the accessory.
     */
    private void startRefreshTask() {
        if (getConfig().get(CONFIG_REFRESH_INTERVAL) instanceof Object refreshInterval) {
            try {
                int refreshIntervalSeconds = Integer.parseInt(refreshInterval.toString());
                if (refreshIntervalSeconds > 0) {
                    ScheduledFuture<?> task = refreshTask;
                    if (task == null || task.isCancelled() || task.isDone()) {
                        refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, INITIAL_DELAY_SECONDS,
                                refreshIntervalSeconds, TimeUnit.SECONDS);
                    }
                }
            } catch (NumberFormatException e) {
                // logged below
            }
        }
        if (refreshTask == null) {
            logger.warn("Invalid refresh interval configuration, polling disabled");
        }
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
                    logger.warn("Unexpected unit '{}' for channel '{}'", channelUnit, channel.getUID());
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
                            logger.warn("Unexpected state option value '{}' for channel '{}'", optionValue,
                                    channel.getUID());
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
                    logger.warn("Channel {} wrong item type 'COLOR'", channel.getUID());
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
                        logger.warn("Fatal Error: ChannelGroupType '{}' is not registered", groupDef.getTypeUID());
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
                                if (chanDef.getLabel() instanceof String value) {
                                    properties.put(name, value);
                                    logger.trace("++++Property '{}:{}'", name, value);
                                }
                            } else {
                                // this is a real channel
                                ChannelType channelType = channelTypeRegistry
                                        .getChannelType(chanDef.getChannelTypeUID());
                                if (channelType == null) {
                                    logger.warn("Fatal Error: ChannelType '{}' is not registered",
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
        eventingFinalize(accessory, channels);

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
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = thing.getChannel(channelUID);
        if (channel == null) {
            logger.warn("Received command '{}' for unknown channel '{}'", command, channelUID);
            return;
        }
        if (command == RefreshType.REFRESH) {
            refresh();
        }
        try {
            if (command instanceof StopMoveType stopMoveType && StopMoveType.STOP == stopMoveType) {
                if (stopMoveChannel instanceof Channel stopMoveChannel) {
                    writeChannel(stopMoveChannel, OnOffType.ON, getRwService());
                } else if (readChannel(channel, getRwService()) instanceof Command actualPosition) {
                    writeChannel(channel, actualPosition, getRwService());
                }
            } else if (channelUID.equals(lightModelClientHSBTypeChannel)) {
                lightModelHandleCommand(command, getRwService());
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
                writeChannel(channel, command, getRwService());
            }
            return; // success
        } catch (InterruptedException e) {
            // shutting down; do nothing
        } catch (Exception e) {
            if (isCommunicationException(e)) {
                // communication exception; log at debug and try to reconnect
                logger.debug("Communication error '{}' sending command '{}' to '{}', reconnecting..", e.getMessage(),
                        command, channelUID);
                scheduleConnectionAttempt();
            } else {
                // other exception; log at warn and don't try to reconnect
                logger.warn("Unexpected error '{}' sending command '{}' to '{}'", e.getMessage(), command, channelUID);
            }
            logger.debug("Stack trace", e);
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                i18nProvider.getText(bundle, "error.error-sending-command", "Error sending command", null));
    }

    @Override
    public void initialize() {
        super.initialize();
        if (isChildAccessory) {
            if (getBridge() instanceof Bridge bridge && bridge.getStatus() == ThingStatus.ONLINE) {
                scheduler.submit(() -> {
                    onAccessoriesLoaded();
                    onRootHandlerReady();
                    updateStatus(ThingStatus.ONLINE);
                });
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void handleRemoval() {
        cancelRefreshTask();
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        cancelRefreshTask();
        lightModel = null;
        lightModelLinks.clear();
        lightModelClientHSBTypeChannel = null;
        eventedCharacteristics.clear();
        super.dispose();
    }

    /**
     * Polls the accessory for its current state and updates the corresponding channels.
     * This method is called periodically by a scheduled executor.
     */
    private void refresh() {
        Long aid = getAccessoryId();
        List<String> queries = new ArrayList<>();
        thing.getChannels().stream().forEach(c -> {
            if (isLinked(c.getUID()) && c.getProperties().get(PROPERTY_IID) instanceof String iid) {
                queries.add("%s.%s".formatted(aid, iid));
            }
        });
        if (queries.isEmpty()) {
            return;
        }
        try {
            String json = getRwService().readCharacteristic(String.join(",", queries));
            updateChannelsFromJson(json);
            return;
        } catch (InterruptedException e) {
            // shutting down; do nothing
        } catch (Exception e) {
            if (isCommunicationException(e)) {
                // communication exception; log at debug and try to reconnect
                logger.debug("Communication error '{}' polling accessory, reconnecting..", e.getMessage());
                scheduleConnectionAttempt();
            } else {
                // other exception; log at warn and don't try to reconnect
                logger.warn("Unexpected error '{}' polling accessory", e.getMessage());
            }
            logger.debug("Stack trace", e);
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                i18nProvider.getText(bundle, "error.polling-error", "Polling error", null));
    }

    private void cancelRefreshTask() {
        if (refreshTask instanceof ScheduledFuture<?> task) {
            task.cancel(true);
        }
        refreshTask = null;
    }

    private @Nullable StateDescription getStateDescription(Channel channel) {
        ChannelTypeUID uid = channel.getChannelTypeUID();
        ChannelType ct = channelTypeRegistry.getChannelType(uid);
        if (ct == null) {
            logger.warn("Channel '{}' is missing a channel type", uid);
            return null;
        }
        StateDescription st = ct.getState();
        if (st == null) {
            logger.warn("Channel '{}' of type '{}' is missing a state description", uid, ct.getUID());
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
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws IOException
     * @throws IllegalStateException
     */
    private void lightModelHandleCommand(Command command, CharacteristicReadWriteClient writer)
            throws IOException, InterruptedException, TimeoutException, ExecutionException, IllegalStateException {
        LightModel lightModel = this.lightModel;
        if (lightModel == null) {
            throw new IllegalStateException("Light model is not initialized");
        }
        lightModel.handleCommand(command);
        Optional<LightModelLink> link;
        link = lightModelLinks.stream().filter(e -> CharacteristicType.HUE == e.cxxType).findFirst();
        if (link.isPresent()) {
            QuantityType<Angle> hue = QuantityType.valueOf(lightModel.getHue(), Units.DEGREE_ANGLE);
            writeChannel(link.get().channel, hue, writer);
        }
        link = lightModelLinks.stream().filter(e -> CharacteristicType.SATURATION == e.cxxType).findFirst();
        if (link.isPresent()) {
            PercentType saturation = new PercentType(BigDecimal.valueOf(lightModel.getSaturation()));
            writeChannel(link.get().channel, saturation, writer);
        }
        link = lightModelLinks.stream().filter(e -> CharacteristicType.BRIGHTNESS == e.cxxType).findFirst();
        if (link.isPresent() && lightModel.getBrightness(true) instanceof PercentType brightness) {
            writeChannel(link.get().channel, brightness, writer);
        }
        link = lightModelLinks.stream().filter(e -> CharacteristicType.ON == e.cxxType).findFirst();
        if (link.isPresent() && lightModel.getOnOff(true) instanceof OnOffType onOff) {
            writeChannel(link.get().channel, onOff, writer);
        }
    }

    /**
     * Finalizes the light model channels by mapping the relevant characteristic and channel links
     * and creating a combined HSB channel.
     *
     * @param accessory the accessory containing the characteristics
     * @param channels the list of channels to finalize
     */
    private void lightModelFinalize(Accessory accessory, List<Channel> channels) {
        if (lightModel == null) {
            return;
        }
        // link channels to characteristic types & iids for the light model
        lightModelLinks.clear();
        for (Channel channel : channels) {
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
        channels.add(channel);
        logger.trace(
                "+++++Channel acceptedItemType:{}, defaultTags:{}, description:{}, kind:{}, label:{}, properties:{}, uid:{}",
                channel.getAcceptedItemType(), channel.getDefaultTags(), channel.getDescription(), channel.getKind(),
                channel.getLabel(), channel.getProperties(), channel.getUID());
        lightModelClientHSBTypeChannel = uid;
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
     * Identifies evented channels by checking for characteristics with the 'ev' permission.
     *
     * @param accessory the accessory containing the characteristics
     * @param channels the list of channels to check
     */
    private void eventingFinalize(Accessory accessory, List<Channel> channels) {
        eventedCharacteristics.clear();
        for (Channel channel : channels) {
            if (isLinked(channel.getUID()) && channel.getProperties().get(PROPERTY_IID) instanceof String iid) {
                for (Service service : accessory.services) {
                    for (Characteristic cxx : service.characteristics) {
                        if (iid.equals(String.valueOf(cxx.iid)) && cxx.perms instanceof List<String> perms
                                && perms.contains("ev")) {
                            Characteristic eventedCxx = new Characteristic();
                            eventedCxx.iid = Long.parseLong(iid);
                            eventedCxx.aid = getAccessoryId();
                            eventedCharacteristics.add(eventedCxx);
                        }
                    }
                }
            }
        }
        logger.debug("Identified {} evented channels", eventedCharacteristics.size());
    }

    /**
     * Reads the state of a specific channel by querying the accessory for the characteristic value.
     *
     * @param channel the channel to read
     * @return the current state of the channel, or null if not found
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws IOException
     * @throws IllegalStateException
     */
    private synchronized @Nullable State readChannel(Channel channel, CharacteristicReadWriteClient reader)
            throws IOException, InterruptedException, TimeoutException, ExecutionException, IllegalStateException {
        Long aid = getAccessoryId();
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
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws IOException
     * @throws IllegalStateException
     */
    private synchronized void writeChannel(Channel channel, Command command, CharacteristicReadWriteClient writer)
            throws IOException, InterruptedException, TimeoutException, ExecutionException, IllegalStateException {
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
        String response = writer.writeCharacteristic(GSON.toJson(service));
        Service serviceResponse = GSON.fromJson(response, Service.class); // check for errors
        if (serviceResponse != null
                && serviceResponse.characteristics instanceof List<Characteristic> characteristics) {
            for (Characteristic cxx : characteristics) {
                if (cxx.getStatusCode() instanceof StatusCode code && code != StatusCode.SUCCESS) {
                    logger.warn("Error writing to channel '{}': {}", channel.getUID(), code);
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
     * Override method to delegate to the bridge IP transport if we are a child accessory.
     *
     * @return own IpTransport service or bridge IpTransport service if we are a child.
     * @throws IllegalAccessException if access to the transport is denied.
     */
    @Override
    protected IpTransport getIpTransport() throws IllegalAccessException {
        if (isChildAccessory) {
            if (getBridge() instanceof Bridge bridge
                    && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
                return bridgeHandler.getIpTransport();
            } else {
                throw new IllegalAccessException("Cannot access bridge IP transport");
            }
        }
        return super.getIpTransport();
    }

    /**
     * Override method to delegate to the bridge read/write service if we are a child accessory.
     *
     * @return own CharacteristicReadWriteClient service or bridge service if we are a child.
     * @throws IllegalAccessException if access to the service is denied.
     */
    @Override
    protected CharacteristicReadWriteClient getRwService() throws IllegalAccessException {
        if (isChildAccessory) {
            if (getBridge() instanceof Bridge bridge
                    && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
                return bridgeHandler.getRwService();
            } else {
                throw new IllegalAccessException("Cannot access bridge read/write service");
            }
        }
        return super.getRwService();
    }

    @Override
    protected boolean checkHandlersInitialized() {
        return isInitialized();
    }

    @Override
    protected void onAccessoriesLoaded() {
        createProperties();
        createChannels();
    }

    @Override
    protected void onRootHandlerReady() {
        startRefreshTask();
    }

    @Override
    public void onEvent(String json) {
        updateChannelsFromJson(json);
    }
}
