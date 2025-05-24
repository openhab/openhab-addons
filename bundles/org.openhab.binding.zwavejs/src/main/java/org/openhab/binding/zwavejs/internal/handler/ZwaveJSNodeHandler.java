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

import static org.openhab.binding.zwavejs.internal.BindingConstants.CONFIGURATION_COMMAND_CLASSES;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.Status;
import org.openhab.binding.zwavejs.internal.api.dto.commands.NodeSetValueCommand;
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
import org.openhab.core.types.State;
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

    public ZwaveJSNodeHandler(final Thing thing, final ZwaveJSTypeGenerator typeGenerator) {
        super(thing);
        this.typeGenerator = typeGenerator;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        super.handleConfigurationUpdate(configurationParameters);
        logger.debug("Node {}. Configuration update", config.id);
        ZwaveJSBridgeHandler handler = getBridgeHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }
        Node node = handler.requestNodeDetails(config.id);
        if (node == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-node-details");
            return;
        }
        ZwaveJSTypeGeneratorResult result = typeGenerator.generate(thing.getUID(), node, true);

        // TODO are we able to determine the changed parameters? The UI has a hold of 'dirty' parameters
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            String key = configurationParameter.getKey();
            Object newValue = configurationParameter.getValue();

            if (result.channels.containsKey(key)) {
                ZwaveJSChannelConfiguration channelConfig = Objects.requireNonNull(result.channels.get(key))
                        .getConfiguration().as(ZwaveJSChannelConfiguration.class);
                NodeSetValueCommand zwaveCommand = new NodeSetValueCommand(config.id, channelConfig);
                zwaveCommand.value = newValue;
                if (zwaveCommand.value != null) {
                    handler.sendCommand(zwaveCommand);
                }
            }
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Node {}. Processing command {} type {} for channel {}", config.id, command,
                command.getClass().getSimpleName(), channelUID);
        ZwaveJSBridgeHandler handler = getBridgeHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        Channel channel = thing.getChannel(channelUID);
        if (channel == null) {
            logger.debug("Channel {} not found", channelUID);
            return;
        }
        ZwaveJSChannelConfiguration channelConfig = channel.getConfiguration().as(ZwaveJSChannelConfiguration.class);
        NodeSetValueCommand zwaveCommand = new NodeSetValueCommand(config.id, channelConfig);

        if (command instanceof OnOffType onOffCommand) {
            if (CoreItemFactory.DIMMER.equals(channelConfig.itemType)) {
                zwaveCommand.value = OnOffType.ON.equals(onOffCommand) ? 255 : 0;
            } else {
                zwaveCommand.value = onOffCommand.equals(channelConfig.inverted ? OnOffType.OFF : OnOffType.ON);
            }
        } else if (command instanceof HSBType hsbTypeCommand) {
            int[] rgb = ColorUtil.hsbToRgb(hsbTypeCommand);
            zwaveCommand.value = String.format("%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
        } else if (command instanceof QuantityType<?> quantityCommand) {
            Unit<?> unit = UnitUtils.parseUnit(channelConfig.incomingUnit);
            if (unit == null) {
                logger.warn("Could not parse '{}' as a unit, this is a bug.", channelConfig.incomingUnit);
                return;
            }
            QuantityType<?> resultValue = Objects.requireNonNull(quantityCommand.toUnit(unit));
            if (channelConfig.factor != 1.0) {
                resultValue = resultValue.divide(new BigDecimal(channelConfig.factor));
            }
            zwaveCommand.value = resultValue;
        } else if (command instanceof PercentType percentTypeCommand) {
            int newValue = percentTypeCommand.intValue();
            if (channelConfig.inverted) {
                newValue = 100 - newValue;
            }
            if (CoreItemFactory.DIMMER.equals(channelConfig.itemType) && newValue == 100) {
                newValue = 99;
            }
            zwaveCommand.value = newValue;
        } else if (command instanceof DecimalType decimalCommand) {
            zwaveCommand.value = decimalCommand.doubleValue();
        } else if (command instanceof DateTimeType dateTimeCommand) {
            throw new UnsupportedOperationException();
        } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
            throw new UnsupportedOperationException();
        } else if (command instanceof NextPreviousType nextPreviousCommand) {
            throw new UnsupportedOperationException();
        } else if (command instanceof OpenClosedType openClosedCommand) {
            zwaveCommand.value = openClosedCommand
                    .equals(channelConfig.inverted ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
        } else if (command instanceof PlayPauseType stringCommand) {
            throw new UnsupportedOperationException();
        } else if (command instanceof PointType pointCommand) {
            throw new UnsupportedOperationException();
        } else if (command instanceof RewindFastforwardType rewindFastforwardCommand) {
            throw new UnsupportedOperationException();
        } else if (command instanceof StopMoveType stopMoveCommand) {
            throw new UnsupportedOperationException();
        } else if (command instanceof StringListType stringListCommand) {
            throw new UnsupportedOperationException();
        } else if (command instanceof UpDownType upDownCommand) {
            throw new UnsupportedOperationException();
        } else if (command instanceof StringType stringCommand) {
            zwaveCommand.value = stringCommand.toString();
        }
        if (zwaveCommand.value != null) {
            handler.sendCommand(zwaveCommand);
        }
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

        // Example for background initialization:
        executorService.execute(() -> {
            internalInitialize();
        });
    }

    private @Nullable ZwaveJSBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null || !bridge.getStatus().equals(ThingStatus.ONLINE)) {
            // when bridge is offline, stop and wait for it to become online
            logger.trace("Node {}. Prevented initialisation as bridge is offline", config.id);
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
        if (!handler.registerNodeListener(this)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Could not register node listener");
            return;
        }
        Node nodeDetails = handler.requestNodeDetails(config.id);
        if (nodeDetails == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-node-details");
            return;
        }
        if (Status.DEAD.equals(nodeDetails.status)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("The Z-Wave JS state of this node is: {}", nodeDetails.status));
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
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            internalInitialize();
        }
    }

    @Override
    public void onNodeRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
    }

    @Override
    public void onNodeAdded(Node node) {
        // onNodeStateChanged(node);
    }

    @Override
    public boolean onNodeStateChanged(Event event) {
        logger.trace("Node {}. State changed event", config.id);
        if (!configurationAsChannels && CONFIGURATION_COMMAND_CLASSES.contains(event.args.commandClassName)) {
            if (event.args.newValue == null) {
                logger.debug("Node {}. Configuration value not set, because it is null.", config.id);
                return false;
            }
            ConfigMetadata details = new ConfigMetadata(getId(), event);
            Configuration configuration = editConfiguration();
            configuration.put(details.id, event.args.newValue);
            updateConfiguration(configuration);
        } else {
            ChannelMetadata metadata = new ChannelMetadata(getId(), event);
            if (!metadata.isIgnoredCommandClass(event.args.commandClassName) && isLinked(metadata.id)) {
                logger.trace("Getting the configuration for linked channel {}", metadata.id);
                Channel channel = thing.getChannel(metadata.id);
                if (channel == null) {
                    logger.debug("Node {}. Channel {} not found, ignoring event", config.id, metadata.id);
                    return false;
                }

                ZwaveJSChannelConfiguration channelConfig = channel.getConfiguration()
                        .as(ZwaveJSChannelConfiguration.class);

                State state = metadata.setState(event.args.newValue, channelConfig.itemType, channelConfig.incomingUnit,
                        channelConfig.inverted);
                if (state != null) {
                    try {
                        updateState(metadata.id, state);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Node {}. Error updating state for channel {} with value {}. {}", event.nodeId,
                                metadata.id, state.toFullString(), e.getMessage());
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onNodeDead(Event event) {
        logger.trace("Node {}. Dead", config.id);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Z-Wave JS reported this node dead");
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
        try {
            ZwaveJSTypeGeneratorResult result = typeGenerator.generate(thing.getUID(), node, configurationAsChannels);

            ThingBuilder builder = editThing();

            if (!result.location.equals(getThing().getLocation()) && !result.location.isBlank()) {
                builder.withLocation(result.location);
            }

            List<Channel> channelsToRemove = new ArrayList<>();
            for (Channel channel : thing.getChannels()) {
                if (!result.channels.containsKey(channel.getUID().getId())) {
                    channelsToRemove.add(channel);
                } else {
                    result.channels.remove(channel.getUID().getId());
                }
            }
            if (!channelsToRemove.isEmpty()) {
                builder.withoutChannels(channelsToRemove);
            }
            if (!result.channels.isEmpty()) {
                List<Channel> channels = new ArrayList<Channel>(result.channels.entrySet().stream()
                        .sorted(Map.Entry.<String, Channel> comparingByKey()).map(m -> m.getValue()).toList());
                builder.withChannels(channels);
            }

            updateThing(builder.build());

            for (Channel channel : thing.getChannels()) {
                if (result.values.containsKey(channel.getUID().getId()) && isLinked(channel.getUID())) {
                    ChannelMetadata dummy = new ChannelMetadata(getId(), node.values.get(0));
                    ZwaveJSChannelConfiguration channelConfig = channel.getConfiguration()
                            .as(ZwaveJSChannelConfiguration.class);
                    State state = dummy.setState(Objects.requireNonNull(result.values.get(channel.getUID().getId())),
                            channelConfig.itemType, channelConfig.incomingUnit, channelConfig.inverted);
                    if (state != null) {
                        updateState(channel.getUID(), state);
                    }
                }
            }

            if (!configurationAsChannels) {
                Configuration configuration = editConfiguration();
                List<String> channelIds = thing.getChannels().stream().map(c -> c.getUID().getId()).toList();

                for (Entry<String, Object> entry : result.values.entrySet()) {
                    if (!channelIds.contains(entry.getKey())) {
                        logger.trace("Node {}. Setting configuration item {} to {}", node.nodeId, entry.getKey(),
                                entry.getValue());
                        configuration.put(entry.getKey(), entry.getValue());
                    }
                }
                updateConfiguration(configuration);
                logger.debug("Done values to configuration items");
            }
        } catch (Exception e) {
            logger.error("Node {}. Error building channels and configuration", node.nodeId, e);
        }

        return true;
    }

    @Override
    public void dispose() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof ZwaveJSBridgeHandler handler) {
            handler.unregisterNodeListener(this);
        }
        super.dispose();
    }
}
