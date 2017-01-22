/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.dsmr.device.DSMRDevice;
import org.openhab.binding.dsmr.device.DSMRDeviceConfiguration;
import org.openhab.binding.dsmr.device.DSMRDeviceConstants.DeviceState;
import org.openhab.binding.dsmr.device.DSMRDeviceConstants.DeviceStateDetail;
import org.openhab.binding.dsmr.device.DSMRDeviceStateListener;
import org.openhab.binding.dsmr.device.discovery.DSMRMeterDiscoveryListener;
import org.openhab.binding.dsmr.internal.discovery.DSMRDiscoveryService;
import org.openhab.binding.dsmr.meter.DSMRMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DSMRBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Volaart - Initial contribution
 */
public class DSMRBridgeHandler extends BaseBridgeHandler implements DSMRDeviceStateListener {

    private Logger logger = LoggerFactory.getLogger(DSMRBridgeHandler.class);

    private DSMRDevice dsmrDevice = null;
    private DSMRMeterDiscoveryListener discoveryService;

    public DSMRBridgeHandler(Bridge bridge, DSMRDiscoveryService discoveryService) {
        super(bridge);

        this.discoveryService = discoveryService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // DSMRBridgeHandler does not support commands
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        DSMRDeviceConfiguration deviceConfig = config.as(DSMRDeviceConfiguration.class);

        for (String key : config.keySet()) {
            logger.debug("Available key: {}", key);
        }

        logger.debug("Using configuration {} from Thing configuration {}", deviceConfig, config);
        if (deviceConfig == null || deviceConfig.serialPort == null || deviceConfig.serialPort.length() == 0) {
            logger.warn("portName is not configured, not starting device");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Serial Port name is not set");
        } else {
            logger.debug("Starting DSMR device");
            dsmrDevice = new DSMRDevice(deviceConfig, this, discoveryService);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "Starting bridge");
            dsmrDevice.startUpDevice();
        }
    }

    @Override
    public void dispose() {
        if (dsmrDevice != null) {
            dsmrDevice.shutdownDevice();
            dsmrDevice = null;
        }
    }

    @Override
    public void handleRemoval() {
        if (dsmrDevice != null) {
            dsmrDevice.shutdownDevice();
            dsmrDevice = null;
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void stateUpdated(DeviceState oldState, DeviceState newState, DeviceStateDetail stateDetail) {
        // No implementation
    }

    @Override
    public void stateChanged(DeviceState oldState, DeviceState newState, DeviceStateDetail stateDetail) {
        /*
         * Only if the Thing is initialized the State may be updated (otherwise an IllegalStateException is thrown)
         * See also BaseThingHandler.updateStatus
         */
        if (isInitialized()) {
            logger.debug("Notifying Thing handler of change from {} to {}", oldState, newState);
            switch (newState) {
                case INITIALIZING:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, stateDetail.stateDetails);
                    break;
                case OFFLINE:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, stateDetail.stateDetails);
                    break;
                case ONLINE:
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.DUTY_CYCLE, stateDetail.stateDetails);
                    break;
                case SHUTDOWN:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, stateDetail.stateDetails);
                    break;
                case STARTING:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, stateDetail.stateDetails);
                    break;
                case CONFIGURATION_PROBLEM:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, stateDetail.stateDetails);
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
        if (childHandler instanceof MeterHandler) {
            MeterHandler mh = (MeterHandler) childHandler;

            DSMRMeter meter = mh.getDSMRMeter();

            if (meter != null) {
                dsmrDevice.addDSMRMeter(meter);
            } else {
                logger.warn("Ignoring adding a null meter from Thing {}", childThing);
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof MeterHandler) {
            MeterHandler mh = (MeterHandler) childHandler;

            DSMRMeter meter = mh.getDSMRMeter();

            if (meter != null) {
                dsmrDevice.removeDSMRMeter(meter);
            } else {
                logger.warn("Ignoring removing a null meter from Thing {}", childThing);
            }

        }
    }
}
