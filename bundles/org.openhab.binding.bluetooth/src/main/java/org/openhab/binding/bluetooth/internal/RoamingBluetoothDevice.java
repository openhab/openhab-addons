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
package org.openhab.binding.bluetooth.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;

/**
 * The {@link RoamingBluetoothDevice} acts as a roaming device by delegating
 * its operations to actual adapters.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class RoamingBluetoothDevice extends BluetoothDevice {

    private Map<BluetoothDevice, Listener> devices = new ConcurrentHashMap<>();

    private @Nullable BluetoothDevice currentDelegate = null;

    protected RoamingBluetoothDevice(RoamingBluetoothBridgeHandler roamingAdapter, BluetoothAddress address) {
        super(roamingAdapter, address);
    }

    public void addBluetoothDevice(BluetoothDevice device) {
        device.addListener(devices.computeIfAbsent(device, Listener::new));
    }

    public void removeBluetoothDevice(BluetoothDevice device) {
        device.removeListener(devices.remove(device));
    }

    @Override
    public boolean connect() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.connect() : false;
    }

    @Override
    public boolean disconnect() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.disconnect() : false;
    }

    @Override
    public boolean enableNotifications(@Nullable BluetoothCharacteristic characteristic) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.enableNotifications(characteristic) : false;
    }

    @Override
    public boolean enableNotifications(@Nullable BluetoothDescriptor descriptor) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.enableNotifications(descriptor) : false;
    }

    @Override
    public boolean disableNotifications(@Nullable BluetoothCharacteristic characteristic) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.disableNotifications(characteristic) : false;
    }

    @Override
    public boolean disableNotifications(@Nullable BluetoothDescriptor descriptor) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.disableNotifications(descriptor) : false;
    }

    @Override
    public boolean discoverServices() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.discoverServices() : false;
    }

    @Override
    public @Nullable ConnectionState getConnectionState() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getConnectionState() : null;
    }

    @Override
    public @Nullable Integer getRssi() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getRssi() : null;
    }

    private @Nullable BluetoothDevice getDelegate() {
        BluetoothDevice newDelegate = null;
        int newRssi = Integer.MIN_VALUE;
        for (BluetoothDevice device : devices.keySet()) {
            ConnectionState state = device.getConnectionState();
            if (state == ConnectionState.CONNECTING || state == ConnectionState.CONNECTED) {
                return device;
            }
            Integer rssi = device.getRssi();
            if (rssi != null && (newDelegate == null || rssi > newRssi)) {
                newRssi = rssi;
                newDelegate = device;
            }
        }
        currentDelegate = newDelegate;
        notifyListeners(BluetoothEventType.ADAPTER_CHANGED, getAdapter(newDelegate));

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
        return getAdapter(currentDelegate);
    }

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
            if (device == getDelegate()) {
                notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic,
                BluetoothCompletionStatus status) {
            if (device == getDelegate()) {
                notifyListeners(BluetoothEventType.CHARACTERISTIC_READ_COMPLETE, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
                BluetoothCompletionStatus status) {
            if (device == getDelegate()) {
                notifyListeners(BluetoothEventType.CHARACTERISTIC_WRITE_COMPLETE, characteristic);
            }
        }

        @Override
        public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
            if (device == getDelegate()) {
                notifyListeners(BluetoothEventType.CHARACTERISTIC_UPDATED, characteristic);
            }
        }

        @Override
        public void onDescriptorUpdate(BluetoothDescriptor bluetoothDescriptor) {
            if (device == getDelegate()) {
                notifyListeners(BluetoothEventType.DESCRIPTOR_UPDATED, bluetoothDescriptor);
            }
        }

        @Override
        public void onAdapterChanged(BluetoothAdapter adapter) {
        }

    }
}
