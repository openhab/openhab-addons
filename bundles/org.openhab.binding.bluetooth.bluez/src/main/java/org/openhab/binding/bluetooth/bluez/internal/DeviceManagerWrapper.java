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
package org.openhab.binding.bluetooth.bluez.internal;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;

import com.github.hypfvieh.bluetooth.DeviceManager;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;

/**
 * This is a threadsafe wrapper for a {@link DeviceManager} that also only exposes the methods
 * required to implement this binding.
 *
 * @author Connor Petty - Initial Contribution
 */
@NonNullByDefault
public class DeviceManagerWrapper {

    private @Nullable DeviceManager deviceManager;

    public DeviceManagerWrapper(@Nullable DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @SuppressWarnings("null")
    public synchronized Collection<BluetoothAdapter> scanForBluetoothAdapters() {
        if (deviceManager != null) {
            return deviceManager.scanForBluetoothAdapters();
        } else {
            return Set.of();
        }
    }

    public synchronized @Nullable BluetoothAdapter getAdapter(BluetoothAddress address) {
        DeviceManager devMgr = deviceManager;
        if (devMgr != null) {
            // we don't use `deviceManager.getAdapter` here since it might perform a scan if the adapter is missing.
            String addr = address.toString();
            List<BluetoothAdapter> adapters = devMgr.getAdapters();
            if (adapters != null) {
                for (BluetoothAdapter btAdapter : adapters) {
                    String btAddr = btAdapter.getAddress();
                    if (addr.equalsIgnoreCase(btAddr)) {
                        return btAdapter;
                    }
                }
            }
        }
        return null;
    }

    public synchronized List<BluetoothDevice> getDevices(BluetoothAdapter adapter) {
        if (deviceManager != null) {
            return deviceManager.getDevices(adapter.getAddress(), true);
        } else {
            return List.of();
        }
    }
}
