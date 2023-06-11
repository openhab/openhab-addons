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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.openhab.binding.plclogo.internal.config.PLCDigitalConfiguration;
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
 * The {@link PLCDigitalHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCDigitalHandler extends PLCCommonHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DIGITAL);

    private final Logger logger = LoggerFactory.getLogger(PLCDigitalHandler.class);
    private AtomicReference<PLCDigitalConfiguration> config = new AtomicReference<>();

    private static final Map<String, Integer> LOGO_BLOCKS_0BA7;
    static {
        Map<String, Integer> buffer = new HashMap<>();
        buffer.put(I_DIGITAL, 24); // 24 digital inputs
        buffer.put(Q_DIGITAL, 16); // 16 digital outputs
        buffer.put(M_DIGITAL, 27); // 27 digital markers
        LOGO_BLOCKS_0BA7 = Collections.unmodifiableMap(buffer);
    }

    private static final Map<String, Integer> LOGO_BLOCKS_0BA8;
    static {
        Map<String, Integer> buffer = new HashMap<>();
        buffer.put(I_DIGITAL, 24); // 24 digital inputs
        buffer.put(Q_DIGITAL, 20); // 20 digital outputs
        buffer.put(M_DIGITAL, 64); // 64 digital markers
        buffer.put(NI_DIGITAL, 64); // 64 network inputs
        buffer.put(NQ_DIGITAL, 64); // 64 network outputs
        LOGO_BLOCKS_0BA8 = Collections.unmodifiableMap(buffer);
    }

    private static final Map<String, Map<String, Integer>> LOGO_BLOCK_NUMBER;
    static {
        Map<String, Map<String, Integer>> buffer = new HashMap<>();
        buffer.put(LOGO_0BA7, LOGO_BLOCKS_0BA7);
        buffer.put(LOGO_0BA8, LOGO_BLOCKS_0BA8);
        LOGO_BLOCK_NUMBER = Collections.unmodifiableMap(buffer);
    }

    /**
     * Constructor.
     */
    public PLCDigitalHandler(Thing thing) {
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
            if (command instanceof RefreshType) {
                int base = getBase(name);
                byte[] buffer = new byte[getBufferLength()];
                int result = client.readDBArea(1, base, buffer.length, S7Client.S7WLByte, buffer);
                if (result == 0) {
                    updateChannel(channel, S7.GetBitAt(buffer, address - base, bit));
                } else {
                    logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else if ((command instanceof OpenClosedType) || (command instanceof OnOffType)) {
                byte[] buffer = new byte[1];
                String type = channel.getAcceptedItemType();
                if (DIGITAL_INPUT_ITEM.equalsIgnoreCase(type)) {
                    S7.SetBitAt(buffer, 0, 0, ((OpenClosedType) command) == OpenClosedType.CLOSED);
                } else if (DIGITAL_OUTPUT_ITEM.equalsIgnoreCase(type)) {
                    S7.SetBitAt(buffer, 0, 0, ((OnOffType) command) == OnOffType.ON);
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

        Boolean force = config.get().isUpdateForced();
        for (Channel channel : channels) {
            ChannelUID channelUID = channel.getUID();
            String name = getBlockFromChannel(channel);

            int bit = getBit(name);
            int address = getAddress(name);
            if ((address != INVALID) && (bit != INVALID)) {
                DecimalType state = (DecimalType) getOldValue(name);
                boolean value = S7.GetBitAt(data, address - getBase(name), bit);
                if ((state == null) || ((value ? 1 : 0) != state.intValue()) || force) {
                    updateChannel(channel, value);
                }
                if (logger.isTraceEnabled()) {
                    int buffer = (data[address - getBase(name)] & 0xFF) + 0x100;
                    logger.trace("Channel {} received [{}].", channelUID, Integer.toBinaryString(buffer).substring(1));
                }
            } else {
                logger.info("Invalid channel {} found.", channelUID);
            }
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
        DecimalType value = state.as(DecimalType.class);
        if (state instanceof OpenClosedType) {
            OpenClosedType type = (OpenClosedType) state;
            value = new DecimalType(type == OpenClosedType.CLOSED ? 1 : 0);
        }

        Channel channel = thing.getChannel(channelUID.getId());
        setOldValue(getBlockFromChannel(channel), value);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config.set(getConfigAs(PLCDigitalConfiguration.class));
    }

    @Override
    protected boolean isValid(final String name) {
        if (2 <= name.length() && (name.length() <= 4)) {
            String kind = getBlockKind();
            if (Character.isDigit(name.charAt(1)) || Character.isDigit(name.charAt(2))) {
                boolean valid = I_DIGITAL.equalsIgnoreCase(kind) || NI_DIGITAL.equalsIgnoreCase(kind);
                valid = valid || Q_DIGITAL.equalsIgnoreCase(kind) || NQ_DIGITAL.equalsIgnoreCase(kind);
                return name.startsWith(kind) && (valid || M_DIGITAL.equalsIgnoreCase(kind));
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
        String kind = getBlockKind();
        String family = getLogoFamily();
        logger.debug("Get block number of {} LOGO! for {} blocks.", family, kind);

        Map<?, Integer> blocks = LOGO_BLOCK_NUMBER.get(family);
        Integer number = (blocks != null) ? blocks.get(kind) : null;
        return (number != null) ? number.intValue() : 0;
    }

    @Override
    protected int getAddress(final String name) {
        int address = super.getAddress(name);
        if (address != INVALID) {
            address = getBase(name) + (address - 1) / 8;
        } else {
            logger.info("Wrong configurated LOGO! block {} found.", name);
        }
        return address;
    }

    @Override
    protected void doInitialization() {
        Thing thing = getThing();
        logger.debug("Initialize LOGO! digital input blocks handler.");

        config.set(getConfigAs(PLCDigitalConfiguration.class));

        super.doInitialization();
        if (ThingStatus.OFFLINE != thing.getStatus()) {
            String kind = getBlockKind();
            String type = config.get().getChannelType();
            String text = DIGITAL_INPUT_ITEM.equalsIgnoreCase(type) ? "input" : "output";

            ThingBuilder tBuilder = editThing();

            String label = thing.getLabel();
            if (label == null) {
                Bridge bridge = getBridge();
                label = (bridge == null) || (bridge.getLabel() == null) ? "Siemens Logo!" : bridge.getLabel();
                label += (": digital " + text + "s");
            }
            tBuilder.withLabel(label);

            for (int i = 0; i < getNumberOfChannels(); i++) {
                String name = kind + String.valueOf(i + 1);
                ChannelUID uid = new ChannelUID(thing.getUID(), name);
                ChannelBuilder cBuilder = ChannelBuilder.create(uid, type);
                cBuilder.withType(new ChannelTypeUID(BINDING_ID, type.toLowerCase()));
                cBuilder.withLabel(name);
                cBuilder.withDescription("Digital " + text + " block " + name);
                cBuilder.withProperties(Collections.singletonMap(BLOCK_PROPERTY, name));
                tBuilder.withChannel(cBuilder.build());
                setOldValue(name, null);
            }

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
            if (Character.isDigit(name.charAt(1))) {
                bit = Integer.parseInt(name.substring(1));
            } else if (Character.isDigit(name.charAt(2))) {
                bit = Integer.parseInt(name.substring(2));
            }
            bit = (bit - 1) % 8;
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
