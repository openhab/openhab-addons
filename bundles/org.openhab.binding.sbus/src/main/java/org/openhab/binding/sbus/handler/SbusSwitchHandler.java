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
package org.openhab.binding.sbus.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sbus.handler.config.SbusChannelConfig;
import org.openhab.binding.sbus.handler.config.SbusDeviceConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SbusSwitchHandler} is responsible for handling commands for Sbus switch devices.
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
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Sbus adapter not initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            int[] statuses = adapter.readStatusChannels(config.subnetId, config.id);

            // Iterate over all channels and update their states
            for (Channel channel : getThing().getChannels()) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
                if (channelConfig.channelNumber > 0 && channelConfig.channelNumber <= statuses.length) {
                    var channelTypeUID = channel.getChannelTypeUID();
                    if (channelTypeUID == null) {
                        logger.warn("Channel {} has no channel type", channel.getUID());
                        continue;
                    }
                    String channelTypeId = channelTypeUID.getId();
                    // 100 when on, something else when off
                    boolean isActive = statuses[channelConfig.channelNumber - 1] == 0x64;

                    if ("switch-channel".equals(channelTypeId)) {
                        updateState(channel.getUID(), isActive ? OnOffType.ON : OnOffType.OFF);
                    } else if ("dimmer-channel".equals(channelTypeId)) {
                        updateState(channel.getUID(), new PercentType(statuses[channelConfig.channelNumber - 1]));
                    } else if ("paired-channel".equals(channelTypeId)) {
                        updateState(channel.getUID(), isActive ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                    }
                }
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error reading device state");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            logger.warn("Sbus adapter not initialized");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Sbus adapter not initialized");
            return;
        }

        try {
            Channel channel = getThing().getChannel(channelUID);
            if (channel != null) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
                if (channelConfig.channelNumber <= 0) {
                    logger.warn("Invalid channel number for {}", channelUID);
                    return;
                }

                SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);

                if (command instanceof OnOffType) {
                    handleOnOffCommand((OnOffType) command, config, channelConfig, channelUID, adapter);
                } else if (command instanceof PercentType) {
                    handlePercentCommand((PercentType) command, config, channelConfig, channelUID, adapter);
                } else if (command instanceof OpenClosedType) {
                    handleOpenClosedCommand((OpenClosedType) command, config, channelConfig, channelUID, adapter);
                }
            }
        } catch (Exception e) {
            logger.error("Error handling command", e);
        }
    }

    private void handleOnOffCommand(OnOffType command, SbusDeviceConfig config, SbusChannelConfig channelConfig,
            ChannelUID channelUID, SbusService adapter) throws Exception {
        boolean isOn = command == OnOffType.ON;
        adapter.writeSingleChannel(config.subnetId, config.id, channelConfig.channelNumber, isOn ? 100 : 0,
                channelConfig.timer);
        updateState(channelUID, isOn ? OnOffType.ON : OnOffType.OFF);
    }

    private void handlePercentCommand(PercentType command, SbusDeviceConfig config, SbusChannelConfig channelConfig,
            ChannelUID channelUID, SbusService adapter) throws Exception {
        adapter.writeSingleChannel(config.subnetId, config.id, channelConfig.channelNumber, command.intValue(),
                channelConfig.timer);
        updateState(channelUID, command);
    }

    private void handleOpenClosedCommand(OpenClosedType command, SbusDeviceConfig config,
            SbusChannelConfig channelConfig, ChannelUID channelUID, SbusService adapter) throws Exception {
        boolean isOpen = command == OpenClosedType.OPEN;
        // Set main channel
        if (getChannelToClose(channelConfig, isOpen) > 0) {
            adapter.writeSingleChannel(config.subnetId, config.id, getChannelToClose(channelConfig, isOpen), 0,
                    channelConfig.timer);
        }
        // Set paired channel to opposite state if configured
        if (getChannelToOpen(channelConfig, isOpen) > 0) {
            adapter.writeSingleChannel(config.subnetId, config.id, getChannelToOpen(channelConfig, isOpen), 0x64,
                    channelConfig.timer);
        }
        updateState(channelUID, isOpen ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
    }

    private int getChannelToOpen(SbusChannelConfig channelConfig, boolean state) {
        return state ? channelConfig.channelNumber : channelConfig.pairedChannelNumber;
    }

    private int getChannelToClose(SbusChannelConfig channelConfig, boolean state) {
        return state ? channelConfig.pairedChannelNumber : channelConfig.channelNumber;
    }
}
