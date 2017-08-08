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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.plclogo.config.PLCLogoAnalogConfiguration;
import org.openhab.binding.plclogo.internal.PLCLogoClient;
import org.openhab.binding.plclogo.internal.PLCLogoDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Moka7.S7;
import Moka7.S7Client;

/**
 * The {@link PLCAnalogBlockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public class PLCAnalogBlockHandler extends PLCBlockHandler {

    private final Logger logger = LoggerFactory.getLogger(PLCAnalogBlockHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_ANALOG);

    private PLCLogoAnalogConfiguration config = getConfigAs(PLCLogoAnalogConfiguration.class);
    private long oldValue = Long.MAX_VALUE;

    /**
     * Constructor.
     */
    public PLCAnalogBlockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} on channel {}", command, channelUID);

        final Bridge bridge = getBridge();
        Objects.requireNonNull(bridge, "PLCAnalogBlockHandler: Bridge may not be null.");

        final Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCAnalogBlockHandler: Thing may not be null.");
        if ((ThingStatus.ONLINE != thing.getStatus()) || (ThingStatus.ONLINE != bridge.getStatus())) {
            return;
        }

        final PLCLogoClient client = getClient();
        final String channelId = channelUID.getId();
        if (!ANALOG_CHANNEL_ID.equals(channelId) || (client == null)) {
            logger.warn("Can not update channel {}: {}.", channelUID, client);
            return;
        }

        final String name = getBlockName();
        final PLCLogoDataType type = getBlockDataType();
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else if (command instanceof DecimalType) {
            final int offset = type.getByteCount();
            if (((offset == 2) || (offset == 4)) && (name != null)) {
                final byte[] buffer = new byte[offset];
                final DecimalType state = (DecimalType) command;
                if (offset == 2) {
                    S7.SetShortAt(buffer, 0, state.intValue());
                } else {
                    S7.SetDWordAt(buffer, 0, state.longValue());
                }
                int result = client.writeDBArea(1, getAddress(), buffer.length, S7Client.S7WLByte, buffer);
                if (result != 0) {
                    logger.warn("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else {
                logger.warn("Invalid block {} found.", name);
            }
        } else if (command instanceof DateTimeType) {
            final int offset = type.getByteCount();
            if ((offset == 2) && (name != null)) {
                final byte[] buffer = new byte[offset];
                final DateTimeType state = (DateTimeType) command;
                if (ANALOG_TIME_CHANNEL.equalsIgnoreCase(config.getType())) {
                    final Calendar calendar = state.getCalendar();
                    buffer[0] = S7.ByteToBCD(calendar.get(Calendar.HOUR_OF_DAY));
                    buffer[1] = S7.ByteToBCD(calendar.get(Calendar.MINUTE));
                } else if (ANALOG_DATE_CHANNEL.equalsIgnoreCase(config.getType())) {
                    final Calendar calendar = state.getCalendar();
                    buffer[0] = S7.ByteToBCD(calendar.get(Calendar.MONTH) + 1);
                    buffer[1] = S7.ByteToBCD(calendar.get(Calendar.DATE));
                }

                int result = client.writeDBArea(1, getAddress(), buffer.length, S7Client.S7WLByte, buffer);
                if (result != 0) {
                    logger.warn("Can not write data to LOGO!: {}.", S7Client.ErrorText(result));
                }
            } else {
                logger.warn("Invalid block {} found.", name);
            }
        } else {
            logger.debug("Not supported command {} received.", command);
        }
    }

    @Override
    public void initialize() {
        synchronized (config) {
            config = getConfigAs(PLCLogoAnalogConfiguration.class);
        }

        scheduler.execute(new Runnable() {
            @Override
            public void run() {
                doInitialization();
            }
        });
    }

    @Override
    public void dispose() {
        logger.debug("Dispose LOGO! {} analog handler.", getBlockName());
        super.dispose();

        oldValue = Long.MAX_VALUE;
    }

    @Override
    public void setData(final byte[] data) {
        final Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCAnalogBlockHandler: Thing may not be null.");
        if (ThingStatus.ONLINE != thing.getStatus()) {
            return;
        }

        if ((data.length == 2) || (data.length == 4)) {
            final Channel channel = thing.getChannel(ANALOG_CHANNEL_ID);
            final long value = data.length == 2 ? S7.GetShortAt(data, 0) : S7.GetDWordAt(data, 0);

            final String type = channel.getAcceptedItemType();
            if (logger.isTraceEnabled()) {
                final String raw = Arrays.toString(data);
                logger.trace("Channel {} accepting {} received {}.", channel.getUID(), type, raw);
            }

            if ((Math.abs(oldValue - value) > config.getThreshold()) || config.isUpdateForced()) {
                if (type.equalsIgnoreCase("Number")) {
                    updateState(channel.getUID(), new DecimalType(value));
                } else if (type.equalsIgnoreCase("DateTime") && (data.length == 2)) {
                    PLCBridgeHandler bridge = (PLCBridgeHandler) getBridge().getHandler();
                    Calendar calendar = (Calendar) bridge.getLogoRTC().clone();
                    calendar.set(Calendar.MILLISECOND, 0);
                    if (ANALOG_TIME_CHANNEL.equalsIgnoreCase(config.getType())) {
                        if ((value < 0) || (value > 0x2359)) {
                            logger.warn("Channel {} got garbage time {}.", channel.getUID(), Long.toHexString(value));
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, S7.BCDtoByte(data[0]));
                        calendar.set(Calendar.MINUTE, S7.BCDtoByte(data[1]));
                    } else if (ANALOG_DATE_CHANNEL.equalsIgnoreCase(config.getType())) {
                        if ((value < 0x0101) || (value > 0x1231)) {
                            logger.warn("Channel {} got garbage date {}.", channel.getUID(), Long.toHexString(value));
                        }
                        calendar.set(Calendar.MONTH, S7.BCDtoByte(data[0]) - 1);
                        calendar.set(Calendar.DATE, S7.BCDtoByte(data[1]));
                    }
                    updateState(channel.getUID(), new DateTimeType(calendar));
                } else {
                    logger.warn("Channel {} will not accept {} items.", channel.getUID(), type);
                }
                logger.debug("Channel {} accepting {} was set to {}.", channel.getUID(), type, value);

                oldValue = value;
            }
        } else {
            logger.warn("Block {} received wrong data {}.", getBlockName(), data);
        }
    }

    @Override
    public String getBlockName() {
        return config.getBlockName();
    }

    @Override
    public PLCLogoDataType getBlockDataType() {
        final String name = getBlockName();
        final String kind = config.getBlockKind(name);
        if ((kind != null) && config.isBlockValid(name)) {
            return kind.equalsIgnoreCase("VD") ? PLCLogoDataType.DWORD : PLCLogoDataType.WORD;
        }
        return PLCLogoDataType.INVALID;
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        synchronized (config) {
            config = getConfigAs(PLCLogoAnalogConfiguration.class);
        }
    }

    @Override
    protected void doInitialization() {
        final Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCAnalogBlockHandler: Thing may not be null.");

        final Bridge bridge = getBridge();
        Objects.requireNonNull(bridge, "PLCAnalogBlockHandler: Bridge may not be null.");

        final String name = getBlockName();
        logger.debug("Initialize LOGO! {} analog handler.", name);

        if (config.isBlockValid(name)) {
            ThingBuilder tBuilder = editThing();

            String text = config.isInputBlock(name) ? INPUT_CHANNEL : OUTPUT_CHANNEL;
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
            tBuilder = tBuilder.withLabel(bridge.getLabel() + ": " + text + " " + name);

            final Channel channel = thing.getChannel(ANALOG_CHANNEL_ID);
            if (channel != null) {
                tBuilder.withoutChannel(channel.getUID());
            }

            final String type = config.getItemType();
            final ChannelUID uid = new ChannelUID(thing.getUID(), ANALOG_CHANNEL_ID);
            ChannelBuilder cBuilder = ChannelBuilder.create(uid, type);
            cBuilder = cBuilder.withType(new ChannelTypeUID(BINDING_ID, type.toLowerCase()));
            cBuilder = cBuilder.withLabel(name);
            cBuilder = cBuilder.withDescription("Analog " + text);
            tBuilder = tBuilder.withChannel(cBuilder.build());

            oldValue = Long.MAX_VALUE;
            updateThing(tBuilder.build());
            super.doInitialization();
        } else {
            final String message = "Can not initialize LOGO! block " + name + ".";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            logger.error("Can not initialize thing {} for LOGO! block {}.", thing.getUID(), name);
        }
    }

    @Override
    protected int getAddress(final String name) {
        int address = INVALID;

        logger.debug("Get address of {} LOGO! for block {} .", getLogoFamily(), name);

        if (config.isBlockValid(name)) {
            final String block = name.trim().split("\\.")[0];
            if (Character.isDigit(block.charAt(2))) {
                address = Integer.parseInt(block.substring(2));
            } else if (Character.isDigit(block.charAt(3))) {
                address = Integer.parseInt(block.substring(3));
            }

            final int base = getBase(name);
            if (base != 0) { // Only VB/VD/VW memory ranges are 0 based
                address = base + (address - 1) * 2;
            }
        } else {
            logger.warn("Wrong configurated LOGO! block {} found.", name);
        }
        return address;
    }

    @Override
    protected int getBit(final String name) {
        logger.debug("Get bit of {} LOGO! for block {} .", getLogoFamily(), name);

        return 0;
    }

    /**
     * Calculate address offset for given block name.
     *
     * @param name Name of the data block
     * @return Calculated address offset
     */
    private int getBase(final String name) {
        int base = 0;

        logger.debug("Get base address of {} LOGO! for block {} .", getLogoFamily(), name);

        final String block = name.trim().split("\\.")[0];
        final Map<?, Integer> family = LOGO_MEMORY_BLOCK.get(getLogoFamily());
        if (Character.isDigit(block.charAt(2))) {
            base = family.get(block.substring(0, 2));
        } else if (Character.isDigit(block.charAt(3))) {
            base = family.get(block.substring(0, 3));
        }
        return base;
    }

}
