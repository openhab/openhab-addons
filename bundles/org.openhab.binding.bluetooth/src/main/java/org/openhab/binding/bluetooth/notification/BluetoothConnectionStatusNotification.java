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
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;

/**
 * The {@link BluetoothConnectionStatusNotification} provides a notification of a change in the device connection state.
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
public class BluetoothConnectionStatusNotification extends BluetoothNotification {
    private ConnectionState connectionState;

    public BluetoothConnectionStatusNotification(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * Returns the connection state for this notification
     * 
     * @return the {@link ConnectionState}
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }
}
