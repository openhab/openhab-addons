/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wink.client.AuthenticationException;
import org.openhab.binding.wink.client.IWinkClient;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.WinkClient;
import org.openhab.binding.wink.client.WinkSupportedDevice;
import org.openhab.binding.wink.internal.discovery.WinkDeviceDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles all command delegation from devices connected to this hub.
 *
 * @author Shawn Crosby
 *
 */
public class WinkHub2BridgeHandler extends BaseBridgeHandler {

    private IWinkClient client = WinkClient.getInstance();
    private final Logger logger = LoggerFactory.getLogger(WinkHub2BridgeHandler.class);

    public WinkHub2BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        WinkDeviceDiscoveryService discovery = new WinkDeviceDiscoveryService(this);

        this.bundleContext.registerService(DiscoveryService.class, discovery, null);

        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                // connect();
            }
        }, 0, TimeUnit.SECONDS);
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Bridge Handler doesn't supporte any commands");
    }

    /**
     * Attempts to change the state of a specified device to the provided state.
     *
     * @param device The device to update
     * @param updatedState Map of properties to update and the new values
     */
    public void setDesiredState(IWinkDevice device, Map<String, String> updatedState) {
        logger.debug("Setting device state: {}", updatedState);
        try {
            client.updateDeviceState(device, updatedState);
        } catch (AuthenticationException e) {
            logger.error("Unable to communicate with wink api: {}", e.getMessage());
        }
    }

    /**
     * Change the state of a device to a 'powered' state
     *
     * @param device The device to power on
     */
    public void switchOnDevice(IWinkDevice device) {
        logger.debug("Switching on Device {}", device);
        Map<String, String> updatedState = new HashMap<String, String>();
        updatedState.put("powered", "true");
        this.setDesiredState(device, updatedState);
    }

    /**
     * Change the 'powered' state of a device to off
     *
     * @param device The device to power off
     */
    public void switchOffDevice(IWinkDevice device) {
        Map<String, String> updatedState = new HashMap<String, String>();
        updatedState.put("powered", "false");
        this.setDesiredState(device, updatedState);
    }

    /**
     * Set the state of a lockable device to 'locked'
     *
     * @param device The device to lock
     */
    public void lockDevice(IWinkDevice device) {
        Map<String, String> updatedState = new HashMap<String, String>();
        updatedState.put("locked", "true");
        this.setDesiredState(device, updatedState);
    }

    /**
     * Set the 'locked' state of a lockable device to unlocked
     *
     * @param device The device to unlock
     */
    public void unLockDevice(IWinkDevice device) {
        Map<String, String> updatedState = new HashMap<String, String>();
        updatedState.put("locked", "false");
        this.setDesiredState(device, updatedState);
    }

    /**
     * Sets the dimmer level of a device to the level specified as a percentage
     *
     * @param device The device to change brightness
     * @param level The percentage of brightness desired
     */
    public void setDeviceDimmerLevel(IWinkDevice device, int level) {
        Map<String, String> updatedState = new HashMap<String, String>();
        if (level > 0) {
            Float fLevel = Float.valueOf(level);
            updatedState.put("powered", "true");
            updatedState.put("brightness", String.valueOf(fLevel / 100.0f));
        } else {
            updatedState.put("powered", "false");
        }
        this.setDesiredState(device, updatedState);
    }

    /**
     * Retrieve a specified device from the Hub API
     *
     * @param deviceType The type of device to retrieve
     * @param uuid The unique identifier for the device
     * @return The device
     */
    public IWinkDevice getDevice(WinkSupportedDevice deviceType, String uuid) {
        logger.debug("Getting device through handler {}", uuid);
        return client.getDevice(deviceType, uuid);
    }

}
