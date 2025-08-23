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
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.msg.MotionSensorStatusReport;
import ro.ciprianpascu.sbus.msg.ReadNineInOneStatusRequest;
import ro.ciprianpascu.sbus.msg.ReadNineInOneStatusResponse;
import ro.ciprianpascu.sbus.msg.SbusResponse;

/**
 * The {@link SbusMotionSensorHandler} is responsible for handling motion sensor devices.
 * It supports reading motion status, dry contact states, and can operate in both polling
 * and listen-only modes. When refresh is set to 0, it operates in listen-only mode using
 * MotionSensorStatusReport broadcasts.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class SbusMotionSensorHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusMotionSensorHandler.class);

    public SbusMotionSensorHandler(Thing thing, TranslationProvider translationProvider, LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
    }

    @Override
    protected void initializeChannels() {
        // Create motion channel - this handler focuses only on motion detection
        createChannel(BindingConstants.CHANNEL_MOTION, BindingConstants.CHANNEL_TYPE_MOTION);
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
            ReadNineInOneStatusResponse response = readNineInOneStatus(adapter, config.subnetId, config.id);
            
            // Update channel states from response
            updateChannelStatesFromResponse(response);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    translationProvider.getText(bundle, "error.device.communication", null, localeProvider.getLocale()));
            logger.warn("Error polling motion sensor device {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    /**
     * Update channel states based on sensor response data.
     *
     * @param response the sensor response containing motion data
     */
    private void updateChannelStatesFromResponse(ReadNineInOneStatusResponse response) {
        // Update motion channel (byte 3: 0=no motion, 1=motion)
        ChannelUID motionChannelUID = new ChannelUID(getThing().getUID(), BindingConstants.CHANNEL_MOTION);
        OnOffType motionState = response.getMotionStatus() == 1 ? OnOffType.ON : OnOffType.OFF;
        updateState(motionChannelUID, motionState);

        logger.debug("Updated motion sensor state - Motion: {}", motionState);
    }

    /**
     * Update channel states based on motion sensor status report data.
     *
     * @param report the motion sensor status report containing motion data
     */
    private void updateChannelStatesFromReport(MotionSensorStatusReport report) {
        // Update motion channel - focus only on motion detection
        ChannelUID motionChannelUID = new ChannelUID(getThing().getUID(), BindingConstants.CHANNEL_MOTION);
        OnOffType motionState = report.getMotionStatus() == 1 ? OnOffType.ON : OnOffType.OFF;
        updateState(motionChannelUID, motionState);

        logger.debug("Updated motion sensor state from report - Motion: {}", motionState);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // Motion sensors are read-only devices, no commands to handle
        logger.debug("Motion sensor is read-only, ignoring command {} for channel {}", command, channelUID);
    }

    /**
     * Reads 9-in-1 sensor status from an SBUS device.
     *
     * @param adapter the SBUS service adapter
     * @param subnetId the subnet ID of the device
     * @param deviceId the device ID
     * @return ReadNineInOneStatusResponse containing sensor data
     * @throws Exception if the SBUS transaction fails
     */
    private ReadNineInOneStatusResponse readNineInOneStatus(SbusService adapter, int subnetId, int deviceId) throws Exception {
        ReadNineInOneStatusRequest request = new ReadNineInOneStatusRequest();
        request.setSubnetID(subnetId);
        request.setUnitID(deviceId);

        SbusResponse response = adapter.executeTransaction(request);
        if (!(response instanceof ReadNineInOneStatusResponse)) {
            throw new Exception("Unexpected response type: " + response.getClass().getSimpleName());
        }

        return (ReadNineInOneStatusResponse) response;
    }

    // Async Message Handling

    @Override
    protected void processAsyncMessage(SbusResponse response) {
        try {
            if (response instanceof MotionSensorStatusReport) {
                // Process motion sensor status report (0x02CA broadcast)
                MotionSensorStatusReport report = (MotionSensorStatusReport) response;
                updateChannelStatesFromReport(report);
                logger.debug("Processed async motion sensor status report for handler {}", getThing().getUID());
            } else if (response instanceof ReadNineInOneStatusResponse) {
                // Process 9-in-1 status response (0xDB01)
                ReadNineInOneStatusResponse statusResponse = (ReadNineInOneStatusResponse) response;
                updateChannelStatesFromResponse(statusResponse);
                logger.debug("Processed async 9-in-1 status response for handler {}", getThing().getUID());
            }
        } catch (Exception e) {
            logger.warn("Error processing async message in motion sensor handler {}: {}", getThing().getUID(), e.getMessage());
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
