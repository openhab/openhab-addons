/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vwweconnect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vwweconnect.internal.model.BaseVehicle;

/**
 * The {@link DeviceStatusListener} is notified when a device status has changed
 * or a device has been removed or added.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public interface DeviceStatusListener {

    /**
     * This method is called whenever the state of the given device has changed.
     *
     * @param thing
     *            The thing that was changed.
     */
    void onDeviceStateChanged(BaseVehicle thing);

    /**
     * This method us called whenever a device is removed.
     *
     * @param thing
     *            The thing that is removed
     */
    void onDeviceRemoved(BaseVehicle thing);

    /**
     * This method us called whenever a device is added.
     *
     * @param thing
     *            The thing which is added.
     */
    void onDeviceAdded(BaseVehicle thing);
}
