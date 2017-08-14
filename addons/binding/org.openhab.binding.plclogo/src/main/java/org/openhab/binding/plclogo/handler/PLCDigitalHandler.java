/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.handler;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.plclogo.config.PLCDigitalConfiguration;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
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
public class PLCDigitalHandler extends PLCCommonHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DIGITAL);

    private final Logger logger = LoggerFactory.getLogger(PLCDigitalHandler.class);
    private PLCDigitalConfiguration config = getConfigAs(PLCDigitalConfiguration.class);

    @SuppressWarnings("serial")
    private static final Map<?, Integer> LOGO_BLOCKS_0BA7 = Collections.unmodifiableMap(new TreeMap<String, Integer>() {
        {
            // @formatter:off
            put("I", 24);  // 24 digital inputs
            put("Q", 16);  // 16 digital outputs
            put("M", 27);  // 27 digital markers
            // @formatter:on
        }
    });

    @SuppressWarnings("serial")
    private static final Map<?, Integer> LOGO_BLOCKS_0BA8 = Collections.unmodifiableMap(new TreeMap<String, Integer>() {
        {
            // @formatter:off
            put( "I", 24);  // 24 digital inputs
            put( "Q", 20);  // 20 digital outputs
            put( "M", 64);  // 64 digital markers
            put("NI", 64);  // 64 network inputs
            put("NQ", 64);  // 64 network outputs
            // @formatter:on
        }
    });

    @SuppressWarnings("serial")
    private static final Map<?, Map<?, Integer>> LOGO_BLOCK_NUMBER = Collections
            .unmodifiableMap(new TreeMap<String, Map<?, Integer>>() {
                {
                    put(LOGO_0BA7, LOGO_BLOCKS_0BA7);
                    put(LOGO_0BA8, LOGO_BLOCKS_0BA8);
                }
            });

    /**
     * Constructor.
     */
    public PLCDigitalHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (!isThingOnline()) {
            return;
        }

        final String channelId = channelUID.getId();
        Objects.requireNonNull(channelId, "PLCDigitalHandler: Invalid channel id found.");

        final PLCLogoClient client = getLogoClient();
        final Channel channel = thing.getChannel(channelId);
        if (!isValid(channelId) || (channel == null)) {
            logger.warn("Can not update channel {}: {}.", channelUID, client);
            return;
        }

        final int bit = getBit(channelId);
        final int address = getAddress(channelId);
        if ((address != INVALID) && (bit != INVALID)) {
            if (command instanceof RefreshType) {
                final int base = getBase(channelId);
                final byte[] buffer = new byte[getBufferLength()];
                int result = client.readDBArea(1, base, buffer.length, S7Client.S7WLByte, buffer);
                if (result == 0) {
                    updateChannel(channel, S7.GetBitAt(buffer, address - base, bit));
                } else {
                    logger.warn("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else if ((command instanceof OpenClosedType) || (command instanceof OnOffType)) {
                final byte[] buffer = new byte[1];
                final String type = channel.getAcceptedItemType();
                if (type.equalsIgnoreCase("Contact")) {
                    S7.SetBitAt(buffer, 0, 0, ((OpenClosedType) command) == OpenClosedType.CLOSED);
                } else if (type.equalsIgnoreCase("Switch")) {
                    S7.SetBitAt(buffer, 0, 0, ((OnOffType) command) == OnOffType.ON);
                } else {
                    logger.warn("Channel {} will not accept {} items.", channelUID, type);
                }
                int result = client.writeDBArea(1, 8 * address + bit, buffer.length, S7Client.S7WLBit, buffer);
                if (result != 0) {
                    logger.warn("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else {
                logger.warn("Channel {} received not supported command {}.", channelUID, command);
            }
        } else {
            logger.warn("Invalid channel {} found.", channelUID);
        }
    }

    @Override
    public void setData(final byte[] data) {
        if (!isThingOnline()) {
            return;
        }

        if (data.length != getBufferLength()) {
            logger.warn("Received and configured data sizes does not match.");
            return;
        }

        final List<Channel> channels = thing.getChannels();
        if (channels.size() != getNumberOfChannels()) {
            logger.warn("Received and configured channels sizes does not match.");
            return;
        }

        for (final Channel channel : channels) {
            final ChannelUID channelUID = channel.getUID();
            Objects.requireNonNull(channelUID, "PLCDigitalHandler: Invalid channel uid found.");

            final String channelId = channelUID.getId();
            Objects.requireNonNull(channelId, "PLCDigitalHandler: Invalid channel id found.");

            final int bit = getBit(channelId);
            final int address = getAddress(channelId);
            if ((address != INVALID) && (bit != INVALID)) {
                final DecimalType state = (DecimalType) getOldValue(channelId);
                boolean value = S7.GetBitAt(data, address - getBase(channelId), bit);
                if ((state == null) || ((value ? 1 : 0) != state.intValue()) || config.isUpdateForced()) {
                    updateChannel(channel, value);
                }
            } else {
                logger.warn("Invalid channel {} found.", channelUID);
            }
        }

        /*
         * if (logger.isTraceEnabled()) {
         * final String raw = "[" + Integer.toBinaryString((data[0] & 0xFF) + 0x100).substring(1) + "]";
         * logger.trace("Channel {} accepting {} received {}.", channel.getUID(), type, raw);
         * }
         */
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
        DecimalType value = (DecimalType) state.as(DecimalType.class);
        if (state instanceof OpenClosedType) {
            final OpenClosedType type = (OpenClosedType) state;
            value = new DecimalType(type == OpenClosedType.CLOSED ? 1 : 0);
        }
        setOldValue(channelUID.getId(), value);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        synchronized (config) {
            config = getConfigAs(PLCDigitalConfiguration.class);
        }
    }

    @Override
    protected boolean isValid(final @NonNull String name) {
        if (2 <= name.length() && (name.length() <= 4)) {
            final String kind = config.getBlockKind();
            if (Character.isDigit(name.charAt(1))) {
                return kind.equalsIgnoreCase(name.substring(0, 1));
            } else if (Character.isDigit(name.charAt(2))) {
                return kind.equalsIgnoreCase(name.substring(0, 2));
            }
        }
        return false;
    }

    @Override
    protected @NonNull String getBlockKind() {
        return config.getBlockKind();
    }

    @Override
    protected int getNumberOfChannels() {
        final String family = getLogoFamily();
        final String kind = config.getBlockKind();
        logger.debug("Get block number of {} LOGO! for {} blocks.", family, kind);

        return LOGO_BLOCK_NUMBER.get(family).get(kind).intValue();
    }

    @Override
    protected int getAddress(final @NonNull String name) {
        int address = super.getAddress(name);
        if (address != INVALID) {
            address = getBase(name) + (address - 1) / 8;
        } else {
            logger.warn("Wrong configurated LOGO! block {} found.", name);
        }
        return address;
    }

    @Override
    protected void doInitialization() {
        final Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCDigitalHandler: Thing may not be null.");

        logger.debug("Initialize LOGO! digital input blocks handler.");

        synchronized (config) {
            config = getConfigAs(PLCDigitalConfiguration.class);
        }

        super.doInitialization();
        if (ThingStatus.OFFLINE != thing.getStatus()) {
            final String kind = config.getBlockKind();
            final String text = kind.equalsIgnoreCase("I") || kind.equalsIgnoreCase("NI") ? "input" : "output";

            ThingBuilder tBuilder = editThing();
            tBuilder.withLabel(getBridge().getLabel() + ": digital " + text + "s");

            final String type = config.getChannelType();
            for (int i = 0; i < getNumberOfChannels(); i++) {
                final String name = kind + String.valueOf(i + 1);
                final ChannelUID uid = new ChannelUID(thing.getUID(), name);
                ChannelBuilder cBuilder = ChannelBuilder.create(uid, type);
                cBuilder.withType(new ChannelTypeUID(BINDING_ID, type.toLowerCase()));
                cBuilder.withLabel(name);
                cBuilder.withDescription("Digital " + text + " block " + name);
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
    private int getBit(final @NonNull String name) {
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
            logger.warn("Wrong configurated LOGO! block {} found.", name);
        }
        return bit;
    }

    private void updateChannel(final @NonNull Channel channel, boolean value) {
        final ChannelUID channelUID = channel.getUID();
        Objects.requireNonNull(channelUID, "PLCDigitalHandler: Invalid channel uid found.");

        final String type = channel.getAcceptedItemType();
        if (type.equalsIgnoreCase("Contact")) {
            updateState(channelUID, value ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
        } else if (type.equalsIgnoreCase("Switch")) {
            updateState(channelUID, value ? OnOffType.ON : OnOffType.OFF);
            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
        } else {
            logger.warn("Channel {} will not accept {} items.", channelUID, type);
        }
    }

}
