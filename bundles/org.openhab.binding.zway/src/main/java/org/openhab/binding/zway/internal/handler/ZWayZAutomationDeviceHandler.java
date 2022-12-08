/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.zway.internal.handler;

import static org.openhab.binding.zway.internal.ZWayBindingConstants.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.zway.internal.config.ZWayZAutomationDeviceConfiguration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;

/**
 * The {@link ZWayZAutomationDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayZAutomationDeviceHandler extends ZWayDeviceHandler {
    public static final ThingTypeUID SUPPORTED_THING_TYPE = THING_TYPE_VIRTUAL_DEVICE;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ZWayZAutomationDeviceConfiguration mConfig;

    private class Initializer implements Runnable {

        @Override
        public void run() {
            ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
            if (zwayBridgeHandler != null && zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                ThingStatusInfo statusInfo = zwayBridgeHandler.getThing().getStatusInfo();

                logger.debug("Change Z-Way device status to bridge status: {}", statusInfo.getStatus());

                // Set thing status to bridge status
                updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());

                // Add all available channels
                DeviceList deviceList = getZWayBridgeHandler().getZWayApi().getDevices();
                if (deviceList != null) {
                    logger.debug("Z-Way devices loaded ({} virtual devices)", deviceList.getDevices().size());

                    // https://community.openhab.org/t/oh2-major-bug-with-scheduled-jobs/12350/11
                    // If any execution of the task encounters an exception, subsequent executions are
                    // suppressed. Otherwise, the task will only terminate via cancellation or
                    // termination of the executor.
                    try {
                        Device device = deviceList.getDeviceById(mConfig.getDeviceId());

                        if (device != null) {
                            logger.debug("Add channel for virtual device: {}", device.getMetrics().getTitle());

                            addDeviceAsChannel(device);

                            // Starts polling job and register all linked items
                            completeInitialization();
                        } else {
                            logger.warn("Initializing Z-Way device handler failed (virtual device not found): {}",
                                    getThing().getLabel());
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                    "Z-Way virtual device with id " + mConfig.getDeviceId() + " not found.");
                        }
                    } catch (Throwable t) {
                        if (t instanceof Exception) {
                            logger.error("{}", t.getMessage());
                        } else if (t instanceof Error) {
                            logger.error("{}", t.getMessage());
                        } else {
                            logger.error("Unexpected error");
                        }
                        if (getThing().getStatus() == ThingStatus.ONLINE) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                    "Error occurred when adding device as channel.");
                        }
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Devices not loaded");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Z-Way bridge handler not found or not ONLINE.");
            }
        }
    }

    public ZWayZAutomationDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Z-Way ZAutomation device handler ...");

        // Set thing status to a valid status
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "Checking configuration and bridge...");

        // Configuration - thing status update with an error message
        mConfig = loadAndCheckConfiguration();

        if (mConfig != null) {
            logger.debug("Configuration complete: {}", mConfig);

            // Start an extra thread to check the connection, because it takes sometimes more
            // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
            scheduler.schedule(new Initializer(), 2, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Z-Way device id required!");
        }
    }

    private void completeInitialization() {
        super.initialize(); // starts polling job and register all linked items
    }

    private ZWayZAutomationDeviceConfiguration loadAndCheckConfiguration() {
        ZWayZAutomationDeviceConfiguration config = getConfigAs(ZWayZAutomationDeviceConfiguration.class);

        String deviceId = config.getDeviceId();
        if (deviceId == null || deviceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Z-Wave device couldn't create, because the device id is missing.");
            return null;
        }

        return config;
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Z-Way ZAutomation handler ...");

        if (mConfig.getDeviceId() != null) {
            mConfig.setDeviceId(null);
        }

        super.dispose();
    }

    @Override
    protected void refreshLastUpdate() {
        logger.debug("Refresh last update for virtual device");

        // Check Z-Way bridge handler
        ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
        if (zwayBridgeHandler == null || !zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Z-Way bridge handler not found or not ONLINE.");
            return;
        }

        // Load and check device from Z-Way server
        DeviceList deviceList = zwayBridgeHandler.getZWayApi().getDevices();
        if (deviceList != null) {
            Device device = deviceList.getDeviceById(mConfig.getDeviceId());
            if (device == null) {
                logger.debug("ZAutomation device not found.");
                return;
            }

            Calendar lastUpdateOfDevice = Calendar.getInstance();
            lastUpdateOfDevice.setTimeInMillis(Long.valueOf(device.getUpdateTime()) * 1000);

            if (lastUpdate == null || lastUpdateOfDevice.after(lastUpdate)) {
                lastUpdate = lastUpdateOfDevice;
            }

            DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
            updateProperty(DEVICE_PROP_LAST_UPDATE, formatter.format(lastUpdate.getTime()));
        }
    }
}
