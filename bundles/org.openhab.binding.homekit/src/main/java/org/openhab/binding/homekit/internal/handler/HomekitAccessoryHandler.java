/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;
import javax.measure.quantity.Angle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.camera.codec.AudioCodec;
import org.openhab.binding.homekit.internal.camera.codec.VideoCodec;
import org.openhab.binding.homekit.internal.camera.ffmpeg.FfmpegInstaller;
import org.openhab.binding.homekit.internal.camera.go2rtc.Go2RtcCameraServlet;
import org.openhab.binding.homekit.internal.camera.go2rtc.Go2RtcConfig;
import org.openhab.binding.homekit.internal.camera.go2rtc.Go2RtcDataPump;
import org.openhab.binding.homekit.internal.camera.go2rtc.Go2RtcDemand;
import org.openhab.binding.homekit.internal.camera.go2rtc.Go2RtcInstaller;
import org.openhab.binding.homekit.internal.camera.go2rtc.Go2RtcProcess;
import org.openhab.binding.homekit.internal.camera.session.SrtpParameters;
import org.openhab.binding.homekit.internal.camera.tlv.AudioSelectedParameters;
import org.openhab.binding.homekit.internal.camera.tlv.SetupEndpointsRequest;
import org.openhab.binding.homekit.internal.camera.tlv.SetupEndpointsResponse;
import org.openhab.binding.homekit.internal.camera.tlv.SrtpStreamConfiguration;
import org.openhab.binding.homekit.internal.camera.tlv.SrtpStreamConfiguration.SessionCommand;
import org.openhab.binding.homekit.internal.camera.tlv.VideoSelectedParameters;
import org.openhab.binding.homekit.internal.discovery.HomekitMdnsDiscoveryParticipant;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.dto.Characteristic;
import org.openhab.binding.homekit.internal.dto.Service;
import org.openhab.binding.homekit.internal.enums.AccessoryCategory;
import org.openhab.binding.homekit.internal.enums.AudioCodecType;
import org.openhab.binding.homekit.internal.enums.CharacteristicType;
import org.openhab.binding.homekit.internal.enums.DataFormatType;
import org.openhab.binding.homekit.internal.enums.ServiceType;
import org.openhab.binding.homekit.internal.enums.SetupEndpointsStatus;
import org.openhab.binding.homekit.internal.enums.StatusCode;
import org.openhab.binding.homekit.internal.enums.VideoCodecType;
import org.openhab.binding.homekit.internal.persistence.HomekitKeyStore;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.binding.homekit.internal.transport.IpTransport;
import org.openhab.core.OpenHAB;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.mac.MacResolver;
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
import org.openhab.core.semantics.model.DefaultSemanticTags.Equipment;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
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
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.openhab.core.util.LightModel;
import org.openhab.core.util.LightModel.LightCapabilities;
import org.openhab.core.util.LightModel.RgbDataType;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
 * @author Andrew Fiddian-Green - Camera talk-back channel
 * @author Andrew Fiddian-Green - Publish camera URL when the channel is linked
 * @author Andrew Fiddian-Green - Register each camera with the viewer servlet
 */
@NonNullByDefault
public class HomekitAccessoryHandler extends HomekitBaseAccessoryHandler
        implements Go2RtcCameraServlet.BrowserStreamProvider {

    // Characteristic types relevant for light model management
    private static final Set<CharacteristicType> LIGHT_MODEL_RELEVANT_TYPES = Set.of(CharacteristicType.HUE,
            CharacteristicType.SATURATION, CharacteristicType.BRIGHTNESS, CharacteristicType.COLOR_TEMPERATURE,
            CharacteristicType.ON);

    // Accessory Categories relevant for snapshot channel
    private static final Set<AccessoryCategory> CAMERA_ACCESSORY_CATEGORIES = Set.of(AccessoryCategory.IP_CAMERA,
            AccessoryCategory.VIDEO_DOORBELL);

    // Characteristic iids relevant for triggering snapshot channel refreshes
    private static final Map<Long, CharacteristicType> SNAPSHOT_REFRESH_TRIGGERS = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(HomekitAccessoryHandler.class);
    private final ChannelTypeRegistry channelTypeRegistry;
    private final ChannelGroupTypeRegistry channelGroupTypeRegistry;
    private final ManagedThingProvider managedThingProvider;
    private final Bundle bundle;

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
     * Channel for the current position (rollershutters)
     */
    private volatile @Nullable Channel positionCurrentChannel = null;

    /*
     * Internal record representing a link between an OH channel and a HomeKit characteristic type & iid.
     * Used for light model management.
     */
    private record LightModelLink(Channel channel, CharacteristicType cxxType, Long cxxIid) {
    }

    private final List<LightModelLink> lightModelLinks = new ArrayList<>();

    /*
     * Assets used for migrating a Thing to a Bridge
     */
    private static final int MIGRATION_DELAY_SECONDS = 1;
    private final AtomicBoolean migrating = new AtomicBoolean(false);
    private final AtomicBoolean disposing = new AtomicBoolean(false);

    /**
     * The go2rtc instance for the camera: its loopback API port, the stream names, and the camera's address.
     * API requests use {@link Go2RtcConfig#API_HOST}; the LAN address is used only for WebRTC media.
     */
    private record Go2RtcEndpoint(int apiPort, String stream, String remoteAddress) {
    }

    // constants for camera talk-back sessions
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long GO2RTC_READY_TIMEOUT_SECONDS = 10;
    private static final long GO2RTC_DEMAND_POLL_SECONDS = 2;
    private static final long GO2RTC_KEEPALIVE_SECONDS = 20;
    private static final long BROWSER_DEMAND_HOLD_SECONDS = 30;
    private static final long BROWSER_OPEN_TIMEOUT_SECONDS = 15;
    private static final long BROWSER_OPEN_POLL_MILLIS = 500;

    // fields for camera talk-back sessions
    private final Go2RtcDemand go2rtcDemand = new Go2RtcDemand();
    private final Go2RtcCameraServlet cameraServlet;
    private final AtomicBoolean go2rtcDemandChecking = new AtomicBoolean(false);
    private final ExecutorService go2rtcExecutor = ThreadPoolManager.getPool(BINDING_ID + "-go2rtc");

    private boolean go2rtcStarting = false;
    private @Nullable Go2RtcProcess go2rtcProcess = null;
    private @Nullable Go2RtcDataPump go2rtcPump = null;
    private @Nullable Go2RtcEndpoint go2rtcEndpoint = null;
    private @Nullable ScheduledFuture<?> go2rtcDemandPoller = null;
    private @Nullable ScheduledFuture<?> go2rtcKeepalive = null;

    private @Nullable Long cameraSetupIid = null;
    private @Nullable Long cameraSelectedIid = null;
    private boolean cameraSessionOpen = false;
    private byte[] cameraSessionId = new byte[16];

    private List<AudioCodec> audioCodecs = List.of();
    private List<VideoCodec> videoCodecs = List.of();

    public HomekitAccessoryHandler(Thing thing, HomekitTypeProvider typeProvider,
            ChannelTypeRegistry channelTypeRegistry, ChannelGroupTypeRegistry channelGroupTypeRegistry,
            HomekitKeyStore keyStore, TranslationProvider i18nProvider, Bundle bundle,
            ManagedThingProvider managedThingProvider, HomekitMdnsDiscoveryParticipant discoveryParticipant,
            MacResolver macResolver, Go2RtcCameraServlet cameraServlet) {
        super(thing, typeProvider, keyStore, i18nProvider, bundle, discoveryParticipant, macResolver);
        this.channelTypeRegistry = channelTypeRegistry;
        this.channelGroupTypeRegistry = channelGroupTypeRegistry;
        this.managedThingProvider = managedThingProvider;
        this.bundle = bundle;
        this.cameraServlet = cameraServlet;
    }

    /**
     * Converts an openHAB Command to a suitable object for writing to a HomeKit characteristic.
     * It handles various conversions including unit conversion, clamping to min/max values,
     * and converting specific types like OnOffType to boolean.
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
                                && getStateDescription(channel) instanceof StateDescription stateDescription
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

        // create the channels
        Map<String, Channel> uniqueChannelsMap = new HashMap<>(); // use map to prevent duplicate Channel ID
        accessory.getChannelGroupDefinitions(thing.getUID(), typeProvider, i18nProvider, bundle).forEach(groupDef -> {
            logger.trace("{} ChannelGroupDefinition id:{}, typeUID:{}, label:{}, description:{}", thing.getUID(),
                    groupDef.getId(), groupDef.getTypeUID(), groupDef.getLabel(), groupDef.getDescription());

            ChannelGroupType channelGroupType = channelGroupTypeRegistry.getChannelGroupType(groupDef.getTypeUID());
            if (channelGroupType == null) {
                logger.warn("{} fatal error ChannelGroupType '{}' is not registered", thing.getUID(),
                        groupDef.getTypeUID());
            } else {
                logger.trace("{}  ChannelGroupType UID:{}, label:{}, category:{}, description:{}", thing.getUID(),
                        channelGroupType.getUID(), channelGroupType.getLabel(), channelGroupType.getCategory(),
                        channelGroupType.getDescription());

                channelGroupType.getChannelDefinitions().forEach(chanDef -> {
                    logger.trace(
                            "{}   ChannelDefinition id:{}, label:{}, description:{}, channelTypeUID:{}, autoUpdatePolicy:{}, properties:{}",
                            thing.getUID(), chanDef.getId(), chanDef.getLabel(), chanDef.getDescription(),
                            chanDef.getChannelTypeUID(), chanDef.getAutoUpdatePolicy(), chanDef.getProperties());

                    ChannelType channelType = channelTypeRegistry.getChannelType(chanDef.getChannelTypeUID());
                    if (channelType == null) {
                        logger.warn("{} fatal error ChannelType '{}' is not registered", thing.getUID(),
                                chanDef.getChannelTypeUID());
                    } else {
                        logger.trace(
                                "{}    ChannelType category:{}, description:{}, itemType:{}, label:{}, autoUpdatePolicy:{}, itemType:{}, kind:{}, tags:{}, uid:{}, unitHint:{}",
                                thing.getUID(), channelType.getCategory(), channelType.getDescription(),
                                channelType.getItemType(), channelType.getLabel(), channelType.getAutoUpdatePolicy(),
                                channelType.getItemType(), channelType.getKind(), channelType.getTags(),
                                channelType.getUID(), channelType.getUnitHint());

                        String channelId = chanDef.getId();
                        if (uniqueChannelsMap.containsKey(channelId)) {
                            logger.debug("{}     Error duplicate channelId:{}", thing.getUID(), channelId);
                        } else {
                            ChannelUID channelUID = new ChannelUID(thing.getUID(), groupDef.getId(), channelId);
                            ChannelBuilder builder = ChannelBuilder.create(channelUID)
                                    .withAcceptedItemType(channelType.getItemType())
                                    .withAutoUpdatePolicy(channelType.getAutoUpdatePolicy())
                                    .withDefaultTags(channelType.getTags()).withKind(channelType.getKind())
                                    .withProperties(chanDef.getProperties()).withType(channelType.getUID());
                            Optional.ofNullable(chanDef.getLabel()).ifPresent(builder::withLabel);
                            Optional.ofNullable(chanDef.getDescription()).ifPresent(builder::withDescription);
                            Channel channel = builder.build();
                            uniqueChannelsMap.put(channelId, channel);
                            logChannelInformation(channel);
                        }
                    }
                });
            }
        });

        lightModelFinalize(accessory, uniqueChannelsMap);
        stopMoveFinalize(accessory, uniqueChannelsMap);
        eventingPollingFinalize(accessory, uniqueChannelsMap);

        Map<String, String> oldProperties = new HashMap<>(thing.getProperties());
        Map<String, String> getProperties = accessory.getProperties(thing.getUID(), typeProvider, i18nProvider, bundle);
        Map<String, String> newProperties;
        if (!getProperties.isEmpty()) {
            newProperties = oldProperties;
            newProperties.putAll(getProperties);
        } else {
            newProperties = null;
        }

        SemanticTag newEquipmentTag = accessory.getSemanticEquipmentTag();
        if (newEquipmentTag == null && oldProperties.get(PROPERTY_ACCESSORY_CATEGORY) instanceof String catProperty
                && AccessoryCategory.from(catProperty) instanceof AccessoryCategory category
                && AccessoryCategory.OTHER != category) {
            newEquipmentTag = accessory.getSemanticEquipmentTag(category);
        }
        if (newEquipmentTag == null) {
            newEquipmentTag = accessory.getSemanticEquipmentTagFromServices();
        }

        String oldEquipmentTag = thing.getSemanticEquipmentTag();
        if (oldEquipmentTag != null && !oldEquipmentTag.isEmpty()) {
            newEquipmentTag = null;
        }

        if (isCamera(accessory)) {
            ChannelBuilder builder = ChannelBuilder
                    .create(new ChannelUID(thing.getUID(), CHANNEL_SNAPSHOT), CoreItemFactory.IMAGE)
                    .withType(CHANNEL_TYPE_SNAPSHOT);
            Channel channel = builder.build();
            uniqueChannelsMap.put(CHANNEL_SNAPSHOT, channel);
            logChannelInformation(channel);
            snapshotTriggersFinalize(accessory);
            findCameraCharacteristics(accessory);
            if (isCameraStreamAvailable()) {
                Channel cameraUrlChannel = ChannelBuilder
                        .create(new ChannelUID(thing.getUID(), CHANNEL_CAMERA_URL), CoreItemFactory.STRING)
                        .withType(CHANNEL_TYPE_CAMERA_URL).build();
                uniqueChannelsMap.put(CHANNEL_CAMERA_URL, cameraUrlChannel);
                logChannelInformation(cameraUrlChannel);
            }
        }

        String oldLabel = thing.getLabel();
        String newLabel = oldLabel == null || oldLabel.isEmpty() ? accessory.getAccessoryInstanceLabel() : null;
        List<Channel> newChannels = !uniqueChannelsMap.isEmpty() ? uniqueChannelsMap.values().stream().toList() : null;

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
                    eventedCharacteristics.size(), newProperties != null ? newProperties.size() : oldProperties.size(),
                    newLabel != null ? newLabel : oldLabel, newEquipmentTag != null ? newEquipmentTag.getName()
                            : oldEquipmentTag != null ? oldEquipmentTag : "n/a");
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
            if (CHANNEL_SNAPSHOT.equals(channelUID.getId())) {
                scheduleSnapshotRefresh();
            } else if (CHANNEL_CAMERA_URL.equals(channelUID.getId())) {
                publishCameraUrl();
            } else {
                requestManualRefresh();
            }
            return;
        }
        try {
            if (command instanceof StopMoveType stopMoveType && StopMoveType.STOP == stopMoveType) {
                if (stopMoveChannel instanceof Channel stopMoveChannel) {
                    writeChannel(stopMoveChannel, OnOffType.ON);
                } else if (positionCurrentChannel instanceof Channel positionChannel
                        && readChannel(positionChannel) instanceof Command currentPosition) {
                    writeChannel(channel, currentPosition);
                } else {
                    logger.debug("{} not able to execute stop command", thing.getUID());
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
            onCommunicationError(e, I18N_SUFFIX_ERROR_SENDING_COMMAND);
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        // Register before go2rtc is ready. Otherwise a camera whose bridge is still starting is a 404, and the Main UI
        // error page reports that the URL was not found.
        cameraServlet.addProvider(thing.getUID().getId(), this);
        if (isBridgedAccessory) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Bridge unavailable while initializing Thing: {}", thing.getUID());
                return;
            }
            if (bridge.getStatus() == ThingStatus.ONLINE) {
                scheduler.submit(() -> {
                    onConnectedThingAccessoriesLoaded();
                    enableEvents(true);
                    updateStatus(ThingStatus.ONLINE);
                });
            } else {
                updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void dispose() {
        disposing.set(true);
        lightModel = null;
        lightModelLinks.clear();
        lightModelClientHSBTypeChannel = null;
        stopMoveChannel = null;
        stopGo2rtc();
        positionCurrentChannel = null;
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
     * @throws TimeoutException if the operation times out
     * @throws InterruptedException if the operation is interrupted
     * @throws IllegalStateException if the accessory ID or characteristic IID are not initialized
     * @throws java.util.concurrent.ExecutionException if there is an execution error
     * @throws java.io.IOException if there is a communication error
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
        logChannelInformation(channel);
        lightModelClientHSBTypeChannel = uid;
    }

    /**
     * Initializes the stop/move button channel by searching for a characteristic of type POSITION_HOLD,
     * and the current position channel by searching for a characteristic of type POSITION_CURRENT.
     *
     * @param accessory the accessory containing the characteristics
     * @param channels the list of channels to search
     */
    private void stopMoveFinalize(Accessory accessory, Map<String, Channel> channels) {
        stopMoveChannel = null;
        positionCurrentChannel = null;
        for (Channel channel : channels.values()) {
            if (channel.getProperties().get(PROPERTY_IID) instanceof String iid) {
                for (Service service : accessory.services) {
                    for (Characteristic cxx : service.characteristics) {
                        if (iid.equals(String.valueOf(cxx.iid))) {
                            CharacteristicType cxxType = cxx.getCharacteristicType();
                            if (CharacteristicType.POSITION_HOLD == cxxType) {
                                stopMoveChannel = channel;
                            } else if (CharacteristicType.POSITION_CURRENT == cxxType) {
                                positionCurrentChannel = channel;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets up the map of characteristic iids that shall trigger snapshot refreshes. Checks the given
     * accessory for any services containing MOTION_DETECTED or INPUT_EVENT characteristics.
     *
     * @param accessory the accessory containing the characteristics
     */
    private void snapshotTriggersFinalize(Accessory accessory) {
        SNAPSHOT_REFRESH_TRIGGERS.clear();
        for (Service service : accessory.services) {
            for (Characteristic cxx : service.characteristics) {
                if (cxx.type instanceof String type) {
                    CharacteristicType cxxType = Characteristic.getCharacteristicType(type);
                    switch (cxxType) {
                        case MOTION_DETECTED, INPUT_EVENT:
                            SNAPSHOT_REFRESH_TRIGGERS.put(cxx.iid, cxxType);
                        default:
                    }
                }
            }
        }
    }

    /**
     * Finalizes the polled and evented characteristics by identifying which characteristics are linked
     * and adding them to the polledCharacteristics list, and which subset of those are evented and adding
     * them also to the eventedCharacteristics list. In case of the special light model HSB channel then we
     * also add the component HUE, SATURATION, BRIGHTNESS, ON, and color temperature characteristics to
     * the list of polled and evented characteristics. The camera snapshot channel is not included.
     *
     * @param accessory the accessory containing the characteristics
     * @param channels the list of channels to check for polled and evented characteristics
     */
    private void eventingPollingFinalize(Accessory accessory, Map<String, Channel> channels) {
        eventedCharacteristics.clear();
        polledCharacteristics.clear();

        final Long aid = getAccessoryId();
        if (aid == null) {
            logger.debug("{} accessory has no ID", thing.getUID());
            return;
        }
        final List<Service> services = accessory.services;
        if (services == null) {
            logger.debug("{} accessory has no services", thing.getUID());
            return;
        }

        for (Channel channel : channels.values()) {
            final ChannelUID channelUID = channel.getUID();
            if (CHANNEL_SNAPSHOT.equals(channelUID.getId())) {
                continue; // skip camera snapshot channel
            }
            if (!isLinked(channelUID)) {
                continue; // skip non-linked channels
            }
            Long channelIID = null;
            boolean checkChannelLinkByIID = !channelUID.equals(lightModelClientHSBTypeChannel);
            if (checkChannelLinkByIID) {
                if (channel.getProperties().get(PROPERTY_IID) instanceof String iidProperty) {
                    try {
                        channelIID = Long.parseLong(iidProperty);
                    } catch (NumberFormatException e) {
                        continue; // error will already have been logged elsewhere
                    }
                } else {
                    continue; // error will already have been logged elsewhere
                }
            }

            boolean iidMatched = false;
            for (Service service : services) {
                final List<Characteristic> characteristics = service.characteristics;
                if (characteristics == null) {
                    continue;
                }
                for (Characteristic characteristic : characteristics) {
                    if (characteristic.iid == null) {
                        continue;
                    }
                    if (checkChannelLinkByIID ? characteristic.iid.equals(channelIID)
                            : requiredByLightModel(characteristic)) {
                        String key = AID_IID_FORMAT.formatted(aid, characteristic.iid);
                        Characteristic entry = new Characteristic();
                        entry.aid = aid;
                        entry.iid = characteristic.iid;
                        polledCharacteristics.put(key, entry);
                        if (characteristic.perms instanceof List<String> perms && perms.contains("ev")) {
                            entry = new Characteristic();
                            entry.aid = aid;
                            entry.iid = characteristic.iid;
                            eventedCharacteristics.put(key, entry);
                        }
                        if (checkChannelLinkByIID) {
                            iidMatched = true;
                            break; // unique IID match found; break inner loop; continue to next channel
                        }
                    }
                }
                if (iidMatched) {
                    break; // unique IID match found; continue to next channel
                }
            }
        }
    }

    /**
     * Helper that returns true if the light model requires the given characteristic type.
     */
    private boolean requiredByLightModel(Characteristic characteristic) {
        return characteristic.type instanceof String characteristicType
                ? LIGHT_MODEL_RELEVANT_TYPES.contains(Characteristic.getCharacteristicType(characteristicType))
                : false;
    }

    /**
     * Reads the state of a specific channel by querying the accessory for the characteristic value.
     *
     * @param channel the channel to read
     * @return the current state of the channel, or null if not found
     * @throws Exception compiler requires us to handle any exception; but actually will be one of the following:
     * @throws TimeoutException if the operation times out
     * @throws InterruptedException if the operation is interrupted
     * @throws IllegalStateException if the read/write service is not initialized
     * @throws java.util.concurrent.ExecutionException if there is an execution error
     * @throws java.io.IOException if there is a communication error
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
     * @throws TimeoutException if the operation times out
     * @throws InterruptedException if the operation is interrupted
     * @throws IllegalStateException if the accessory ID or characteristic IID are not initialized
     * @throws java.util.concurrent.ExecutionException if there is an execution error
     * @throws java.io.IOException if there is a communication error
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
        boolean snapshotChannelExists = thing.getChannel(CHANNEL_SNAPSHOT) != null;
        boolean snapshotChannelRefresh = false;
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
                            // check for snapshot refresh triggers
                            if (snapshotChannelExists && !snapshotChannelRefresh && SNAPSHOT_REFRESH_TRIGGERS
                                    .get(cxx.iid) instanceof CharacteristicType triggerCxxType) {
                                switch (triggerCxxType) {
                                    case MOTION_DETECTED:
                                        snapshotChannelRefresh |= OpenClosedType.OPEN == state;
                                        break;
                                    case INPUT_EVENT:
                                        snapshotChannelRefresh = true;
                                    default:
                                }
                            }
                        }
                    }
                }
            }
        }
        if (snapshotChannelRefresh) {
            scheduleSnapshotRefresh();
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
        createProperties(); // we need the properties before eventually starting a migration
        if (isMigratable() && setupMigration()) {
            return; // skip further processing if migrating
        }
        createChannels();
        markAsReady(thing);
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
            final Channel channel = thing.getChannel(channelUID);
            if (channel == null) {
                return; // OH core ensures this does not happen
            }
            if (CHANNEL_CAMERA_URL.equals(channelUID.getId())) {
                publishCameraUrl();
                return;
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

    @Override
    protected void initializeNotReadyThings() {
        notReadyThings.clear();
        notReadyThings.add(thing); // a self connected accessory requires only itself to be ready
    }

    /**
     * Logs detailed trace information about the given channel if trace logging is enabled.
     */
    private void logChannelInformation(Channel channel) {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "{}     Channel acceptedItemType:{}, defaultTags:{}, description:{}, kind:{}, label:{}, properties:{}, uid:{}",
                    thing.getUID(), channel.getAcceptedItemType(), channel.getDefaultTags(), channel.getDescription(),
                    channel.getKind(), channel.getLabel(), channel.getProperties(), channel.getUID());
        }
    }

    /**
     * Returns true if the given accessory has camera like functions;
     * <ul>
     * <li>its category as declared in its JSON is a camera category</li>
     * <li>its category as discovered by mDNS is a camera category</li>
     * <li>one of its JSON services supports camera RTP stream management</li>
     * <li>one of its JSON characteristics supports video stream configuration</li>
     * </ul>
     * 
     * @param accessory the accessory to check
     * @return true if the accessory is a camera, false otherwise
     */
    private boolean isCamera(Accessory accessory) {
        if (CAMERA_ACCESSORY_CATEGORIES.contains(accessory.getAccessoryType())) {
            return true;
        }
        if (thing.getProperties().get(PROPERTY_ACCESSORY_CATEGORY) instanceof String catProperty
                && AccessoryCategory.from(catProperty) instanceof AccessoryCategory category
                && CAMERA_ACCESSORY_CATEGORIES.contains(category)) {
            return true;
        }
        if (accessory.services instanceof List<Service> services) {
            for (Service service : services) {
                if (ServiceType.CAMERA_RTP_STREAM_MANAGEMENT == service.getServiceType()) {
                    return true;
                }
                if (service.characteristics instanceof List<Characteristic> characteristics) {
                    for (Characteristic characteristic : characteristics) {
                        if (CharacteristicType.SUPPORTED_VIDEO_STREAM_CONFIGURATION == characteristic
                                .getCharacteristicType()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Introduce this getter so we can override it in the JUnit test class.
     */
    protected ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * Determines if the existing Thing is eligible for migration to a Bridge. This is the case if the Thing is of
     * type Accessory, has embedded accessories (i.e. child accessories), and is a managed Thing. If the Thing is not
     * eligible for migration, logs an appropriate message to inform the user about the reason and how to fix it.
     *
     * @return true if the Thing is eligible for migration, false otherwise
     */
    private boolean isMigratable() {
        if (THING_TYPE_ACCESSORY.equals(thing.getThingTypeUID()) && getAccessories().size() > 1) {
            if (managedThingProvider.get(thing.getUID()) == null) {
                logger.info("{} has embedded accessories; try editing Thing-Type from 'accessory' to 'bridge'.",
                        thing.getUID());
                return false;
            }
            if (thing.getConfiguration().getProperties().get(CONFIG_UNIQUE_ID) == null) {
                logger.warn("{} is missing unique ID; cannot auto-migrate it", thing.getUID());
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Sets up migration of an accessory Thing to a Bridge. Purpose is to remove the current Thing from the registry
     * and create a new Bridge that inherits the current Thing's network attributes, and create a new bridged accessory
     * Thing as a child of the Bridge that will host the current Thing's channels. The channels and tags are not
     * directly inherited because the new Thing handler recreates them dynamically (albeit with different ChannelUIDs).
     * Critically the Bridge inherits the same IP network configuration and HomeKit UniqueID so the new Bridge continues
     * to use the old Thing's stored pairing key. The migration does not execute if either of the 'disposing' or
     * 'migrating' flags are set. And this method sets the 'migrating' flag to prevent overlapping calls. The migration
     * is performed asynchronously on a separate thread after a short delay.
     * 
     * @return true if the migration was successfully scheduled, false otherwise
     */
    private boolean setupMigration() {
        // create a copy of the old Thing that is to be migrated
        Thing sourceThing = ThingBuilder.create(thing).build();

        if (!(sourceThing.getConfiguration().getProperties().get(CONFIG_UNIQUE_ID) instanceof String sourceUniqueId)) {
            logger.warn("{} is missing unique ID; cannot auto-migrate it", thing.getUID());
            return false;
        }

        // create new Bridge with old Thing's Id and all its attributes, except channels and tag
        Bridge targetBridge = BridgeBuilder.create(THING_TYPE_BRIDGE, sourceThing.getUID().getId()) //
                .withConfiguration(sourceThing.getConfiguration()) //
                .withLabel(sourceThing.getLabel()) //
                .withLocation(sourceThing.getLocation()) //
                .withProperties(sourceThing.getProperties()) //
                .withSemanticEquipmentTag(Equipment.NETWORK_APPLIANCE) //
                .build();

        if (managedThingProvider.get(targetBridge.getUID()) != null) {
            logger.warn("{} already exists; cannot auto-migrate {}", targetBridge.getUID(), thing.getUID());
            return false;
        }

        // apply converted-from-accessory property (actual value is irrelevant, only absence or presence counts)
        targetBridge.setProperty(PROPERTY_CONVERTED_FROM_ACCESSORY, CHECK_MARK);

        // create new bridged accessory #1 child Thing that will host the old Thing's channels
        Configuration targetConfiguration = new Configuration();
        targetConfiguration.put(CONFIG_ACCESSORY_ID, BigDecimal.valueOf(1));

        String targetLabel = sourceThing.getLabel();
        Matcher matcher = THING_LABEL_PATTERN.matcher(targetLabel);
        if (matcher.matches()) {
            String baseLabel = matcher.group(1);
            String suffix = matcher.group(2);
            if (baseLabel != null && !baseLabel.isBlank() && suffix != null && !suffix.isBlank()) {
                targetLabel = baseLabel.trim() + " (" + suffix + "-1)";
            } else {
                targetLabel = targetLabel + " (1)";
            }
        } else {
            targetLabel = targetLabel + " (1)";
        }

        String targetUniqueId = STRING_AID_FMT.formatted(sourceUniqueId, 1);
        ThingUID targetUID = new ThingUID(THING_TYPE_BRIDGED_ACCESSORY, targetBridge.getUID(), "1");
        Thing targetThing = ThingBuilder.create(THING_TYPE_BRIDGED_ACCESSORY, targetUID) //
                .withBridge(targetBridge.getUID()) //
                .withConfiguration(targetConfiguration) //
                .withLabel(targetLabel) //
                .withLocation(sourceThing.getLocation()) //
                .withProperty(PROPERTY_UNIQUE_ID, targetUniqueId) //
                .withSemanticEquipmentTag(sourceThing.getSemanticEquipmentTag()) //
                .build();

        if (managedThingProvider.get(targetThing.getUID()) != null) {
            logger.warn("{} already exists; cannot auto-migrate {}", targetThing.getUID(), thing.getUID());
            return false;
        }

        if (!disposing.get() && !migrating.getAndSet(true)) {
            try {
                getScheduler().schedule(() -> doMigration(sourceUniqueId, sourceThing, targetBridge, targetThing),
                        MIGRATION_DELAY_SECONDS, TimeUnit.SECONDS);
                logger.info("{} has embedded accessories; auto-migrating it", thing.getUID());
                updateStatus(ThingStatus.UNKNOWN, "@text/status.migrating-accessory-to-bridge");
                return true;
            } catch (Exception e) {
                migrating.set(false);
                logger.warn("{} auto-migrating {}", e.getMessage(), thing.getUID(), e);
            }
        }
        return false;
    }

    /**
     * Task to remove the given existing accessory Thing from the registry and add the new Bridge.
     * <p>
     * NOTE: There is currently no dedicated framework API to migrate an existing Thing to a Bridge
     * while preserving its identifier and configuration. In this specific migration scenario we are
     * therefore forced perform an explicit remove+add ourself on the ThingRegistry:
     *
     * <ul>
     * <li>The old accessory Thing is first removed using its current UID.</li>
     * <li>A new Bridge with the same id, configuration, properties, label, location and tag is then added.</li>
     * <li>Suppress mDNS discovery for the old Thing id (being identically the new Bridge id) so as to prevent
     * re-discovery of the original accessory Thing.</li>
     * </ul>
     *
     * This operation is executed asynchronously and only when the handler has determined that the
     * existing accessory Thing must be upgraded to a Bridge (for accessories with child accessories). The
     * caller has set the handler status to OFFLINE before the conversion starts to reflect that the
     * conversion is in progress. Because the original attributes are deliberately copied to the new Bridge,
     * and because the change is a one-time structural migration, this direct registry manipulation is
     * considered safe in this context.
     * <p>
     * Clears the 'migrating' flag if the migration fails so thing can potentially continue to be used.
     * 
     * @param uniqueId the unique ID of both the existing Thing and the new Bridge
     * @param oldThing the existing Thing to be removed
     * @param newBridge the new Bridge instance to be added
     * @param newThing the new child Thing that will host the existing Thing's channels
     */
    private void doMigration(String uniqueId, Thing oldThing, Bridge newBridge, Thing newThing) {
        if (disposing.get() || !migrating.get()) {
            return;
        }
        boolean bridgeAdded = false;
        boolean thingAdded = false;
        try {
            managedThingProvider.add(newBridge);
            bridgeAdded = true;
            managedThingProvider.add(newThing);
            thingAdded = true;
            managedThingProvider.remove(oldThing.getUID()); // remove existing Thing only after successful adds
        } catch (Exception e) {
            if (thingAdded) {
                try {
                    managedThingProvider.remove(newThing.getUID());
                } catch (Exception e1) {
                    logger.warn("{} while rolling back adding of {}", e1.getMessage(), newThing.getUID());
                }
            }
            if (bridgeAdded) {
                try {
                    managedThingProvider.remove(newBridge.getUID());
                } catch (Exception e1) {
                    logger.warn("{} while rolling back adding of {}", e1.getMessage(), newBridge.getUID());
                }
            }
            logger.warn("{} while auto-migrating {} to {}; try editing Thing-Type from 'accessory' to 'bridge'.",
                    e.getMessage(), oldThing.getUID(), newBridge.getUID());
            migrating.set(false);
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/status.migrating-accessory-to-bridge-failed");
            return;
        }
        String mac = thing.getProperties().get(Thing.PROPERTY_MAC_ADDRESS);
        discoveryParticipant.setTypeMapping(true, uniqueId, mac); // enable mDNS thing type mapping
        logger.info("Successfully auto-migrated {} to {} with {}", oldThing.getUID(), newBridge.getUID(),
                newThing.getUID());
    }

    @Override
    protected String unpairInner() {
        // prevent un-pairing (which would erase the pairing keys) while migration is in progress
        return migrating.get() ? ACTION_RESULT_ERROR_FORMAT.formatted("auto-migration in progress")
                : super.unpairInner();
    }

    /*
     * ========================= Camera video, audio, and talk-back support =================================
     */

    /**
     * Negotiates the camera's audio and video SRTP with the go2rtc data pump and starts the camera streams.
     * The actual session is opened on demand by {@link #checkGo2rtcDemand} and closed again when go2rtc has
     * no consumers.
     *
     * @throws Exception if the camera does not support talk-back or the exchange fails
     */
    private void openCameraSession(Go2RtcDataPump pump, String remoteAddress) throws Exception {
        logger.debug("{} openCameraSession() opening session", thing.getUID());
        Long aid;
        Long setupIid;
        Long selectedIid;
        synchronized (this) {
            aid = getAccessoryId();
            setupIid = cameraSetupIid;
            selectedIid = cameraSelectedIid;
        }
        if (aid == null || setupIid == null || selectedIid == null) {
            throw new IllegalStateException("Camera talk-back characteristics not available");
        }

        byte[] sessionId = new byte[16];
        RANDOM.nextBytes(sessionId);
        SrtpParameters talkParams = SrtpParameters.random();
        // the controller's video key: video talk-back is not supported, but the key encrypts video Receiver Reports
        SrtpParameters videoControlParams = SrtpParameters.random();

        SetupEndpointsRequest endpointRequest = new SetupEndpointsRequest(sessionId, false, localHost(remoteAddress),
                pump.videoPort(), pump.audioPort(), videoControlParams, talkParams);
        logger.debug("{} openCameraSession() {}", thing.getUID(), endpointRequest);

        writeTlvCharacteristic(aid, setupIid, endpointRequest.encode());
        SetupEndpointsResponse endpointResponse = SetupEndpointsResponse.decode(readTlvCharacteristic(aid, setupIid));
        logger.debug("{} openCameraSession() {}", thing.getUID(), endpointResponse);

        if (endpointResponse.status() != SetupEndpointsStatus.SUCCESS) {
            logger.debug("{} openCameraSession() camera rejected talk-back setup: {}", thing.getUID(),
                    endpointResponse.status());
            throw new IllegalStateException("Camera rejected talk-back setup: " + endpointResponse.status());
        }

        SrtpParameters audioParams = endpointResponse.audioSrtpParameters();
        SrtpParameters videoParams = endpointResponse.videoSrtpParameters();
        Integer talkPort = endpointResponse.talkPort();
        logger.debug("{} openCameraSession() audioParams ={}, videoParams={}, talk port={}", thing.getUID(),
                audioParams, videoParams, talkPort);
        if (audioParams == null || videoParams == null || talkPort == null) {
            logger.debug("{} openCameraSession() camera did not return SRTP parameters or a talk port", thing.getUID());
            throw new IllegalStateException("Camera did not return SRTP parameters or a talk port");
        }

        synchronized (this) {
            cameraSessionId = sessionId;
            cameraSessionOpen = true;
        }

        Go2RtcDataPump.Session session = pump.startSession(audioParams, videoParams, talkParams, videoControlParams,
                remoteAddress.split(":")[0], talkPort);
        logger.debug("{} openCameraSession() {}", thing.getUID(), session);

        SrtpStreamConfiguration config = new SrtpStreamConfiguration(sessionId, SessionCommand.START,
                selectedVideoParams(pump.videoCodec()), selectedAudioParams(pump.audioCodec()));
        logger.debug("{} openCameraSession() {}", thing.getUID(), config);

        writeTlvCharacteristic(aid, selectedIid, config.encode());
    }

    /**
     * Ends the camera media session, and stops the data pump from relaying packets.
     *
     * @param notifyCamera whether to tell the camera to end the session; false avoids blocking I/O during disposal
     */
    private void closeCameraSession(Go2RtcDataPump pump, boolean notifyCamera) {
        byte[] id;
        Long aid;
        Long selectedIid;
        synchronized (this) {
            if (!cameraSessionOpen) {
                return;
            }
            cameraSessionOpen = false;
            pump.stopSession();
            id = cameraSessionId;
            aid = getAccessoryId();
            selectedIid = cameraSelectedIid;
        }

        if (notifyCamera && aid != null && selectedIid != null) {
            try {
                writeTlvCharacteristic(aid, selectedIid,
                        new SrtpStreamConfiguration(id, SessionCommand.END, selectedAudioParams(pump.audioCodec()))
                                .encode());
            } catch (Exception e) {
                logger.debug("{} failed to end talk-back session: {}", thing.getUID(), e.getMessage());
            }
        }
        logger.debug("{} closeCameraSession() session closed", thing.getUID());
    }

    /**
     * Starts the go2rtc browser bridge once, if the camera declares a usable audio codec. The start runs on the
     * go2rtc executor because the go2rtc binary may need to be downloaded first. A failed start clears the flag,
     * so the next call retries it.
     */
    private synchronized void startGo2rtcIfAvailable() {
        if (!isCameraStreamAvailable() || go2rtcStarting
                || !(getConfig().get(CONFIG_IP_ADDRESS) instanceof String remoteIpAddress)) {
            return;
        }
        List<AudioCodec> audioCodecs = this.audioCodecs;
        List<VideoCodec> videoCodecs = this.videoCodecs;
        go2rtcStarting = true;
        logger.debug("{} startGo2rtcIfAvailable() audioCodecs={}, videoCodecs={}, remoteIpAddress={}", thing.getUID(),
                audioCodecs, videoCodecs, remoteIpAddress);
        go2rtcExecutor.execute(() -> startGo2rtc(audioCodecs, videoCodecs, remoteIpAddress));
    }

    /**
     * Returns the path to the cache directory for this binding, creating it if necessary.
     */
    private static Path cacheDir() {
        return Paths.get(OpenHAB.getUserDataFolder(), "cache", BINDING_ID);
    }

    /**
     * Starts the go2rtc browser bridge, which is a separate process that relays audio and video streams to the browser.
     * The go2rtc process is started with a YAML configuration that specifies the ports, codecs, and commands to run.
     * If the process fails to start or does not become ready, it is stopped and cleaned up.
     *
     * @param audioCodecs the list of audio codecs supported by the camera
     * @param videoCodecs the list of video codecs supported by the camera
     * @param remoteIpAddress the IP address of the remote client (browser)
     */
    private void startGo2rtc(List<AudioCodec> audioCodecs, List<VideoCodec> videoCodecs, String remoteIpAddress) {
        logger.debug("{} startGo2rtc() called", thing.getUID());

        Go2RtcDataPump pump = null;
        Go2RtcProcess process = null;
        boolean succeeded = false;

        try {
            Path go2rtcBinary = new Go2RtcInstaller(cacheDir()).ensureBinary();
            String host = localHost(remoteIpAddress);
            Go2RtcConfig.Address address = new Go2RtcConfig.Address(host, Go2RtcConfig.freeTcpPort(),
                    Go2RtcConfig.freeTcpUdpPort(), Go2RtcConfig.freeTcpPort());
            logger.debug("{} startGo2rtc() address={}", thing.getUID(), address);

            String ffmpegBinary = AudioCodec.preferred(audioCodecs).codecType() == AudioCodecType.AAC_ELD
                    ? FfmpegInstaller.ensureBinary(cacheDir())
                    : null;
            pump = new Go2RtcDataPump(audioCodecs, videoCodecs, ffmpegBinary);
            logger.debug("{} startGo2rtc() pump={}", thing.getUID(), pump);
            synchronized (this) {
                if (disposing.get()) {
                    return;
                }
                go2rtcPump = pump;
            }

            String listenStreamId = "homekit_" + thing.getUID().getId().replaceAll("[^A-Za-z0-9_]", "_");
            String talkStreamId = listenStreamId + "_talk";
            String yaml = Go2RtcConfig.toYaml(address, listenStreamId, talkStreamId);
            logger.debug("{} startGo2rtc() yaml=\n{}", thing.getUID(), yaml);

            process = new Go2RtcProcess(go2rtcBinary);
            process.start(yaml);
            if (!awaitGo2rtcReady(process, Go2RtcConfig.API_HOST, address.apiPort())) {
                logger.debug("{} startGo2rtc() go2rtc did not become ready", thing.getUID());
                throw new IOException("go2rtc did not become ready");
            }
            pump.connect(address.rtspPort(), listenStreamId, talkStreamId);

            synchronized (this) {
                if (disposing.get()) {
                    return;
                }
                go2rtcProcess = process;
                go2rtcEndpoint = new Go2RtcEndpoint(address.apiPort(), listenStreamId, remoteIpAddress);
                go2rtcDemandPoller = scheduler.scheduleWithFixedDelay(this::requestGo2rtcDemandCheck,
                        GO2RTC_DEMAND_POLL_SECONDS, GO2RTC_DEMAND_POLL_SECONDS, TimeUnit.SECONDS);
                go2rtcKeepalive = scheduler.scheduleWithFixedDelay(this::sendGo2rtcKeepalive, GO2RTC_KEEPALIVE_SECONDS,
                        GO2RTC_KEEPALIVE_SECONDS, TimeUnit.SECONDS);
                cameraServlet.addProvider(thing.getUID().getId(), this);
                logger.debug("{} startGo2rtc() endPoint={}", thing.getUID(), go2rtcEndpoint);
            }

            publishCameraUrl();
            logger.debug("{} startGo2rtc() succeeded", thing.getUID());
            succeeded = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | IllegalStateException | IllegalArgumentException e) {
            logger.debug("{} startGo2rtc() setup failed: {}", thing.getUID(), e.getMessage());
        } catch (Exception e) {
            logger.warn("{} startGo2rtc() setup failed unexpectedly", thing.getUID(), e);
        } finally {
            if (!succeeded) {
                synchronized (this) {
                    go2rtcStarting = false;
                }
                if (process != null) {
                    process.stop();
                }
                if (pump != null) {
                    synchronized (this) {
                        if (Objects.equals(go2rtcPump, pump)) {
                            go2rtcPump = null;
                        }
                    }
                    closeCameraSession(pump, true);
                    pump.close();
                }
            }
        }
    }

    /**
     * Publishes the browser URL of this camera. openHAB keeps channel state on the linked item, so a publish before the
     * item is linked is discarded. The URL does not depend on go2rtc already being ready.
     */
    private void publishCameraUrl() {
        String viewerUrl = cameraServlet.viewerUrl(thing.getUID().getId());
        if (viewerUrl == null) {
            logger.warn("{} openHAB network address or HTTP port unavailable, camera URL not published",
                    thing.getUID());
            return;
        }
        updateState(new ChannelUID(thing.getUID(), CHANNEL_CAMERA_URL), new StringType(viewerUrl));
        logger.debug("{} camera URL {}", thing.getUID(), viewerUrl);
    }

    /**
     * Stops the go2rtc process and data pump, and cancels the demand poller.
     */
    private void stopGo2rtc() {
        logger.debug("{} stopGo2rtc() succeeded", thing.getUID());
        Go2RtcProcess process;
        Go2RtcDataPump pump;
        ScheduledFuture<?> poller;
        ScheduledFuture<?> keepalive;
        synchronized (this) {
            cameraServlet.removeProvider(thing.getUID().getId(), this);
            process = go2rtcProcess;
            pump = go2rtcPump;
            poller = go2rtcDemandPoller;
            keepalive = go2rtcKeepalive;
            go2rtcProcess = null;
            go2rtcPump = null;
            go2rtcEndpoint = null;
            go2rtcDemandPoller = null;
            go2rtcKeepalive = null;
            go2rtcStarting = false;
            go2rtcDemand.reset();
        }
        if (poller != null) {
            poller.cancel(false);
        }
        if (keepalive != null) {
            keepalive.cancel(false);
        }
        if (process != null) {
            go2rtcExecutor.execute(process::stop);
        }
        if (pump != null) {
            closeCameraSession(pump, false);
            pump.close();
        }
    }

    /**
     * Called periodically by the scheduler. Keeps the go2rtc publisher connection alive between camera sessions.
     */
    private void sendGo2rtcKeepalive() {
        Go2RtcDataPump pump;
        synchronized (this) {
            pump = go2rtcPump;
        }
        if (disposing.get() || pump == null) {
            return;
        }
        try {
            pump.keepAlive();
        } catch (IOException e) {
            logger.debug("{} go2rtc publisher keepalive failed: {}", thing.getUID(), e.getMessage());
        }
    }

    /**
     * Called periodically by the scheduler. Checks if the go2rtc process has any consumers for the
     * camera stream, and opens or closes the camera session accordingly.
     */
    private void requestGo2rtcDemandCheck() {
        if (disposing.get() || go2rtcDemandChecking.getAndSet(true)) {
            return;
        }
        try {
            go2rtcExecutor.execute(this::checkGo2rtcDemand);
        } catch (RejectedExecutionException e) {
            go2rtcDemandChecking.set(false);
        }
    }

    /**
     * Checks if the go2rtc process has any consumers for the camera stream, and opens or closes the camera
     * session accordingly.
     * If the go2rtc process is not running or the endpoint is not available, this method does nothing.
     * If the go2rtc process cannot be queried for consumers, this method logs a debug message and does nothing.
     * If the camera session needs to be opened or closed, this method calls {@link #openCameraSession} or
     * {@link #closeCameraSession}.
     */
    private void checkGo2rtcDemand() {
        try {
            Go2RtcProcess process;
            Go2RtcDataPump pump;
            Go2RtcEndpoint endpoint;
            synchronized (this) {
                process = go2rtcProcess;
                pump = go2rtcPump;
                endpoint = go2rtcEndpoint;
            }
            if (process == null || pump == null || endpoint == null) {
                return;
            }

            boolean wanted;
            try {
                wanted = process.hasConsumers(Go2RtcConfig.API_HOST, endpoint.apiPort(), endpoint.stream(),
                        endpoint.stream() + "_talk");
            } catch (IOException e) {
                // an unreadable go2rtc state is unknown demand, so the current camera session is left unchanged.
                logger.debug("{} cannot read go2rtc consumers: {}", thing.getUID(), e.getMessage());
                return;
            }

            Go2RtcDemand.Action action;
            synchronized (this) {
                action = go2rtcDemand.update(wanted, cameraSessionOpen, Instant.now());
            }

            if (action == Go2RtcDemand.Action.OPEN) {
                try {
                    openCameraSession(pump, endpoint.remoteAddress());
                } catch (Exception e) {
                    synchronized (this) {
                        go2rtcDemand.openFailed(Instant.now());
                    }
                    logger.info("{} camera media session unavailable, retrying later: {}", thing.getUID(),
                            e.getMessage());
                    return;
                }
                boolean stale;
                synchronized (this) {
                    stale = disposing.get() || !Objects.equals(go2rtcPump, pump);
                }
                if (stale) {
                    closeCameraSession(pump, true);
                }
            } else if (action == Go2RtcDemand.Action.CLOSE) {
                closeCameraSession(pump, true);
            }

            boolean open;
            synchronized (this) {
                open = cameraSessionOpen;
            }
            if (open) {
                updateTalkDemand(process, pump, endpoint);
            }
        } finally {
            go2rtcDemandChecking.set(false);
        }
    }

    @Override
    public boolean openBrowserStream() throws InterruptedException {
        Instant deadline = Instant.now().plusSeconds(BROWSER_OPEN_TIMEOUT_SECONDS);
        while (!disposing.get() && Instant.now().isBefore(deadline)) {
            startGo2rtcIfAvailable();
            Go2RtcProcess process;
            Go2RtcEndpoint endpoint;
            synchronized (this) {
                process = go2rtcProcess;
                endpoint = go2rtcEndpoint;
                if (process != null && endpoint != null) {
                    go2rtcDemand.hold(Instant.now().plusSeconds(BROWSER_DEMAND_HOLD_SECONDS));
                }
            }
            if (process != null && endpoint != null) {
                requestGo2rtcDemandCheck();
                try {
                    if (process.hasProducers(Go2RtcConfig.API_HOST, endpoint.apiPort(), endpoint.stream())) {
                        return true;
                    }
                } catch (IOException e) {
                    logger.debug("{} cannot read go2rtc producers: {}", thing.getUID(), e.getMessage());
                }
            }
            Thread.sleep(BROWSER_OPEN_POLL_MILLIS);
        }
        return false;
    }

    @Override
    public @Nullable String answerBrowserOffer(String offer) throws IOException {
        return answerWebRtc(offer, false);
    }

    @Override
    public @Nullable String answerBrowserTalkOffer(String offer) throws IOException {
        return answerWebRtc(offer, true);
    }

    private @Nullable String answerWebRtc(String offer, boolean talk) throws IOException {
        Go2RtcProcess process;
        Go2RtcEndpoint endpoint;
        synchronized (this) {
            process = go2rtcProcess;
            endpoint = go2rtcEndpoint;
        }
        if (process == null || endpoint == null || disposing.get()) {
            return null;
        }
        String stream = talk ? endpoint.stream() + "_talk" : endpoint.stream();
        return process.answerWebRtc(Go2RtcConfig.API_HOST, endpoint.apiPort(), stream, offer);
    }

    /**
     * Starts the browser talk relay while go2rtc has a producer on the talk stream, which a browser publishes to
     * during a talk session, and stops it otherwise.
     */
    private void updateTalkDemand(Go2RtcProcess process, Go2RtcDataPump pump, Go2RtcEndpoint endpoint) {
        try {
            if (process.hasProducers(Go2RtcConfig.API_HOST, endpoint.apiPort(), endpoint.stream() + "_talk")) {
                pump.startTalk();
            } else {
                pump.stopTalk();
            }
        } catch (IOException e) {
            logger.debug("{} cannot read go2rtc talk producers: {}", thing.getUID(), e.getMessage());
        }
    }

    /**
     * Waits for the go2rtc process to become ready, or until the timeout expires.
     *
     * @param process the go2rtc process
     * @param host the host to check
     * @param apiPort the API port to check
     * @return true if the process is ready, false if it timed out or exited
     * @throws InterruptedException if interrupted while waiting
     */
    private static boolean awaitGo2rtcReady(Go2RtcProcess process, String host, int apiPort)
            throws InterruptedException {
        Instant deadline = Instant.now().plusSeconds(GO2RTC_READY_TIMEOUT_SECONDS);
        while (Instant.now().isBefore(deadline) && process.isRunning()) {
            if (process.isReady(host, apiPort)) {
                return true;
            }
            Thread.sleep(200);
        }
        return false;
    }

    /**
     * Returns the local host that routes to the camera IP address. No packets are sent.
     */
    private static String localHost(String remoteIp) throws IOException {
        try (DatagramSocket probe = new DatagramSocket()) {
            probe.connect(InetAddress.getByName(remoteIp.split(":")[0]), 80);
            return probe.getLocalAddress().getHostAddress();
        }
    }

    /**
     * Writes a TLV8 value to a characteristic.
     */
    private void writeTlvCharacteristic(long aid, long iid, byte[] tlv) throws Exception {
        JsonObject entry = new JsonObject();
        entry.addProperty("aid", aid);
        entry.addProperty("iid", iid);
        entry.addProperty("value", Base64.getEncoder().encodeToString(tlv));
        JsonArray characteristics = new JsonArray();
        characteristics.add(entry);
        JsonObject body = new JsonObject();
        body.add("characteristics", characteristics);
        writeCharacteristics(GSON.toJson(body));
    }

    /**
     * Reads the TLV8 value of a characteristic. Used for write-only-readable characteristics such as SetupEndpoints,
     * whose response to a preceding write is only available via a subsequent read (and not via the write response).
     *
     * @return the TLV8 bytes of the characteristic value
     */
    private byte[] readTlvCharacteristic(long aid, long iid) throws Exception {
        String response = readCharacteristics("%s.%s".formatted(aid, iid));
        JsonObject result = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("characteristics").get(0)
                .getAsJsonObject();
        if (!(result.get("value") instanceof JsonPrimitive value)) {
            throw new IllegalStateException("Camera returned no value for characteristic " + iid + ": " + result);
        }
        return Base64.getDecoder().decode(value.getAsString());
    }

    /**
     * Finds the camera characteristics of the given accessory, and stores their IIDs and supported codecs.
     * If all required characteristics are found, starts the go2rtc process if it is not already running.
     *
     * @param accessory the accessory to inspect
     */
    private void findCameraCharacteristics(Accessory accessory) {
        cameraSetupIid = null;
        cameraSelectedIid = null;
        audioCodecs = List.of();
        videoCodecs = List.of();
        final List<Service> services = accessory.services;
        if (services == null) {
            return;
        }
        for (Service service : services) {
            final List<Characteristic> characteristics = service.characteristics;
            if (characteristics == null) {
                continue;
            }
            for (Characteristic characteristic : characteristics) {
                if (characteristic.iid == null) {
                    continue;
                }
                switch (Characteristic.getCharacteristicType(characteristic.type)) {
                    case SETUP_ENDPOINTS -> cameraSetupIid = characteristic.iid;
                    case SELECTED_RTP_STREAM_CONFIGURATION -> cameraSelectedIid = characteristic.iid;
                    case SUPPORTED_AUDIO_CONFIGURATION ->
                        audioCodecs = decodeAudioSupportedCodecs(characteristic.value);
                    case SUPPORTED_VIDEO_STREAM_CONFIGURATION ->
                        videoCodecs = decodeVideoSupportedCodecs(characteristic.value);
                    default -> {
                    }
                }
            }
        }
        if (isCameraStreamAvailable()) {
            startGo2rtcIfAvailable();
        }
    }

    /**
     * Returns true if the camera has the required setup characteristics, H.264 video, and audio that go2rtc can receive
     * either directly (Opus) or after transcoding (AAC-ELD).
     */
    private boolean isCameraStreamAvailable() {
        return cameraSetupIid != null && cameraSelectedIid != null && audioCodecs.stream().anyMatch(
                codec -> codec.codecType() == AudioCodecType.OPUS || codec.codecType() == AudioCodecType.AAC_ELD)
                && videoCodecs.stream().anyMatch(codec -> codec.codecType() == VideoCodecType.H_264);
    }

    /**
     * Returns the selected audio parameters for the given codec, using its advertised bit-rate mode and the fixed SSRC
     * and payload type.
     */
    private static AudioSelectedParameters selectedAudioParams(AudioCodec codec) {
        return new AudioSelectedParameters(codec.codecType(), codec.channels(), codec.constantBitrate(),
                codec.sampleRateKhz(), codec.packetTimeMs(), Go2RtcDataPump.AUDIO_PAYLOAD_TYPE,
                Go2RtcDataPump.AUDIO_SYNC_SOURCE, codec.maxBitrateKbps(), 0.5f, false, 13);
    }

    /**
     * Returns the selected video parameters for the given codec, using the fixed SSRC and payload type.
     */
    private static VideoSelectedParameters selectedVideoParams(VideoCodec codec) {
        return new VideoSelectedParameters(codec.profile(), codec.level(), codec.width(), codec.height(),
                codec.frameRate(), Go2RtcDataPump.VIDEO_PAYLOAD_TYPE, Go2RtcDataPump.VIDEO_SYNC_SOURCE,
                codec.maxBitrateKbps(), 0.5f);
    }

    /**
     * Decodes the supported video codecs from the given JSON value, which is expected to be a base64-encoded TLV8 byte
     * array. If the value is not a valid base64 string or does not decode to a valid TLV8 structure, returns an empty
     * list.
     */
    private List<VideoCodec> decodeVideoSupportedCodecs(@Nullable JsonElement value) {
        if (value instanceof JsonPrimitive primitive) {
            try {
                byte[] tlv = Base64.getDecoder().decode(primitive.getAsString());
                return VideoCodec.decode(tlv);
            } catch (IllegalArgumentException e) {
                logger.debug("{} invalid supported video codec configuration: {}", thing.getUID(), e.getMessage());
            }
        }
        return List.of();
    }

    /**
     * Decodes the supported audio codecs from the given JSON value, which is expected to be a base64-encoded TLV8 byte
     * array. If the value is not a valid base64 string or does not decode to a valid TLV8 structure, returns an empty
     * list.
     */
    private List<AudioCodec> decodeAudioSupportedCodecs(@Nullable JsonElement value) {
        if (value instanceof JsonPrimitive primitive) {
            try {
                byte[] tlv = Base64.getDecoder().decode(primitive.getAsString());
                return AudioCodec.decode(tlv);
            } catch (IllegalArgumentException e) {
                logger.debug("{} invalid supported audio codec configuration: {}", thing.getUID(), e.getMessage());
            }
        }
        return List.of();
    }
}
