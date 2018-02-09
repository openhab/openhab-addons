/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.tellstick.device.iface.Device;

/**
 * Interface for the telldus bridge modules
 *
 * @author Jarle Hjortland
 *
 */
public interface TelldusBridgeHandler {

    /**
     * TelldusDeviceController
     * Add a status listener.
     *
     * @param deviceStatusListener
     * @return
     */
    boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener);

    /**
     * Remove a status listener.
     *
     * @param deviceStatusListener
     * @return
     */
    boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener);

    /**
     * Get a device from the bridgehandler.
     *
     * @param serialNumber
     * @return
     */
    Device getDevice(String serialNumber);

    /**
     * Get a sensor from the bridgehandler.
     *
     * @param serialNumber
     * @return
     */
    Device getSensor(String deviceUUId);

    /**
     * Tell the bridge to rescan for new devices.
     *
     */
    void rescanTelldusDevices();

    /**
     * Get the controller to communicate with devices.
     *
     * @return
     */
    TelldusDeviceController getController();

    /**
     * Send a command to the controller.
     *
     * @param channelUID
     * @param command
     */
    void handleCommand(ChannelUID channelUID, Command command);

}