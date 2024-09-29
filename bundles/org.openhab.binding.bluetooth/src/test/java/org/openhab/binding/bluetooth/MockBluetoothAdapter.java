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
package org.openhab.binding.bluetooth;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingUID;

/**
 * Mock implementation of a {@link BluetoothAdapter}.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class MockBluetoothAdapter implements BluetoothAdapter {

    private Map<BluetoothAddress, MockBluetoothDevice> devices = new ConcurrentHashMap<>();
    private BluetoothAddress address = TestUtils.randomAddress();
    private ThingUID uid = TestUtils.randomThingUID();

    @Override
    public ThingUID getUID() {
        return uid;
    }

    @Override
    public void addDiscoveryListener(BluetoothDiscoveryListener listener) {
    }

    @Override
    public void removeDiscoveryListener(@Nullable BluetoothDiscoveryListener listener) {
    }

    @Override
    public void scanStart() {
    }

    @Override
    public void scanStop() {
    }

    @Override
    public @Nullable BluetoothAddress getAddress() {
        return address;
    }

    @Override
    public MockBluetoothDevice getDevice(BluetoothAddress address) {
        return Objects.requireNonNull(devices.computeIfAbsent(address, addr -> new MockBluetoothDevice(this, addr)));
    }

    @Override
    public boolean hasHandlerForDevice(BluetoothAddress address) {
        return false;
    }

    @Override
    public @Nullable String getLocation() {
        return null;
    }

    @Override
    public @Nullable String getLabel() {
        return null;
    }
}
