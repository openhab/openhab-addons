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
package org.openhab.binding.bluetooth.bluegiga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.bluegiga.handler.BlueGigaBridgeHandler;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BluetoothAddressType;

/**
 * Tests fallback behavior for unknown Bluetooth address types.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class BlueGigaBluetoothDeviceTest {

    @Test
    void unknownAddressTypeAttemptsAlternatePublicAndRandomTypes() {
        assertEquals(BluetoothAddressType.GAP_ADDRESS_TYPE_RANDOM,
                BlueGigaBluetoothDevice.alternateAddressType(BluetoothAddressType.GAP_ADDRESS_TYPE_PUBLIC));
        assertEquals(BluetoothAddressType.GAP_ADDRESS_TYPE_PUBLIC,
                BlueGigaBluetoothDevice.alternateAddressType(BluetoothAddressType.GAP_ADDRESS_TYPE_RANDOM));
    }

    @Test
    @SuppressWarnings("null")
    void adapterStoppedFailsPendingCharacteristicRead() throws Exception {
        BlueGigaBridgeHandler handler = mock(BlueGigaBridgeHandler.class);
        BluetoothAddress address = new BluetoothAddress("E0:7D:EA:26:94:2F");
        BlueGigaBluetoothCharacteristic characteristic = new BlueGigaBluetoothCharacteristic(42);
        characteristic.setUUID(UUID.randomUUID());
        when(handler.bgReadCharacteristic(7, 42)).thenReturn(true);
        BlueGigaBluetoothDevice device = new BlueGigaBluetoothDevice(handler, address,
                BluetoothAddressType.GAP_ADDRESS_TYPE_PUBLIC);
        Field connectionField = BlueGigaBluetoothDevice.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.setInt(device, 7);

        try {
            CompletableFuture<byte[]> future = device.readCharacteristic(characteristic);
            device.adapterStopped();

            assertTrue(future.isCompletedExceptionally());
            assertThrows(CompletionException.class, future::join);
        } finally {
            device.dispose();
        }
    }

    @Test
    @SuppressWarnings("null")
    void adapterStoppedFailsPendingCharacteristicWrite() throws Exception {
        BlueGigaBridgeHandler handler = mock(BlueGigaBridgeHandler.class);
        BluetoothAddress address = new BluetoothAddress("E0:7D:EA:26:94:2F");
        BlueGigaBluetoothCharacteristic characteristic = new BlueGigaBluetoothCharacteristic(42);
        characteristic.setUUID(UUID.randomUUID());
        when(handler.bgWriteCharacteristic(eq(7), eq(42), any(int[].class))).thenReturn(true);
        BlueGigaBluetoothDevice device = new BlueGigaBluetoothDevice(handler, address,
                BluetoothAddressType.GAP_ADDRESS_TYPE_PUBLIC);
        Field connectionField = BlueGigaBluetoothDevice.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.setInt(device, 7);

        try {
            CompletableFuture<@Nullable Void> future = device.writeCharacteristic(characteristic, new byte[] { 1 });
            device.adapterStopped();

            assertTrue(future.isCompletedExceptionally());
            assertThrows(CompletionException.class, future::join);
        } finally {
            device.dispose();
        }
    }

    @Test
    @SuppressWarnings("null")
    void timeoutClearsTimerBeforeNotifyingHandler() throws Exception {
        BlueGigaBridgeHandler handler = mock(BlueGigaBridgeHandler.class);
        BluetoothAddress address = new BluetoothAddress("E0:7D:EA:26:94:2F");
        when(handler.bgConnect(address, BluetoothAddressType.GAP_ADDRESS_TYPE_PUBLIC)).thenReturn(true);
        BlueGigaBluetoothDevice device = new BlueGigaBluetoothDevice(handler, address, BluetoothAddressType.UNKNOWN);
        Field timerField = BlueGigaBluetoothDevice.class.getDeclaredField("connectTimer");
        timerField.setAccessible(true);
        Field timeoutTaskField = BlueGigaBluetoothDevice.class.getDeclaredField("connectTimeoutTask");
        timeoutTaskField.setAccessible(true);
        AtomicBoolean timerWasClearedBeforeNotification = new AtomicBoolean();

        doAnswer(invocation -> {
            timerWasClearedBeforeNotification.set(timerField.get(device) == null);
            device.connectionAttemptFailed();
            return null;
        }).when(handler).connectionAttemptTimedOut(address);

        try {
            assertTrue(device.connect());
            device.connectionAttemptStarted();
            ScheduledFuture<?> timer = (ScheduledFuture<?>) Objects.requireNonNull(timerField.get(device));
            ((Runnable) Objects.requireNonNull(timeoutTaskField.get(device))).run();

            assertTrue(timerWasClearedBeforeNotification.get());
            assertNull(timerField.get(device));
            timer.cancel(false);
        } finally {
            device.dispose();
        }
    }
}
