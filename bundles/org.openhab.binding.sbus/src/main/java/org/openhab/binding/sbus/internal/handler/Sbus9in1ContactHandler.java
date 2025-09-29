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

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.sbus.BindingConstants;
import org.openhab.binding.sbus.internal.SbusService;
import org.openhab.binding.sbus.internal.config.SbusChannelConfig;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.core.library.types.OpenClosedType;
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
 * The {@link Sbus9in1ContactHandler} is responsible for handling contact sensor channels
 * from 9-in-1 sensor devices. It supports reading dry contact states and can operate in both
 * polling and listen-only modes. When refresh is set to 0, it operates in listen-only mode using
 * MotionSensorStatusReport broadcasts from 9-in-1 devices.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class Sbus9in1ContactHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(Sbus9in1ContactHandler.class);

    public Sbus9in1ContactHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeChannels() {
        // Create contact channels based on configured channels
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (BindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID())) {
                // Contact channels are already defined in the thing configuration
                logger.debug("Initialized contact channel: {}", channelUID.getId());
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
            logger.warn("Error polling 9-in-1 contact sensor device {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    /**
     * Update channel states based on sensor response data.
     *
     * @param response the sensor response containing contact data
     */
    private void updateChannelStatesFromResponse(ReadNineInOneStatusResponse response) {
        // Update contact channels from 9-in-1 response
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (BindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID())) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);

                // Use channelNumber to determine which dry contact (1 or 2, default to 1)
                int channelNumber = channelConfig.channelNumber > 0 ? channelConfig.channelNumber : 1;
                boolean contactState = false;

                if (channelNumber == 1) {
                    contactState = response.getDryContact1Status() > 0;
                } else if (channelNumber == 2) {
                    contactState = response.getDryContact2Status() > 0;
                }

                OpenClosedType state = contactState ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                updateState(channelUID, state);

                logger.debug("Updated 9-in-1 contact channel {} (number {}) state: {}", channelUID.getId(),
                        channelNumber, state);
            }
        }
    }

    /**
     * Update channel states based on motion sensor status report data.
     *
     * @param report the motion sensor status report containing contact data
     */
    private void updateChannelStatesFromReport(MotionSensorStatusReport report) {
        // Update contact channels from motion sensor status report
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (BindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID())) {
                SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);

                // Use channelNumber to determine which dry contact (1 or 2, default to 1)
                int channelNumber = channelConfig.channelNumber > 0 ? channelConfig.channelNumber : 1;
                boolean contactState = false;

                if (channelNumber == 1) {
                    contactState = report.getDryContactStatus(0) > 0; // First dry contact (index 0)
                } else if (channelNumber == 2) {
                    contactState = report.getDryContactStatus(1) > 0; // Second dry contact (index 1)
                }

                OpenClosedType state = contactState ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                updateState(channelUID, state);

                logger.debug("Updated 9-in-1 contact channel {} (number {}) state from report: {}", channelUID.getId(),
                        channelNumber, state);
            }
        }
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // 9-in-1 contact sensors are read-only devices, no commands to handle
        logger.debug("9-in-1 contact sensor is read-only, ignoring command {} for channel {}", command, channelUID);
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
                logger.debug("Processed async motion sensor status report for 9-in-1 contact handler {}",
                        getThing().getUID());
            } else if (response instanceof ReadNineInOneStatusResponse statusResponse) {
                // Process 9-in-1 status response (0xDB01)
                updateChannelStatesFromResponse(statusResponse);
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Processed async 9-in-1 status response for 9-in-1 contact handler {}",
                        getThing().getUID());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error processing async message in 9-in-1 contact sensor handler {}: {}", getThing().getUID(),
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
