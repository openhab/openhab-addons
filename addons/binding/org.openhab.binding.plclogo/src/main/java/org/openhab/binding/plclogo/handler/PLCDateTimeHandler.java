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

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
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
import org.openhab.binding.plclogo.config.PLCDateTimeConfiguration;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
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
public class PLCDateTimeHandler extends PLCCommonHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DATETIME);

    private final Logger logger = LoggerFactory.getLogger(PLCDateTimeHandler.class);
    private PLCDateTimeConfiguration config = getConfigAs(PLCDateTimeConfiguration.class);

    private static final String DATE_TIME_ITEM_TYPE = "DateTime";
    private static final String RAW_VALUE_ITEM_TYPE = "Number";

    /**
     * Constructor.
     */
    public PLCDateTimeHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (!isThingOnline()) {
            return;
        }

        final String channelId = channelUID.getId();
        Objects.requireNonNull(channelId, "PLCDateTimeHandler: Invalid channel id found.");

        final PLCLogoClient client = getLogoClient();
        final Channel channel = thing.getChannel(channelId);
        if (!isValid(config.getBlockName()) || (channel == null)) {
            logger.warn("Can not update channel {}: {}.", channelUID, client);
            return;
        }

        final int address = getAddress(config.getBlockName());
        if (address != INVALID) {
            if (command instanceof RefreshType) {
                final byte[] buffer = new byte[getBufferLength()];
                int result = client.readDBArea(1, 0, buffer.length, S7Client.S7WLByte, buffer);
                if (result == 0) {
                    updateChannel(channel, S7.GetShortAt(buffer, address));
                } else {
                    logger.warn("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else if (command instanceof DateTimeType) {
                final byte[] buffer = new byte[2];
                final String type = channel.getAcceptedItemType();
                if (type.equalsIgnoreCase(DATE_TIME_ITEM_TYPE)) {
                    final ZonedDateTime datetime = ((DateTimeType) command).getZonedDateTime();
                    if (channelId.equalsIgnoreCase("Time")) {
                        buffer[0] = S7.ByteToBCD(datetime.getHour());
                        buffer[1] = S7.ByteToBCD(datetime.getMinute());
                    } else if (channelId.equalsIgnoreCase("Date")) {
                        buffer[0] = S7.ByteToBCD(datetime.getMonthValue());
                        buffer[1] = S7.ByteToBCD(datetime.getDayOfMonth());
                    }
                } else {
                    logger.warn("Channel {} will not accept {} items.", channelUID, type);
                }
                int result = client.writeDBArea(1, address, buffer.length, S7Client.S7WLByte, buffer);
                if (result != 0) {
                    logger.warn("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else {
                logger.warn("Channel {} received not supported command {}.", channelUID, command);
            }
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

        final int address = getAddress(config.getBlockName());
        for (final Channel channel : channels) {
            final ChannelUID channelUID = channel.getUID();
            Objects.requireNonNull(channelUID, "PLCDateTimeHandler: Invalid channel uid found.");

            if (address != INVALID) {
                final DecimalType state = (DecimalType) getOldValue(config.getBlockName());
                int value = S7.GetShortAt(data, address);
                if ((state == null) || (value != state.intValue()) || config.isUpdateForced()) {
                    updateChannel(channel, value);
                }
            } else {
                logger.warn("Invalid channel {} found.", channelUID);
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
        synchronized (config) {
            config = getConfigAs(PLCDateTimeConfiguration.class);
        }
    }

    @Override
    protected boolean isValid(final @NonNull String name) {
        if (3 <= name.length() && (name.length() <= 5)) {
            final String kind = config.getBlockKind();
            if (Character.isDigit(name.charAt(2)) && kind.equalsIgnoreCase(name.substring(0, 2))) {
                return kind.equalsIgnoreCase("VW");
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
        return 2;
    }

    @Override
    protected void doInitialization() {
        final Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCDateTimeHandler: Thing may not be null.");

        logger.debug("Initialize LOGO! {} date/time handler.");

        synchronized (config) {
            config = getConfigAs(PLCDateTimeConfiguration.class);
        }

        super.doInitialization();
        if (ThingStatus.OFFLINE != thing.getStatus()) {
            final String block = config.getBlockType();
            String text = block.equalsIgnoreCase("Time") ? "Time" : "Date";

            ThingBuilder tBuilder = editThing();
            tBuilder.withLabel(getBridge().getLabel() + ": " + text.toLowerCase() + " in/output");

            final String name = config.getBlockName();
            final String type = config.getChannelType();
            final ChannelUID uid = new ChannelUID(thing.getUID(), block.equalsIgnoreCase("Time") ? "time" : "date");
            ChannelBuilder cBuilder = ChannelBuilder.create(uid, type);
            cBuilder.withType(new ChannelTypeUID(BINDING_ID, type.toLowerCase()));
            cBuilder.withLabel(name);
            cBuilder.withDescription(text + " block parameter " + name);
            tBuilder.withChannel(cBuilder.build());

            cBuilder = ChannelBuilder.create(new ChannelUID(thing.getUID(), "value"), RAW_VALUE_ITEM_TYPE);
            cBuilder.withType(new ChannelTypeUID(BINDING_ID, RAW_VALUE_ITEM_TYPE.toLowerCase()));
            cBuilder.withLabel(name);
            cBuilder.withDescription(text + " block parameter " + name);
            tBuilder.withChannel(cBuilder.build());
            setOldValue(name, null);

            updateThing(tBuilder.build());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void updateChannel(final @NonNull Channel channel, int value) {
        final ChannelUID channelUID = channel.getUID();
        Objects.requireNonNull(channelUID, "PLCDateTimeHandler: Invalid channel uid found.");

        final String type = channel.getAcceptedItemType();
        if (type.equalsIgnoreCase(DATE_TIME_ITEM_TYPE)) {
            final String channelId = channelUID.getId();
            Objects.requireNonNull(channelId, "PLCDateTimeHandler: Invalid channel id found.");

            final PLCBridgeHandler bridge = (PLCBridgeHandler) getBridge().getHandler();
            Objects.requireNonNull(bridge, "PLCDateTimeHandler: Invalid handler found.");
            ZonedDateTime datetime = ZonedDateTime.from(bridge.getLogoRTC());

            final byte[] data = new byte[2];
            S7.SetShortAt(data, 0, value);
            if (channelId.equalsIgnoreCase("Time")) {
                if ((value < 0) || (value > 0x2359)) {
                    logger.warn("Channel {} got garbage time {}.", channelUID, Long.toHexString(value));
                }
                datetime = datetime.withHour(S7.BCDtoByte(data[0]));
                datetime = datetime.withMinute(S7.BCDtoByte(data[1]));
            } else if (channelId.equalsIgnoreCase("Date")) {
                if ((value < 0x0101) || (value > 0x1231)) {
                    logger.warn("Channel {} got garbage date {}.", channelUID, Long.toHexString(value));
                }
                datetime = datetime.withMonth(S7.BCDtoByte(data[0]));
                datetime = datetime.withDayOfMonth(S7.BCDtoByte(data[1]));
            }
            updateState(channelUID, new DateTimeType(datetime));
            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, datetime);
        } else if (type.equalsIgnoreCase(RAW_VALUE_ITEM_TYPE)) {
            updateState(channelUID, new DecimalType(value));
            logger.debug("Channel {} accepting {} was set to {}.", channelUID, type, value);
        } else {
            logger.warn("Channel {} will not accept {} items.", channelUID, type);
        }
    }

}
