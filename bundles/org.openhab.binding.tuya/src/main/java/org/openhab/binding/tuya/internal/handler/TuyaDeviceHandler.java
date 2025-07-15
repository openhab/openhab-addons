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
package org.openhab.binding.tuya.internal.handler;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CHANNEL_TYPE_UID_COLOR;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CHANNEL_TYPE_UID_DIMMER;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CHANNEL_TYPE_UID_IR_CODE;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CHANNEL_TYPE_UID_NUMBER;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CHANNEL_TYPE_UID_QUANTITY;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CHANNEL_TYPE_UID_STRING;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CHANNEL_TYPE_UID_SWITCH;
import static org.openhab.core.library.CoreItemFactory.NUMBER;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.config.ChannelConfiguration;
import org.openhab.binding.tuya.internal.config.DeviceConfiguration;
import org.openhab.binding.tuya.internal.local.DeviceInfoSubscriber;
import org.openhab.binding.tuya.internal.local.DeviceStatusListener;
import org.openhab.binding.tuya.internal.local.TuyaDevice;
import org.openhab.binding.tuya.internal.local.UdpDiscoveryListener;
import org.openhab.binding.tuya.internal.local.dto.DeviceInfo;
import org.openhab.binding.tuya.internal.local.dto.IrCode;
import org.openhab.binding.tuya.internal.util.ConversionUtil;
import org.openhab.binding.tuya.internal.util.IrUtils;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.netty.channel.EventLoopGroup;

/**
 * The {@link TuyaDeviceHandler} handles commands and state updates
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TuyaDeviceHandler extends BaseThingHandler implements DeviceInfoSubscriber, DeviceStatusListener {
    private static final List<String> COLOUR_CHANNEL_CODES = List.of("colour_data");
    private static final List<String> DIMMER_CHANNEL_CODES = List.of("bright_value", "bright_value_1", "bright_value_2",
            "temp_value");

    private final Logger logger = LoggerFactory.getLogger(TuyaDeviceHandler.class);

    private final Gson gson;
    private final UdpDiscoveryListener udpDiscoveryListener;
    private final BaseDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider;
    private final EventLoopGroup eventLoopGroup;
    private DeviceConfiguration configuration = new DeviceConfiguration();
    private @Nullable TuyaDevice tuyaDevice;
    private final Map<String, SchemaDp> schemaDps;
    private boolean oldColorMode = false;

    private @Nullable ScheduledFuture<?> reconnectFuture;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> irLearnJob;
    private boolean disposing = false;

    private final Map<Integer, String> dpToChannelId = new HashMap<>();
    private final Map<Integer, List<String>> dp2ToChannelId = new HashMap<>();
    private final Map<String, ChannelTypeUID> channelIdToChannelTypeUID = new HashMap<>();
    private final Map<String, ChannelConfiguration> channelIdToConfiguration = new HashMap<>();

    private final ExpiringCacheMap<Integer, @Nullable Object> deviceStatusCache = new ExpiringCacheMap<>(
            Duration.ofSeconds(10));
    private final Map<String, State> channelStateCache = new HashMap<>();

    public TuyaDeviceHandler(Thing thing, Map<String, SchemaDp> schemaDps, Gson gson,
            BaseDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider, EventLoopGroup eventLoopGroup,
            UdpDiscoveryListener udpDiscoveryListener) {
        super(thing);
        this.schemaDps = schemaDps;
        this.gson = gson;
        this.udpDiscoveryListener = udpDiscoveryListener;
        this.eventLoopGroup = eventLoopGroup;
        this.dynamicCommandDescriptionProvider = dynamicCommandDescriptionProvider;
    }

    @Override
    public void processDeviceStatus(Map<Integer, Object> deviceStatus) {
        logger.trace("'{}' received status message '{}'", thing.getUID(), deviceStatus);

        if (deviceStatus.isEmpty()) {
            // if status is empty -> need to use control method to request device status
            Map<Integer, @Nullable Object> commandRequest = new HashMap<>();
            dpToChannelId.keySet().forEach(dp -> commandRequest.put(dp, null));
            dp2ToChannelId.keySet().forEach(dp -> commandRequest.put(dp, null));

            TuyaDevice tuyaDevice = this.tuyaDevice;
            if (tuyaDevice != null) {
                tuyaDevice.set(commandRequest);
            }
            return;
        }

        deviceStatus.forEach(this::addSingleExpiringCache);
        deviceStatus.forEach(this::processChannelStatus);
    }

    private void processChannelStatus(Integer dp, Object value) {
        String channelId = dpToChannelId.get(dp);
        if (channelId != null) {
            ChannelConfiguration channelConfiguration = channelIdToConfiguration.get(channelId);
            ChannelTypeUID channelTypeUID = channelIdToChannelTypeUID.get(channelId);

            if (channelConfiguration == null || channelTypeUID == null) {
                logger.warn("Could not find configuration or type for channel '{}' in thing '{}'", channelId,
                        thing.getUID());
                return;
            }

            if (Boolean.FALSE.equals(deviceStatusCache.get(channelConfiguration.dp2))) {
                // skip update if the channel is off!
                return;
            }

            try {
                if (value instanceof String stringValue && CHANNEL_TYPE_UID_COLOR.equals(channelTypeUID)) {
                    oldColorMode = stringValue.length() == 14;
                    updateState(channelId, ConversionUtil.hexColorDecode(stringValue));
                    return;
                } else if (value instanceof String stringValue && CHANNEL_TYPE_UID_STRING.equals(channelTypeUID)) {
                    updateState(channelId, new StringType(stringValue));
                    return;
                } else if (Double.class.isAssignableFrom(value.getClass())
                        && CHANNEL_TYPE_UID_DIMMER.equals(channelTypeUID)) {
                    updateState(channelId,
                            ConversionUtil.brightnessDecode((double) value, 0, channelConfiguration.max));
                    return;
                } else if (Double.class.isAssignableFrom(value.getClass())
                        && CHANNEL_TYPE_UID_NUMBER.equals(channelTypeUID)) {
                    updateState(channelId, new DecimalType((double) value));
                    return;
                } else if (value instanceof String string && CHANNEL_TYPE_UID_NUMBER.equals(channelTypeUID)) {
                    updateState(channelId, new DecimalType(string));
                    return;
                } else if ((Double.class.isAssignableFrom(value.getClass()) || value instanceof String)
                        && CHANNEL_TYPE_UID_QUANTITY.equals(channelTypeUID)) {
                    BigDecimal d;
                    if (value instanceof String stringValue) {
                        d = new BigDecimal(stringValue);
                    } else {
                        d = new BigDecimal((double) value);
                    }

                    SchemaDp schemaDp = schemaDps.get(channelId);

                    if (schemaDp != null) {
                        d = d.movePointLeft(schemaDp.scale);

                        Unit<?> unit = schemaDp.parsedUnit;
                        if (unit != null) {
                            updateState(channelId, new QuantityType<>(d, unit));
                            return;
                        }
                    }

                    updateState(channelId, new DecimalType(d));
                    return;
                } else if (Boolean.class.isAssignableFrom(value.getClass())
                        && CHANNEL_TYPE_UID_SWITCH.equals(channelTypeUID)) {
                    updateState(channelId, OnOffType.from((boolean) value));
                    return;
                } else if (value instanceof String && CHANNEL_TYPE_UID_IR_CODE.equals(channelTypeUID)) {
                    if (channelConfiguration.dp == 2) {
                        String decoded = convertBase64Code(channelConfiguration, (String) value);
                        logger.info("thing {} received ir code: {}", thing.getUID(), decoded);
                        updateState(channelId, new StringType(decoded));
                        irStartLearning(channelConfiguration.activeListen);
                    }
                    return;
                }
            } catch (IllegalArgumentException ignored) {
            }
            logger.warn("Could not update channel '{}' of thing '{}' with value '{}'. Datatype incompatible.",
                    channelId, getThing().getUID(), value);
            updateState(channelId, UnDefType.UNDEF);
        } else {
            // try additional channelDps, only OnOffType
            List<String> channelIds = dp2ToChannelId.get(dp);
            if (channelIds == null) {
                logger.debug("Could not find channel for dp '{}' in thing '{}'", dp, thing.getUID());
            } else {
                if (Boolean.class.isAssignableFrom(value.getClass())) {
                    OnOffType state = OnOffType.from((boolean) value);
                    channelIds.forEach(ch -> updateState(ch, state));
                    return;
                }
                logger.warn("Could not update channel '{}' of thing '{}' with value {}. Datatype incompatible.",
                        channelIds, getThing().getUID(), value);
            }
        }
    }

    @Override
    public void connectionStatus(boolean status) {
        if (status) {
            updateStatus(ThingStatus.ONLINE);
            int pollingInterval = configuration.pollingInterval;
            TuyaDevice tuyaDevice = this.tuyaDevice;
            if (tuyaDevice != null && pollingInterval > 0) {
                pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                    tuyaDevice.refreshStatus(
                            Stream.concat(dpToChannelId.keySet().stream(), dp2ToChannelId.keySet().stream()).toList());
                }, pollingInterval, pollingInterval, TimeUnit.SECONDS);
            }

            // start learning code if thing is online and presents 'ir-code' channel
            channelIdToChannelTypeUID.entrySet().stream().filter(e -> CHANNEL_TYPE_UID_IR_CODE.equals(e.getValue()))
                    .map(Map.Entry::getKey).findAny().map(channelIdToConfiguration::get)
                    .ifPresent(irCodeChannelConfig -> irStartLearning(irCodeChannelConfig.activeListen));
        } else {
            updateStatus(ThingStatus.OFFLINE);
            ScheduledFuture<?> pollingJob = this.pollingJob;
            if (pollingJob != null) {
                pollingJob.cancel(true);
                this.pollingJob = null;
            }
            TuyaDevice tuyaDevice = this.tuyaDevice;
            ScheduledFuture<?> reconnectFuture = this.reconnectFuture;
            // only re-connect if a device is present, we are not disposing the thing and either the reconnectFuture is
            // empty or already done
            if (tuyaDevice != null && !disposing && (reconnectFuture == null || reconnectFuture.isDone())) {
                this.reconnectFuture = scheduler.schedule(this::connectDevice, 5000, TimeUnit.MILLISECONDS);
            }
            if (channelIdToChannelTypeUID.containsValue(CHANNEL_TYPE_UID_IR_CODE)) {
                irStopLearning();
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            State state = channelStateCache.get(channelUID.getId());
            if (state != null) {
                updateState(channelUID, state);
            }
            return;
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.warn("Channel '{}' received a command but device is not ONLINE. Discarding command.", channelUID);
            return;
        }

        Map<Integer, @Nullable Object> commandRequest = new HashMap<>();

        ChannelTypeUID channelTypeUID = channelIdToChannelTypeUID.get(channelUID.getId());
        ChannelConfiguration configuration = channelIdToConfiguration.get(channelUID.getId());
        if (channelTypeUID == null || configuration == null) {
            logger.warn("Could not determine channel type or configuration for channel '{}'. Discarding command.",
                    channelUID);
            return;
        }

        if (CHANNEL_TYPE_UID_COLOR.equals(channelTypeUID)) {
            if (command instanceof HSBType) {
                commandRequest.put(configuration.dp, ConversionUtil.hexColorEncode((HSBType) command, oldColorMode));
                ChannelConfiguration workModeConfig = channelIdToConfiguration.get("work_mode");
                if (workModeConfig != null) {
                    commandRequest.put(workModeConfig.dp, "colour");
                }
                if (configuration.dp2 != 0) {
                    commandRequest.put(configuration.dp2, ((HSBType) command).getBrightness().doubleValue() > 0.0);
                }
            } else if (command instanceof PercentType percentCommand) {
                State oldState = channelStateCache.get(channelUID.getId());
                if (!(oldState instanceof HSBType)) {
                    logger.debug("Discarding command '{}' to channel '{}', cannot determine old state", command,
                            channelUID);
                    return;
                }
                HSBType newState = new HSBType(((HSBType) oldState).getHue(), ((HSBType) oldState).getSaturation(),
                        percentCommand);
                commandRequest.put(configuration.dp, ConversionUtil.hexColorEncode(newState, oldColorMode));
                ChannelConfiguration workModeConfig = channelIdToConfiguration.get("work_mode");
                if (workModeConfig != null) {
                    commandRequest.put(workModeConfig.dp, "colour");
                }
                if (configuration.dp2 != 0) {
                    commandRequest.put(configuration.dp2, (percentCommand).doubleValue() > 0.0);
                }
            } else if (command instanceof OnOffType) {
                if (configuration.dp2 != 0) {
                    commandRequest.put(configuration.dp2, OnOffType.ON.equals(command));
                }
            }
        } else if (CHANNEL_TYPE_UID_DIMMER.equals(channelTypeUID)) {
            if (command instanceof PercentType percentCommand) {
                int value = ConversionUtil.brightnessEncode(percentCommand, 0, configuration.max);
                if (configuration.reversed) {
                    value = configuration.max - value;
                }
                if (value >= configuration.min) {
                    commandRequest.put(configuration.dp, value);
                }
                if (configuration.dp2 != 0) {
                    commandRequest.put(configuration.dp2, value >= configuration.min);
                }
                ChannelConfiguration workModeConfig = channelIdToConfiguration.get("work_mode");
                if (workModeConfig != null) {
                    commandRequest.put(workModeConfig.dp, "white");
                }
            } else if (command instanceof OnOffType) {
                if (configuration.dp2 != 0) {
                    commandRequest.put(configuration.dp2, OnOffType.ON.equals(command));
                }
            }
        } else if (CHANNEL_TYPE_UID_STRING.equals(channelTypeUID)) {
            commandRequest.put(configuration.dp, command.toString());
        } else if (CHANNEL_TYPE_UID_QUANTITY.equals(channelTypeUID) || CHANNEL_TYPE_UID_NUMBER.equals(channelTypeUID)) {
            if (command instanceof QuantityType quantityType) {
                SchemaDp schemaDp = schemaDps.get(channelUID.getId());

                if (schemaDp != null && !schemaDp.unit.isEmpty()) {
                    // If the item type for the channel is not dimensioned the unit is not usable and we
                    // assume whoever sent a quantity instead of a bare number knows what they are doing.
                    Channel channel = thing.getChannel(channelUID.getId());
                    if (channel != null && !NUMBER.equals(channel.getAcceptedItemType())) {
                        quantityType = quantityType.toUnit(schemaDp.unit);
                    }
                }

                if (quantityType != null) {
                    BigDecimal d = quantityType.toBigDecimal();
                    if (schemaDp != null) {
                        d = d.movePointRight(schemaDp.scale);
                    }
                    command = new DecimalType(d);
                }
            }

            if (command instanceof DecimalType decimalType) {
                commandRequest.put(configuration.dp,
                        configuration.sendAsString ? String.format("%d", decimalType.intValue())
                                : decimalType.intValue());
            }
        } else if (CHANNEL_TYPE_UID_SWITCH.equals(channelTypeUID)) {
            if (command instanceof OnOffType) {
                commandRequest.put(configuration.dp, OnOffType.ON.equals(command));
            }
        } else if (CHANNEL_TYPE_UID_IR_CODE.equals(channelTypeUID)) {
            if (command instanceof StringType) {
                switch (configuration.irType) {
                    case "base64" -> {
                        commandRequest.put(1, "study_key");
                        commandRequest.put(7, command.toString());
                    }
                    case "tuya-head" -> {
                        if (!configuration.irCode.isBlank()) {
                            commandRequest.put(1, "send_ir");
                            commandRequest.put(3, configuration.irCode);
                            commandRequest.put(4, command.toString());
                            commandRequest.put(10, configuration.irSendDelay);
                            commandRequest.put(13, configuration.irCodeType);
                        } else {
                            logger.warn("irCode is not set for channel {}", channelUID);
                        }
                    }
                    case "nec" -> {
                        long code = convertHexCode(command.toString());
                        String base64Code = IrUtils.necToBase64(code);
                        commandRequest.put(1, "study_key");
                        commandRequest.put(7, base64Code);
                    }
                    case "samsung" -> {
                        long code = convertHexCode(command.toString());
                        String base64Code = IrUtils.samsungToBase64(code);
                        commandRequest.put(1, "study_key");
                        commandRequest.put(7, base64Code);
                    }
                }
                irStopLearning();
            }
        }

        TuyaDevice tuyaDevice = this.tuyaDevice;
        if (!commandRequest.isEmpty() && tuyaDevice != null) {
            tuyaDevice.set(commandRequest);
        }

        if (CHANNEL_TYPE_UID_IR_CODE.equals(channelTypeUID)) {
            if (command instanceof StringType) {
                irStartLearning(configuration.activeListen);
            }
        }
    }

    @Override
    public void dispose() {
        disposing = true;
        ScheduledFuture<?> future = reconnectFuture;
        if (future != null) {
            future.cancel(true);
        }
        future = this.pollingJob;
        if (future != null) {
            future.cancel(true);
        }
        if (configuration.ip.isEmpty()) {
            // unregister listener only if IP is not fixed
            udpDiscoveryListener.unregisterListener(this);
        }
        TuyaDevice tuyaDevice = this.tuyaDevice;
        if (tuyaDevice != null) {
            tuyaDevice.dispose();
            this.tuyaDevice = null;
        }
        irStopLearning();

        dpToChannelId.clear();
        dp2ToChannelId.clear();
        channelIdToChannelTypeUID.clear();
        channelIdToConfiguration.clear();
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(DeviceConfiguration.class);

        // check if we have channels and add them if available
        if (thing.getChannels().isEmpty()) {
            addChannels();
        }

        thing.getChannels().forEach(this::configureChannel);

        if (!configuration.ip.isBlank()) {
            deviceInfoChanged(new DeviceInfo(configuration.ip, configuration.protocol));
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for IP address");
            udpDiscoveryListener.registerListener(configuration.deviceId, this);
        }

        disposing = false;
    }

    @Override
    public void deviceInfoChanged(DeviceInfo deviceInfo) {
        logger.info("Configuring IP address '{}' for thing '{}'.", deviceInfo, thing.getUID());

        TuyaDevice tuyaDevice = this.tuyaDevice;
        if (tuyaDevice != null) {
            tuyaDevice.dispose();
        }
        updateStatus(ThingStatus.UNKNOWN);

        this.tuyaDevice = new TuyaDevice(gson, this, eventLoopGroup, configuration.deviceId,
                configuration.localKey.getBytes(StandardCharsets.UTF_8), deviceInfo.ip, deviceInfo.protocolVersion);
    }

    private void addChannels() {
        ThingBuilder thingBuilder = editThing();
        ThingUID thingUID = thing.getUID();
        ThingHandlerCallback callback = getCallback();

        if (callback == null) {
            logger.warn("Thing callback not found. Cannot auto-detect thing '{}' channels.", thingUID);
            return;
        }

        Map<String, Channel> channels = new LinkedHashMap<>(schemaDps.entrySet().stream().map(e -> {
            String channelId = e.getKey();
            SchemaDp schemaDp = e.getValue();

            ChannelUID channelUID = new ChannelUID(thingUID, channelId);
            String acceptedItemType = null;
            Map<@Nullable String, @Nullable Object> configuration = new HashMap<>();
            configuration.put("dp", schemaDp.id);

            ChannelTypeUID channeltypeUID;
            if (COLOUR_CHANNEL_CODES.contains(channelId)) {
                channeltypeUID = CHANNEL_TYPE_UID_COLOR;
            } else if (DIMMER_CHANNEL_CODES.contains(channelId)) {
                channeltypeUID = CHANNEL_TYPE_UID_DIMMER;
                configuration.put("min", schemaDp.min);
                configuration.put("max", schemaDp.max);
            } else if ("bool".equals(schemaDp.type)) {
                channeltypeUID = CHANNEL_TYPE_UID_SWITCH;
            } else if ("enum".equals(schemaDp.type)) {
                channeltypeUID = CHANNEL_TYPE_UID_STRING;
                List<String> range = Objects.requireNonNullElse(schemaDp.range, List.of());
                configuration.put("range", String.join(",", range));
            } else if ("string".equals(schemaDp.type)) {
                channeltypeUID = CHANNEL_TYPE_UID_STRING;
            } else if ("value".equals(schemaDp.type)) {
                channeltypeUID = CHANNEL_TYPE_UID_NUMBER;
                configuration.put("min", schemaDp.min);
                configuration.put("max", schemaDp.max);

                if (schemaDp.scale > 0 || !schemaDp.unit.isEmpty()) {
                    channeltypeUID = CHANNEL_TYPE_UID_QUANTITY;

                    if (!schemaDp.unit.isEmpty()) {
                        Unit<?> unit = schemaDp.parsedUnit;
                        if (unit == null) {
                            unit = UnitUtils.parseUnit(schemaDp.unit);
                            schemaDp.parsedUnit = unit;
                        }

                        if (unit != null) {
                            String dimension = UnitUtils.getDimensionName(unit);
                            if (dimension != null) {
                                acceptedItemType = "Number:" + dimension;
                            } else {
                                logger.warn("{} has unit \"{}\" but openHAB doesn't know the dimension", channelId,
                                        schemaDp.unit);
                            }
                        }
                    }
                }
            } else {
                // e.g. type "raw", add empty channel
                return Map.entry("", ChannelBuilder.create(channelUID).build());
            }

            if (schemaDp.label.isEmpty()) {
                schemaDp.label = schemaDp.code;

                String label = StringUtils.capitalizeByWhitespace(schemaDp.code.replaceAll("_", " "));
                if (label != null) {
                    label = label.trim();
                    if (!label.isEmpty()) {
                        schemaDp.label = label;
                    }
                }
            }

            return Map.entry(channelId, callback.createChannelBuilder(channelUID, channeltypeUID) //
                    .withAcceptedItemType(acceptedItemType) //
                    .withLabel(schemaDp.label) //
                    .withConfiguration(new Configuration(configuration)) //
                    .build());
        }).filter(c -> !c.getKey().isEmpty()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        List<String> channelSuffixes = List.of("", "_1", "_2");
        List<String> switchChannels = List.of("switch_led", "led_switch");
        channelSuffixes.forEach(suffix -> switchChannels.forEach(channel -> {
            Channel switchChannel = channels.get(channel + suffix);
            if (switchChannel != null) {
                // remove switch channel if brightness or color is present and add to dp2 instead
                ChannelConfiguration config = switchChannel.getConfiguration().as(ChannelConfiguration.class);
                Channel colourChannel = channels.get("colour_data" + suffix);
                Channel brightChannel = channels.get("bright_value" + suffix);
                boolean remove = false;

                if (colourChannel != null) {
                    colourChannel.getConfiguration().put("dp2", config.dp);
                    remove = true;
                }
                if (brightChannel != null) {
                    brightChannel.getConfiguration().put("dp2", config.dp);
                    remove = true;
                }

                if (remove) {
                    channels.remove(channel + suffix);
                }
            }
        }));

        channels.values().forEach(thingBuilder::withChannel);

        updateThing(thingBuilder.build());
    }

    private void configureChannel(Channel channel) {
        ChannelConfiguration configuration = channel.getConfiguration().as(ChannelConfiguration.class);
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();

        if (channelTypeUID == null) {
            logger.warn("Could not determine ChannelTypeUID for '{}'", channel.getUID());
            return;
        }

        String channelId = channel.getUID().getId();

        if (!configuration.range.isEmpty()) {
            List<CommandOption> commandOptions = toCommandOptionList(
                    Arrays.stream(configuration.range.split(",")).collect(Collectors.toList()));
            dynamicCommandDescriptionProvider.setCommandOptions(channel.getUID(), commandOptions);
        }

        dpToChannelId.put(configuration.dp, channelId);
        channelIdToConfiguration.put(channelId, configuration);
        channelIdToChannelTypeUID.put(channelId, channelTypeUID);

        // check if we have additional DPs (these are switch DP for color or brightness only)
        if (configuration.dp2 != 0) {
            List<String> list = Objects
                    .requireNonNull(dp2ToChannelId.computeIfAbsent(configuration.dp2, ArrayList::new));
            list.add(channelId);
        }
        if (CHANNEL_TYPE_UID_IR_CODE.equals(channelTypeUID)) {
            irStartLearning(configuration.activeListen);
        }
    }

    private void connectDevice() {
        TuyaDevice tuyaDevice = this.tuyaDevice;
        if (tuyaDevice == null) {
            logger.warn("Cannot connect {} because the device is not set.", thing.getUID());
            return;
        }
        // clear the future here because timing issues can prevent the next attempt if we fail again
        reconnectFuture = null;
        tuyaDevice.connect();
    }

    private List<CommandOption> toCommandOptionList(List<String> options) {
        return options.stream()
                .map(c -> new CommandOption(c, StringUtils.capitalizeByWhitespace(c.replaceAll("_", " ")))).toList();
    }

    private void addSingleExpiringCache(Integer key, Object value) {
        ExpiringCache<@Nullable Object> expiringCache = new ExpiringCache<>(Duration.ofSeconds(10), () -> null);
        expiringCache.putValue(value);
        deviceStatusCache.put(key, expiringCache);
    }

    @Override
    protected void updateState(String channelId, State state) {
        channelStateCache.put(channelId, state);
        super.updateState(channelId, state);
    }

    private long convertHexCode(String code) {
        String sCode = code.startsWith("0x") ? code.substring(2) : code;
        return Long.parseLong(sCode, 16);
    }

    private String convertBase64Code(ChannelConfiguration channelConfig, String encoded) {
        String decoded = "";
        try {
            if (channelConfig.irType.equals("nec")) {
                decoded = IrUtils.base64ToNec(encoded);
                IrCode code = Objects.requireNonNull(gson.fromJson(decoded, IrCode.class));
                decoded = "0x" + code.hex;
            } else if (channelConfig.irType.equals("samsung")) {
                decoded = IrUtils.base64ToSamsung(encoded);
                IrCode code = Objects.requireNonNull(gson.fromJson(decoded, IrCode.class));
                decoded = "0x" + code.hex;
            } else {
                if (encoded.length() > 68) {
                    decoded = IrUtils.base64ToNec(encoded);
                    if (decoded.isEmpty()) {
                        decoded = IrUtils.base64ToSamsung(encoded);
                    }
                    IrCode code = Objects.requireNonNull(gson.fromJson(decoded, IrCode.class));
                    decoded = code.type + ": 0x" + code.hex;
                } else {
                    decoded = encoded;
                }
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Incorrect json response: {}", e.getMessage());
            decoded = encoded;
        } catch (RuntimeException e) {
            logger.warn("Unable decode key code'{}', reason: {}", decoded, e.getMessage());
        }
        return decoded;
    }

    private void repeatStudyCode() {
        Map<Integer, @Nullable Object> commandRequest = new HashMap<>();
        commandRequest.put(1, "study");
        TuyaDevice tuyaDevice = this.tuyaDevice;
        if (tuyaDevice != null) {
            tuyaDevice.set(commandRequest);
        }
    }

    private void irStopLearning() {
        logger.debug("[tuya:ir-controller] stop ir learning");
        ScheduledFuture<?> feature = irLearnJob;
        if (feature != null) {
            feature.cancel(true);
            this.irLearnJob = null;
        }
    }

    private void irStartLearning(boolean available) {
        irStopLearning();
        if (available) {
            logger.debug("[tuya:ir-controller] start ir learning");
            irLearnJob = scheduler.scheduleWithFixedDelay(this::repeatStudyCode, 200, 29000, TimeUnit.MILLISECONDS);
        }
    }
}
