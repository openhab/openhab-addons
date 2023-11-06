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
package org.openhab.binding.bluetooth;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCharacteristic.GattCharacteristic;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;

/**
 * Mock implementation of a {@link BluetoothDevice}.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class MockBluetoothDevice extends BaseBluetoothDevice {

    private AtomicBoolean servicesDiscovered = new AtomicBoolean(false);

    /**
     * This is the name that returned in the DEVICE_NAME characteristic
     */
    private @Nullable String deviceName = null;

    public MockBluetoothDevice(BluetoothAdapter adapter, BluetoothAddress address) {
        super(adapter, address);
    }

    @Override
    public boolean connect() {
        this.connectionState = ConnectionState.CONNECTED;
        notifyListeners(BluetoothEventType.CONNECTION_STATE,
                new BluetoothConnectionStatusNotification(ConnectionState.CONNECTED));

        discoverServices();

        return true;
    }

    @Override
    public boolean discoverServices() {
        if (!servicesDiscovered.getAndSet(true)) {
            populateServices();
            notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
        }
        return true;
    }

    protected void populateServices() {
        if (deviceName != null) {
            BluetoothService service = new BluetoothService(BluetoothService.GattService.DEVICE_INFORMATION.getUUID());
            service.addCharacteristic(new BluetoothCharacteristic(GattCharacteristic.DEVICE_NAME.getUUID(), 0));
            addService(service);
        }
    }

    @Override
    public boolean disconnect() {
        return true;
    }

    @Override
    public CompletableFuture<byte[]> readCharacteristic(BluetoothCharacteristic characteristic) {
        if (characteristic.getGattCharacteristic() == GattCharacteristic.DEVICE_NAME) {
            String name = deviceName;
            if (name != null) {
                return CompletableFuture.completedFuture(name.getBytes(StandardCharsets.UTF_8));
            } else {
                return CompletableFuture.completedFuture(new byte[0]);
            }
        }
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    protected void notifyListeners(BluetoothEventType event, Object... args) {
        CompletableFuture.runAsync(() -> super.notifyListeners(event, args));
    }

    @Override
    public CompletableFuture<@Nullable Void> writeCharacteristic(BluetoothCharacteristic characteristic, byte[] value) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<@Nullable Void> enableNotifications(BluetoothCharacteristic characteristic) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public boolean isNotifying(BluetoothCharacteristic characteristic) {
        return false;
    }

    @Override
    public CompletableFuture<@Nullable Void> disableNotifications(BluetoothCharacteristic characteristic) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public boolean enableNotifications(BluetoothDescriptor descriptor) {
        return false;
    }

    @Override
    public boolean disableNotifications(BluetoothDescriptor descriptor) {
        return false;
    }
}
