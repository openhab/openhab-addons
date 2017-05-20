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
import org.openhab.binding.plclogo.PLCLogoBindingConstants;
import org.openhab.binding.plclogo.config.PLCLogoAnalogConfiguration;
import org.openhab.binding.plclogo.internal.PLCLogoDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Moka7.S7;

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
     * {@inheritDoc}
     */
    public PLCAnalogBlockHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void initialize() {
        final Thing thing = getThing();
        Objects.requireNonNull(thing, "PLCAnalogBlockHandler: Thing may not be null.");

        final Bridge bridge = getBridge();
        Objects.requireNonNull(bridge, "PLCAnalogBlockHandler: Bridge may not be null.");

        synchronized (config) {
            config = getConfigAs(PLCLogoAnalogConfiguration.class);
        }
        logger.debug("Initialize LOGO! {} analog handler.", config.getBlockName());

        final String name = config.getBlockName();
        if (config.isBlockValid() && (bridge != null)) {
            ThingBuilder tBuilder = editThing();

            String text = config.isInputBlock() ? INPUT_CHANNEL : OUTPUT_CHANNEL;
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
            super.initialize();
        } else {
            final String message = "Can not initialize LOGO! block. Please check blocks.";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            logger.error("Can not initialize thing {} for LOGO! block {}.", thing.getUID(), name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void dispose() {
        logger.debug("Dispose LOGO! {} analog handler.", config.getBlockName());
        super.dispose();

        oldValue = Long.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setData(final byte[] data) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBlockName() {
        return config.getBlockName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PLCLogoDataType getBlockDataType() {
        final String kind = config.getBlockKind();
        if ((kind != null) && config.isBlockValid()) {
            return kind.equalsIgnoreCase("VD") ? PLCLogoDataType.DWORD : PLCLogoDataType.WORD;
        }
        return PLCLogoDataType.INVALID;
    }

    /**
     * Returns configured channel type for configured block.
     *
     * @see PLCLogoBindingConstants#ANALOG_DATE_CHANNEL
     * @see PLCLogoBindingConstants#ANALOG_TIME_CHANNEL
     * @see PLCLogoBindingConstants#ANALOG_NUMBER_CHANNEL
     * @return Configured channel type
     */
    public String getChannelType() {
        return config.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        synchronized (config) {
            config = getConfigAs(PLCLogoAnalogConfiguration.class);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getAddress(final String name) {
        int address = -1;

        logger.debug("Get address of {} LOGO! for block {} .", getLogoFamily(), name);

        if (config.isBlockValid()) {
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
            logger.error("Wrong configurated LOGO! block {} found.", name);
        }
        return address;
    }

    /**
     * {@inheritDoc}
     */
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
