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
package org.openhab.binding.sbus.internal.handler;

import org.openhab.binding.sbus.BindingConstants;
import org.openhab.binding.sbus.internal.SbusService;
import org.openhab.binding.sbus.internal.config.SbusChannelConfig;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.msg.ReadStatusChannelsRequest;
import ro.ciprianpascu.sbus.msg.ReadStatusChannelsResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;
import ro.ciprianpascu.sbus.msg.WriteSingleChannelRequest;
import ro.ciprianpascu.sbus.procimg.ByteRegister;
import ro.ciprianpascu.sbus.procimg.InputRegister;
import ro.ciprianpascu.sbus.procimg.Register;
import ro.ciprianpascu.sbus.procimg.WordRegister;

/**
 * The {@link SbusSwitchHandler} is responsible for handling commands for Sbus switch devices.
 * It supports reading the current state and switching the device on/off.
 *
 * @author Ciprian Pascu - Initial contribution
 */
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
                logger.warn("{}", "@text/error.channel.invalid-number [\"" + channel.getUID().toString() + "\"]");
            }
        }
    }

    @Override
    protected void pollDevice() {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, translationProvider.getText(bundle,
                    "error.device.adapter-not-initialized", null, localeProvider.getLocale()));
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            int[] statuses = readStatusChannels(adapter, config.subnetId, config.id);

            // Iterate over all channels and update their states
            updateChannelStatesFromStatus(statuses);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.device.read-state");
        }
    }

    /**
     * Update channel states based on status values.
     */
    private void updateChannelStatesFromStatus(int[] statuses) {
        for (Channel channel : getThing().getChannels()) {
            if (!isLinked(channel.getUID())) {
                continue;
            }
            SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
            if (channelConfig.channelNumber > 0 && channelConfig.channelNumber <= statuses.length) {
                var channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID == null) {
                    logger.warn("{}", "@text/error.channel.no-type [\"" + channel.getUID().toString() + "\"]");
                    continue;
                }
                String channelTypeId = channelTypeUID.getId();
                // 100 when on, something else when off
                boolean isActive = statuses[channelConfig.channelNumber - 1] == 0x64;

                if (BindingConstants.CHANNEL_TYPE_SWITCH.equals(channelTypeId)) {
                    updateState(channel.getUID(), OnOffType.from(isActive));
                } else if (BindingConstants.CHANNEL_TYPE_DIMMER.equals(channelTypeId)) {
                    updateState(channel.getUID(), new PercentType(statuses[channelConfig.channelNumber - 1]));
                } else if (BindingConstants.CHANNEL_TYPE_PAIRED.equals(channelTypeId)) {
                    updateState(channel.getUID(), isActive ? UpDownType.UP : UpDownType.DOWN);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, translationProvider.getText(bundle,
                    "error.device.adapter-not-initialized", null, localeProvider.getLocale()));
            return;
        }

        try {
            Channel channel = getThing().getChannel(channelUID);
            if (channel != null) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
                if (channelConfig.channelNumber <= 0) {
                    logger.warn("{}", "@text/error.channel.invalid-number [\"" + channelUID.toString() + "\"]");
                    return;
                }

                SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);

                if (command instanceof OnOffType onOffCommand) {
                    handleOnOffCommand(onOffCommand, config, channelConfig, channelUID, adapter);
                } else if (command instanceof PercentType percentCommand) {
                    handlePercentCommand(percentCommand, config, channelConfig, channelUID, adapter);
                } else if (command instanceof UpDownType upDownCommand) {
                    handleUpDownCommand(upDownCommand, config, channelConfig, channelUID, adapter);
                } else if (command instanceof StopMoveType stopMoveCommand && stopMoveCommand == StopMoveType.STOP) {
                    handleStopMoveCommand(config, channelConfig, channelUID, adapter);
                }
            }
        } catch (Exception e) {
            logger.warn("{}", "@text/error.device.send-command", e);
        }
    }

    private void handleOnOffCommand(OnOffType command, SbusDeviceConfig config, SbusChannelConfig channelConfig,
            ChannelUID channelUID, SbusService adapter) throws Exception {
        boolean isOn = command == OnOffType.ON;
        int value = isOn ? 100 : 0;
        writeSingleChannel(adapter, config.subnetId, config.id, channelConfig.channelNumber, value,
                channelConfig.timer);
        updateState(channelUID, OnOffType.from(isOn));
    }

    private void handlePercentCommand(PercentType command, SbusDeviceConfig config, SbusChannelConfig channelConfig,
            ChannelUID channelUID, SbusService adapter) throws Exception {
        int value = command.intValue();
        writeSingleChannel(adapter, config.subnetId, config.id, channelConfig.channelNumber, value,
                channelConfig.timer);
        updateState(channelUID, command);
    }

    // SBUS Protocol Adaptation Methods

    /**
     * Reads status channel values from an SBUS device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @return array of status channel values
     * @throws Exception if the SBUS transaction fails
     */
    private int[] readStatusChannels(SbusService adapter, int subnetId, int deviceId) throws Exception {
        // Construct SBUS request
        ReadStatusChannelsRequest request = new ReadStatusChannelsRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);

        // Execute transaction and parse response
        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof ReadStatusChannelsResponse)) {
            throw new Exception("Unexpected response type: " + response.getClass().getSimpleName());
        }

        ReadStatusChannelsResponse statusResponse = (ReadStatusChannelsResponse) response;
        InputRegister[] registers = statusResponse.getRegisters();
        int[] statuses = new int[registers.length];
        for (int i = 0; i < registers.length; i++) {
            statuses[i] = registers[i].getValue();
        }
        return statuses;
    }

    /**
     * Writes a value to a single SBUS channel.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @param channelNumber the channel number to write to
     * @param value the value to write (0-255)
     * @param timer timer value in seconds (-1 for no timer)
     * @throws Exception if the SBUS transaction fails
     */
    private void writeSingleChannel(SbusService adapter, int subnetId, int deviceId, int channelNumber, int value,
            int timer) throws Exception {
        // Construct SBUS write request
        WriteSingleChannelRequest request = new WriteSingleChannelRequest(timer >= 0);
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);
        request.setChannelNo(channelNumber);

        // Create registers for value and timer
        Register[] registers;
        if (timer >= 0) {
            registers = new Register[2];
            registers[0] = new ByteRegister((byte) value); // Value register
            registers[1] = new WordRegister((short) timer); // Timer register
        } else {
            registers = new Register[1];
            registers[0] = new ByteRegister((byte) value); // Value register
        }
        request.setRegisters(registers);

        // Execute transaction
        adapter.executeTransaction(request);
    }

    private void handleUpDownCommand(UpDownType command, SbusDeviceConfig config, SbusChannelConfig channelConfig,
            ChannelUID channelUID, SbusService adapter) throws Exception {
        boolean isUp = command == UpDownType.UP;
        // Set main channel
        if (getChannelToClose(channelConfig, isUp) > 0) {
            writeSingleChannel(adapter, config.subnetId, config.id, getChannelToClose(channelConfig, isUp), 0,
                    channelConfig.timer);
        }
        // Set paired channel to opposite state if configured
        if (getChannelToOpen(channelConfig, isUp) > 0) {
            writeSingleChannel(adapter, config.subnetId, config.id, getChannelToOpen(channelConfig, isUp), 0x64,
                    channelConfig.timer);
        }
        updateState(channelUID, isUp ? UpDownType.UP : UpDownType.DOWN);
    }

    private int getChannelToOpen(SbusChannelConfig channelConfig, boolean state) {
        return state ? channelConfig.channelNumber : channelConfig.pairedChannelNumber;
    }

    private int getChannelToClose(SbusChannelConfig channelConfig, boolean state) {
        return state ? channelConfig.pairedChannelNumber : channelConfig.channelNumber;
    }

    private void handleStopMoveCommand(SbusDeviceConfig config, SbusChannelConfig channelConfig, ChannelUID channelUID,
            SbusService adapter) throws Exception {
        // For STOP command, we stop both channels by setting them to 0
        if (channelConfig.channelNumber > 0) {
            writeSingleChannel(adapter, config.subnetId, config.id, channelConfig.channelNumber, 0,
                    channelConfig.timer);
        }
        if (channelConfig.pairedChannelNumber > 0) {
            writeSingleChannel(adapter, config.subnetId, config.id, channelConfig.pairedChannelNumber, 0,
                    channelConfig.timer);
        }
        // We don't update the state here as the rollershutter is neither UP nor DOWN after stopping
    }

    // Async Message Handling

    @Override
    protected void processAsyncMessage(SbusResponse response) {
        try {
            if (response instanceof ReadStatusChannelsResponse) {
                // Process status channel response using existing logic
                ReadStatusChannelsResponse statusResponse = (ReadStatusChannelsResponse) response;
                InputRegister[] registers = statusResponse.getRegisters();
                int[] statuses = new int[registers.length];
                for (int i = 0; i < registers.length; i++) {
                    statuses[i] = registers[i].getValue();
                }

                // Update channel states based on async message
                updateChannelStatesFromStatus(statuses);
                logger.debug("Processed async status message for switch handler {}", getThing().getUID());
            }
        } catch (Exception e) {
            logger.warn("Error processing async message in switch handler {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    @Override
    protected boolean isMessageRelevant(SbusResponse response) {
        if (!(response instanceof ReadStatusChannelsResponse)) {
            return false;
        }

        // Check if the message is for this device based on subnet and unit ID
        SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
        return response.getSubnetID() == config.subnetId && response.getUnitID() == config.id;
    }
}
