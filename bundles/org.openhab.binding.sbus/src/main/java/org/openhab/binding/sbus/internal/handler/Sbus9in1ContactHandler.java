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
 * The {@link Sbus9in1ContactHandler} handles 9-in-1 sensor devices with dry contact channels.
 * It uses ReadNineInOneStatusRequest/Response protocol for polling and processes
 * MotionSensorStatusReport for async updates.
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
        // Validate channel configuration
        for (Channel channel : getThing().getChannels()) {
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID().getId())) {
                logger.debug("Initialized 9-in-1 contact channel: {}", channel.getUID().getId());
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

            // Update all contact channels from the response
            updateContactChannelsFromResponse(response);

            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.device.communication");
            logger.warn("Error polling 9-in-1 contact sensor {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Contact sensors are read-only
        logger.debug("9-in-1 contact sensor is read-only, ignoring command {} for channel {}", command, channelUID);
    }

    @Override
    protected void processAsyncMessage(SbusResponse response) {
        try {
            if (response instanceof MotionSensorStatusReport report) {
                // Process motion sensor status report for dry contact updates
                updateContactChannelsFromReport(report);
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Processed async motion sensor status report for 9-in-1 contact handler {}",
                        getThing().getUID());
            } else if (response instanceof ReadNineInOneStatusResponse statusResponse) {
                // Process 9-in-1 status response
                updateContactChannelsFromResponse(statusResponse);
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Processed async 9-in-1 status response for contact handler {}", getThing().getUID());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error processing async message in 9-in-1 contact handler {}: {}", getThing().getUID(),
                    e.getMessage());
        }
    }

    @Override
    protected boolean isMessageRelevant(SbusResponse response) {
        SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);

        if (response instanceof MotionSensorStatusReport) {
            // Motion sensor status reports are broadcast messages
            // They are relevant if they come from our device
            return response.getSourceSubnetID() == config.subnetId && response.getSourceUnitID() == config.id;
        } else if (response instanceof ReadNineInOneStatusResponse) {
            // Check if the response is for this device
            return response.getSubnetID() == config.subnetId && response.getUnitID() == config.id;
        }
        return false;
    }

    /**
     * Read 9-in-1 sensor status from device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID
     * @param deviceId the device ID
     * @return ReadNineInOneStatusResponse
     * @throws IllegalStateException if communication fails
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

    /**
     * Update contact channels from ReadNineInOneStatusResponse.
     *
     * @param response the 9-in-1 status response
     */
    private void updateContactChannelsFromResponse(ReadNineInOneStatusResponse response) {
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID().getId())) {
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
     * Update contact channels from MotionSensorStatusReport.
     *
     * @param report the motion sensor status report
     */
    private void updateContactChannelsFromReport(MotionSensorStatusReport report) {
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (channel.getChannelTypeUID() != null
                    && BindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID().getId())) {
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
}
