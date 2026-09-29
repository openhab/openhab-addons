/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.function.Consumer;

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

    public synchronized Collection<BluetoothAdapter> scanForBluetoothAdapters() {
        DeviceManager deviceManager = this.deviceManager;
        if (deviceManager != null) {
            return deviceManager.scanForBluetoothAdapters();
        } else {
            return Set.of();
        }
    }

    public synchronized @Nullable BluetoothAdapter getAdapter(BluetoothAddress address) {
        DeviceManager deviceManager = this.deviceManager;
        if (deviceManager != null) {
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
        }
        return null;
    }

    public synchronized List<BluetoothDevice> getDevices(BluetoothAdapter adapter) {
        DeviceManager deviceManager = this.deviceManager;
        if (deviceManager != null) {
            return deviceManager.getDevices(adapter.getAddress(), true);
        } else {
            return List.of();
        }
    }

    void setLazyScan(boolean lazyScan) {
        DeviceManager deviceManager = this.deviceManager;
        if (deviceManager != null) {
            deviceManager.setLazyScan(lazyScan);
        }
    }

    /**
     * Registers a listener invoked with a device's DBus object path when BlueZ removes that device
     * object (ObjectManager InterfacesRemoved). Lets the binding invalidate its own cached state for
     * a device when the underlying BlueZ object disappears.
     */
    public synchronized void registerDeviceRemovedListener(Consumer<String> listener) {
        DeviceManager deviceManager = this.deviceManager;
        if (deviceManager != null) {
            deviceManager.registerDeviceRemovedListener(listener);
        }
    }

    /**
     * Registers a listener invoked with an adapter's DBus object path when BlueZ removes that adapter
     * object (ObjectManager InterfacesRemoved), e.g. a USB dongle being unplugged. Lets the binding
     * invalidate its cached adapter proxy and the devices found through it.
     */
    public synchronized void registerAdapterRemovedListener(Consumer<String> listener) {
        DeviceManager deviceManager = this.deviceManager;
        if (deviceManager != null) {
            deviceManager.registerAdapterRemovedListener(listener);
        }
    }

    public synchronized void unregisterDeviceRemovedListener(Consumer<String> listener) {
        DeviceManager deviceManager = this.deviceManager;
        if (deviceManager != null) {
            deviceManager.unregisterDeviceRemovedListener(listener);
        }
    }

    public synchronized void unregisterAdapterRemovedListener(Consumer<String> listener) {
        DeviceManager deviceManager = this.deviceManager;
        if (deviceManager != null) {
            deviceManager.unregisterAdapterRemovedListener(listener);
        }
    }
}
