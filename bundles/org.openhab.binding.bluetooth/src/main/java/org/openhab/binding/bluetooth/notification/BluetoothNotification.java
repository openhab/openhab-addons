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
package org.openhab.binding.bluetooth.notification;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;

/**
 * The {@link BluetoothNotification} is the base class for Bluetooth device notifications
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
public abstract class BluetoothNotification {

    protected @Nullable BluetoothAddress address;

    /**
     * Returns the bluetooth address for this frame
     */
    public @Nullable BluetoothAddress getAddress() {
        return address;
    }
}
