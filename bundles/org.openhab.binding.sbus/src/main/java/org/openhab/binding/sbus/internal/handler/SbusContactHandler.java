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

import org.openhab.binding.sbus.BindingConstants;
import org.openhab.binding.sbus.internal.SbusService;
import org.openhab.binding.sbus.internal.config.ContactSensorType;
import org.openhab.binding.sbus.internal.config.SbusChannelConfig;
import org.openhab.binding.sbus.internal.config.SbusContactConfig;
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
import ro.ciprianpascu.sbus.msg.ReadDryChannelsRequest;
import ro.ciprianpascu.sbus.msg.ReadDryChannelsResponse;
import ro.ciprianpascu.sbus.msg.ReadNineInOneStatusRequest;
import ro.ciprianpascu.sbus.msg.ReadNineInOneStatusResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;
import ro.ciprianpascu.sbus.procimg.InputRegister;

/**
 * The {@link SbusContactHandler} handles Sbus contact sensor devices.
 * It supports two protocols internally, selected via the {@code type} configuration parameter:
 * <ul>
 * <li>{@code 012c} (default) - traditional dry contact sensors using ReadDryChannelsRequest/Response</li>
 * <li>{@code 02ca} - 9-in-1 multi-sensor devices using ReadNineInOneStatusRequest/Response and
 * MotionSensorStatusReport broadcasts</li>
 * </ul>
 * The active protocol is (re-)resolved every time {@link #initializeChannels()} runs. Since the default
 * {@code thingUpdated()} implementation disposes and re-initializes the same handler instance when the
 * Thing configuration changes, changing the {@code type} parameter on an existing Thing takes effect
 * immediately without requiring the Thing to be deleted and recreated.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class SbusContactHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusContactHandler.class);

    private volatile ContactSensorType sensorType = ContactSensorType.SENSOR_012C;

    public SbusContactHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeChannels() {
        SbusContactConfig config = getConfigAs(SbusContactConfig.class);
        sensorType = config.getSensorType();
        logger.debug("Initialized contact handler {} with sensor type {}", getThing().getUID(), sensorType);

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
        if (sensorType == ContactSensorType.MULTI_SENSOR_02CA) {
            pollNineInOneDevice();
        } else {
            pollDryChannelsDevice();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Contact sensors are read-only
        logger.debug("Contact device is read-only, ignoring command");
    }

    @Override
    protected void processAsyncMessage(SbusResponse response) {
        if (sensorType == ContactSensorType.MULTI_SENSOR_02CA) {
            processNineInOneAsyncMessage(response);
        } else {
            processDryChannelsAsyncMessage(response);
        }
    }

    @Override
    protected boolean isMessageRelevant(SbusResponse response) {
        SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
        if (sensorType == ContactSensorType.MULTI_SENSOR_02CA) {
            if (response instanceof MotionSensorStatusReport) {
                // Motion sensor status reports are broadcast messages
                return response.getSourceSubnetID() == config.subnetId && response.getSourceUnitID() == config.id;
            } else if (response instanceof ReadNineInOneStatusResponse) {
                return response.getSubnetID() == config.subnetId && response.getUnitID() == config.id;
            }
            return false;
        } else {
            if (response instanceof ReadDryChannelsResponse) {
                return response.getSubnetID() == config.subnetId && response.getUnitID() == config.id;
            }
            return false;
        }
    }

    // 012C Dry Contact Protocol

    private void pollDryChannelsDevice() {
        final SbusService adapter = super.sbusAdapter;
        if (adapter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.device.adapter-not-initialized");
            return;
        }

        try {
            SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
            boolean[] contactStates = readContactStatusChannels(adapter, config.subnetId, config.id);

            updateChannelStatesFromStatuses(contactStates);
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.device.read-state");
            logger.warn("Error polling contact device {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    /**
     * Reads contact status channel values from an SBUS device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @return array of contact status values (true for open, false for closed)
     * @throws IllegalStateException if the SBUS transaction fails
     */
    private boolean[] readContactStatusChannels(SbusService adapter, int subnetId, int deviceId)
            throws IllegalStateException {
        // Construct SBUS request
        ReadDryChannelsRequest request = new ReadDryChannelsRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);

        // Execute transaction and parse response
        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof ReadDryChannelsResponse statusResponse)) {
            throw new IllegalStateException(
                    "Unexpected response type: " + (response != null ? response.getClass().getSimpleName() : "null"));
        }

        return extractContactStatuses(statusResponse);
    }

    private void processDryChannelsAsyncMessage(SbusResponse response) {
        try {
            if (response instanceof ReadDryChannelsResponse statusResponse) {
                // Process status channel response using existing logic
                boolean[] statuses = extractContactStatuses(statusResponse);
                updateChannelStatesFromStatuses(statuses);
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Processed async contact status message for handler {}", getThing().getUID());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error processing async message in contact handler {}: {}", getThing().getUID(),
                    e.getMessage());
        }
    }

    /**
     * Update channel states based on contact status values from async message.
     * Reuses the existing polling logic but with data from async message.
     */
    private void updateChannelStatesFromStatuses(boolean[] contactStates) {
        // Iterate over all channels and update their states
        for (Channel channel : getThing().getChannels()) {
            if (!isLinked(channel.getUID())) {
                continue;
            }
            SbusChannelConfig channelConfig = channel.getConfiguration().as(SbusChannelConfig.class);
            if (channelConfig.channelNumber > 0 && channelConfig.channelNumber <= contactStates.length) {
                boolean isOpen = contactStates[channelConfig.channelNumber - 1];
                updateState(channel.getUID(), isOpen ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            }
        }
    }

    /**
     * Extract contact status values from ReadDryChannelsResponse.
     * Reuses existing logic from readContactStatusChannels method.
     */
    private boolean[] extractContactStatuses(ReadDryChannelsResponse response) {
        InputRegister[] registers = response.getRegisters();
        boolean[] statuses = new boolean[registers.length];

        for (int i = 0; i < registers.length; i++) {
            statuses[i] = (registers[i].getValue() & 0xff) > 0; // Convert to boolean
        }
        return statuses;
    }

    // 02CA / 9-in-1 Multi-Sensor Protocol

    private void pollNineInOneDevice() {
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

    private void processNineInOneAsyncMessage(SbusResponse response) {
        try {
            if (response instanceof MotionSensorStatusReport report) {
                // Process motion sensor status report for dry contact updates
                updateContactChannelsFromReport(report);
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Processed async motion sensor status report for 9-in-1 contact handler {}",
                        getThing().getUID());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error processing async message in 9-in-1 contact handler {}: {}", getThing().getUID(),
                    e.getMessage());
        }
    }

    /**
     * Update contact channels from ReadNineInOneStatusResponse.
     *
     * @param response the 9-in-1 status response
     */
    private void updateContactChannelsFromResponse(ReadNineInOneStatusResponse response) {
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            var channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null && BindingConstants.CHANNEL_TYPE_CONTACT.equals(channelTypeUID.getId())) {
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
            var channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null && BindingConstants.CHANNEL_TYPE_CONTACT.equals(channelTypeUID.getId())) {
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
