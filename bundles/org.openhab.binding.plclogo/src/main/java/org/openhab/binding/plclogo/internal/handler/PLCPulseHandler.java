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

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.BINDING_ID;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.BLOCK_PROPERTY;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DIGITAL_INPUT_ITEM;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DIGITAL_OUTPUT_ITEM;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.I_DIGITAL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.MEMORY_BYTE;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.M_DIGITAL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.NI_DIGITAL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.NQ_DIGITAL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.OBSERVE_CHANNEL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.Q_DIGITAL;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.STATE_CHANNEL;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plclogo.internal.config.PLCPulseConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Moka7.S7;
import Moka7.S7Client;

/**
 * The {@link PLCPulseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCPulseHandler extends PLCCommonHandler {

    private final Logger logger = LoggerFactory.getLogger(PLCPulseHandler.class);
    private volatile @NonNullByDefault({}) PLCPulseConfiguration config;
    private volatile @Nullable Boolean received;

    /**
     * Constructor.
     */
    public PLCPulseHandler(Thing thing) {
        super(thing);
        config = getConfigAs(PLCPulseConfiguration.class);
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

        final var bit = getBit(name);
        final var address = getAddress(name);
        final var client = getLogoClient();
        if ((address != INVALID) && (bit != INVALID) && (client != null)) {
            final var buffer = new byte[1];
            if (command instanceof RefreshType) {
                int result = client.readBytes(address, buffer.length, buffer);
                if (result == 0) {
                    updateChannel(channel, S7.GetBitAt(buffer, 0, bit));
                } else {
                    logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else if ((command instanceof OpenClosedType) || (command instanceof OnOffType)) {
                String type = channel.getAcceptedItemType();
                if (DIGITAL_INPUT_ITEM.equalsIgnoreCase(type)) {
                    final var received = OpenClosedType.CLOSED.equals(command);
                    S7.SetBitAt(buffer, 0, 0, received);
                    this.received = received;
                } else if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type)) {
                    final var received = OnOffType.ON.equals(command);
                    S7.SetBitAt(buffer, 0, 0, received);
                    this.received = received;
                } else {
                    logger.debug("Channel {} will not accept {} items.", channelUID, type);
                }
                int result = client.writeBits(8 * address + bit, buffer.length, buffer);
                if (result != 0) {
                    logger.debug("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else {
                logger.debug("Channel {} received not supported command {}.", channelUID, command);
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

        final var channels = thing.getChannels();
        if (channels.size() != getNumberOfChannels()) {
            logger.info("Received and configured channel sizes does not match.");
            return;
        }

        final var client = getLogoClient();
        for (final var channel : channels) {
            final var channelUID = channel.getUID();
            final var name = getBlockFromChannel(channel);

            final var bit = getBit(name);
            final var address = getAddress(name);
            if ((address != INVALID) && (bit != INVALID) && (client != null)) {
                final var state = (DecimalType) getOldValue(channelUID.getId());
                if (STATE_CHANNEL.equalsIgnoreCase(channelUID.getId())) {
                    final var value = S7.GetBitAt(data, address - getBase(name), bit);
                    if ((state == null) || ((value ? 1 : 0) != state.intValue())) {
                        updateChannel(channel, value);
                    }
                    if (logger.isTraceEnabled()) {
                        final var buffer = (data[address - getBase(name)] & 0xFF) + 0x100;
                        logger.trace("Channel {} received [{}].", channelUID,
                                Integer.toBinaryString(buffer).substring(1));
                    }
                } else if (OBSERVE_CHANNEL.equalsIgnoreCase(channelUID.getId())) {
                    handleCommand(channelUID, RefreshType.REFRESH);
                    final var current = (DecimalType) getOldValue(channelUID.getId());
                    if ((state != null) && !state.equals(current)) {
                        final var pulse = config.getPulseLength();
                        scheduler.schedule(() -> {
                            final var received = this.received;
                            if (received != null) {
                                byte[] buffer = new byte[1];
                                S7.SetBitAt(buffer, 0, 0, !received);
                                final var block = config.getBlockName();
                                final var bit1 = 8 * getAddress(block) + getBit(block);
                                final var result = client.writeBits(bit1, buffer.length, buffer);
                                if (result != 0) {
                                    logger.debug("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                                }
                            } else {
                                logger.debug("Invalid received value on channel {}.", channelUID);
                            }
                            this.received = null;
                        }, pulse.longValue(), TimeUnit.MILLISECONDS);
                    }
                } else {
                    logger.info("Invalid channel {} found.", channelUID);
                }
            } else {
                logger.info("Invalid channel {} or client {} found.", channelUID, client);
            }
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
        var value = state.as(DecimalType.class);
        if (state instanceof OpenClosedType openClosedState) {
            value = new DecimalType(openClosedState == OpenClosedType.CLOSED ? 1 : 0);
        }

        setOldValue(channelUID.getId(), value);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config = getConfigAs(PLCPulseConfiguration.class);
    }

    @Override
    protected boolean isValid(final String name) {
        if ((2 <= name.length()) && (name.length() <= 7)) {
            final var kind = config.getObservedBlockKind();
            if (Character.isDigit(name.charAt(1))) {
                final var valid = I_DIGITAL.equalsIgnoreCase(kind) || Q_DIGITAL.equalsIgnoreCase(kind);
                return name.startsWith(kind) && (valid || M_DIGITAL.equalsIgnoreCase(kind));
            } else if (Character.isDigit(name.charAt(2))) {
                String bKind = getBlockKind();
                var valid = NI_DIGITAL.equalsIgnoreCase(kind) || NQ_DIGITAL.equalsIgnoreCase(kind);
                valid = name.startsWith(kind) && (valid || MEMORY_BYTE.equalsIgnoreCase(kind));
                return (name.startsWith(bKind) && MEMORY_BYTE.equalsIgnoreCase(bKind)) || valid;
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
        return 2;
    }

    @Override
    protected int getAddress(final String name) {
        int address = super.getAddress(name);
        if (address != INVALID) {
            int base = getBase(name);
            if (base != 0) {
                address = base + (address - 1) / 8;
            }
        } else {
            logger.info("Wrong configurated LOGO! block {} found.", name);
        }
        return address;
    }

    @Override
    protected void doInitialization() {
        final var thing = getThing();
        logger.debug("Initialize LOGO! pulse handler.");

        config = getConfigAs(PLCPulseConfiguration.class);

        super.doInitialization();
        if (ThingStatus.OFFLINE != thing.getStatus()) {
            ThingBuilder tBuilder = editThing();

            var label = thing.getLabel();
            if (label == null) {
                Bridge bridge = getBridge();
                label = (bridge == null) || (bridge.getLabel() == null) ? "Siemens Logo!" : bridge.getLabel();
                label += (": digital pulse in/output");
            }
            tBuilder.withLabel(label);

            final var bName = config.getBlockName();
            final var bType = config.getChannelType();
            final var uid = new ChannelUID(thing.getUID(), STATE_CHANNEL);
            var cBuilder = ChannelBuilder.create(uid, bType);
            cBuilder.withType(new ChannelTypeUID(BINDING_ID, bType.toLowerCase()));
            cBuilder.withLabel(bName);
            cBuilder.withDescription("Control block " + bName);
            cBuilder.withProperties(Map.of(BLOCK_PROPERTY, bName));
            tBuilder.withChannel(cBuilder.build());
            setOldValue(STATE_CHANNEL, null);

            final var oName = config.getObservedBlock();
            final var oType = config.getObservedChannelType();
            cBuilder = ChannelBuilder.create(new ChannelUID(thing.getUID(), OBSERVE_CHANNEL), oType);
            cBuilder.withType(new ChannelTypeUID(BINDING_ID, oType.toLowerCase()));
            cBuilder.withLabel(oName);
            cBuilder.withDescription("Observed block " + oName);
            cBuilder.withProperties(Map.of(BLOCK_PROPERTY, oName));
            tBuilder.withChannel(cBuilder.build());
            setOldValue(OBSERVE_CHANNEL, null);

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
            } else if (parts.length == 1) {
                if (Character.isDigit(parts[0].charAt(1))) {
                    bit = Integer.parseInt(parts[0].substring(1));
                } else if (Character.isDigit(parts[0].charAt(2))) {
                    bit = Integer.parseInt(parts[0].substring(2));
                } else if (Character.isDigit(parts[0].charAt(3))) {
                    bit = Integer.parseInt(parts[0].substring(3));
                }
                bit = (bit - 1) % 8;
            }
        } else {
            logger.info("Wrong configurated LOGO! block {} found.", name);
        }

        return bit;
    }

    private void updateChannel(final Channel channel, boolean value) {
        ChannelUID channelUID = channel.getUID();
        String type = channel.getAcceptedItemType();
        if (DIGITAL_INPUT_ITEM.equalsIgnoreCase(type)) {
            updateState(channelUID, value ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
        } else if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type)) {
            updateState(channelUID, OnOffType.from(value));
            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
        } else {
            logger.debug("Channel {} will not accept {} items.", channelUID, type);
        }
    }
}
