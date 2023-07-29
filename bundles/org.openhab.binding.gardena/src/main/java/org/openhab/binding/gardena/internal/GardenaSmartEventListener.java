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
package org.openhab.binding.gardena.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gardena.internal.model.dto.Device;

/**
 * Listener with methods called from events within the {@link GardenaSmart} class.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public interface GardenaSmartEventListener {

    /**
     * Called when a device has been updated.
     */
    void onDeviceUpdated(Device device);

    /**
     * Called when a new device has been detected.
     */
    void onNewDevice(Device device);

    /**
     * Called when an unrecoverable error occurs.
     */
    void onError();
}
