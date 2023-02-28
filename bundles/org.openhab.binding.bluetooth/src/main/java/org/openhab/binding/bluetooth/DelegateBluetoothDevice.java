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

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DelegateBluetoothDevice} is an abstract parent class for BluetoothDevice implementations
 * that delegate their functions to other BluetoothDevice instances.
 *
 * @author Connor Petty - Initial Contribution
 */
@NonNullByDefault
public abstract class DelegateBluetoothDevice extends BluetoothDevice {

    public DelegateBluetoothDevice(BluetoothAdapter adapter, BluetoothAddress address) {
        super(adapter, address);
    }

    protected abstract @Nullable BluetoothDevice getDelegate();

    @Override
    public @Nullable String getName() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getName() : null;
    }

    @Override
    public @Nullable Integer getManufacturerId() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getManufacturerId() : null;
    }

    @Override
    public @Nullable Integer getRssi() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getRssi() : null;
    }

    @Override
    public @Nullable Integer getTxPower() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getTxPower() : null;
    }

    @Override
    public @Nullable BluetoothService getServices(UUID uuid) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getServices(uuid) : null;
    }

    @Override
    public Collection<BluetoothService> getServices() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getServices() : Collections.emptySet();
    }

    @Override
    public boolean supportsService(UUID uuid) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.supportsService(uuid) : false;
    }

    @Override
    public ConnectionState getConnectionState() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getConnectionState() : ConnectionState.DISCOVERED;
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
    public boolean discoverServices() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.discoverServices() : false;
    }

    @Override
    public CompletableFuture<byte[]> readCharacteristic(BluetoothCharacteristic characteristic) {
        BluetoothDevice delegate = getDelegate();
        if (delegate == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Delegate is null"));
        }
        return delegate.readCharacteristic(characteristic);
    }

    @Override
    public CompletableFuture<@Nullable Void> writeCharacteristic(BluetoothCharacteristic characteristic, byte[] value) {
        BluetoothDevice delegate = getDelegate();
        if (delegate == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Delegate is null"));
        }
        return delegate.writeCharacteristic(characteristic, value);
    }

    @Override
    public boolean isNotifying(BluetoothCharacteristic characteristic) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.isNotifying(characteristic) : false;
    }

    @Override
    public CompletableFuture<@Nullable Void> enableNotifications(BluetoothCharacteristic characteristic) {
        BluetoothDevice delegate = getDelegate();
        if (delegate == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Delegate is null"));
        }
        return delegate.enableNotifications(characteristic);
    }

    @Override
    public CompletableFuture<@Nullable Void> disableNotifications(BluetoothCharacteristic characteristic) {
        BluetoothDevice delegate = getDelegate();
        if (delegate == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Delegate is null"));
        }
        return delegate.disableNotifications(characteristic);
    }

    @Override
    public boolean enableNotifications(BluetoothDescriptor descriptor) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.enableNotifications(descriptor) : false;
    }

    @Override
    public boolean disableNotifications(BluetoothDescriptor descriptor) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.disableNotifications(descriptor) : false;
    }

    @Override
    protected boolean addService(BluetoothService service) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.addService(service) : false;
    }

    @Override
    protected Collection<BluetoothDeviceListener> getListeners() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getListeners() : Collections.emptySet();
    }

    @Override
    public @Nullable BluetoothCharacteristic getCharacteristic(UUID uuid) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getCharacteristic(uuid) : null;
    }

    @Override
    public boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.awaitConnection(timeout, unit) : false;
    }

    @Override
    public boolean awaitServiceDiscovery(long timeout, TimeUnit unit) throws InterruptedException {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.awaitServiceDiscovery(timeout, unit) : false;
    }

    @Override
    public boolean isServicesDiscovered() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.isServicesDiscovered() : false;
    }

    @Override
    protected void dispose() {
        BluetoothDevice delegate = getDelegate();
        if (delegate != null) {
            delegate.dispose();
        }
    }
}
