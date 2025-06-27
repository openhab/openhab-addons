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
                    final var result = client.readDBArea(1, 0, buffer.length, S7Client.S7WLByte, buffer);
                    if (result == 0) {
                        if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type) && MEMORY_BYTE.equalsIgnoreCase(kind)) {
                            final var value = S7.GetBitAt(buffer, address, getBit(name));
                            updateState(channelUID, OnOffType.from(value));
                            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                        } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_BYTE.equalsIgnoreCase(kind)) {
                            final var value = buffer[address];
                            updateState(channelUID, new DecimalType(value));
                            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                        } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_WORD.equalsIgnoreCase(kind)) {
                            final var value = S7.GetShortAt(buffer, address);
                            updateState(channelUID, new DecimalType(value));
                            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                        } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_DWORD.equalsIgnoreCase(kind)) {
                            final var value = S7.GetDIntAt(buffer, address);
                            updateState(channelUID, new DecimalType(value));
                            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                        } else {
                            logger.debug("Channel {} will not accept {} items.", channelUID, type);
                        }
                    } else {
                        logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                    }
                }
                case DecimalType decimalCommand -> {
                    final var length = MEMORY_BYTE.equalsIgnoreCase(kind) ? 1 : 2;
                    final var buffer = new byte[MEMORY_DWORD.equalsIgnoreCase(kind) ? 4 : length];
                    if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_BYTE.equalsIgnoreCase(kind)) {
                        buffer[0] = decimalCommand.byteValue();
                    } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_WORD.equalsIgnoreCase(kind)) {
                        S7.SetShortAt(buffer, 0, decimalCommand.intValue());
                    } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_DWORD.equalsIgnoreCase(kind)) {
                        S7.SetDIntAt(buffer, 0, decimalCommand.intValue());
                    } else {
                        logger.debug("Channel {} will not accept {} items.", channelUID, type);
                    }
                    final var result = client.writeDBArea(1, address, buffer.length, S7Client.S7WLByte, buffer);
                    if (result != 0) {
                        logger.debug("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                    }
                }
                case OnOffType ignored -> {
                    final var buffer = new byte[1];
                    if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type) && MEMORY_BYTE.equalsIgnoreCase(kind)) {
                        S7.SetBitAt(buffer, 0, 0, OnOffType.ON.equals(command));
                    } else {
                        logger.debug("Channel {} will not accept {} items.", channelUID, type);
                    }
                    final var bit = 8 * address + getBit(name);
                    final var result = client.writeDBArea(1, bit, buffer.length, S7Client.S7WLBit, buffer);
                    if (result != 0) {
                        logger.debug("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
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
                final var kind = getBlockKind();
                final var type = channel.getAcceptedItemType();
                final var force = config.isUpdateForced();

                if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type) && kind.equalsIgnoreCase(MEMORY_BYTE)) {
                    OnOffType state = (OnOffType) getOldValue(name);
                    OnOffType value = OnOffType.from(S7.GetBitAt(data, address, getBit(name)));
                    if ((state == null) || (value != state) || force) {
                        updateState(channelUID, value);
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    }
                    if (logger.isTraceEnabled()) {
                        final var buffer = Integer.toBinaryString((data[address] & 0xFF) + 0x100);
                        logger.trace("Channel {} received [{}].", channelUID, buffer.substring(1));
                    }
                } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_BYTE.equalsIgnoreCase(kind)) {
                    final var threshold = config.getThreshold();
                    final var state = (DecimalType) getOldValue(name);
                    final var value = data[address];
                    if ((state == null) || (Math.abs(value - state.intValue()) > threshold) || force) {
                        updateState(channelUID, new DecimalType(value));
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Channel {} received [{}].", channelUID, data[address]);
                    }
                } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_WORD.equalsIgnoreCase(kind)) {
                    final var threshold = config.getThreshold();
                    final var state = (DecimalType) getOldValue(name);
                    final var value = S7.GetShortAt(data, address);
                    if ((state == null) || (Math.abs(value - state.intValue()) > threshold) || force) {
                        updateState(channelUID, new DecimalType(value));
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Channel {} received [{}, {}].", channelUID, data[address], data[address + 1]);
                    }
                } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_DWORD.equalsIgnoreCase(kind)) {
                    final var threshold = config.getThreshold();
                    final var state = (DecimalType) getOldValue(name);
                    final var value = S7.GetDIntAt(data, address);
                    if ((state == null) || (Math.abs(value - state.intValue()) > threshold) || force) {
                        updateState(channelUID, new DecimalType(value));
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Channel {} received [{}, {}, {}, {}].", channelUID, data[address],
                                data[address + 1], data[address + 2], data[address + 3]);
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
        super.updateState(channelUID, state);

        final var channel = getThing().getChannel(channelUID.getId());
        setOldValue(getBlockFromChannel(channel), state);
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
