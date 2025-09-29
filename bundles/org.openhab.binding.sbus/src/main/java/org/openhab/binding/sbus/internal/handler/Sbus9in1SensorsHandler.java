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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sbus.internal.SbusService;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
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
@NonNullByDefault
public class Sbus9in1SensorsHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(Sbus9in1SensorsHandler.class);

    // Specialized handlers for different sensor types
    private Sbus9in1ContactHandler contactHandler;
    private SbusMotionSensorHandler motionHandler;
    private SbusLuxSensorHandler luxHandler;

    public Sbus9in1SensorsHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void initializeChannels() {
        // Create specialized handlers based on configured channels
        createSpecializedHandlers();

        // Initialize channels for each specialized handler
        if (contactHandler != null) {
            contactHandler.initializeChannels();
        }
        if (motionHandler != null) {
            motionHandler.initializeChannels();
        }
        if (luxHandler != null) {
            luxHandler.initializeChannels();
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

            // Route the response to all active specialized handlers
            routeResponseToHandlers(response);

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
                // Route motion sensor status report to all active handlers
                routeReportToHandlers(report);
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Processed async motion sensor status report for sensor handler {}", getThing().getUID());
            } else if (response instanceof ReadNineInOneStatusResponse statusResponse) {
                // Route 9-in-1 status response to all active handlers
                routeResponseToHandlers(statusResponse);
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
     * Create specialized handlers based on the configured channels.
     */
    private void createSpecializedHandlers() {
        // Check configured channels and create appropriate handlers
        for (org.openhab.core.thing.Channel channel : getThing().getChannels()) {
            if (channel.getChannelTypeUID() != null) {
                String channelType = channel.getChannelTypeUID().getId();

                // Create contact handler for contact channels
                if ("contact-channel".equals(channelType) && contactHandler == null) {
                    contactHandler = new Sbus9in1ContactHandler(getThing());
                    logger.debug("Created 9-in-1 contact handler for sensor {}", getThing().getUID());
                }

                // Create motion handler for motion channels
                if ("motion-channel".equals(channelType) && motionHandler == null) {
                    motionHandler = new SbusMotionSensorHandler(getThing());
                    logger.debug("Created motion handler for sensor {}", getThing().getUID());
                }

                // Create lux handler for lux channels
                if ("lux-channel".equals(channelType) && luxHandler == null) {
                    luxHandler = new SbusLuxSensorHandler(getThing());
                    logger.debug("Created lux handler for sensor {}", getThing().getUID());
                }
            }
        }
    }

    private List<String> getConfiguredChannelTypes() {
        List<String> channelTypes = new ArrayList<>();

        getThing().getChannels().forEach(channel -> {
            if (channel.getChannelTypeUID() != null) {
                String channelType = channel.getChannelTypeUID().getId();
                if (!channelTypes.contains(channelType)) {
                    channelTypes.add(channelType);
                }
            }
        });

        return channelTypes;
    }

    // Message Routing Methods

    /**
     * Route ReadNineInOneStatusResponse to appropriate specialized handlers.
     */
    private void routeResponseToHandlers(ReadNineInOneStatusResponse response) {
        // Route to contact handler if it exists
        if (contactHandler != null) {
            contactHandler.processAsyncMessage(response);
        }

        // Route to motion handler if it exists
        if (motionHandler != null) {
            motionHandler.processAsyncMessage(response);
        }

        // Route to lux handler if it exists
        if (luxHandler != null) {
            luxHandler.processAsyncMessage(response);
        }
    }

    /**
     * Route MotionSensorStatusReport to appropriate specialized handlers.
     */
    private void routeReportToHandlers(MotionSensorStatusReport report) {
        // Route to contact handler if it exists
        if (contactHandler != null) {
            contactHandler.processAsyncMessage(report);
        }

        // Route to motion handler if it exists
        if (motionHandler != null) {
            motionHandler.processAsyncMessage(report);
        }

        // Route to lux handler if it exists
        if (luxHandler != null) {
            luxHandler.processAsyncMessage(report);
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
        // Dispose specialized handlers
        if (contactHandler != null) {
            contactHandler.dispose();
            contactHandler = null;
        }
        if (motionHandler != null) {
            motionHandler.dispose();
            motionHandler = null;
        }
        if (luxHandler != null) {
            luxHandler.dispose();
            luxHandler = null;
        }

        super.dispose();
    }
}
