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

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sbus.internal.SbusService;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.binding.sbus.internal.helper.SbusContactHelper;
import org.openhab.binding.sbus.internal.helper.SbusLuxHelper;
import org.openhab.binding.sbus.internal.helper.SbusMotionHelper;
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
 * The {@link Sbus9in1SensorsHandler} is responsible for coordinating 9-in-1 sensor devices.
 * It acts as a central coordinator that polls the 9-in-1 sensor and routes the data
 * to specialized handlers (contact, motion, lux) based on configured channels.
 * This handler manages the communication with the physical device while delegating
 * channel-specific processing to the appropriate specialized handlers.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class Sbus9in1SensorsHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(Sbus9in1SensorsHandler.class);

    // Specialized helpers for different sensor types
    private @Nullable SbusContactHelper contactHelper;
    private @Nullable SbusMotionHelper motionHelper;
    private @Nullable SbusLuxHelper luxHelper;

    public Sbus9in1SensorsHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeChannels() {
        // Create specialized helpers based on configured channels
        createSpecializedHelpers();

        // Initialize helpers
        if (contactHelper != null) {
            contactHelper.initialize();
        }
        if (motionHelper != null) {
            motionHelper.initialize();
        }
        if (luxHelper != null) {
            luxHelper.initialize();
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

            // Route the response to all active specialized helpers
            routeMessageToHelpers(response);

            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/error.device.communication");
            logger.warn("Error polling 9-in-1 sensor device {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // 9-in-1 sensors are read-only devices, no commands to handle
        logger.debug("9-in-1 sensor is read-only, ignoring command {} for channel {}", command, channelUID);
    }

    @Override
    protected void processAsyncMessage(SbusResponse response) {
        try {
            if (response instanceof MotionSensorStatusReport report) {
                // Route motion sensor status report to all active helpers
                routeMessageToHelpers(report);
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Processed async motion sensor status report for sensor handler {}", getThing().getUID());
            } else if (response instanceof ReadNineInOneStatusResponse statusResponse) {
                // Route 9-in-1 status response to all active helpers
                routeMessageToHelpers(statusResponse);
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Processed async 9-in-1 status response for sensor handler {}", getThing().getUID());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error processing async message in sensor handler {}: {}", getThing().getUID(), e.getMessage());
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

    /**
     * Create specialized helpers based on the configured channels.
     */
    private void createSpecializedHelpers() {
        // Create contact helper if there are contact channels
        SbusContactHelper tempContactHelper = new SbusContactHelper(getThing(), this);
        if (tempContactHelper.hasRelevantChannels()) {
            contactHelper = tempContactHelper;
            logger.debug("Created contact helper for sensor {}", getThing().getUID());
        }

        // Create motion helper if there are motion channels
        SbusMotionHelper tempMotionHelper = new SbusMotionHelper(getThing(), this);
        if (tempMotionHelper.hasRelevantChannels()) {
            motionHelper = tempMotionHelper;
            logger.debug("Created motion helper for sensor {}", getThing().getUID());
        }

        // Create lux helper if there are lux channels
        SbusLuxHelper tempLuxHelper = new SbusLuxHelper(getThing(), this);
        if (tempLuxHelper.hasRelevantChannels()) {
            luxHelper = tempLuxHelper;
            logger.debug("Created lux helper for sensor {}", getThing().getUID());
        }
    }

    // Message Routing Methods

    /**
     * Route SBUS async messages to appropriate specialized helpers.
     * This method uses the generic processAsyncMessage() method in each helper.
     */
    private void routeMessageToHelpers(SbusResponse response) {
        // Route to contact helper if it exists
        if (contactHelper != null) {
            contactHelper.processMessage(response);
        }

        // Route to motion helper if it exists
        if (motionHelper != null) {
            motionHelper.processMessage(response);
        }

        // Route to lux helper if it exists
        if (luxHelper != null) {
            luxHelper.processMessage(response);
        }
    }

    // SBUS Protocol Methods

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

    @Override
    public void dispose() {
        // Dispose specialized helpers
        if (contactHelper != null) {
            contactHelper.dispose();
            contactHelper = null;
        }
        if (motionHelper != null) {
            motionHelper.dispose();
            motionHelper = null;
        }
        if (luxHelper != null) {
            luxHelper.dispose();
            luxHelper = null;
        }

        super.dispose();
    }
}
