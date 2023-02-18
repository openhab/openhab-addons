/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.DimmerOutputCommand;
import org.openhab.binding.lcn.internal.common.LcnAddr;
import org.openhab.binding.lcn.internal.common.LcnAddrMod;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.connection.Connection;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.binding.lcn.internal.converter.Converter;
import org.openhab.binding.lcn.internal.converter.Converters;
import org.openhab.binding.lcn.internal.converter.InversionConverter;
import org.openhab.binding.lcn.internal.converter.S0Converter;
import org.openhab.binding.lcn.internal.subhandler.AbstractLcnModuleSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleMetaAckSubHandler;
import org.openhab.binding.lcn.internal.subhandler.LcnModuleMetaFirmwareSubHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LcnModuleHandler} is responsible for handling commands, which are
 * sent to or received from one of the channels.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(LcnModuleHandler.class);
    private static final int FIRMWARE_VERSION_LENGTH = 6;
    private static final Map<String, Converter> VALUE_CONVERTERS = new HashMap<>();
    private static final InversionConverter INVERSION_CONVERTER = new InversionConverter();
    private @Nullable LcnAddrMod moduleAddress;
    private final Map<LcnChannelGroup, AbstractLcnModuleSubHandler> subHandlers = new HashMap<>();
    private final List<AbstractLcnModuleSubHandler> metadataSubHandlers = new ArrayList<>();
    private final Map<ChannelUID, Converter> converters = new HashMap<>();

    static {
        VALUE_CONVERTERS.put("temperature", Converters.TEMPERATURE);
        VALUE_CONVERTERS.put("light", Converters.LIGHT);
        VALUE_CONVERTERS.put("co2", Converters.CO2);
        VALUE_CONVERTERS.put("current", Converters.CURRENT);
        VALUE_CONVERTERS.put("voltage", Converters.VOLTAGE);
        VALUE_CONVERTERS.put("angle", Converters.ANGLE);
        VALUE_CONVERTERS.put("windspeed", Converters.WINDSPEED);
    }

    public LcnModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        LcnModuleConfiguration localConfig = getConfigAs(LcnModuleConfiguration.class);
        LcnAddrMod localModuleAddress = moduleAddress = new LcnAddrMod(localConfig.segmentId, localConfig.moduleId);

        try {
            ModInfo info = getPckGatewayHandler().getModInfo(localModuleAddress);
            readFirmwareVersionFromProperty().ifPresent(info::setFirmwareVersion);
            requestFirmwareVersionAndSerialNumberIfNotSet();

            // create sub handlers
            for (LcnChannelGroup type : LcnChannelGroup.values()) {
                subHandlers.put(type, type.createSubHandler(this, info));
            }

            // meta sub handlers, which are not assigned to a channel group
            metadataSubHandlers.add(new LcnModuleMetaAckSubHandler(this, info));
            metadataSubHandlers.add(new LcnModuleMetaFirmwareSubHandler(this, info));

            // initialize converters
            for (Channel channel : thing.getChannels()) {
                Object unitObject = channel.getConfiguration().get("unit");
                Object parameterObject = channel.getConfiguration().get("parameter");
                Object invertState = channel.getConfiguration().get("invertState");
                Object invertUpDown = channel.getConfiguration().get("invertUpDown");

                // Initialize value converters
                if (unitObject instanceof String) {
                    switch ((String) unitObject) {
                        case "power":
                        case "energy":
                            converters.put(channel.getUID(), new S0Converter(parameterObject));
                            break;
                        default:
                            Converter converter = VALUE_CONVERTERS.get(unitObject);
                            if (converter != null) {
                                converters.put(channel.getUID(), converter);
                            }
                            break;
                    }
                }

                // Initialize inversion converter
                if (Boolean.TRUE.equals(invertState) || Boolean.TRUE.equals(invertUpDown)) {
                    converters.put(channel.getUID(), INVERSION_CONVERTER);
                }

            }

            // module is assumed as online, when the corresponding Bridge (PckGatewayHandler) is online.
            updateStatus(ThingStatus.ONLINE);

            // trigger REFRESH commands for all linked Channels to start polling
            getThing().getChannels().forEach(channel -> {
                if (isLinked(channel.getUID())) {
                    channelLinked(channel.getUID());
                }
            });
        } catch (LcnException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    /**
     * Triggers requesting the firmware version of the LCN module. The message also contains the serial number.
     *
     * @throws LcnException when the handler is not initialized
     */
    protected void requestFirmwareVersionAndSerialNumberIfNotSet() throws LcnException {
        if (readFirmwareVersionFromProperty().isEmpty()) {
            LcnAddrMod localModuleAddress = moduleAddress;
            if (localModuleAddress != null) {
                getPckGatewayHandler().getModInfo(localModuleAddress).requestFirmwareVersion();
            }
        }
    }

    private Optional<Integer> readFirmwareVersionFromProperty() {
        String prop = getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION);

        if (prop == null || prop.length() != FIRMWARE_VERSION_LENGTH) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(prop, 16));
        } catch (NumberFormatException e) {
            logger.warn("{}: Serial number property invalid", moduleAddress);
            return Optional.empty();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUid, Command command) {
        try {
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
                subHandler.handleCommandOnOff((OnOffType) command, channelGroup, number.get());
            } else if (command instanceof DimmerOutputCommand) {
                subHandler.handleCommandDimmerOutput((DimmerOutputCommand) command, number.get());
            } else if (command instanceof PercentType && number.isPresent()) {
                subHandler.handleCommandPercent((PercentType) command, channelGroup, number.get());
            } else if (command instanceof HSBType) {
                subHandler.handleCommandHsb((HSBType) command, channelUid.getIdWithoutGroup());
            } else if (command instanceof PercentType) {
                subHandler.handleCommandPercent((PercentType) command, channelGroup, channelUid.getIdWithoutGroup());
            } else if (command instanceof StringType) {
                subHandler.handleCommandString((StringType) command, number.orElse(0));
            } else if (command instanceof DecimalType) {
                DecimalType decimalType = (DecimalType) command;
                DecimalType nativeValue = getConverter(channelUid).onCommandFromItem(decimalType.doubleValue());
                subHandler.handleCommandDecimal(nativeValue, channelGroup, number.get());
            } else if (command instanceof QuantityType) {
                QuantityType<?> quantityType = (QuantityType<?>) command;
                DecimalType nativeValue = getConverter(channelUid).onCommandFromItem(quantityType);
                subHandler.handleCommandDecimal(nativeValue, channelGroup, number.get());
            } else if (command instanceof UpDownType) {
                Channel channel = thing.getChannel(channelUid);
                if (channel != null) {
                    Object invertConfig = channel.getConfiguration().get("invertUpDown");
                    boolean invertUpDown = invertConfig instanceof Boolean && (boolean) invertConfig;
                    subHandler.handleCommandUpDown((UpDownType) command, channelGroup, number.get(), invertUpDown);
                }
            } else if (command instanceof StopMoveType) {
                subHandler.handleCommandStopMove((StopMoveType) command, channelGroup, number.get());
            } else {
                throw new LcnException("Unsupported command type");
            }
        } catch (IllegalArgumentException | NoSuchElementException | LcnException e) {
            logger.warn("{}: Failed to handle command {}: {}", channelUid, command.getClass().getSimpleName(),
                    e.getMessage());
        }
    }

    @NonNullByDefault({}) // getOrDefault()
    private Converter getConverter(ChannelUID channelUid) {
        return converters.getOrDefault(channelUid, Converters.IDENTITY);
    }

    /**
     * Invoked when a PCK messages arrives from the PCK gateway
     *
     * @param pck the message without line termination
     */
    public void handleStatusMessage(String pck) {
        subHandlers.values().forEach(h -> h.tryParse(pck));

        metadataSubHandlers.forEach(h -> h.tryParse(pck));
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
    public void sendPck(byte[] command) throws LcnException {
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
        Converter converter = converters.get(channelUid);

        State convertedState = state;
        if (converter != null) {
            try {
                convertedState = converter.onStateUpdateFromHandler(state);
            } catch (LcnException e) {
                logger.warn("{}: {}{}: Value conversion failed: {}", moduleAddress, channelGroup, channelId,
                        e.getMessage());
            }
        }

        updateState(channelUid, convertedState);
    }

    /**
     * Updates the LCN module's serial number property.
     *
     * @param serialNumber the new serial number
     */
    public void updateSerialNumberProperty(String serialNumber) {
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
    }

    /**
     * Updates the LCN module's serial number property.
     *
     * @param serialNumber the new serial number
     */
    public void updateFirmwareVersionProperty(String firmwareVersion) {
        updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
    }

    /**
     * Invoked when a trigger for this LCN module should be fired to openHAB.
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
                        getPckGatewayHandler().getTimeoutMs(), System.currentTimeMillis());
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

    @Override
    public void dispose() {
        metadataSubHandlers.clear();
        subHandlers.clear();
        converters.clear();
    }
}
