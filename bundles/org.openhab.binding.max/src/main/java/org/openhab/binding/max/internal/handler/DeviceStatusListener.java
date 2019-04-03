/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.max.internal.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.max.internal.device.Device;

/**
 * The {@link DeviceStatusListener} is notified when a device status has changed
 * or a device has been removed or added.
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
public interface DeviceStatusListener {

    /**
     * This method is called whenever the state of the given device has changed.
     *
     * @param bridge
     *            The MAX! Cube bridge the changed device is connected to.
     * @param device
     *            The device which received the state update.
     */
    public void onDeviceStateChanged(ThingUID bridge, Device device);

    /**
     * This method us called whenever a device is removed.
     *
     * @param bridge
     *            The MAX! Cube bridge the removed device was connected to.
     * @param device
     *            The device which is removed.
     */
    public void onDeviceRemoved(MaxCubeBridgeHandler bridge, Device device);

    /**
     * This method us called whenever a device is added.
     *
     * @param bridge
     *            The MAX! Cube bridge the added device was connected to.
     * @param device
     *            The device which is added.
     */
    public void onDeviceAdded(Bridge bridge, Device device);

    /**
     * This method us called whenever a device config is updated.
     *
     * @param bridge
     *            The MAX! Cube bridge the device was connected to.
     * @param device
     *            The device which config is changed.
     */
    public void onDeviceConfigUpdate(Bridge bridge, Device device);

}
