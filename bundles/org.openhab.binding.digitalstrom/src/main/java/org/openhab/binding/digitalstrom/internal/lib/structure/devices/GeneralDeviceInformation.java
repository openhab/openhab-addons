/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices;

import org.openhab.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;

/**
 * The {@link GeneralDeviceInformation} interface contains all informations of digitalSTROM devices, which are
 * identical for all device types. It also contains the methods to implement the mechanism of the
 * {@link DeviceStatusListener}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface GeneralDeviceInformation {

    /**
     * Returns the user defined name of this device.
     *
     * @return name of this device
     */
    String getName();

    /**
     * Sets the name of this device;
     *
     * @param name to set
     */
    void setName(String name);

    /**
     * Returns the dSID of this device.
     *
     * @return {@link DSID} dSID
     */
    DSID getDSID();

    /**
     * Returns the dSUID of this device.
     *
     * @return dSID
     */
    String getDSUID();

    /**
     * This device is available in his zone or not.
     * Every 24h the dSM (meter) checks, if the devices are
     * plugged in
     *
     * @return true, if device is available otherwise false
     */
    Boolean isPresent();

    /**
     * Sets this device is available in his zone or not.
     *
     * @param isPresent (true = available | false = not available)
     */
    void setIsPresent(boolean isPresent);

    /**
     * Register a {@link DeviceStatusListener} to this {@link Device}.
     *
     * @param deviceStatuslistener to register
     */
    void registerDeviceStatusListener(DeviceStatusListener deviceStatuslistener);

    /**
     * Unregister the {@link DeviceStatusListener} to this {@link Device} if it exists.
     *
     * @return the unregistered {@link DeviceStatusListener} or null if no one was registered
     */
    DeviceStatusListener unregisterDeviceStatusListener();

    /**
     * Returns true, if a {@link DeviceStatusListener} is registered to this {@link Device}, otherwise false.
     *
     * @return return true, if a lister is registered, otherwise false
     */
    boolean isListenerRegisterd();

    /**
     * Returns true, if this device is valid, otherwise false.
     *
     * @return true, if valid
     */
    Boolean isValid();

    /**
     * Sets the valid state.
     *
     * @param isValid the new valid state
     */
    void setIsValid(boolean isValid);

    /**
     * Returns the in the digitalSTROM web interface displayed dSID.
     *
     * @return displayed dSID
     */
    String getDisplayID();

    /**
     * Returns the registered {@link DeviceStatusListener} or null, if no {@link DeviceStatusListener} is registered
     *
     * @return the registered {@link DeviceStatusListener} or null
     */
    DeviceStatusListener getDeviceStatusListener();
}
