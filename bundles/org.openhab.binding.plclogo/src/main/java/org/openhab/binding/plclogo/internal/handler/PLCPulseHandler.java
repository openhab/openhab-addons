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
package org.openhab.binding.plclogo.internal.handler;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
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
import org.openhab.core.thing.ThingTypeUID;
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_PULSE);

    private final Logger logger = LoggerFactory.getLogger(PLCPulseHandler.class);
    private AtomicReference<PLCPulseConfiguration> config = new AtomicReference<>();
    private AtomicReference<@Nullable Boolean> received = new AtomicReference<>();

    /**
     * Constructor.
     */
    public PLCPulseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!isThingOnline()) {
            return;
        }

        Channel channel = getThing().getChannel(channelUID.getId());
        String name = getBlockFromChannel(channel);
        if (!isValid(name) || (channel == null)) {
            logger.debug("Can not update channel {}, block {}.", channelUID, name);
            return;
        }

        int bit = getBit(name);
        int address = getAddress(name);
        PLCLogoClient client = getLogoClient();
        if ((address != INVALID) && (bit != INVALID) && (client != null)) {
            byte[] buffer = new byte[1];
            if (command instanceof RefreshType) {
                int result = client.readDBArea(1, address, buffer.length, S7Client.S7WLByte, buffer);
                if (result == 0) {
                    updateChannel(channel, S7.GetBitAt(buffer, 0, bit));
                } else {
                    logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else if ((command instanceof OpenClosedType) || (command instanceof OnOffType)) {
                String type = channel.getAcceptedItemType();
                if (DIGITAL_INPUT_ITEM.equalsIgnoreCase(type)) {
                    boolean flag = ((OpenClosedType) command == OpenClosedType.CLOSED);
                    S7.SetBitAt(buffer, 0, 0, flag);
                    received.set(flag);
                } else if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type)) {
                    boolean flag = ((OnOffType) command == OnOffType.ON);
                    S7.SetBitAt(buffer, 0, 0, flag);
                    received.set(flag);
                } else {
                    logger.debug("Channel {} will not accept {} items.", channelUID, type);
                }
                int result = client.writeDBArea(1, 8 * address + bit, buffer.length, S7Client.S7WLBit, buffer);
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

        List<Channel> channels = thing.getChannels();
        if (channels.size() != getNumberOfChannels()) {
            logger.info("Received and configured channel sizes does not match.");
            return;
        }

        PLCLogoClient client = getLogoClient();
        for (Channel channel : channels) {
            ChannelUID channelUID = channel.getUID();
            String name = getBlockFromChannel(channel);

            int bit = getBit(name);
            int address = getAddress(name);
            if ((address != INVALID) && (bit != INVALID) && (client != null)) {
                DecimalType state = (DecimalType) getOldValue(channelUID.getId());
                if (STATE_CHANNEL.equalsIgnoreCase(channelUID.getId())) {
                    boolean value = S7.GetBitAt(data, address - getBase(name), bit);
                    if ((state == null) || ((value ? 1 : 0) != state.intValue())) {
                        updateChannel(channel, value);
                    }
                    if (logger.isTraceEnabled()) {
                        int buffer = (data[address - getBase(name)] & 0xFF) + 0x100;
                        logger.trace("Channel {} received [{}].", channelUID,
                                Integer.toBinaryString(buffer).substring(1));
                    }
                } else if (OBSERVE_CHANNEL.equalsIgnoreCase(channelUID.getId())) {
                    handleCommand(channelUID, RefreshType.REFRESH);
                    DecimalType current = (DecimalType) getOldValue(channelUID.getId());
                    if ((state != null) && (current.intValue() != state.intValue())) {
                        Integer pulse = config.get().getPulseLength();
                        scheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                Boolean value = received.getAndSet(null);
                                if (value != null) {
                                    byte[] buffer = new byte[1];
                                    S7.SetBitAt(buffer, 0, 0, !value.booleanValue());
                                    String block = config.get().getBlockName();
                                    int bit = 8 * getAddress(block) + getBit(block);
                                    int result = client.writeDBArea(1, bit, buffer.length, S7Client.S7WLBit, buffer);
                                    if (result != 0) {
                                        logger.debug("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                                    }
                                } else {
                                    logger.debug("Invalid received value on channel {}.", channelUID);
                                }
                            }
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
        DecimalType value = state.as(DecimalType.class);
        if (state instanceof OpenClosedType openClosedState) {
            value = new DecimalType(openClosedState == OpenClosedType.CLOSED ? 1 : 0);
        }

        setOldValue(channelUID.getId(), value);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config.set(getConfigAs(PLCPulseConfiguration.class));
    }

    @Override
    protected boolean isValid(final String name) {
        if (2 <= name.length() && (name.length() <= 7)) {
            String kind = config.get().getObservedBlockKind();
            if (Character.isDigit(name.charAt(1))) {
                boolean valid = I_DIGITAL.equalsIgnoreCase(kind) || Q_DIGITAL.equalsIgnoreCase(kind);
                return name.startsWith(kind) && (valid || M_DIGITAL.equalsIgnoreCase(kind));
            } else if (Character.isDigit(name.charAt(2))) {
                String bKind = getBlockKind();
                boolean valid = NI_DIGITAL.equalsIgnoreCase(kind) || NQ_DIGITAL.equalsIgnoreCase(kind);
                valid = name.startsWith(kind) && (valid || MEMORY_BYTE.equalsIgnoreCase(kind));
                return (name.startsWith(bKind) && MEMORY_BYTE.equalsIgnoreCase(bKind)) || valid;
            }
        }
        return false;
    }

    @Override
    protected String getBlockKind() {
        return config.get().getBlockKind();
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
        Thing thing = getThing();
        logger.debug("Initialize LOGO! pulse handler.");

        config.set(getConfigAs(PLCPulseConfiguration.class));

        super.doInitialization();
        if (ThingStatus.OFFLINE != thing.getStatus()) {
            ThingBuilder tBuilder = editThing();

            String label = thing.getLabel();
            if (label == null) {
                Bridge bridge = getBridge();
                label = (bridge == null) || (bridge.getLabel() == null) ? "Siemens Logo!" : bridge.getLabel();
                label += (": digital pulse in/output");
            }
            tBuilder.withLabel(label);

            String bName = config.get().getBlockName();
            String bType = config.get().getChannelType();
            ChannelUID uid = new ChannelUID(thing.getUID(), STATE_CHANNEL);
            ChannelBuilder cBuilder = ChannelBuilder.create(uid, bType);
            cBuilder.withType(new ChannelTypeUID(BINDING_ID, bType.toLowerCase()));
            cBuilder.withLabel(bName);
            cBuilder.withDescription("Control block " + bName);
            cBuilder.withProperties(Map.of(BLOCK_PROPERTY, bName));
            tBuilder.withChannel(cBuilder.build());
            setOldValue(STATE_CHANNEL, null);

            String oName = config.get().getObservedBlock();
            String oType = config.get().getObservedChannelType();
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
            updateState(channelUID, value ? OnOffType.ON : OnOffType.OFF);
            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
        } else {
            logger.debug("Channel {} will not accept {} items.", channelUID, type);
        }
    }
}
