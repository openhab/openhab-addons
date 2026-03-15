/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.sbus.BindingConstants;
import org.openhab.binding.sbus.internal.SbusService;
import org.openhab.binding.sbus.internal.config.SbusChannelConfig;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.msg.MotionSensorStatusReport;
import ro.ciprianpascu.sbus.msg.ReadNineInOneStatusRequest;
import ro.ciprianpascu.sbus.msg.ReadNineInOneStatusResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;

/**
 * The {@link SbusLuxSensorHandler} is responsible for handling lux sensor devices.
 * It supports reading light level (LUX) values and can operate in both polling
 * and listen-only modes. When refresh is set to 0, it operates in listen-only mode using
 * MotionSensorStatusReport broadcasts from 9-in-1 devices.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class SbusLuxSensorHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusLuxSensorHandler.class);

    public SbusLuxSensorHandler(Thing thing) {
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.device.adapter-not-initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            ReadNineInOneStatusResponse response = readNineInOneStatus(adapter, config.subnetId, config.id);

            // Update channel states from response
            updateChannelStatesFromResponse(response);
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.device.communication");
            logger.warn("Error polling lux sensor device {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    /**
     * Update channel states based on sensor response data.
     *
     * @param response the sensor response containing lux data
     */
    private void updateChannelStatesFromResponse(ReadNineInOneStatusResponse response) {
        // Update lux channel (byte 2: LUX value - one byte unsigned, 0-255 range)
        // Shift 8-bit value to 16-bit range, then convert to lux with unit
        ChannelUID luxChannelUID = new ChannelUID(getThing().getUID(), BindingConstants.CHANNEL_LUX);
        int luxValueSigned = response.getLuxValue();
        int luxValue8bit = luxValueSigned & 0xFF; // Convert to unsigned 8-bit value (0-255)
        int luxValue16bit = luxValue8bit << 8; // Shift to 16-bit range (0-65280)
        QuantityType<?> luxValue = new QuantityType<>(luxValue16bit, Units.LUX);
        updateState(luxChannelUID, luxValue);

        logger.debug("Updated lux sensor state - LUX: {} (8-bit: {}, 16-bit: {}, raw: {})", luxValue16bit, luxValue8bit,
                luxValue16bit, luxValueSigned);
    }

    /**
     * Update channel states based on motion sensor status report data.
     *
     * @param report the motion sensor status report containing lux data
     */
    private void updateChannelStatesFromReport(MotionSensorStatusReport report) {
        // Update lux channel (bytes 6-7: LUX value - two bytes unsigned, 0-65535 range)
        // Convert to lux with unit
        ChannelUID luxChannelUID = new ChannelUID(getThing().getUID(), BindingConstants.CHANNEL_LUX);
        int luxValueSigned = report.getLuxValue();
        int luxValue16bit = luxValueSigned & 0xFFFF; // Convert to unsigned 16-bit value (0-65535)
        QuantityType<?> luxValue = new QuantityType<>(luxValue16bit, Units.LUX);
        updateState(luxChannelUID, luxValue);

        logger.debug("Updated lux sensor state from report - LUX: {} (16-bit: {}, raw: {})", luxValue16bit,
                luxValue16bit, luxValueSigned);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // Lux sensors are read-only devices, no commands to handle
        logger.debug("Lux sensor is read-only, ignoring command {} for channel {}", command, channelUID);
    }

    /**
     * Reads 9-in-1 sensor status from an SBUS device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @return ReadNineInOneStatusResponse containing sensor data
     * @throws IllegalStateException if the SBUS transaction fails
     */
    private ReadNineInOneStatusResponse readNineInOneStatus(SbusService adapter, int subnetId, int deviceId)
            throws IllegalStateException {
        ReadNineInOneStatusRequest request = new ReadNineInOneStatusRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);

        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof ReadNineInOneStatusResponse statusResponse)) {
            throw new IllegalStateException(
                    "Unexpected response type: " + (response != null ? response.getClass().getSimpleName() : "null"));
        }

        return statusResponse;
    }

    // Async Message Handling

    @Override
    protected void processAsyncMessage(SbusResponse response) {
        try {
            if (response instanceof MotionSensorStatusReport report) {
                // Process motion sensor status report (0x02CA broadcast)
                updateChannelStatesFromReport(report);
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Processed async motion sensor status report for lux handler {}", getThing().getUID());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error processing async message in lux sensor handler {}: {}", getThing().getUID(),
                    e.getMessage());
        }
    }

    @Override
    protected boolean isMessageRelevant(SbusResponse response) {
        if (response instanceof MotionSensorStatusReport) {
            // Motion sensor status reports are broadcast messages (to FF:FF)
            // They are relevant if they come from our device
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            return response.getSourceSubnetID() == config.subnetId && response.getSourceUnitID() == config.id;
        } else if (response instanceof ReadNineInOneStatusResponse) {
            // Check if the response is for this device based on subnet and unit ID
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            return response.getSubnetID() == config.subnetId && response.getUnitID() == config.id;
        }
        return false;
    }
}
