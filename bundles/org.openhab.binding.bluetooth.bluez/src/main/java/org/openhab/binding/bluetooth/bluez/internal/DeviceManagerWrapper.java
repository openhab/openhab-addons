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
package org.openhab.binding.bluetooth.bluez.internal;

import java.util.Collection;
import java.util.List;

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

    private DeviceManager deviceManager;

    public DeviceManagerWrapper(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public synchronized Collection<BluetoothAdapter> scanForBluetoothAdapters() {
        return deviceManager.scanForBluetoothAdapters();
    }

    public synchronized @Nullable BluetoothAdapter getAdapter(BluetoothAddress address) {
        // we don't use `deviceManager.getAdapter` here since it might perform a scan if the adapter is missing.
        String addr = address.toString();
        List<BluetoothAdapter> adapters = deviceManager.getAdapters();
        if (adapters != null) {
            for (BluetoothAdapter btAdapter : adapters) {
                String btAddr = btAdapter.getAddress();
                if (addr.equalsIgnoreCase(btAddr)) {
                    return btAdapter;
                }
            }
        }
        return null;
    }

    public synchronized List<BluetoothDevice> getDevices(BluetoothAdapter adapter) {
        return deviceManager.getDevices(adapter.getAddress(), true);
    }
}
