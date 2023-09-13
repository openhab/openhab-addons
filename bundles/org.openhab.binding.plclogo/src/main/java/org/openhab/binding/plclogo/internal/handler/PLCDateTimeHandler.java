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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.openhab.binding.plclogo.internal.config.PLCDateTimeConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
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
 * The {@link PLCDateTimeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCDateTimeHandler extends PLCCommonHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_DATETIME);

    private final Logger logger = LoggerFactory.getLogger(PLCDateTimeHandler.class);
    private AtomicReference<PLCDateTimeConfiguration> config = new AtomicReference<>();

    /**
     * Constructor.
     */
    public PLCDateTimeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!isThingOnline()) {
            return;
        }

        Channel channel = getThing().getChannel(channelUID.getId());
        String name = config.get().getBlockName();
        if (!isValid(name) || (channel == null)) {
            logger.debug("Can not update channel {}, block {}.", channelUID, name);
            return;
        }

        int address = getAddress(name);
        PLCLogoClient client = getLogoClient();
        if ((address != INVALID) && (client != null)) {
            if (command instanceof RefreshType) {
                byte[] buffer = new byte[getBufferLength()];
                int result = client.readDBArea(1, 0, buffer.length, S7Client.S7WLByte, buffer);
                if (result == 0) {
                    updateChannel(channel, S7.GetShortAt(buffer, address));
                } else {
                    logger.debug("Can not read data from LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else if (command instanceof DateTimeType dateTimeCommand) {
                byte[] buffer = new byte[2];
                String type = channel.getAcceptedItemType();
                if (DATE_TIME_ITEM.equalsIgnoreCase(type)) {
                    ZonedDateTime datetime = dateTimeCommand.getZonedDateTime();
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
                int result = client.writeDBArea(1, address, buffer.length, S7Client.S7WLByte, buffer);
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

        List<Channel> channels = getThing().getChannels();
        if (channels.size() != getNumberOfChannels()) {
            logger.info("Received and configured channel sizes does not match.");
            return;
        }

        String name = config.get().getBlockName();
        Boolean force = config.get().isUpdateForced();
        for (Channel channel : channels) {
            int address = getAddress(name);
            if (address != INVALID) {
                DecimalType state = (DecimalType) getOldValue(name);
                int value = S7.GetShortAt(data, address);
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
            setOldValue(config.get().getBlockName(), state);
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        config.set(getConfigAs(PLCDateTimeConfiguration.class));
    }

    @Override
    protected boolean isValid(final String name) {
        if (3 <= name.length() && (name.length() <= 5)) {
            String kind = getBlockKind();
            if (Character.isDigit(name.charAt(2))) {
                return name.startsWith(kind) && MEMORY_WORD.equalsIgnoreCase(kind);
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
    protected void doInitialization() {
        Thing thing = getThing();
        logger.debug("Initialize LOGO! date/time handler.");

        config.set(getConfigAs(PLCDateTimeConfiguration.class));

        super.doInitialization();
        if (ThingStatus.OFFLINE != thing.getStatus()) {
            String block = config.get().getBlockType();
            String text = "Time".equalsIgnoreCase(block) ? "Time" : "Date";

            ThingBuilder tBuilder = editThing();

            String label = thing.getLabel();
            if (label == null) {
                Bridge bridge = getBridge();
                label = (bridge == null) || (bridge.getLabel() == null) ? "Siemens Logo!" : bridge.getLabel();
                label += (": " + text.toLowerCase() + " in/output");
            }
            tBuilder.withLabel(label);

            String name = config.get().getBlockName();
            String type = config.get().getChannelType();
            ChannelUID uid = new ChannelUID(thing.getUID(), "Time".equalsIgnoreCase(block) ? "time" : "date");
            ChannelBuilder cBuilder = ChannelBuilder.create(uid, type);
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
        ChannelUID channelUID = channel.getUID();
        PLCBridgeHandler handler = getBridgeHandler();
        if (handler == null) {
            String name = config.get().getBlockName();
            logger.debug("Can not update channel {}, block {}.", channelUID, name);
            return;
        }

        String type = channel.getAcceptedItemType();
        if (DATE_TIME_ITEM.equalsIgnoreCase(type)) {
            String channelId = channelUID.getId();
            ZonedDateTime datetime = ZonedDateTime.from(handler.getLogoRTC());

            byte[] data = new byte[2];
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
