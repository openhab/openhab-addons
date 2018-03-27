/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.Device;
import org.openhab.binding.gardena.internal.model.Location;
import org.openhab.binding.gardena.internal.model.Setting;

/**
 * Describes the methods required for the communication with Gardens Smart Home.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface GardenaSmart {

    /**
     * Initializes Gardena Smart Home and loads all devices from all locations.
     */
    public void init(String id, GardenaConfig config, GardenaSmartEventListener eventListener,
            ScheduledExecutorService scheduler) throws GardenaException;

    /**
     * Disposes Gardena Smart Home.
     */
    public void dispose();

    /**
     * Loads all devices from all locations.
     */
    public void loadAllDevices() throws GardenaException;

    /**
     * Returns all locations.
     */
    public Set<Location> getLocations();

    /**
     * Returns a device with the given id.
     */
    public Device getDevice(String deviceId) throws GardenaException;

    /**
     * Sends a command to Gardena Smart Home.
     */
    public void sendCommand(Device device, GardenaSmartCommandName commandName, Object value) throws GardenaException;

    /**
     * Sends a setting to Gardena Smart Home.
     */
    public void sendSetting(Setting setting, Object value) throws GardenaException;

    /**
     * Returns the id.
     */
    public String getId();
}
