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
package org.openhab.binding.gardena.internal;

import org.openhab.binding.gardena.internal.model.Device;

/**
 * Listener with methods called from events within the {@link GardenaSmart} class.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface GardenaSmartEventListener {

    /**
     * Called when a device has been updated.
     */
    public void onDeviceUpdated(Device device);

    /**
     * Called when a new device has been detected.
     */
    public void onNewDevice(Device device);

    /**
     * Called when a device has been deleted.
     */
    public void onDeviceDeleted(Device device);

    /**
     * Called when the connection is lost to Gardena Smart Home.
     */
    public void onConnectionLost();

    /**
     * Called when the connection is resumed to Gardena Smart Home.
     */
    public void onConnectionResumed();
}
