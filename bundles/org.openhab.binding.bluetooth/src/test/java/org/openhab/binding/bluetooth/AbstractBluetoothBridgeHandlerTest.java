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
package org.openhab.binding.bluetooth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Bridge;

/**
 * Tests {@link AbstractBluetoothBridgeHandler}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class AbstractBluetoothBridgeHandlerTest {

    @Test
    void forEachDeviceDoesNotHoldRegistryLockDuringCallback() throws Exception {
        TestBridgeHandler handler = new TestBridgeHandler();
        BluetoothAddress firstAddress = new BluetoothAddress("12:34:56:78:9A:BC");
        BluetoothAddress secondAddress = new BluetoothAddress("AB:CD:EF:12:34:56");
        handler.getDevice(firstAddress);

        try {
            handler.forEachDevice(device -> {
                CompletableFuture<MockBluetoothDevice> lookup = CompletableFuture
                        .supplyAsync(() -> handler.getDevice(secondAddress));
                MockBluetoothDevice lookedUpDevice = assertDoesNotThrow(() -> lookup.get(5, TimeUnit.SECONDS));
                assertEquals(secondAddress, lookedUpDevice.getAddress());
            });
        } finally {
            handler.dispose();
        }
    }

    private static class TestBridgeHandler extends AbstractBluetoothBridgeHandler<MockBluetoothDevice> {
        TestBridgeHandler() {
            super(org.mockito.Mockito.mock(Bridge.class));
        }

        @Override
        protected MockBluetoothDevice createDevice(BluetoothAddress address) {
            return new MockBluetoothDevice(this, address);
        }

        @Override
        public @Nullable BluetoothAddress getAddress() {
            return new BluetoothAddress("00:00:00:00:00:00");
        }
    }
}
