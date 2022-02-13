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
package org.openhab.binding.bluetooth.roaming.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.DelegateBluetoothDevice;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;

/**
 * The {@link RoamingBluetoothDevice} acts as a roaming device by delegating
 * its operations to actual adapters.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class RoamingBluetoothDevice extends DelegateBluetoothDevice {

    private final Map<BluetoothDevice, Listener> devices = new ConcurrentHashMap<>();

    private final List<BluetoothDeviceListener> eventListeners = new CopyOnWriteArrayList<>();

    private final AtomicReference<@Nullable BluetoothDevice> currentDelegateRef = new AtomicReference<>();

    protected RoamingBluetoothDevice(RoamingBridgeHandler roamingAdapter, BluetoothAddress address) {
        super(roamingAdapter, address);
    }

    public void addBluetoothDevice(BluetoothDevice device) {
        device.addListener(Objects.requireNonNull(devices.computeIfAbsent(device, Listener::new)));
    }

    public void removeBluetoothDevice(BluetoothDevice device) {
        BluetoothDeviceListener listener = devices.remove(device);
        if (listener != null) {
            device.removeListener(listener);
        }
    }

    @Override
    protected Collection<BluetoothDeviceListener> getListeners() {
        return eventListeners;
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    protected @Nullable BluetoothDevice getDelegate() {
        BluetoothDevice newDelegate = null;
        int newRssi = Integer.MIN_VALUE;
        for (BluetoothDevice device : devices.keySet()) {
            ConnectionState state = device.getConnectionState();
            if (state == ConnectionState.CONNECTING || state == ConnectionState.CONNECTED) {
                newDelegate = device;
                break;
            }
            Integer rssi = device.getRssi();
            if (rssi != null && (newDelegate == null || rssi > newRssi)) {
                newRssi = rssi;
                newDelegate = device;
            }
        }
        BluetoothDevice oldDelegate = currentDelegateRef.getAndSet(newDelegate);
        if (oldDelegate != newDelegate) { // using reference comparison is valid in this case
            notifyListeners(BluetoothEventType.ADAPTER_CHANGED, getAdapter(newDelegate));
        }
        return newDelegate;
    }

    private BluetoothAdapter getAdapter(@Nullable BluetoothDevice delegate) {
        if (delegate != null) {
            return delegate.getAdapter();
        }
        // as a last resort we return our "actual" adapter
        return super.getAdapter();
    }

    @Override
    public BluetoothAdapter getAdapter() {
        return getAdapter(currentDelegateRef.get());
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private class Listener implements BluetoothDeviceListener {

        private BluetoothDevice device;

        public Listener(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
            if (device == getDelegate()) {
                notifyListeners(BluetoothEventType.SCAN_RECORD, scanNotification);
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
            if (device == getDelegate()) {
                notifyListeners(BluetoothEventType.CONNECTION_STATE, connectionNotification);
            }
        }

        @Override
        public void onServicesDiscovered() {
            device.getServices().forEach(RoamingBluetoothDevice.this::addService);
            if (device == getDelegate()) {
                notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] value) {
            if (device == getDelegate()) {
                notifyListeners(BluetoothEventType.CHARACTERISTIC_UPDATED, characteristic, value);
            }
        }

        @Override
        public void onDescriptorUpdate(BluetoothDescriptor bluetoothDescriptor, byte[] value) {
            if (device == getDelegate()) {
                notifyListeners(BluetoothEventType.DESCRIPTOR_UPDATED, bluetoothDescriptor, value);
            }
        }

        @Override
        public void onAdapterChanged(BluetoothAdapter adapter) {
            // do nothing since we are the ones that are supposed to trigger this
        }
    }
}
