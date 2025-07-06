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
package org.openhab.binding.plclogo.internal.handler;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.ANALOG_ITEM;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.BINDING_ID;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.BLOCK_PROPERTY;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DIGITAL_OUTPUT_ITEM;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.MEMORY_BYTE;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.MEMORY_DWORD;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.MEMORY_WORD;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.STATE_CHANNEL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.VALUE_CHANNEL;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plclogo.internal.config.PLCMemoryConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Moka7.S7;
import Moka7.S7Client;

/**
 * The {@link PLCMemoryHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCMemoryHandler extends PLCCommonHandler {

    private final Logger logger = LoggerFactory.getLogger(PLCMemoryHandler.class);
    private volatile @NonNullByDefault({}) PLCMemoryConfiguration config;

    /**
     * Constructor.
     */
    public PLCMemoryHandler(Thing thing) {
        super(thing);
        config = getConfigAs(PLCMemoryConfiguration.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!isThingOnline()) {
            return;
        }

        final var channel = getThing().getChannel(channelUID.getId());
        final var name = getBlockFromChannel(channel);
        if (!isValid(name) || (channel == null)) {
            logger.debug("Can not update channel {}, block {}.", channelUID, name);
            return;
        }

        final var address = getAddress(name);
        final var client = getLogoClient();
        if ((address != INVALID) && (client != null)) {
            final var kind = getBlockKind();
            final var type = channel.getAcceptedItemType();
            switch (command) {
                case RefreshType ignored -> {
                    final var buffer = new byte[getBufferLength()];
                    final var result = client.readBytes(0, buffer.length, buffer);
                    if (result == 0) {
                        if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type)) {
                            updateState(channelUID,
                                    kind.equalsIgnoreCase(MEMORY_BYTE)
                                            ? OnOffType.from(S7.GetBitAt(buffer, address, getBit(name)))
                                            : UnDefType.UNDEF);
                        } else if (ANALOG_ITEM.equalsIgnoreCase(type)) {
                            updateState(channelUID, switch (kind.toUpperCase()) {
                                case MEMORY_BYTE -> new DecimalType(buffer[address]);
                                case MEMORY_WORD -> new DecimalType(S7.GetShortAt(buffer, address));
                                case MEMORY_DWORD -> new DecimalType(S7.GetDIntAt(buffer, address));
                                default -> UnDefType.UNDEF;
                            });
                        } else {
                            logger.debug("Channel {} will not accept {} items.", channelUID, type);
                        }
                    } else {
                        logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                    }
                }
                case DecimalType decimalCommand -> {
                    if (ANALOG_ITEM.equalsIgnoreCase(type)) {
                        if (MEMORY_BYTE.equalsIgnoreCase(kind)) {
                            final byte[] buffer = { decimalCommand.byteValue() };
                            final var result = client.writeBytes(address, buffer.length, buffer);
                            if (result != 0) {
                                logger.debug("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                            }
                        } else if (MEMORY_WORD.equalsIgnoreCase(kind)) {
                            final byte[] buffer = { 0, 0 };
                            S7.SetShortAt(buffer, 0, decimalCommand.intValue());
                            final var result = client.writeBytes(address, buffer.length, buffer);
                            if (result != 0) {
                                logger.debug("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                            }
                        } else if (MEMORY_DWORD.equalsIgnoreCase(kind)) {
                            final byte[] buffer = { 0, 0, 0, 0 };
                            S7.SetDIntAt(buffer, 0, decimalCommand.intValue());
                            final var result = client.writeBytes(address, buffer.length, buffer);
                            if (result != 0) {
                                logger.debug("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                            }
                        } else {
                            logger.debug("Channel {} will not accept {} items.", channelUID, kind);
                        }
                    } else {
                        logger.debug("Channel {} will not accept {} items.", channelUID, type);
                    }
                }
                case OnOffType ignored -> {
                    if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type)) {
                        if (MEMORY_BYTE.equalsIgnoreCase(kind)) {
                            final byte[] buffer = { 0 };
                            S7.SetBitAt(buffer, 0, 0, OnOffType.ON.equals(command));
                            final var bit = 8 * address + getBit(name);
                            final var result = client.writeBits(bit, buffer.length, buffer);
                            if (result != 0) {
                                logger.debug("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                            }
                        } else {
                            logger.debug("Channel {} will not accept {} items.", channelUID, kind);
                        }
                    } else {
                        logger.debug("Channel {} will not accept {} items.", channelUID, type);
                    }
                }
                default -> logger.debug("Channel {} received not supported command {}.", channelUID, command);
            }
        } else {
            logger.info("Invalid channel {} or client {} found.", channelUID, client);
        }
    }

    @Override
    public void setData(final byte[] data) {
        if (!isThingOnline()) {
            return;
        }

        if (data.length != getBufferLength()) {
            logger.info("Received and configured data sizes does not match.");
            return;
        }

        final var channels = getThing().getChannels();
        if (channels.size() != getNumberOfChannels()) {
            logger.info("Received and configured channel sizes does not match.");
            return;
        }

        for (final var channel : channels) {
            final var channelUID = channel.getUID();
            final var name = getBlockFromChannel(channel);

            int address = getAddress(name);
            if (address != INVALID) {
                final var type = channel.getAcceptedItemType();
                final var force = config.isUpdateForced();

                if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type)) {
                    final var kind = getBlockKind();
                    if (MEMORY_BYTE.equalsIgnoreCase(kind)) {
                        final var value = OnOffType.from(S7.GetBitAt(data, address, getBit(name)));
                        if (!value.equals(getOldValue(name)) || force) {
                            updateState(channelUID, value);
                        }
                        if (logger.isTraceEnabled()) {
                            final var buffer = Integer.toBinaryString((data[address] & 0xFF) + 0x100);
                            logger.trace("Channel {} received [{}].", channelUID, buffer.substring(1));
                        }
                    } else {
                        logger.debug("Channel {} will not accept {} items.", channelUID, kind);
                        updateState(channelUID, UnDefType.UNDEF);
                    }
                } else if (ANALOG_ITEM.equalsIgnoreCase(type)) {
                    final var kind = getBlockKind();
                    final var value = switch (kind.toUpperCase()) {
                        case MEMORY_BYTE -> {
                            logger.trace("Channel {} received [{}].", channelUID, data[address]);
                            yield new DecimalType(data[address]);
                        }
                        case MEMORY_WORD -> {
                            logger.trace("Channel {} received [{}, {}].", channelUID, data[address], data[address + 1]);
                            yield new DecimalType(S7.GetShortAt(data, address));
                        }
                        case MEMORY_DWORD -> {
                            logger.trace("Channel {} received [{}, {}, {}, {}].", channelUID, data[address],
                                    data[address + 1], data[address + 2], data[address + 3]);
                            yield new DecimalType(S7.GetDIntAt(data, address));
                        }
                        default -> {
                            logger.debug("Channel {} will not accept {} items.", channelUID, kind);
                            yield UnDefType.UNDEF;
                        }
                    };
                    final var state = getOldValue(name);
                    if ((state instanceof DecimalType decimalState) && (value instanceof DecimalType decimalValue)) {
                        final var threshold = config.getThreshold();
                        if ((Math.abs(decimalValue.longValue() - decimalState.longValue()) > threshold) || force) {
                            updateState(channelUID, value);
                        }
                    } else {
                        updateState(channelUID, value);
                    }
                } else {
                    logger.debug("Channel {} will not accept {} items.", channelUID, type);
                }
            } else {
                logger.info("Invalid channel {} found.", channelUID);
            }
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        final var channel = getThing().getChannel(channelUID.getId());
        final var name = getBlockFromChannel(channel);
        if (isValid(name) && (channel != null)) {
            final var type = channel.getAcceptedItemType();
            try {
                super.updateState(channelUID, state);
                setOldValue(name, state);
                logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, state);
            } catch (IllegalArgumentException exception) {
                super.updateState(channelUID, UnDefType.UNDEF);
                setOldValue(name, UnDefType.UNDEF);
                logger.warn("Channel {} accepting {} received invalid argument.", channelUID, type);
            }
        } else {
            logger.debug("Can not update channel {}, block {}.", channelUID, name);
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config = getConfigAs(PLCMemoryConfiguration.class);
    }

    @Override
    protected boolean isValid(final String name) {
        if ((3 <= name.length()) && (name.length() <= 7)) {
            final var kind = getBlockKind();
            if (Character.isDigit(name.charAt(2))) {
                var valid = MEMORY_BYTE.equalsIgnoreCase(kind) || MEMORY_WORD.equalsIgnoreCase(kind);
                return name.startsWith(kind) && (valid || MEMORY_DWORD.equalsIgnoreCase(kind));
            }
        }
        return false;
    }

    @Override
    protected String getBlockKind() {
        return config.getBlockKind();
    }

    @Override
    protected int getNumberOfChannels() {
        return 1;
    }

    @Override
    protected void doInitialization() {
        final var thing = getThing();
        logger.debug("Initialize LOGO! memory handler.");

        config = getConfigAs(PLCMemoryConfiguration.class);

        super.doInitialization();
        if (ThingStatus.OFFLINE != thing.getStatus()) {
            final var kind = getBlockKind();
            final var name = config.getBlockName();
            final var isDigital = MEMORY_BYTE.equalsIgnoreCase(kind) && (getBit(name) != INVALID);
            final var text = isDigital ? "Digital" : "Analog";

            final var tBuilder = editThing();

            var label = thing.getLabel();
            if (label == null) {
                Bridge bridge = getBridge();
                label = (bridge == null) || (bridge.getLabel() == null) ? "Siemens Logo!" : bridge.getLabel();
                label += (": " + text.toLowerCase() + " in/output");
            }
            tBuilder.withLabel(label);

            final var type = config.getChannelType();
            final var uid = new ChannelUID(thing.getUID(), isDigital ? STATE_CHANNEL : VALUE_CHANNEL);
            final var cBuilder = ChannelBuilder.create(uid, type);
            cBuilder.withType(new ChannelTypeUID(BINDING_ID, type.toLowerCase()));
            cBuilder.withLabel(name);
            cBuilder.withDescription(text + " in/output block " + name);
            cBuilder.withProperties(Map.of(BLOCK_PROPERTY, name));
            tBuilder.withChannel(cBuilder.build());
            setOldValue(name, null);

            updateThing(tBuilder.build());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Calculate bit within address for block with given name.
     *
     * @param name Name of the LOGO! block
     * @return Calculated bit
     */
    private int getBit(final String name) {
        int bit = INVALID;

        logger.debug("Get bit of {} LOGO! for block {} .", getLogoFamily(), name);

        if (isValid(name) && (getAddress(name) != INVALID)) {
            String[] parts = name.trim().split("\\.");
            if (parts.length > 1) {
                bit = Integer.parseInt(parts[1]);
            }
        } else {
            logger.info("Wrong configurated LOGO! block {} found.", name);
        }

        return bit;
    }
}
