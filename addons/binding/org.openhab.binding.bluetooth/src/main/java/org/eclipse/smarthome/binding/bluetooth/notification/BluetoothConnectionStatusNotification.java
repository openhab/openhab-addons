/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bluetooth.notification;

import org.eclipse.smarthome.binding.bluetooth.BluetoothDevice.ConnectionState;

/**
 * The {@link BluetoothConnectionStatusNotification} provides a notification of a change in the device connection state.
 *
 * @author Chris Jackson - Initial contribution
 */
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
    };
}
