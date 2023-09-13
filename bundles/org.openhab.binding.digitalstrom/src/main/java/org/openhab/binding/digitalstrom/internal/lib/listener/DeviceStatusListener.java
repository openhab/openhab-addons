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
package org.openhab.binding.digitalstrom.internal.lib.listener;

import org.openhab.binding.digitalstrom.internal.lib.structure.devices.GeneralDeviceInformation;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceStateUpdate;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ChangeableDeviceConfigEnum;

/**
 * The {@link DeviceStatusListener} is notified, if a
 * {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device} status or configuration has changed,
 * if a scene configuration is added to a {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device}
 * or if a device has been added or removed. The {@link DeviceStatusListener}
 * can be also registered by a {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.Circuit}
 * to get informed by configuration or status changes.
 * <p>
 * By implementation with the id {@link #DEVICE_DISCOVERY} this listener can be used as a device discovery to get
 * informed, if a new {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device} or
 * {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.Circuit} is added or removed from the
 * digitalSTROM-System.<br>
 * For that the {@link DeviceStatusListener} has to be registered on the
 * {@link org.openhab.binding.digitalstrom.internal.lib.manager.DeviceStatusManager#registerDeviceListener(DeviceStatusListener)}.
 * Then the {@link DeviceStatusListener} gets informed by the methods {@link #onDeviceAdded(GeneralDeviceInformation)}
 * and {@link #onDeviceRemoved(GeneralDeviceInformation)}.
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface DeviceStatusListener {

    /**
     * ID of the device discovery listener.
     */
    static final String DEVICE_DISCOVERY = "DeviceDiscovery";

    /**
     * This method is called whenever the state of the
     * {@link org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device} has changed and passes the new
     * device state as an {@link DeviceStateUpdate} object.
     *
     * @param deviceStateUpdate new device status
     */
    void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate);

    /**
     * This method is called whenever a device is removed.
     *
     * @param device which is removed
     */
    void onDeviceRemoved(GeneralDeviceInformation device);

    /**
     * This method is called whenever a device is added.
     *
     * @param device which is added
     */
    void onDeviceAdded(GeneralDeviceInformation device);

    /**
     * This method is called whenever a configuration of a device has changed. What configuration has changed
     * can be see by the given parameter whatConfig to handle the change.<br>
     * Please have a look at {@link ChangeableDeviceConfigEnum} to see what configuration are changeable.
     *
     * @param whatConfig has changed
     */
    void onDeviceConfigChanged(ChangeableDeviceConfigEnum whatConfig);

    /**
     * This method is called whenever a scene configuration is added to a device.
     *
     * @param sceneID of a read scene configuration
     */
    void onSceneConfigAdded(short sceneID);

    /**
     * Returns the id of this {@link DeviceStatusListener}.
     *
     * @return the device listener id
     */
    String getDeviceStatusListenerID();
}
