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
     * Loads the value of the given {@link HmDatapoint} from the device.
     * 
     * @param dp The HmDatapoint that shall be loaded
     */
    public void loadDatapointValue(HmDatapoint dp) throws IOException;

    /**
     * Reenumerates the set of VALUES datapoints for the given channel.
     */
    public void updateChannelValueDatapoints(HmChannel channel) throws IOException;

    /**
     * Prepares the device for reloading all values from the gateway.
     */
    public void triggerDeviceValuesReload(HmDevice device);

    /**
     * Sends the datapoint to the Homematic gateway or executes virtual datapoints.
     * 
     * @param dp The datapoint to send/execute
     * @param dpConfig The configuration of the datapoint
     * @param newValue The new value for the datapoint
     * @param rxMode The rxMode with which the value should be sent to the device
     *            ({@link HomematicBindingConstants#RX_BURST_MODE "BURST"} for burst mode,
     *            {@link HomematicBindingConstants#RX_WAKEUP_MODE "WAKEUP"} for wakeup mode, or null for the default
     *            mode)
     */
    public void sendDatapoint(HmDatapoint dp, HmDatapointConfig dpConfig, Object newValue, String rxMode)
            throws IOException, HomematicClientException;

    /**
     * Returns the id of the HomematicGateway.
     */
    public String getId();

    /**
     * Set install mode of homematic controller. During install mode the
     * controller will accept any device (normal mode)
     * 
     * @param enable <i>true</i> will start install mode, whereas <i>false</i>
     *            will stop it
     * @param seconds specify how long the install mode should last
     * @throws IOException if RpcClient fails to propagate command
     */
    public void setInstallMode(boolean enable, int seconds) throws IOException;

    /**
     * Get current install mode of homematic contoller
     * 
     * @return the current time in seconds that the controller remains in
     *         <i>install_mode==true</i>, respectively <i>0</i> in case of
     *         <i>install_mode==false</i>
     * @throws IOException if RpcClient fails to propagate command
     */
    public int getInstallMode() throws IOException;

    /**
     * Loads all rssi values from the gateway.
     */
    public void loadRssiValues() throws IOException;

    /**
     * Starts the connection and event tracker threads.
     */
    public void startWatchdogs();

    /**
     * Deletes the device from the gateway.
     *
     * @param address The address of the device to be deleted
     * @param reset <i>true</i> will perform a factory reset on the device before deleting it.
     * @param force <i>true</i> will delete the device even if it is not reachable.
     * @param defer <i>true</i> will delete the device once it becomes available.
     */
    public void deleteDevice(String address, boolean reset, boolean force, boolean defer);
}
