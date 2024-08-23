/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.handler;

import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.tellstick.device.iface.Device;

/**
 * Interface for the telldus bridge modules
 *
 * @author Jarle Hjortland - Initial contribution
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
     * @param deviceUUId
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
