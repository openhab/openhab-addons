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
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.DATE_TIME_ITEM;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.MEMORY_WORD;
import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.VALUE_CHANNEL;

import java.time.ZoneId;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plclogo.internal.config.PLCDateTimeConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
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
 * The {@link PLCDateTimeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCDateTimeHandler extends PLCCommonHandler {

    private final Logger logger = LoggerFactory.getLogger(PLCDateTimeHandler.class);
    private volatile @NonNullByDefault({}) PLCDateTimeConfiguration config;

    /**
     * Constructor.
     */
    public PLCDateTimeHandler(Thing thing) {
        super(thing);
        config = getConfigAs(PLCDateTimeConfiguration.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!isThingOnline()) {
            return;
        }

        final var channel = getThing().getChannel(channelUID.getId());
        final var name = config.getBlockName();
        if (!isValid(name) || (channel == null)) {
            logger.debug("Can not update channel {}, block {}.", channelUID, name);
            return;
        }

        final var address = getAddress(name);
        final var client = getLogoClient();
        if ((address != INVALID) && (client != null)) {
            if (command instanceof RefreshType) {
                final var buffer = new byte[getBufferLength()];
                final var result = client.readDBArea(1, 0, buffer.length, S7Client.S7WLByte, buffer);
                if (result == 0) {
                    updateChannel(channel, S7.GetShortAt(buffer, address));
                } else {
                    logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else if (command instanceof DateTimeType dateTimeCommand) {
                final var buffer = new byte[2];
                final var type = channel.getAcceptedItemType();
                if (DATE_TIME_ITEM.equalsIgnoreCase(type)) {
                    final var datetime = dateTimeCommand.getZonedDateTime(ZoneId.systemDefault());
                    if ("Time".equalsIgnoreCase(channelUID.getId())) {
                        buffer[0] = S7.ByteToBCD(datetime.getHour());
                        buffer[1] = S7.ByteToBCD(datetime.getMinute());
                    } else if ("Date".equalsIgnoreCase(channelUID.getId())) {
                        buffer[0] = S7.ByteToBCD(datetime.getMonthValue());
                        buffer[1] = S7.ByteToBCD(datetime.getDayOfMonth());
                    }
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

        final var channels = getThing().getChannels();
        if (channels.size() != getNumberOfChannels()) {
            logger.info("Received and configured channel sizes does not match.");
            return;
        }

        final var name = config.getBlockName();
        final var force = config.isUpdateForced();
        for (Channel channel : channels) {
            final var address = getAddress(name);
            if (address != INVALID) {
                final var state = (DecimalType) getOldValue(name);
                final var value = S7.GetShortAt(data, address);
                if ((state == null) || (value != state.intValue()) || force) {
                    updateChannel(channel, value);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Channel {} received [{}, {}].", channel.getUID(), data[address], data[address + 1]);
                }
            } else {
                logger.info("Invalid channel {} found.", channel.getUID());
            }
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
        if (state instanceof DecimalType) {
            setOldValue(config.getBlockName(), state);
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config = getConfigAs(PLCDateTimeConfiguration.class);
    }

    @Override
    protected boolean isValid(final String name) {
        if ((3 <= name.length()) && (name.length() <= 5)) {
            final var kind = getBlockKind();
            if (Character.isDigit(name.charAt(2))) {
                return name.startsWith(kind) && MEMORY_WORD.equalsIgnoreCase(kind);
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
    protected void doInitialization() {
        final var thing = getThing();
        logger.debug("Initialize LOGO! date/time handler.");

        config = getConfigAs(PLCDateTimeConfiguration.class);

        super.doInitialization();
        if (ThingStatus.OFFLINE != thing.getStatus()) {
            final var block = config.getBlockType();
            final var text = "Time".equalsIgnoreCase(block) ? "Time" : "Date";

            final var tBuilder = editThing();

            var label = thing.getLabel();
            if (label == null) {
                Bridge bridge = getBridge();
                label = (bridge == null) || (bridge.getLabel() == null) ? "Siemens Logo!" : bridge.getLabel();
                label += (": " + text.toLowerCase() + " in/output");
            }
            tBuilder.withLabel(label);

            final var name = config.getBlockName();
            final var type = config.getChannelType();
            final var uid = new ChannelUID(thing.getUID(), "Time".equalsIgnoreCase(block) ? "time" : "date");
            var cBuilder = ChannelBuilder.create(uid, type);
            cBuilder.withType(new ChannelTypeUID(BINDING_ID, type.toLowerCase()));
            cBuilder.withLabel(name);
            cBuilder.withDescription(text + " block parameter " + name);
            cBuilder.withProperties(Map.of(BLOCK_PROPERTY, name));
            tBuilder.withChannel(cBuilder.build());

            cBuilder = ChannelBuilder.create(new ChannelUID(thing.getUID(), VALUE_CHANNEL), ANALOG_ITEM);
            cBuilder.withType(new ChannelTypeUID(BINDING_ID, ANALOG_ITEM.toLowerCase()));
            cBuilder.withLabel(name);
            cBuilder.withDescription(text + " block parameter " + name);
            cBuilder.withProperties(Map.of(BLOCK_PROPERTY, name));
            tBuilder.withChannel(cBuilder.build());
            setOldValue(name, null);

            updateThing(tBuilder.build());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void updateChannel(final Channel channel, int value) {
        final var channelUID = channel.getUID();
        final var handler = getBridgeHandler();
        if (handler == null) {
            final var name = config.getBlockName();
            logger.debug("Can not update channel {}, block {}.", channelUID, name);
            return;
        }

        final var type = channel.getAcceptedItemType();
        if (DATE_TIME_ITEM.equalsIgnoreCase(type)) {
            final var channelId = channelUID.getId();
            var datetime = handler.getLogoRTC();

            final var data = new byte[2];
            S7.SetShortAt(data, 0, value);
            if ("Time".equalsIgnoreCase(channelId)) {
                if ((value < 0x0) || (value > 0x2359)) {
                    logger.debug("Channel {} got garbage time {}.", channelUID, Long.toHexString(value));
                }
                datetime = datetime.withHour(S7.BCDtoByte(data[0]));
                datetime = datetime.withMinute(S7.BCDtoByte(data[1]));
            } else if ("Date".equalsIgnoreCase(channelId)) {
                if ((value < 0x0101) || (value > 0x1231)) {
                    logger.debug("Channel {} got garbage date {}.", channelUID, Long.toHexString(value));
                }
                datetime = datetime.withMonth(S7.BCDtoByte(data[0]));
                datetime = datetime.withDayOfMonth(S7.BCDtoByte(data[1]));
            } else {
                logger.debug("Channel {} has wrong id {}.", channelUID, channelId);
            }
            updateState(channelUID, new DateTimeType(datetime));
            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, datetime);
        } else if (ANALOG_ITEM.equalsIgnoreCase(type)) {
            updateState(channelUID, new DecimalType(value));
            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
        } else {
            logger.debug("Channel {} will not accept {} items.", channelUID, type);
        }
    }
}
