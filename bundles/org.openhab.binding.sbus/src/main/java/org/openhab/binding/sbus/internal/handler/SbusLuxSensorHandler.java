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
import org.openhab.core.library.types.DecimalType;
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
 * The {@link SbusLuxSensorHandler} is responsible for handling lux sensor devices.
 * It supports reading light level (LUX) values and can operate in both polling
 * and listen-only modes. When refresh is set to 0, it operates in listen-only mode using
 * MotionSensorStatusReport broadcasts from 9-in-1 devices.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public class SbusLuxSensorHandler extends AbstractSbusHandler {

    private final Logger logger = LoggerFactory.getLogger(SbusLuxSensorHandler.class);

    public SbusLuxSensorHandler(Thing thing, TranslationProvider translationProvider, LocaleProvider localeProvider) {
        super(thing, translationProvider, localeProvider);
    }

    @Override
    protected void initializeChannels() {
        // Create lux channel
        createChannel(BindingConstants.CHANNEL_LUX, BindingConstants.CHANNEL_TYPE_LUX);
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
            logger.warn("Error polling lux sensor device {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    /**
     * Update channel states based on sensor response data.
     *
     * @param response the sensor response containing lux data
     */
    private void updateChannelStatesFromResponse(ReadNineInOneStatusResponse response) {
        // Update lux channel (byte 2: LUX value)
        ChannelUID luxChannelUID = new ChannelUID(getThing().getUID(), BindingConstants.CHANNEL_LUX);
        DecimalType luxValue = new DecimalType(response.getLuxValue());
        updateState(luxChannelUID, luxValue);

        logger.debug("Updated lux sensor state - LUX: {}", luxValue);
    }

    /**
     * Update channel states based on motion sensor status report data.
     *
     * @param report the motion sensor status report containing lux data
     */
    private void updateChannelStatesFromReport(MotionSensorStatusReport report) {
        // Update lux channel (bytes 6-7: LUX value as 2-byte value)
        ChannelUID luxChannelUID = new ChannelUID(getThing().getUID(), BindingConstants.CHANNEL_LUX);
        DecimalType luxValue = new DecimalType(report.getLuxValue());
        updateState(luxChannelUID, luxValue);

        logger.debug("Updated lux sensor state from report - LUX: {}", luxValue);
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
                logger.debug("Processed async motion sensor status report for lux handler {}", getThing().getUID());
            } else if (response instanceof ReadNineInOneStatusResponse) {
                // Process 9-in-1 status response (0xDB01)
                ReadNineInOneStatusResponse statusResponse = (ReadNineInOneStatusResponse) response;
                updateChannelStatesFromResponse(statusResponse);
                logger.debug("Processed async 9-in-1 status response for lux handler {}", getThing().getUID());
            }
        } catch (Exception e) {
            logger.warn("Error processing async message in lux sensor handler {}: {}", getThing().getUID(), e.getMessage());
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
