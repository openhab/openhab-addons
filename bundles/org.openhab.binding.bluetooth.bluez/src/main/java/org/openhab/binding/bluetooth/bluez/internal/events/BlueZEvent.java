/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.bluez.internal.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;

/**
 * The {@link BlueZEvent} class represents an event from dbus due to
 * changes in the properties of a bluetooth device.
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public abstract class BlueZEvent {

    private String dbusPath;

    private @Nullable BluetoothAddress device;
    private @Nullable String adapterName;

    public BlueZEvent(String dbusPath) {
        this.dbusPath = dbusPath;

        // the rest of the code should be equivalent to parsing with the following regex:
        // "/org/bluez/(?<adapterName>[^/]+)(/dev_(?<deviceMac>[^/]+).*)?"
        if (!dbusPath.startsWith("/org/bluez/")) {
            return;
        }
        int start = dbusPath.indexOf('/', 11);
        if (start == -1) {
            this.adapterName = dbusPath.substring(11);
            return;
        } else {
            this.adapterName = dbusPath.substring(11, start);
        }
        start++;
        int end = dbusPath.indexOf('/', start);
        String mac;
        if (end == -1) {
            mac = dbusPath.substring(start);
        } else {
            mac = dbusPath.substring(start, end);
        }
        if (!mac.startsWith("dev_")) {
            return;
        }
        mac = mac.substring(4); // trim off the "dev_" prefix
        if (!mac.isEmpty()) {
            this.device = new BluetoothAddress(mac.replace('_', ':').toUpperCase());
        }
    }

    public String getDbusPath() {
        return dbusPath;
    }

    public @Nullable BluetoothAddress getDevice() {
        return device;
    }

    public @Nullable String getAdapterName() {
        return adapterName;
    }

    public abstract void dispatch(BlueZEventListener listener);

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + dbusPath;
    }
}
