/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lcn.internal.common.DimmerOutputCommand;
import org.openhab.binding.lcn.internal.common.LcnAddr;
import org.openhab.binding.lcn.internal.common.LcnAddrMod;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.connection.Connection;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.binding.lcn.internal.converter.AbstractVariableValueConverter;
import org.openhab.binding.lcn.internal.converter.AngleConverter;
import org.openhab.binding.lcn.internal.converter.Co2Converter;
import org.openhab.binding.lcn.internal.converter.CurrentConverter;
import org.openhab.binding.lcn.internal.converter.EnergyConverter;
import org.openhab.binding.lcn.internal.converter.IdentityConverter;
import org.openhab.binding.lcn.internal.converter.LightConverter;
import org.openhab.binding.lcn.internal.converter.PowerConverter;
import org.openhab.binding.lcn.internal.converter.TemperatureConverter;
import org.openhab.binding.lcn.internal.converter.VoltageConverter;
import org.openhab.binding.lcn.internal.converter.WindspeedConverter;
import org.openhab.binding.lcn.internal.subhandler.AbstractLcnModuleSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleMetaAckSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleMetaFirmwareSubHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LcnModuleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(LcnModuleHandler.class);
    private @Nullable LcnAddrMod moduleAddress;
    private Map<LcnChannelGroup, @Nullable AbstractLcnModuleSubHandler> subHandlers;
    private List<AbstractLcnModuleSubHandler> metadataSubHandlers;
    private Map<ChannelUID, @Nullable AbstractVariableValueConverter> converters;

    public LcnModuleHandler(Thing thing) {
        super(thing);

        subHandlers = Collections.synchronizedMap(new HashMap<>());
        metadataSubHandlers = Collections.synchronizedList(new LinkedList<>());
        converters = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        LcnModuleConfiguration localConfig = getConfigAs(LcnModuleConfiguration.class);
        LcnAddrMod localModuleAddress = moduleAddress = new LcnAddrMod(localConfig.segmentId, localConfig.moduleId);

        try {
            // create sub handlers
            ModInfo info = getPckGatewayHandler().getModInfo(localModuleAddress);
            for (LcnChannelGroup type : LcnChannelGroup.values()) {

                AbstractLcnModuleSubHandler newHandler = type.getSubHandlerClass()
                        .getDeclaredConstructor(LcnModuleHandler.class, ModInfo.class).newInstance(this, info);

                subHandlers.put(type, newHandler);
            }

            // meta sub handlers, which are not assigned to a channel group
            // initialize() can be invoked multiple times on the same handler, when changing a Channel's config
            metadataSubHandlers.clear();
            metadataSubHandlers.add(new LcnModuleMetaAckSubHandler(this, info));
            metadataSubHandlers.add(new LcnModuleMetaFirmwareSubHandler(this, info));

            // initialize variable value converters
            for (Channel channel : thing.getChannels()) {
                Object unitObject = channel.getConfiguration().get("unit");
                Object parameterObject = channel.getConfiguration().get("parameter");

                if (unitObject instanceof String) {
                    switch ((String) unitObject) {
                        case "temperature":
                            converters.put(channel.getUID(), new TemperatureConverter());
                            break;
                        case "light":
                            converters.put(channel.getUID(), new LightConverter());
                            break;
                        case "co2":
                            converters.put(channel.getUID(), new Co2Converter());
                            break;
                        case "power":
                            converters.put(channel.getUID(), new PowerConverter(parameterObject));
                            break;
                        case "energy":
                            converters.put(channel.getUID(), new EnergyConverter(parameterObject));
                            break;
                        case "current":
                            converters.put(channel.getUID(), new CurrentConverter());
                            break;
                        case "voltage":
                            converters.put(channel.getUID(), new VoltageConverter());
                            break;
                        case "angle":
                            converters.put(channel.getUID(), new AngleConverter());
                            break;
                        case "windspeed":
                            converters.put(channel.getUID(), new WindspeedConverter());
                            break;
                    }
                }
            }

            // module is assumed as online, when the corresponding Bridge (PckGatewayHandler) is online.
            updateStatus(ThingStatus.ONLINE);
        } catch (LcnException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            logger.warn("Failed to initialize handler: {}: {}: {}", localModuleAddress, e.getClass().getSimpleName(),
                    e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUid, Command command) {
        // this method can be invoked, when initialize() has not finished, yet.
        if (thing.getStatus() != ThingStatus.ONLINE) {
            return;
        }

        try {
            // refresh command can be received, when Bridge is initializing, so synchronize
            synchronized (getPckGatewayHandler()) {
                String groupId = channelUid.getGroupId();

                if (!channelUid.isInGroup()) {
                    return;
                }

                if (groupId == null) {
                    throw new LcnException("Group ID is null");
                }

                LcnChannelGroup channelGroup = LcnChannelGroup.valueOf(groupId.toUpperCase());
                AbstractLcnModuleSubHandler subHandler = subHandlers.get(channelGroup);

                if (subHandler == null) {
                    throw new LcnException("Sub Handler not found for: " + channelGroup);
                }

                Optional<Integer> number = channelUidToChannelNumber(channelUid, channelGroup);

                if (command instanceof RefreshType) {
                    number.ifPresent(n -> subHandler.handleRefresh(channelGroup, n));
                    subHandler.handleRefresh(channelUid.getIdWithoutGroup());
                } else if (command instanceof OnOffType) {
                    subHandler.handleCommandOnOff(castCommand(command), channelGroup, number.get());
                } else if (command instanceof DimmerOutputCommand) {
                    subHandler.handleCommandDimmerOutput(castCommand(command), number.get());
                } else if (command instanceof PercentType && number.isPresent()) {
                    subHandler.handleCommandPercent(castCommand(command), channelGroup, number.get());
                } else if (command instanceof HSBType) {
                    subHandler.handleCommandHsb(castCommand(command), channelUid.getIdWithoutGroup());
                } else if (command instanceof PercentType) {
                    subHandler.handleCommandPercent(castCommand(command), channelGroup, channelUid.getIdWithoutGroup());
                } else if (command instanceof StringType) {
                    subHandler.handleCommandString(castCommand(command), number.get());
                } else if (command instanceof DecimalType) {
                    DecimalType decimalType = castCommand(command);
                    DecimalType nativeValue = getConverter(channelUid).onCommandFromItem(decimalType.doubleValue());
                    subHandler.handleCommandDecimal(nativeValue, channelGroup, number.get());
                } else if (command instanceof QuantityType) {
                    QuantityType<?> quantityType = castCommand(command);
                    DecimalType nativeValue = getConverter(channelUid).onCommandFromItem(quantityType);
                    subHandler.handleCommandDecimal(nativeValue, channelGroup, number.get());
                } else if (command instanceof UpDownType) {
                    subHandler.handleCommandUpDown(castCommand(command), channelGroup, number.get());
                } else if (command instanceof StopMoveType) {
                    subHandler.handleCommandStopMove(castCommand(command), channelGroup, number.get());
                } else {
                    throw new LcnException("Unsupported command type");
                }
            }
        } catch (IllegalArgumentException | NoSuchElementException | LcnException e) {
            logger.warn("{}: Failed to handle command {}: {}", channelUid, command.getClass().getSimpleName(),
                    e.getMessage());
        }
    }

    @NonNullByDefault({}) // getOrDefault()
    private AbstractVariableValueConverter getConverter(ChannelUID channelUid) throws LcnException {
        return converters.getOrDefault(channelUid, IdentityConverter.getInstance());
    }

    /**
     * Convenience method to cast a command.
     *
     * @param <T> the concrete type to be casted to
     * @param command the command to be casted
     * @return the concrete command
     * @throws LcnException when the command cannot be casted
     */
    @SuppressWarnings("unchecked")
    private <T extends Command> T castCommand(Command command) throws LcnException {
        try {
            return (T) command;
        } catch (ClassCastException e) {
            throw new LcnException("Unexpected command type");
        }
    }

    /**
     * Invoked when a PCK messages arrives from the PCK gateway
     *
     * @param pck the message without line termination
     */
    @SuppressWarnings("null")
    public void handleStatusMessage(String pck) {
        // this method can be invoked, when initialize() has not finished, yet.
        if (thing.getStatus() != ThingStatus.ONLINE) {
            return;
        }

        synchronized (subHandlers) {
            for (AbstractLcnModuleSubHandler handler : subHandlers.values()) {
                if (handler.tryParse(pck)) {
                    break;
                }
            }
        }

        synchronized (metadataSubHandlers) {
            metadataSubHandlers.forEach(h -> h.tryParse(pck));
        }
    }

    private Optional<Integer> channelUidToChannelNumber(ChannelUID channelUid, LcnChannelGroup channelGroup)
            throws LcnException {
        try {
            int number = Integer.parseInt(channelUid.getIdWithoutGroup()) - 1;

            if (!channelGroup.isValidId(number)) {
                throw new LcnException("Out of range: " + number);
            }
            return Optional.of(number);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private PckGatewayHandler getPckGatewayHandler() throws LcnException {
        Bridge bridge = getBridge();
        if (bridge == null) {
            throw new LcnException("No LCN-PCK gateway configured for this module");
        }

        PckGatewayHandler handler = (PckGatewayHandler) bridge.getHandler();
        if (handler == null) {
            throw new LcnException("Could not get PckGatewayHandler");
        }
        return handler;
    }

    /**
     * Queues a PCK string for sending.
     *
     * @param command without the address part
     * @throws LcnException when the module address is unknown
     */
    public void sendPck(String command) throws LcnException {
        getPckGatewayHandler().queue(getCommandAddress(), true, command);
    }

    /**
     * Queues a PCK byte buffer for sending.
     *
     * @param command without the address part
     * @throws LcnException when the module address is unknown
     */
    public void sendPck(ByteBuffer command) throws LcnException {
        getPckGatewayHandler().queue(getCommandAddress(), true, command);
    }

    /**
     * Gets the address, which shall be used when sending commands into the LCN bus. This can also be a group address.
     *
     * @return the address to send to
     * @throws LcnException when the address is unknown
     */
    protected LcnAddr getCommandAddress() throws LcnException, LcnException {
        LcnAddr localAddress = moduleAddress;
        if (localAddress == null) {
            throw new LcnException("Module address not set");
        }
        return localAddress;
    }

    /**
     * Invoked when an update for this LCN module should be fired to openHAB.
     *
     * @param channelGroup the Channel to update
     * @param channelId the ID within the Channel to update
     * @param state the new state
     */
    public void updateChannel(LcnChannelGroup channelGroup, String channelId, State state) {
        ChannelUID channelUid = createChannelUid(channelGroup, channelId);
        AbstractVariableValueConverter converter = converters.get(channelUid);

        State convertedState = state;
        if (converter != null) {
            convertedState = converter.onStateUpdateFromHandler(state);
        }
        updateState(channelUid, convertedState);
    }

    /**
     * Invoked when an trigger for this LCN module should be fired to openHAB.
     *
     * @param channelGroup the Channel to update
     * @param channelId the ID within the Channel to update
     * @param event the event used to trigger
     */
    public void triggerChannel(LcnChannelGroup channelGroup, String channelId, String event) {
        triggerChannel(createChannelUid(channelGroup, channelId), event);
    }

    private ChannelUID createChannelUid(LcnChannelGroup channelGroup, String channelId) {
        return new ChannelUID(thing.getUID(), channelGroup.name().toLowerCase() + "#" + channelId);
    }

    /**
     * Checks the LCN module address against the own.
     *
     * @param physicalSegmentId which is 0 if it is the local segment
     * @param moduleId
     * @return true, if the given address matches the own address
     */
    public boolean isMyAddress(String physicalSegmentId, String moduleId) {
        try {
            return new LcnAddrMod(getPckGatewayHandler().toLogicalSegmentId(Integer.parseInt(physicalSegmentId)),
                    Integer.parseInt(moduleId)).equals(getStatusMessageAddress());
        } catch (LcnException e) {
            return false;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(LcnModuleActions.class);
    }

    /**
     * Invoked when an Ack from this module has been received.
     */
    public void onAckRceived() {
        try {
            Connection connection = getPckGatewayHandler().getConnection();
            LcnAddrMod localModuleAddress = moduleAddress;
            if (connection != null && localModuleAddress != null) {
                getPckGatewayHandler().getModInfo(localModuleAddress).onAck(LcnBindingConstants.CODE_ACK, connection,
                        getPckGatewayHandler().getTimeoutMs(), System.nanoTime());
            }
        } catch (LcnException e) {
            logger.warn("Connection or module address not set");
        }
    }

    /**
     * Gets the address the handler shall react to, when a status message from this address is processed.
     *
     * @return the address for status messages
     */
    public LcnAddrMod getStatusMessageAddress() {
        LcnAddrMod localmoduleAddress = moduleAddress;
        if (localmoduleAddress != null) {
            return localmoduleAddress;
        } else {
            return new LcnAddrMod(0, 0);
        }
    }
}
