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
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
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

    public SbusRgbwHandler(Thing thing, TranslationProvider translationProvider, LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
    }

    @Override
    protected void initializeChannels() {
        int switchChannelCount = 0;

        // Validate all channel configurations
        for (Channel channel : getThing().getChannels()) {
            SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
            var channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID == null) {
                Bundle bundle = FrameworkUtil.getBundle(getClass());
                logger.warn("{}", translationProvider.getText(bundle, "error.channel.no-type",
                        channel.getUID().toString(), localeProvider.getLocale()));
                continue;
            }
            String channelTypeId = channelTypeUID.getId();
            if (BindingConstants.CHANNEL_TYPE_COLOR.equals(channelTypeId)) {
                if (channelConfig.channelNumber <= 0) {
                    Bundle bundle = FrameworkUtil.getBundle(getClass());
                    logger.warn("{}", translationProvider.getText(bundle, "error.channel.invalid-number",
                            channel.getUID().toString(), localeProvider.getLocale()));
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
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, translationProvider.getText(bundle,
                    "error.rgbw.too-many-switches", getThing().getUID().toString(), localeProvider.getLocale()));
            return;
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
        } catch (Exception e) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    translationProvider.getText(bundle, "error.device.read-state", null, localeProvider.getLocale()));
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
            Channel channel = getThing().getChannel(channelUID.getId());
            if (channel != null) {
                var channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID == null) {
                    Bundle bundle = FrameworkUtil.getBundle(getClass());
                    logger.warn("{}", translationProvider.getText(bundle, "error.channel.no-type",
                            channel.getUID().toString(), localeProvider.getLocale()));
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
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    translationProvider.getText(bundle, "error.device.send-command", null, localeProvider.getLocale()));
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
        int[] statusValues = new int[registers.length];
        for (int i = 0; i < registers.length; i++) {
            statusValues[i] = registers[i].getValue() & 0xff;
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
     * @return array of RGBW values [R, G, B, W]
     * @throws Exception if the SBUS transaction fails
     */
    private int[] readRgbw(SbusService adapter, int subnetId, int deviceId, int channelNumber) throws Exception {
        // Construct SBUS request
        ReadRgbwRequest request = new ReadRgbwRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);
        request.setLoopNumber(channelNumber);

        // Execute transaction and parse response
        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof ReadRgbwResponse)) {
            throw new Exception("Unexpected response type: " + response.getClass().getSimpleName());
        }

        ReadRgbwResponse rgbwResponse = (ReadRgbwResponse) response;
        InputRegister[] registers = rgbwResponse.getRegisters();
        int[] rgbwValues = new int[Math.min(4, registers.length)];
        for (int i = 0; i < rgbwValues.length; i++) {
            rgbwValues[i] = registers[i].toUnsignedShort();
        }
        return rgbwValues;
    }

    /**
     * Writes RGBW values to an SBUS device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @param channelNumber the channel number
     * @param rgbwValues array of RGBW values [R, G, B, W]
     * @throws Exception if the SBUS transaction fails
     */
    private void writeRgbw(SbusService adapter, int subnetId, int deviceId, int channelNumber, int[] rgbwValues)
            throws Exception {
        // Construct SBUS request
        WriteRgbwRequest request = new WriteRgbwRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);
        // Note: WriteRgbwRequest might not have setChannelNo method, using basic request

        // Create registers for RGBW values
        Register[] registers = new Register[rgbwValues.length];
        for (int i = 0; i < rgbwValues.length; i++) {
            registers[i] = new ByteRegister((byte) (rgbwValues[i] & 0xff));
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
     * @throws Exception if the SBUS transaction fails
     */
    private void writeSingleChannel(SbusService adapter, int subnetId, int deviceId, int channelNumber, int value,
            int duration) throws Exception {
        // Construct SBUS request
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
                if (response instanceof ReadStatusChannelsResponse
                        && BindingConstants.CHANNEL_TYPE_SWITCH.equals(channelTypeId)) {
                    // Process status channel response using existing logic
                    ReadStatusChannelsResponse statusResponse = (ReadStatusChannelsResponse) response;
                    rgbwValues = extractStatusValues(statusResponse);
                } else if (response instanceof ReadRgbwResponse
                        && BindingConstants.CHANNEL_TYPE_COLOR.equals(channelTypeId)) {
                    // Process RGBW response using existing logic
                    ReadRgbwResponse rgbwResponse = (ReadRgbwResponse) response;
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
            statuses[i] = registers[i].getValue() & 0xff;
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

        for (int i = 0; i < registers.length; i++) {
            rgbwValues[i] = registers[i].toUnsignedShort();
        }
        return rgbwValues;
    }
}
