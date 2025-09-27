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
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.msg.ReadRgbwRequest;
import ro.ciprianpascu.sbus.msg.ReadRgbwResponse;
import ro.ciprianpascu.sbus.msg.ReadStatusChannelsRequest;
import ro.ciprianpascu.sbus.msg.ReadStatusChannelsResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;
import ro.ciprianpascu.sbus.msg.WriteRgbwRequest;
import ro.ciprianpascu.sbus.msg.WriteSingleChannelRequest;
import ro.ciprianpascu.sbus.procimg.ByteRegister;
import ro.ciprianpascu.sbus.procimg.InputRegister;
import ro.ciprianpascu.sbus.procimg.Register;
import ro.ciprianpascu.sbus.procimg.WordRegister;

/**
 * The {@link SbusRgbwHandler} is responsible for handling commands for Sbus RGBW devices.
 * It supports reading and controlling red, green, blue, and white color channels.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class SbusRgbwHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusRgbwHandler.class);

    public SbusRgbwHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeChannels() {
        int switchChannelCount = 0;

        // Validate all channel configurations
        for (Channel channel : getThing().getChannels()) {
            SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
            var channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID == null) {
                logger.warn("Channel {} has no channel type", channel.getUID());
                continue;
            }
            String channelTypeId = channelTypeUID.getId();
            if (BindingConstants.CHANNEL_TYPE_COLOR.equals(channelTypeId)) {
                if (channelConfig.channelNumber <= 0) {
                    logger.warn("Channel {} has invalid channel number configuration", channel.getUID());
                }
            }
            if (BindingConstants.CHANNEL_TYPE_SWITCH.equals(channelTypeId)) {
                switchChannelCount++;
                if (channelConfig.channelNumber <= 0) {
                    logger.warn("Channel {} has invalid channel number configuration", channel.getUID());
                }
            }
        }
        if (switchChannelCount > 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.rgbw.too-many-switches [\"" + getThing().getUID().toString() + "\"]");
            return;
        }
    }

    @Override
    protected void pollDevice() {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.device.adapter-not-initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);

            // Update all channels
            for (Channel channel : getThing().getChannels()) {
                if (!isLinked(channel.getUID())) {
                    continue;
                }
                var channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID == null) {
                    logger.warn("Channel {} has no channel type", channel.getUID().toString());
                    continue;
                }
                String channelTypeId = channelTypeUID.getId();
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);

                if (BindingConstants.CHANNEL_TYPE_COLOR.equals(channelTypeId)) {
                    // Read status channels for switch states
                    int[] statuses = readStatusChannels(adapter, config.subnetId, config.id);
                    boolean isActive = isAnyRgbwValueActive(statuses);
                    // Read RGBW values for this channel
                    int[] rgbwValues = isActive ? statuses
                            : readRgbw(adapter, config.subnetId, config.id, channelConfig.channelNumber);
                    fixRgbwValues(rgbwValues);
                    // Check if white channel should be disabled
                    boolean enableWhite = true; // Default to true if not specified
                    if (channel.getConfiguration().containsKey("enableWhite")) {
                        enableWhite = (boolean) channel.getConfiguration().get("enableWhite");
                    }
                    if (!enableWhite) {
                        rgbwValues = new int[] { rgbwValues[0], rgbwValues[1], rgbwValues[2] };
                    }
                    // Convert RGBW to HSB using our custom conversion
                    HSBType color = ColorUtil.rgbToHsb(rgbwValues);
                    color = new HSBType(color.getHue(), color.getSaturation(),
                            isActive ? PercentType.HUNDRED : PercentType.ZERO);
                    updateState(channel.getUID(), color);
                } else if (BindingConstants.CHANNEL_TYPE_SWITCH.equals(channelTypeId)) {
                    // Read status channels for switch states
                    int[] statuses = readStatusChannels(adapter, config.subnetId, config.id);

                    // Update switch state
                    boolean isActive = isAnyRgbwValueActive(statuses);
                    updateState(channel.getUID(), isActive ? OnOffType.ON : OnOffType.OFF);
                }
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.device.communication");
            logger.warn("Error polling RGBW device {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.device.adapter-not-initialized");
            return;
        }

        try {
            Channel channel = getThing().getChannel(channelUID.getId());
            if (channel != null) {
                var channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID == null) {
                    logger.warn("Channel {} has no channel type", channel.getUID());
                    return;
                }
                String channelTypeId = channelTypeUID.getId();
                SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);

                if (BindingConstants.CHANNEL_TYPE_COLOR.equals(channelTypeId)
                        && command instanceof HSBType hsbCommand) {
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        writeSingleChannel(adapter, config.subnetId, config.id, channelConfig.channelNumber, 0, -1);
                        hsbCommand = new HSBType(hsbCommand.getHue(), hsbCommand.getSaturation(), PercentType.ZERO);
                    } else {
                        // Check if white channel should be disabled
                        boolean enableWhite = true; // Default to true if not specified
                        if (channel.getConfiguration().containsKey("enableWhite")) {
                            enableWhite = (boolean) channel.getConfiguration().get("enableWhite");
                        }
                        // Handle color command
                        // If white is disabled, set the white component (index 3) to 0
                        int[] rgbw = null;
                        if (enableWhite) {
                            rgbw = ColorUtil.hsbToRgbw(hsbCommand);
                        } else {
                            var converted = ColorUtil.hsbToRgb(hsbCommand);
                            rgbw = new int[] { converted[0], converted[1], converted[2], 0 };
                        }
                        writeRgbw(adapter, config.subnetId, config.id, channelConfig.channelNumber, rgbw);
                        writeSingleChannel(adapter, config.subnetId, config.id, channelConfig.channelNumber, 100, -1);
                        hsbCommand = new HSBType(hsbCommand.getHue(), hsbCommand.getSaturation(), PercentType.HUNDRED);
                    }
                    updateState(channelUID, hsbCommand);
                } else if (BindingConstants.CHANNEL_TYPE_SWITCH.equals(channelTypeId)
                        && command instanceof OnOffType onOffCommand) {
                    // Handle switch command
                    boolean isOn = onOffCommand == OnOffType.ON;
                    writeSingleChannel(adapter, config.subnetId, config.id, channelConfig.channelNumber, isOn ? 100 : 0,
                            -1);
                    updateState(channelUID, OnOffType.from(isOn));
                }
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.device.send-command");
        }
    }

    /**
     * Checks if any RGBW value is greater than 0.
     *
     * @param rgbw an int array [R, G, B, W] each in [0..255]
     * @return true if any value is greater than 0, false otherwise
     */
    private boolean isAnyRgbwValueActive(int[] rgbw) {
        for (int value : rgbw) {
            if (value > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any RGBW value is smaller than 0.
     *
     * @param rgbw an int array [R, G, B, W] each in [0..255]
     */
    private void fixRgbwValues(int[] rgbw) {
        for (int i = 0; i < rgbw.length; i++) {
            if (rgbw[i] < 0) {
                rgbw[i] = 0;
            }
        }
    }

    /** Scale 0..255 -> 0..100 and clamp to [0,100]. If already <=100, just clamp. */
    private static int toPct100(int v) {
        if (v <= 100) {
            return Math.max(0, Math.min(100, v));
        }
        int pct = Math.round((v * 100f) / 255f);
        return Math.max(0, Math.min(100, pct));
    }

    /** Ensures an RGBW array of length 4, scaled & clamped to 0..100. */
    private static int[] toPct100(int[] rgbwIn) {
        int[] out = new int[4];
        for (int i = 0; i < 4; i++) {
            int v = (rgbwIn != null && i < rgbwIn.length) ? rgbwIn[i] : 0;
            out[i] = toPct100(v);
        }
        return out;
    }

    /** Scale 0..100 -> 0..255 and clamp to [0,255]. */
    private static int to255(int v) {
        v = Math.max(0, Math.min(100, v)); // Clamp to 0..100 first
        return Math.round((v * 255f) / 100f);
    }

    /** Converts an array from 0..100 to 0..255 range. */
    private static int[] to255(int[] pctIn) {
        int[] out = new int[pctIn.length];
        for (int i = 0; i < pctIn.length; i++) {
            out[i] = to255(pctIn[i]);
        }
        return out;
    }

    // SBUS Protocol Adaptation Methods

    /**
     * Reads status channel values from an SBUS device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @return array of status channel values (converted from 0..100 to 0..255)
     * @throws IllegalStateException if the SBUS transaction fails
     */
    private int[] readStatusChannels(SbusService adapter, int subnetId, int deviceId) throws IllegalStateException {
        // Construct SBUS request
        ReadStatusChannelsRequest request = new ReadStatusChannelsRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);

        // Execute transaction and parse response
        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof ReadStatusChannelsResponse statusResponse)) {
            throw new IllegalStateException("Unexpected response type: " + response.getClass().getSimpleName());
        }
        InputRegister[] registers = statusResponse.getRegisters();
        int[] statusValues = new int[registers.length];
        for (int i = 0; i < registers.length; i++) {
            int rawValue = registers[i].getValue() & 0xff;
            // Convert from 0..100 (device protocol) to 0..255 (internal representation)
            statusValues[i] = to255(rawValue);
        }
        return statusValues;
    }

    /**
     * Reads RGBW values from an SBUS device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @param channelNumber the channel number
     * @return array of RGBW values [R, G, B, W] (converted from 0..100 to 0..255)
     * @throws IllegalStateException if the SBUS transaction fails
     */
    private int[] readRgbw(SbusService adapter, int subnetId, int deviceId, int channelNumber)
            throws IllegalStateException {
        // Construct SBUS request
        ReadRgbwRequest request = new ReadRgbwRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);
        request.setLoopNumber(channelNumber);

        // Execute transaction and parse response
        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof ReadRgbwResponse rgbwResponse)) {
            throw new IllegalStateException("Unexpected response type: " + response.getClass().getSimpleName());
        }
        InputRegister[] registers = rgbwResponse.getRegisters();
        int[] rgbwValues = new int[Math.min(4, registers.length)];
        for (int i = 0; i < rgbwValues.length; i++) {
            int rawValue = registers[i].toUnsignedShort();
            // Convert from 0..100 (device protocol) to 0..255 (internal representation)
            rgbwValues[i] = to255(rawValue);
        }
        return rgbwValues;
    }

    /**
     * Writes RGBW preset (DD32). Values must be 0..100 and LoopNo goes first.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @param channelNumber the channel number
     * @param rgbwValues array of RGBW values [R, G, B, W]
     * @throws IllegalStateException if the SBUS transaction fails
     */
    private void writeRgbw(SbusService adapter, int subnetId, int deviceId, int channelNumber, int[] rgbwValues)
            throws IllegalStateException {
        // 1) Scale/clamp to 0..100 per protocol
        int[] pct = toPct100(rgbwValues);

        // 2) Build DD32 frame: [LoopNo, R, G, B, W]
        WriteRgbwRequest request = new WriteRgbwRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);
        request.setLoopNumber(channelNumber); // LoopNo (1..40) MUST go first for 0xDD32

        // Create registers for RGBW values
        Register[] registers = new Register[pct.length];
        for (int i = 0; i < pct.length; i++) {
            registers[i] = new ByteRegister((byte) (pct[i] & 0xff));
        }
        request.setRegisters(registers);

        // Execute transaction
        adapter.executeTransaction(request);
    }

    /**
     * Writes a single channel value to an SBUS device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @param channelNumber the channel number
     * @param value the channel value (0-100)
     * @param duration the duration (-1 for indefinite)
     * @throws IllegalStateException if the SBUS transaction fails
     */
    private void writeSingleChannel(SbusService adapter, int subnetId, int deviceId, int channelNumber, int value,
            int duration) throws IllegalStateException {
        // Clamp to 0..100; if caller gave 0..255, scale first
        value = Math.max(0, Math.min(100, value <= 100 ? value : Math.round((value * 100f) / 255f)));
        WriteSingleChannelRequest request = new WriteSingleChannelRequest(duration >= 0);
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);
        request.setChannelNo(channelNumber);

        // Create registers for value and duration
        Register[] registers;
        if (duration >= 0) {
            registers = new Register[2];
            registers[0] = new ByteRegister((byte) (value & 0xff)); // Value register
            registers[1] = new WordRegister((short) duration); // Duration register
        } else {
            registers = new Register[1];
            registers[0] = new ByteRegister((byte) (value & 0xff)); // Value register
        }
        request.setRegisters(registers);

        // Execute transaction
        adapter.executeTransaction(request);
    }

    // Async Message Handling

    @Override
    protected void processAsyncMessage(SbusResponse response) {
        for (Channel channel : getThing().getChannels()) {
            if (!isLinked(channel.getUID())) {
                continue;
            }

            var channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null) {
                String channelTypeId = channelTypeUID.getId();
                int[] rgbwValues = new int[] {};
                // Process the response based on type and available channels
                if (response instanceof ReadStatusChannelsResponse statusResponse
                        && BindingConstants.CHANNEL_TYPE_SWITCH.equals(channelTypeId)) {
                    // Process status channel response using existing logic
                    rgbwValues = extractStatusValues(statusResponse);
                } else if (response instanceof ReadRgbwResponse rgbwResponse
                        && BindingConstants.CHANNEL_TYPE_COLOR.equals(channelTypeId)) {
                    // Process RGBW response using existing logic
                    rgbwValues = extractRgbwValues(rgbwResponse);
                }
                fixRgbwValues(rgbwValues);
                // Check if white channel should be disabled
                boolean enableWhite = true; // Default to true if not specified
                if (channel.getConfiguration().containsKey("enableWhite")) {
                    enableWhite = (boolean) channel.getConfiguration().get("enableWhite");
                }
                if (!enableWhite) {
                    rgbwValues = new int[] { rgbwValues[0], rgbwValues[1], rgbwValues[2] };
                }
                // Convert RGBW to HSB using our custom conversion
                HSBType color = ColorUtil.rgbToHsb(rgbwValues);
                color = new HSBType(color.getHue(), color.getSaturation(),
                        color.getBrightness().intValue() > 0 ? PercentType.HUNDRED : PercentType.ZERO);
                updateState(channel.getUID(), color);
                logger.debug("Processed async RGBW message for handler {}", getThing().getUID());
            }
        }
    }

    @Override
    protected boolean isMessageRelevant(SbusResponse response) {
        if (!(response instanceof ReadStatusChannelsResponse || response instanceof ReadRgbwResponse)) {
            return false;
        }

        // Check if the message is for this device based on subnet and unit ID
        SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
        return response.getSubnetID() == config.subnetId && response.getUnitID() == config.id;
    }

    /**
     * Extract status values from ReadStatusChannelsResponse.
     * Reuses existing logic from readStatusChannels method.
     */
    private int[] extractStatusValues(ReadStatusChannelsResponse response) {
        InputRegister[] registers = response.getRegisters();
        int[] statuses = new int[registers.length];

        for (int i = 0; i < registers.length; i++) {
            int rawValue = registers[i].getValue() & 0xff;
            // Convert from 0..100 (device protocol) to 0..255 (internal representation)
            statuses[i] = to255(rawValue);
        }
        return statuses;
    }

    /**
     * Extract RGBW values from ReadRgbwResponse.
     * Reuses existing logic from readRgbw method.
     */
    private int[] extractRgbwValues(ReadRgbwResponse response) {
        InputRegister[] registers = response.getRegisters();
        int[] rgbwValues = new int[Math.min(4, registers.length)];

        for (int i = 0; i < rgbwValues.length; i++) {
            int rawValue = registers[i].toUnsignedShort();
            // Convert from 0..100 (device protocol) to 0..255 (internal representation)
            rgbwValues[i] = to255(rawValue);
        }
        return rgbwValues;
    }
}
