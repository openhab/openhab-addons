/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sbus.handler;

import static org.openhab.binding.sbus.BindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sbus.internal.config.SbusChannelConfig;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.facade.SbusAdapter;

/**
 * The {@link SbusRgbwHandler} is responsible for handling commands for SBUS RGBW devices.
 * It supports reading and controlling red, green, blue, and white color channels.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusRgbwHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusRgbwHandler.class);

    public SbusRgbwHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeChannels() {
        // Get all channel configurations from the thing
        for (Channel channel : getThing().getChannels()) {
            // Channels are already defined in thing-types.xml, just validate their configuration
            SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
            if (channelConfig.channelNumber <= 0) {
                logger.warn("Channel {} has invalid channel number configuration", channel.getUID());
            }
        }
    }

    @Override
    protected void pollDevice() {
        handleReadRgbwValues();
    }

    private void handleReadRgbwValues() {
        final SbusAdapter adapter = super.sbusAdapter;
        if (adapter == null) {
            logger.warn("SBUS adapter not initialized");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "SBUS adapter not initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            int[] rgbwValues = adapter.readRgbw(config.subnetId, config.id);
            if (rgbwValues != null && rgbwValues.length >= 4) {
                // Update each channel based on its ID
                for (Channel channel : getThing().getChannels()) {
                    String channelId = channel.getUID().getId();
                    if (CHANNEL_RED.equals(channelId)) {
                        updateState(channel.getUID(), new PercentType(rgbwValues[0]));
                    } else if (CHANNEL_GREEN.equals(channelId)) {
                        updateState(channel.getUID(), new PercentType(rgbwValues[1]));
                    } else if (CHANNEL_BLUE.equals(channelId)) {
                        updateState(channel.getUID(), new PercentType(rgbwValues[2]));
                    } else if (CHANNEL_WHITE.equals(channelId)) {
                        updateState(channel.getUID(), new PercentType(rgbwValues[3]));
                    }
                }
            } else {
                logger.warn("Invalid RGBW values received from SBUS device");
            }
        } catch (Exception e) {
            logger.error("Error reading RGBW values", e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final SbusAdapter adapter = super.sbusAdapter;
        if (adapter == null) {
            logger.warn("SBUS adapter not initialized");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "SBUS adapter not initialized");
            return;
        }

        try {
            String channelId = channelUID.getId();
            if (command instanceof PercentType) {
                int value = ((PercentType) command).intValue();
                SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
                int[] currentValues = adapter.readRgbw(config.subnetId, config.id);
                if (currentValues == null || currentValues.length < 4) {
                    logger.warn("Failed to read current RGBW values");
                    return;
                }

                int[] newValues = currentValues.clone();
                if (CHANNEL_RED.equals(channelId)) {
                    newValues[0] = value;
                } else if (CHANNEL_GREEN.equals(channelId)) {
                    newValues[1] = value;
                } else if (CHANNEL_BLUE.equals(channelId)) {
                    newValues[2] = value;
                } else if (CHANNEL_WHITE.equals(channelId)) {
                    newValues[3] = value;
                }

                // Update each channel's state
                Channel channel = getThing().getChannel(channelId);
                if (channel != null) {
                    SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
                    if (channelConfig.channelNumber > 0) {
                        adapter.writeSingleChannel(config.subnetId, config.id, channelConfig.channelNumber, value > 0);
                        updateState(channelUID, (PercentType) command);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error handling command", e);
        }
    }
}
