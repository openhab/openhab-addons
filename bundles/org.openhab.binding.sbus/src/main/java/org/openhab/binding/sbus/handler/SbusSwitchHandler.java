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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sbus.internal.config.SbusChannelConfig;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.facade.SbusAdapter;

/**
 * The {@link SbusSwitchHandler} is responsible for handling commands for SBUS switch devices.
 * It supports reading the current state and switching the device on/off.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusSwitchHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusSwitchHandler.class);

    public SbusSwitchHandler(Thing thing) {
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
        handleReadStatusChannels();
    }

    private void handleReadStatusChannels() {
        final SbusAdapter adapter = super.sbusAdapter;
        if (adapter == null) {
            logger.warn("SBUS adapter not initialized");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "SBUS adapter not initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            boolean[] statuses = adapter.readStatusChannels(config.subnetId, config.id);
            if (statuses == null) {
                logger.warn("Received null status channels from SBUS device");
                return;
            }

            // Iterate over all channels and update their states
            for (Channel channel : getThing().getChannels()) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
                if (channelConfig.channelNumber > 0 && channelConfig.channelNumber <= statuses.length) {
                    State state = statuses[channelConfig.channelNumber - 1] ? OnOffType.ON : OnOffType.OFF;
                    updateState(channel.getUID(), state);
                }
            }
        } catch (Exception e) {
            logger.error("Error reading status channels", e);
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
            Channel channel = getThing().getChannel(channelUID);
            if (channel != null) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
                if (channelConfig.channelNumber > 0) {
                    boolean isOn = command.equals(OnOffType.ON);
                    SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
                    adapter.writeSingleChannel(config.subnetId, config.id, channelConfig.channelNumber, isOn);
                    updateState(channelUID, isOn ? OnOffType.ON : OnOffType.OFF);
                }
            }
        } catch (Exception e) {
            logger.error("Error handling command", e);
        }
    }
}
