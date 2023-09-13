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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.openhab.binding.plclogo.internal.config.PLCMemoryConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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
 * The {@link PLCMemoryHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCMemoryHandler extends PLCCommonHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_MEMORY);

    private final Logger logger = LoggerFactory.getLogger(PLCMemoryHandler.class);
    private AtomicReference<PLCMemoryConfiguration> config = new AtomicReference<>();

    /**
     * Constructor.
     */
    public PLCMemoryHandler(Thing thing) {
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

        int address = getAddress(name);
        PLCLogoClient client = getLogoClient();
        if ((address != INVALID) && (client != null)) {
            String kind = getBlockKind();
            String type = channel.getAcceptedItemType();
            if (command instanceof RefreshType) {
                byte[] buffer = new byte[getBufferLength()];
                int result = client.readDBArea(1, 0, buffer.length, S7Client.S7WLByte, buffer);
                if (result == 0) {
                    if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type) && MEMORY_BYTE.equalsIgnoreCase(kind)) {
                        boolean value = S7.GetBitAt(buffer, address, getBit(name));
                        updateState(channelUID, value ? OnOffType.ON : OnOffType.OFF);
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_BYTE.equalsIgnoreCase(kind)) {
                        int value = buffer[address];
                        updateState(channelUID, new DecimalType(value));
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_WORD.equalsIgnoreCase(kind)) {
                        int value = S7.GetShortAt(buffer, address);
                        updateState(channelUID, new DecimalType(value));
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_DWORD.equalsIgnoreCase(kind)) {
                        int value = S7.GetDIntAt(buffer, address);
                        updateState(channelUID, new DecimalType(value));
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    } else {
                        logger.debug("Channel {} will not accept {} items.", channelUID, type);
                    }
                } else {
                    logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else if (command instanceof DecimalType decimalCommand) {
                int length = MEMORY_BYTE.equalsIgnoreCase(kind) ? 1 : 2;
                byte[] buffer = new byte[MEMORY_DWORD.equalsIgnoreCase(kind) ? 4 : length];
                if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_BYTE.equalsIgnoreCase(kind)) {
                    buffer[0] = decimalCommand.byteValue();
                } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_WORD.equalsIgnoreCase(kind)) {
                    S7.SetShortAt(buffer, 0, decimalCommand.intValue());
                } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_DWORD.equalsIgnoreCase(kind)) {
                    S7.SetDIntAt(buffer, 0, decimalCommand.intValue());
                } else {
                    logger.debug("Channel {} will not accept {} items.", channelUID, type);
                }
                int result = client.writeDBArea(1, address, buffer.length, S7Client.S7WLByte, buffer);
                if (result != 0) {
                    logger.debug("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else if (command instanceof OnOffType onOffCommand) {
                byte[] buffer = new byte[1];
                if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type) && MEMORY_BYTE.equalsIgnoreCase(kind)) {
                    S7.SetBitAt(buffer, 0, 0, onOffCommand == OnOffType.ON);
                } else {
                    logger.debug("Channel {} will not accept {} items.", channelUID, type);
                }
                int result = client.writeDBArea(1, 8 * address + getBit(name), buffer.length, S7Client.S7WLBit, buffer);
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

        for (Channel channel : channels) {
            ChannelUID channelUID = channel.getUID();
            String name = getBlockFromChannel(channel);

            int address = getAddress(name);
            if (address != INVALID) {
                String kind = getBlockKind();
                String type = channel.getAcceptedItemType();
                Boolean force = config.get().isUpdateForced();

                if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type) && kind.equalsIgnoreCase(MEMORY_BYTE)) {
                    OnOffType state = (OnOffType) getOldValue(name);
                    OnOffType value = S7.GetBitAt(data, address, getBit(name)) ? OnOffType.ON : OnOffType.OFF;
                    if ((state == null) || (value != state) || force) {
                        updateState(channelUID, value);
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    }
                    if (logger.isTraceEnabled()) {
                        int buffer = (data[address] & 0xFF) + 0x100;
                        logger.trace("Channel {} received [{}].", channelUID,
                                Integer.toBinaryString(buffer).substring(1));
                    }
                } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_BYTE.equalsIgnoreCase(kind)) {
                    Integer threshold = config.get().getThreshold();
                    DecimalType state = (DecimalType) getOldValue(name);
                    int value = data[address];
                    if ((state == null) || (Math.abs(value - state.intValue()) > threshold) || force) {
                        updateState(channelUID, new DecimalType(value));
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Channel {} received [{}].", channelUID, data[address]);
                    }
                } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_WORD.equalsIgnoreCase(kind)) {
                    Integer threshold = config.get().getThreshold();
                    DecimalType state = (DecimalType) getOldValue(name);
                    int value = S7.GetShortAt(data, address);
                    if ((state == null) || (Math.abs(value - state.intValue()) > threshold) || force) {
                        updateState(channelUID, new DecimalType(value));
                        logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Channel {} received [{}, {}].", channelUID, data[address], data[address + 1]);
                    }
                } else if (ANALOG_ITEM.equalsIgnoreCase(type) && MEMORY_DWORD.equalsIgnoreCase(kind)) {
                    Integer threshold = config.get().getThreshold();
                    DecimalType state = (DecimalType) getOldValue(name);
                    int value = S7.GetDIntAt(data, address);
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

        Channel channel = thing.getChannel(channelUID.getId());
        setOldValue(getBlockFromChannel(channel), state);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config.set(getConfigAs(PLCMemoryConfiguration.class));
    }

    @Override
    protected boolean isValid(final String name) {
        if (3 <= name.length() && (name.length() <= 7)) {
            String kind = getBlockKind();
            if (Character.isDigit(name.charAt(2))) {
                boolean valid = MEMORY_BYTE.equalsIgnoreCase(kind) || MEMORY_WORD.equalsIgnoreCase(kind);
                return name.startsWith(kind) && (valid || MEMORY_DWORD.equalsIgnoreCase(kind));
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
        return 1;
    }

    @Override
    protected void doInitialization() {
        Thing thing = getThing();
        logger.debug("Initialize LOGO! memory handler.");

        config.set(getConfigAs(PLCMemoryConfiguration.class));

        super.doInitialization();
        if (ThingStatus.OFFLINE != thing.getStatus()) {
            String kind = getBlockKind();
            String name = config.get().getBlockName();
            boolean isDigital = MEMORY_BYTE.equalsIgnoreCase(kind) && (getBit(name) != INVALID);
            String text = isDigital ? "Digital" : "Analog";

            ThingBuilder tBuilder = editThing();

            String label = thing.getLabel();
            if (label == null) {
                Bridge bridge = getBridge();
                label = (bridge == null) || (bridge.getLabel() == null) ? "Siemens Logo!" : bridge.getLabel();
                label += (": " + text.toLowerCase() + " in/output");
            }
            tBuilder.withLabel(label);

            String type = config.get().getChannelType();
            ChannelUID uid = new ChannelUID(thing.getUID(), isDigital ? STATE_CHANNEL : VALUE_CHANNEL);
            ChannelBuilder cBuilder = ChannelBuilder.create(uid, type);
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
