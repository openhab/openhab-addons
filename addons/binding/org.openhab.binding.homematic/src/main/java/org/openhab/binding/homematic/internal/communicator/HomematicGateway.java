/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator;

import java.io.IOException;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;

/**
 * Describes the methods required for the communication with a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface HomematicGateway {

    /**
     * Initializes the Homematic gateway and starts the watchdog thread.
     */
    public void initialize() throws IOException;

    /**
     * Disposes the HomematicGateway and stops everything.
     */
    public void dispose();

    /**
     * Returns the cached datapoint.
     */
    public HmDatapoint getDatapoint(HmDatapointInfo dpInfo) throws HomematicClientException;

    /**
     * Returns the cached device.
     */
    public HmDevice getDevice(String address) throws HomematicClientException;

    /**
     * Cancel loading all device metadata.
     */
    public void cancelLoadAllDeviceMetadata();

    /**
     * Loads all device, channel and datapoint metadata from the gateway.
     */
    public void loadAllDeviceMetadata() throws IOException;

    /**
     * Loads all values into the given channel.
     */
    public void loadChannelValues(HmChannel channel) throws IOException;

    /**
     * Prepares the device for reloading all values from the gateway.
     */
    public void triggerDeviceValuesReload(HmDevice device);

    /**
     * Sends the datapoint to the Homematic gateway or executes virtual datapoints.
     */
    public void sendDatapoint(HmDatapoint dp, HmDatapointConfig dpConfig, Object newValue)
            throws IOException, HomematicClientException;

    /**
     * Returns the id of the HomematicGateway.
     */
    public String getId();

    /**
     * Loads all rssi values from the gateway.
     */
    public void loadRssiValues() throws IOException;

    /**
     * Starts the connection and event tracker threads.
     */
    public void startWatchdogs();

}