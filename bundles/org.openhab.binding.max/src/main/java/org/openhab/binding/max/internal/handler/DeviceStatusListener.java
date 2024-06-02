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
package org.openhab.binding.max.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link DeviceStatusListener} is notified when a device status has changed
 * or a device has been removed or added.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public interface DeviceStatusListener {

    /**
     * This method is called whenever the state of the given device has changed.
     *
     * @param bridge The MAX! Cube bridge the changed device is connected to
     * @param device The device which received the state update
     */
    void onDeviceStateChanged(ThingUID bridge, Device device);

    /**
     * This method is called whenever a device is removed.
     *
     * @param bridge The MAX! Cube bridge the removed device was connected to
     * @param device The device which is removed
     */
    void onDeviceRemoved(MaxCubeBridgeHandler bridge, Device device);

    /**
     * This method is called whenever a device is added.
     *
     * @param bridge The MAX! Cube bridge the added device was connected to
     * @param device The device which is added
     */
    void onDeviceAdded(Bridge bridge, Device device);

    /**
     * This method is called whenever a device config is updated.
     *
     * @param bridge The MAX! Cube bridge the device was connected to
     * @param device The device which config is changed
     */
    void onDeviceConfigUpdate(Bridge bridge, Device device);
}
