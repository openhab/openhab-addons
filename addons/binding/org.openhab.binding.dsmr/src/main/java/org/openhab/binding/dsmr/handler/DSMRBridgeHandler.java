/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.dsmr.internal.device.DSMRDevice;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceConfiguration;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceConstants;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceConstants.DeviceState;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceStateListener;
import org.openhab.binding.dsmr.internal.discovery.DSMRMeterDiscoveryListener;
import org.openhab.binding.dsmr.internal.discovery.DSMRMeterDiscoveryService;
import org.openhab.binding.dsmr.internal.meter.DSMRMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DSMRBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author M. Volaart - Initial contribution
 * @since 2.1.0
 */
public class DSMRBridgeHandler extends BaseBridgeHandler implements DSMRDeviceStateListener {
    // logger
    private final Logger logger = LoggerFactory.getLogger(DSMRBridgeHandler.class);

    // DSMRDevice that belongs to this DSMRBridgeHandler
    private DSMRDevice dsmrDevice = null;

    // The Discovery service for this bridge
    private DSMRMeterDiscoveryListener discoveryService;

    // The available meters for this bridge
    private final List<DSMRMeter> availableMeters = new ArrayList<DSMRMeter>();

    // Watchdog
    private ScheduledFuture<?> watchdog;

    /**
     * Constructor
     *
     * @param bridge the Bridge ThingType
     * @param discoveryService the DSMRMeterDiscoveryService to use for new DSMR meters
     */
    public DSMRBridgeHandler(Bridge bridge, DSMRMeterDiscoveryService discoveryService) {
        super(bridge);

        this.discoveryService = discoveryService;
    }

    /**
     * The DSMRBridgeHandler does not support handling commands
     *
     * @param channelUID
     * @param command
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // DSMRBridgeHandler does not support commands
    }

    /**
     * Initializes this DSMRBridgeHandler
     *
     * This method will get the corresponding configuration and initialize and start the corresponding DSMRDevice
     */
    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        DSMRDeviceConfiguration deviceConfig = config.as(DSMRDeviceConfiguration.class);

        logger.debug("Using configuration {}", deviceConfig);
        if (deviceConfig == null || deviceConfig.serialPort == null || deviceConfig.serialPort.length() == 0) {
            logger.warn("portName is not configured, not starting device");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Serial Port name is not set");
        } else {
            logger.debug("Starting DSMR device");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "Starting bridge");

            // Create a new DSMR Device and give it a read only list of available DSMR Meters
            dsmrDevice = new DSMRDevice(deviceConfig, this, discoveryService,
                    Collections.unmodifiableList(availableMeters));
            dsmrDevice.startDevice();

            // Initialize meter watchdog
            watchdog = scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    dsmrDevice.alive();
                }
            }, DSMRDeviceConstants.RECOVERY_TIMEOUT, DSMRDeviceConstants.RECOVERY_TIMEOUT, TimeUnit.MILLISECONDS);

        }
    }

    /**
     * On dispose the DSMR device is removed
     */
    @Override
    public void dispose() {
        if (watchdog != null && !watchdog.isCancelled()) {
            watchdog.cancel(true);
            watchdog = null;
        }
        if (dsmrDevice != null) {
            dsmrDevice.stopDevice();
            dsmrDevice = null;
        }

    }

    @Override
    public void handleRemoval() {
        if (watchdog != null && !watchdog.isCancelled()) {
            watchdog.cancel(true);
            watchdog = null;
        }
        if (dsmrDevice != null) {
            dsmrDevice.stopDevice();
            dsmrDevice = null;
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void stateUpdated(DeviceState oldState, DeviceState newState, String stateDetail) {
        // No implementation
    }

    @Override
    public void stateChanged(DeviceState oldState, DeviceState newState, String stateDetail) {
        /*
         * Only if the Thing is initialized the State may be updated (otherwise an IllegalStateException is thrown)
         * See also BaseThingHandler.updateStatus
         */
        if (isInitialized()) {
            logger.debug("Notifying Thing handler of change from {} to {}", oldState, newState);
            switch (newState) {
                case INITIALIZING:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, stateDetail);
                    break;
                case OFFLINE:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, stateDetail);
                    break;
                case ONLINE:
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.DUTY_CYCLE, stateDetail);
                    break;
                case SHUTDOWN:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, stateDetail);
                    break;
                case STARTING:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, stateDetail);
                    break;
                case SWITCH_PORT_SPEED:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, stateDetail);
                    break;
                case CONFIGURATION_PROBLEM:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, stateDetail);
                default:
                    logger.warn("Received unknown state {}", newState);
                    updateStatus(ThingStatus.UNKNOWN);
            }
        } else {
            logger.debug("Ignore state change to {} since DSMRBridgeHandler is not initialized yet", newState);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof DSMRMeterHandler) {
            DSMRMeterHandler mh = (DSMRMeterHandler) childHandler;

            DSMRMeter meter = mh.getDSMRMeter();

            if (meter != null) {
                logger.debug("Add DSMR Meter {} to set of supported meters", meter);
                availableMeters.add(meter);
            } else {
                logger.warn("Ignoring adding a null meter from Thing {}", childThing);
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof DSMRMeterHandler) {
            DSMRMeterHandler mh = (DSMRMeterHandler) childHandler;

            DSMRMeter meter = mh.getDSMRMeter();

            if (meter != null) {
                availableMeters.remove(meter);
            } else {
                logger.warn("Ignoring removing a null meter from Thing {}", childThing);
            }

        }
    }
}
