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
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.I_ANALOG;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_0BA7;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.LOGO_0BA8;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.M_ANALOG;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.NI_ANALOG;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.NQ_ANALOG;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.Q_ANALOG;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plclogo.internal.config.PLCAnalogConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
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
 * The {@link PLCAnalogHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCAnalogHandler extends PLCCommonHandler {

    private final Logger logger = LoggerFactory.getLogger(PLCAnalogHandler.class);
    private volatile @NonNullByDefault({}) PLCAnalogConfiguration config;

    private static final Map<String, Integer> LOGO_BLOCKS_0BA7;
    static {
        Map<String, Integer> buffer = new HashMap<>();
        buffer.put(I_ANALOG, 8); // 8 analog inputs
        buffer.put(Q_ANALOG, 2); // 2 analog outputs
        buffer.put(M_ANALOG, 16); // 16 analog markers
        LOGO_BLOCKS_0BA7 = Collections.unmodifiableMap(buffer);
    }

    private static final Map<String, Integer> LOGO_BLOCKS_0BA8;
    static {
        Map<String, Integer> buffer = new HashMap<>();
        buffer.put(I_ANALOG, 8); // 8 analog inputs
        buffer.put(Q_ANALOG, 8); // 8 analog outputs
        buffer.put(M_ANALOG, 64); // 64 analog markers
        buffer.put(NI_ANALOG, 32); // 32 network analog inputs
        buffer.put(NQ_ANALOG, 16); // 16 network analog outputs
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
    public PLCAnalogHandler(Thing thing) {
        super(thing);
        config = getConfigAs(PLCAnalogConfiguration.class);
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
            if (command instanceof RefreshType) {
                final var base = getBase(name);
                final var buffer = new byte[getBufferLength()];
                final var result = client.readDBArea(1, base, buffer.length, S7Client.S7WLByte, buffer);
                if (result == 0) {
                    updateChannel(channel, S7.GetShortAt(buffer, address - base));
                } else {
                    logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else if (command instanceof DecimalType decimalCommand) {
                final var buffer = new byte[2];
                String type = channel.getAcceptedItemType();
                if (ANALOG_ITEM.equalsIgnoreCase(type)) {
                    S7.SetShortAt(buffer, 0, decimalCommand.intValue());
                } else {
                    logger.debug("Channel {} will not accept {} items.", channelUID, type);
                }
                final var result = client.writeDBArea(1, address, buffer.length, S7Client.S7WLByte, buffer);
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

        final var force = config.isUpdateForced();
        final var threshold = config.getThreshold();
        for (final var channel : channels) {
            final var channelUID = channel.getUID();
            final var name = getBlockFromChannel(channel);

            final var address = getAddress(name);
            if (address != INVALID) {
                final var state = (DecimalType) getOldValue(name);
                final var value = S7.GetShortAt(data, address - getBase(name));
                if ((state == null) || (Math.abs(value - state.intValue()) > threshold) || force) {
                    updateChannel(channel, value);
                }
                if (logger.isTraceEnabled()) {
                    int index = address - getBase(name);
                    logger.trace("Channel {} received [{}, {}].", channelUID, data[index], data[index + 1]);
                }
            } else {
                logger.info("Invalid channel {} found.", channelUID);
            }
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);

        final var channel = thing.getChannel(channelUID.getId());
        setOldValue(getBlockFromChannel(channel), state);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config = getConfigAs(PLCAnalogConfiguration.class);
    }

    @Override
    protected boolean isValid(final String name) {
        if ((3 <= name.length()) && (name.length() <= 5)) {
            final var kind = getBlockKind();
            if (Character.isDigit(name.charAt(2)) || Character.isDigit(name.charAt(3))) {
                var valid = I_ANALOG.equalsIgnoreCase(kind) || NI_ANALOG.equalsIgnoreCase(kind);
                valid = valid || Q_ANALOG.equalsIgnoreCase(kind) || NQ_ANALOG.equalsIgnoreCase(kind);
                return name.startsWith(kind) && (valid || M_ANALOG.equalsIgnoreCase(kind));
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
        final var kind = getBlockKind();
        final var family = getLogoFamily();
        logger.debug("Get block number of {} LOGO! for {} blocks.", family, kind);

        final var blocks = LOGO_BLOCK_NUMBER.get(family);
        final var number = (blocks != null) ? blocks.get(kind) : null;
        return (number != null) ? number : 0;
    }

    @Override
    protected int getAddress(final String name) {
        int address = super.getAddress(name);
        if (address != INVALID) {
            address = getBase(name) + (address - 1) * 2;
        } else {
            logger.info("Wrong configurated LOGO! block {} found.", name);
        }
        return address;
    }

    @Override
    protected void doInitialization() {
        final var thing = getThing();
        logger.debug("Initialize LOGO! analog input blocks handler.");

        config = getConfigAs(PLCAnalogConfiguration.class);

        super.doInitialization();
        if (ThingStatus.OFFLINE != thing.getStatus()) {
            final var kind = getBlockKind();
            final var text = I_ANALOG.equalsIgnoreCase(kind) || NI_ANALOG.equalsIgnoreCase(kind) ? "input" : "output";

            final var tBuilder = editThing();

            var label = thing.getLabel();
            if (label == null) {
                final var bridge = getBridge();
                label = (bridge == null) || (bridge.getLabel() == null) ? "Siemens Logo!" : bridge.getLabel();
                label += (": analog " + text + "s");
            }
            tBuilder.withLabel(label);

            final var type = config.getChannelType();
            for (int i = 0; i < getNumberOfChannels(); i++) {
                final var name = kind + String.valueOf(i + 1);
                final var uid = new ChannelUID(thing.getUID(), name);
                final var cBuilder = ChannelBuilder.create(uid, type);
                cBuilder.withType(new ChannelTypeUID(BINDING_ID, type.toLowerCase()));
                cBuilder.withLabel(name);
                cBuilder.withDescription("Analog " + text + " block " + name);
                cBuilder.withProperties(Map.of(BLOCK_PROPERTY, name));
                tBuilder.withChannel(cBuilder.build());
                setOldValue(name, null);
            }

            updateThing(tBuilder.build());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void updateChannel(final Channel channel, int value) {
        final var channelUID = channel.getUID();
        final var type = channel.getAcceptedItemType();
        if (ANALOG_ITEM.equalsIgnoreCase(type)) {
            updateState(channelUID, new DecimalType(value));
            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
        } else {
            logger.debug("Channel {} will not accept {} items.", channelUID, type);
        }
    }
}
