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
package org.openhab.binding.zwavejs.internal.handler;

import static org.openhab.binding.zwavejs.internal.BindingConstants.*;
import static org.openhab.binding.zwavejs.internal.CommandClassConstants.EQUIPMENT_MAP;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.Status;
import org.openhab.binding.zwavejs.internal.api.dto.commands.NodeGetValueCommand;
import org.openhab.binding.zwavejs.internal.api.dto.commands.NodeSetValueCommand;
import org.openhab.binding.zwavejs.internal.config.ColorCapability;
import org.openhab.binding.zwavejs.internal.config.ZwaveJSBridgeConfiguration;
import org.openhab.binding.zwavejs.internal.config.ZwaveJSChannelConfiguration;
import org.openhab.binding.zwavejs.internal.config.ZwaveJSNodeConfiguration;
import org.openhab.binding.zwavejs.internal.conversion.ChannelMetadata;
import org.openhab.binding.zwavejs.internal.conversion.ConfigMetadata;
import org.openhab.binding.zwavejs.internal.type.ZwaveJSTypeGenerator;
import org.openhab.binding.zwavejs.internal.type.ZwaveJSTypeGeneratorResult;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.validation.ConfigValidationException;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZwaveJSNodeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSNodeHandler extends BaseThingHandler implements ZwaveNodeListener {

    private final Logger logger = LoggerFactory.getLogger(ZwaveJSNodeHandler.class);
    private final ZwaveJSTypeGenerator typeGenerator;
    private ZwaveJSNodeConfiguration config = new ZwaveJSNodeConfiguration();
    private boolean configurationAsChannels = false;
    protected ScheduledExecutorService executorService = scheduler;

    // Nodes may contain multiple lighting endpoints; this map holds each one's ColorCapability.
    private Map<Integer, ColorCapability> colorCapabilities = new HashMap<>();

    public ZwaveJSNodeHandler(final Thing thing, final ZwaveJSTypeGenerator typeGenerator) {
        super(thing);
        this.typeGenerator = typeGenerator;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        super.handleConfigurationUpdate(configurationParameters);
        logger.debug("Node {}. Configuration update: {}", config.id, configurationParameters.keySet());

        ZwaveJSBridgeHandler handler = getBridgeHandler();
        if (handler == null || !handler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.warn("Node {}. Bridge handler is null or Bridge is offline, cannot process configuration update",
                    config.id);
            return;
        }

        Node node = handler.requestNodeDetails(config.id);
        if (node == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-node-details");
            return;
        }

        ZwaveJSTypeGeneratorResult result;
        try {
            result = typeGenerator.generate(thing.getUID(), node, true);
        } catch (Exception e) {
            logger.warn("Node {}. Error generating type information during configuration update", config.id, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.build-channels-failed");
            return;
        }

        // Process each configuration parameter update
        // TODO are we able to determine the changed parameters? The UI has a hold of 'dirty' parameters
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            String key = configurationParameter.getKey();
            Object newValue = configurationParameter.getValue();

            if (result.channels.containsKey(key)) {
                ZwaveJSChannelConfiguration channelConfig = Objects.requireNonNull(result.channels.get(key))
                        .getConfiguration().as(ZwaveJSChannelConfiguration.class);
                NodeSetValueCommand zwaveCommand = new NodeSetValueCommand(config.id, channelConfig);
                zwaveCommand.value = newValue;
                handler.sendCommand(zwaveCommand);
            } else {
                logger.debug("Node {}. Configuration key '{}' not found in generated channels, skipping.", config.id,
                        key);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Node {}. Processing command {} type {} for channel {}", config.id, command,
                command.getClass().getSimpleName(), channelUID);
        ZwaveJSBridgeHandler handler = getBridgeHandler();
        if (handler == null || !handler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.warn("Node {}. Bridge handler is null or Bridge is offline, cannot process command", config.id);
            return;
        }

        Channel channel = thing.getChannel(channelUID);
        if (channel == null) {
            logger.debug("Channel {} not found", channelUID);
            return;
        }

        ZwaveJSChannelConfiguration channelConfig = channel.getConfiguration().as(ZwaveJSChannelConfiguration.class);
        // Handle RefreshType
        if (command instanceof RefreshType) {
            NodeGetValueCommand zwaveCommand = new NodeGetValueCommand(config.id, channelConfig);
            handler.sendCommand(zwaveCommand);
            return;
        }

        NodeSetValueCommand zwaveCommand = new NodeSetValueCommand(config.id, channelConfig);

        ColorCapability colorCap = colorCapabilities.get(channelConfig.endpoint);
        boolean isColorChannelCmd = colorCap != null && colorCap.colorChannels.contains(channelUID);
        boolean isColorTempChannelCmd = colorCap != null && channelUID.equals(colorCap.colorTempChannel);

        if (command instanceof OnOffType onOffCommand) {
            zwaveCommand.value = handleOnOffTypeCommand(channelConfig, channel, colorCap, isColorChannelCmd,
                    onOffCommand);
        } else if (command instanceof HSBType hsbTypeCommand) {
            zwaveCommand.value = handleHSBTypeCommand(channelConfig, channel, colorCap, isColorChannelCmd,
                    hsbTypeCommand);
        } else if (command instanceof QuantityType<?> quantityCommand) {
            zwaveCommand.value = handleQuantityTypeCommand(channelConfig, quantityCommand);
        } else if (command instanceof PercentType percentTypeCommand) {
            zwaveCommand.value = handlePercentTypeCommand(channelConfig, channel, colorCap, isColorChannelCmd,
                    isColorTempChannelCmd, percentTypeCommand);
        } else if (command instanceof DecimalType decimalCommand) {
            zwaveCommand.value = decimalCommand.doubleValue();
        } else if (command instanceof DateTimeType dateTimeCommand) {
            throw new UnsupportedOperationException(dateTimeCommand.toString() + " is currently not supported");
        } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
            throw new UnsupportedOperationException(increaseDecreaseCommand.toString() + " is currently not supported");
        } else if (command instanceof NextPreviousType nextPreviousCommand) {
            throw new UnsupportedOperationException(nextPreviousCommand.toString() + " is currently not supported");
        } else if (command instanceof OpenClosedType openClosedCommand) {
            zwaveCommand.value = openClosedCommand == (channelConfig.inverted ? OpenClosedType.CLOSED
                    : OpenClosedType.OPEN);
        } else if (command instanceof PlayPauseType playPauseCommand) {
            throw new UnsupportedOperationException(playPauseCommand.toString() + " is currently not supported");
        } else if (command instanceof PointType pointCommand) {
            throw new UnsupportedOperationException(pointCommand.toString() + " is currently not supported");
        } else if (command instanceof RewindFastforwardType rewindFastforwardCommand) {
            throw new UnsupportedOperationException(
                    rewindFastforwardCommand.toString() + " is currently not supported");
        } else if (command instanceof StopMoveType stopMoveCommand) {
            throw new UnsupportedOperationException(stopMoveCommand.toString() + " is currently not supported");
        } else if (command instanceof StringListType stringListCommand) {
            throw new UnsupportedOperationException(stringListCommand.toString() + " is currently not supported");
        } else if (command instanceof UpDownType upDownCommand) {
            throw new UnsupportedOperationException(upDownCommand.toString() + " is currently not supported");
        } else if (command instanceof StringType stringCommand) {
            zwaveCommand.value = stringCommand.toString();
        }
        if (zwaveCommand.value != null) {
            handler.sendCommand(zwaveCommand);
        }
    }

    private @Nullable Object handleOnOffTypeCommand(ZwaveJSChannelConfiguration channelConfig, Channel channel,
            @Nullable ColorCapability colorCap, boolean isColorChannelCommand, OnOffType onOffCommand) {
        // If this is a color channel, delegate to percent type logic (0% or 100%)
        if (isColorChannelCommand) {
            PercentType percent = (OnOffType.OFF == onOffCommand) ? PercentType.ZERO : PercentType.HUNDRED;
            return handlePercentTypeCommand(channelConfig, channel, colorCap, true, false, percent);
        }

        // For dimmer channels, ON is mapped to 255, which means restore to the last brightness.
        if (CoreItemFactory.DIMMER.equals(channel.getAcceptedItemType())) {
            return (OnOffType.ON == onOffCommand) ? 255 : 0;
        }

        // For other types, handle inversion if needed.
        return (onOffCommand == (channelConfig.inverted ? OnOffType.OFF : OnOffType.ON));
    }

    private @Nullable Object handlePercentTypeCommand(ZwaveJSChannelConfiguration channelConfig, Channel channel,
            @Nullable ColorCapability colorCap, boolean isColorChannelCmd, boolean isColorTempChannelCmd,
            PercentType percentTypeCommand) {
        if (isColorChannelCmd && colorCap != null) {
            HSBType hsb = new HSBType(colorCap.cachedColor.getHue(), colorCap.cachedColor.getSaturation(),
                    percentTypeCommand);
            return handleHSBTypeCommand(channelConfig, channel, colorCap, true, hsb);
        }

        if (isColorTempChannelCmd && colorCap != null) {
            int byteValue = Math.max(0, Math.min(255, percentTypeCommand.intValue() * 255 / 100));
            if (colorCap.warmWhiteChannel instanceof ChannelUID warmWhiteChannel) {
                DecimalType warm = new DecimalType(byteValue);
                scheduler.submit(() -> handleCommand(warmWhiteChannel, warm));
            }
            if (colorCap.coldWhiteChannel instanceof ChannelUID coldWhiteChannel) {
                DecimalType cold = new DecimalType(255 - byteValue);
                scheduler.submit(() -> handleCommand(coldWhiteChannel, cold));
            }
            return null;
        }

        // For non-color channels, handle inversion and the dimmer 100% edge case.
        int value = percentTypeCommand.intValue();
        if (channelConfig.inverted) {
            value = 100 - value;
        }

        // For dimmers, 100% is represented as 99 (zero based value).
        if (CoreItemFactory.DIMMER.equals(channel.getAcceptedItemType()) && value == 100) {
            value = 99;
        }
        return value;
    }

    private @Nullable Object handleHSBTypeCommand(ZwaveJSChannelConfiguration channelConfig, Channel channel,
            @Nullable ColorCapability colorCap, boolean isColorChannelCommand, HSBType hsbTypeCommand) {
        if (isColorChannelCommand && colorCap != null) {
            colorCap.cachedColor = hsbTypeCommand;
        }

        HSBType hsbFull = new HSBType(hsbTypeCommand.getHue(), hsbTypeCommand.getSaturation(), PercentType.HUNDRED);
        int[] rgb = ColorUtil.hsbToRgb(hsbFull);

        if (channel.getUID().getId().contains(HEX)) {
            return "%02X%02X%02X".formatted(rgb[0], rgb[1], rgb[2]);
        } else {
            Map<String, Integer> colorMap = new HashMap<>();
            colorMap.put(RED, rgb[0]);
            colorMap.put(GREEN, rgb[1]);
            colorMap.put(BLUE, rgb[2]);

            if (colorCap != null) {
                if (colorCap.coldWhiteChannel != null) {
                    colorMap.put(COLD_WHITE, 0);
                }
                if (colorCap.warmWhiteChannel != null) {
                    colorMap.put(WARM_WHITE, 0);
                }
            }

            if (isColorChannelCommand && colorCap != null
                    && colorCap.dimmerChannel instanceof ChannelUID dimmerChannel) {
                // Schedule brightness command(s) for dimmer channel(s)
                scheduler.submit(() -> handleCommand(dimmerChannel, hsbTypeCommand.getBrightness()));
            }
            return colorMap;
        }
    }

    private @Nullable Object handleQuantityTypeCommand(ZwaveJSChannelConfiguration channelConfig,
            QuantityType<?> quantityCommand) {
        Unit<?> unit = UnitUtils.parseUnit(channelConfig.incomingUnit);
        if (unit == null) {
            logger.warn("Could not parse '{}' as a unit, this is a bug.", channelConfig.incomingUnit);
            return null;
        }

        Double value = Objects.requireNonNull(quantityCommand.toUnit(unit)).doubleValue();
        if (channelConfig.factor != 1.0) {
            value = value / channelConfig.factor;
        }
        return value;
    }

    @Override
    public void initialize() {
        ZwaveJSNodeConfiguration config = this.config = getConfigAs(ZwaveJSNodeConfiguration.class);

        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.id-invalid");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        executorService.execute(() -> {
            internalInitialize();
        });
    }

    private @Nullable ZwaveJSBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.trace("Node {}. Prevented initialisation as bridge is null", config.id);
            return null;
        }
        if (bridge.getHandler() instanceof ZwaveJSBridgeHandler handler) {
            return handler;
        }
        return null;
    }

    private void internalInitialize() {
        ZwaveJSBridgeHandler handler = getBridgeHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        if (!handler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        handler.registerNodeListener(this);
        Node nodeDetails = handler.requestNodeDetails(config.id);
        if (nodeDetails == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-node-details");
            return;
        }
        if (Status.DEAD == nodeDetails.status) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.dead-node");
            return;
        }
        if (!setupThing(nodeDetails)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.build-channels-failed");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        internalInitialize();
    }

    @Override
    public boolean onNodeStateChanged(Event event) {
        logger.trace("Node {}. State changed event", config.id);

        // Handle configuration value updates
        if (!configurationAsChannels && CONFIGURATION_COMMAND_CLASSES.contains(event.args.commandClass)) {
            if (event.args.newValue == null) {
                logger.debug("Node {}. Configuration value not set, because it is null.", config.id);
                return false;
            }
            ConfigMetadata details = new ConfigMetadata(getId(), event);
            Configuration configuration = editConfiguration();
            configuration.put(details.id, event.args.newValue);
            updateConfiguration(configuration);
            return true;
        }

        // Handle channel state updates
        ChannelMetadata metadata = new ChannelMetadata(getId(), event);
        if (metadata.isIgnoredCommandClass(event.args.commandClassName) || !isLinked(metadata.id)) {
            return true;
        }

        logger.trace("Getting the configuration for linked channel {}", metadata.id);
        Channel channel = thing.getChannel(metadata.id);
        if (channel == null) {
            logger.debug("Node {}. Channel {} not found, ignoring event", config.id, metadata.id);
            return false;
        }

        ZwaveJSChannelConfiguration channelConfig = channel.getConfiguration().as(ZwaveJSChannelConfiguration.class);

        State state = metadata.setState(event.args.newValue, Objects.requireNonNull(channel.getAcceptedItemType()),
                channelConfig.incomingUnit, channelConfig.inverted);

        if (state == null) {
            return true;
        }

        // Handle color and color temperature state updates
        ColorCapability colorCap = colorCapabilities.get(channelConfig.endpoint);
        state = handleColorUpdate(colorCap, state, channel, channelConfig);
        state = handleColorTemperatureUpdate(colorCap, state, channel, channelConfig);

        try {
            updateState(metadata.id, state);
        } catch (IllegalArgumentException e) {
            logger.warn("Node {}. Error updating state for channel {} with value {}:{}. {}", event.nodeId, metadata.id,
                    state.getClass().getSimpleName(), state.toFullString(), e.getMessage());
        }

        return true;
    }

    /**
     * If the channel has a matching {@link ColorCapability} that supports color channel then either:
     *
     * <li>If the new {@link State} is an {@link HSBType} and the target channel is in the {@link ColorCapability}'s set
     * of color channels then return a new {@link HSBType} derived from the new HS parts plus the cached B part, or</li>
     *
     * <li>If the new {@link State} is a {@link PercentType} and the channel's accepted item type is 'Dimmer' then
     * update the {@link ColorCapability}'s cached B part, and notify all other channel's whose accepted item type is
     * 'Color' with the new HSB value.</li>
     * <p>
     *
     * @param colorCapability the colorCapability for this endpoint, which may be null
     * @param newState the incoming state from the web socket
     * @param targetChannel the target channel
     * @param channelConfig the target channelconfiguration
     *
     * @return either the incoming state or the newly updated one
     */
    private State handleColorUpdate(@Nullable ColorCapability colorCapability, State newState, Channel targetChannel,
            ZwaveJSChannelConfiguration channelConfig) {
        if (colorCapability == null || colorCapability.colorChannels.isEmpty()) {
            return newState;
        }

        ChannelUID targetUID = targetChannel.getUID();

        if (colorCapability.colorChannels.contains(targetUID) && newState instanceof HSBType color) {
            colorCapability.cachedColor = new HSBType(color.getHue(), color.getSaturation(),
                    colorCapability.cachedColor.getBrightness());
            return colorCapability.cachedColor;
        }

        if (targetUID.equals(colorCapability.dimmerChannel) && newState instanceof Number brightness) {
            colorCapability.cachedColor = new HSBType(colorCapability.cachedColor.getHue(),
                    colorCapability.cachedColor.getSaturation(), new PercentType(brightness.intValue()));
            colorCapability.colorChannels.forEach(c -> updateState(c, colorCapability.cachedColor));
        }

        return newState;
    }

    /**
     * If the channel has a matching {@link ColorCapability} that supports color temperature commands, the target
     * channel UID matches the ColorCapability's warm or cold white UID, and new {@link State} is a {@link Number}
     * then update the color temperature cache with the appropriate new value, and update the color temperature channel
     * respectively.
     *
     * @param colorCapability the colorCapability for this endpoint, which may be null
     * @param newState the incoming state from the web socket
     * @param targetChannel the target channel
     * @param channelConfig the target channelconfiguration
     *
     * @return the incoming state
     */
    private State handleColorTemperatureUpdate(@Nullable ColorCapability colorCapability, State newState,
            Channel targetChannel, ZwaveJSChannelConfiguration channelConfig) {
        if (colorCapability == null || colorCapability.colorTempChannel == null) {
            return newState;
        }

        boolean isWarmTarget = targetChannel.getUID().equals(colorCapability.warmWhiteChannel);
        boolean isColdTarget = targetChannel.getUID().equals(colorCapability.coldWhiteChannel);

        if ((isWarmTarget || isColdTarget) && newState instanceof Number number) {
            if (isWarmTarget) {
                colorCapability.cachedWarmWhite = number;
            }
            if (isColdTarget) {
                colorCapability.cachedColdWhite = number;
            }

            State colorTemp = UnDefType.UNDEF;
            int warm = colorCapability.cachedWarmWhite.intValue();
            int cold = colorCapability.cachedColdWhite.intValue();

            int colorTempPercent = -1;
            if (warm > 0 && cold > 0) {
                colorTempPercent = warm * 100 / (warm + cold);
            } else if (warm >= 0) {
                colorTempPercent = warm * 100 / 255;
            } else if (cold >= 0) {
                colorTempPercent = (255 - cold) * 100 / 255;
            }

            if (colorTempPercent >= 0) {
                colorTemp = new PercentType(Math.max(0, Math.min(100, colorTempPercent)));
                updateState(Objects.requireNonNull(colorCapability.colorTempChannel), colorTemp);
            } else {
                updateState(Objects.requireNonNull(colorCapability.colorTempChannel), UnDefType.UNDEF);
            }
        }

        return newState;
    }

    @Override
    public void onNodeDead(Event event) {
        logger.trace("Node {}. Dead", config.id);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.comm-error.dead-node");
    }

    @Override
    public void onNodeRemoved(Event event) {
        logger.trace("Node {}. Removed", config.id);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.conf-error.node-removed");
    }

    @Override
    public void onNodeAlive(Event event) {
        logger.trace("Node {}. Alive", config.id);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public Integer getId() {
        return this.config.id;
    }

    private boolean setupThing(Node node) {
        logger.debug("Node {}. Building channels and configuration, containing {} values", node.nodeId,
                node.values.size());

        configurationAsChannels = Objects.requireNonNull(getBridge()).getConfiguration()
                .as(ZwaveJSBridgeConfiguration.class).configurationChannels;

        ZwaveJSTypeGeneratorResult result;
        try {
            result = typeGenerator.generate(thing.getUID(), node, configurationAsChannels);
        } catch (Exception e) {
            logger.warn("Node {}. Error generating type information", node.nodeId, e);
            return false;
        }

        ThingBuilder builder = editThing();

        // Update location if needed
        if (!result.location.equals(getThing().getLocation()) && !result.location.isBlank()) {
            builder.withLocation(result.location);
        }

        // Update channels
        builder = updateChannels(builder, result);

        colorCapabilities = result.colorCapabilities;
        if (logger.isDebugEnabled()) {
            colorCapabilities.forEach((e, c) -> logger.debug("Node {}. Endpoint {}, {}", node.nodeId, e, c));
        }

        updateThing(builder.build());

        // Initialize state for channels and configuration
        initializeChannelAndConfigState(node, result);

        return true;
    }

    /**
     * Updates the channels of a ThingBuilder based on the provided ZwaveJSTypeGeneratorResult.
     * Does not touch existing Thing channels and their configuration.
     * 
     * <p>
     * This method performs the following actions:
     * <ul>
     * <li>Removes channels from the ThingBuilder that are no longer part of the result.</li>
     * <li>Adds new channels from the result that are not already present in the ThingBuilder.</li>
     * <li>Sets a semantic equipment tag for the ThingBuilder if applicable.</li>
     * </ul>
     * 
     * @param builder The {@link ThingBuilder} to update.
     * @param result The {@link ZwaveJSTypeGeneratorResult} containing the updated channel information.
     * @return The updated {@link ThingBuilder}.
     */
    private ThingBuilder updateChannels(ThingBuilder builder, ZwaveJSTypeGeneratorResult result) {
        Map<String, Channel> existingChannelEntries = thing.getChannels().stream()
                .collect(Collectors.toMap(channel -> channel.getUID().getId(), channel -> channel));

        // remove channels that are no longer part of the thing
        for (Map.Entry<String, Channel> existingEntry : existingChannelEntries.entrySet()) {
            if (!result.channels.containsKey(existingEntry.getKey())) {
                logger.trace("Node {}. Removing {} channel", this.config.id, existingEntry.getKey());
                builder.withoutChannel(existingEntry.getValue().getUID());
            }
        }

        // Add new channels that are not already present
        for (Map.Entry<String, Channel> newEntry : result.channels.entrySet()) {
            if (!existingChannelEntries.containsKey(newEntry.getKey())) {
                logger.trace("Node {}. Adding {} channel", this.config.id, newEntry.getKey());
                builder.withChannel(newEntry.getValue());
            }
        }

        SemanticTag equipmentTag = getEquipmentTag(result.channels.values());
        if (equipmentTag != null) {
            logger.debug("Node {}. Setting semantic equipment tag {}", this.config.id, equipmentTag);
            builder.withSemanticEquipmentTag(equipmentTag);
        } else {
            logger.debug("Node {}. No semantic equipment tag set", this.config.id);
        }

        return builder;
    }

    private void initializeChannelAndConfigState(Node node, ZwaveJSTypeGeneratorResult result) {
        // Set initial state for linked channels
        for (Channel channel : thing.getChannels()) {
            if (result.values.containsKey(channel.getUID().getId()) && isLinked(channel.getUID())) {
                ChannelMetadata dummy = new ChannelMetadata(getId(), node.values.get(0));
                ZwaveJSChannelConfiguration channelConfig = channel.getConfiguration()
                        .as(ZwaveJSChannelConfiguration.class);
                State state = dummy.setState(Objects.requireNonNull(result.values.get(channel.getUID().getId())),
                        Objects.requireNonNull(channel.getAcceptedItemType()), channelConfig.incomingUnit,
                        channelConfig.inverted);
                if (state != null) {
                    // Initialize color and color temperature channels
                    ColorCapability colorCap = colorCapabilities.get(channelConfig.endpoint);
                    state = handleColorUpdate(colorCap, state, channel, channelConfig);
                    state = handleColorTemperatureUpdate(colorCap, state, channel, channelConfig);
                    try {
                        updateState(channel.getUID(), state);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Node {}. Error initializing state for channel {} with value {}:{}. {}",
                                node.nodeId, channel.getUID().getId(), state.getClass().getSimpleName(),
                                state.toFullString(), e.getMessage());
                    }
                }
            }
        }

        // Set configuration items if not using configurationAsChannels
        if (!configurationAsChannels) {
            Configuration configuration = editConfiguration();
            List<String> channelIds = thing.getChannels().stream().map(c -> c.getUID().getId()).toList();

            for (Entry<String, Object> entry : result.values.entrySet()) {
                if (!channelIds.contains(entry.getKey())) {
                    logger.trace("Node {}. Setting configuration item {} to {}", node.nodeId, entry.getKey(),
                            entry.getValue());
                    try {
                        Object entryValue = entry.getValue();
                        if (entryValue instanceof Map map) {
                            entryValue = map.toString();
                        }
                        configuration.put(entry.getKey(), entryValue);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Node {}. Error setting configuration item {} to {}: {}", node.nodeId,
                                entry.getKey(), entry.getValue(), e.getMessage());
                    }
                }
            }
            updateConfiguration(configuration);
            logger.debug("Node {}. Done values to configuration items", node.nodeId);
        }
    }

    @Override
    public void dispose() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof ZwaveJSBridgeHandler handler) {
            handler.unregisterNodeListener(this);
        }
        super.dispose();
    }

    private @Nullable SemanticTag getEquipmentTag(Collection<Channel> channels) {
        Set<Integer> commandClassIds = channels.stream()
                .map(channel -> channel.getConfiguration().as(ZwaveJSChannelConfiguration.class))
                .filter(Objects::nonNull).map(config -> Integer.valueOf(config.commandClassId))
                .collect(Collectors.toSet());

        // Find the first matching equipment tag based on command class IDs
        for (Map.Entry<Set<Integer>, SemanticTag> entry : EQUIPMENT_MAP.entrySet()) {
            if (commandClassIds.removeAll(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
