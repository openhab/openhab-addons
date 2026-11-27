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
package org.openhab.binding.bluetooth.bluegiga.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaTransactionManager;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaConnectDirectResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaEndProcedureCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaEndProcedureResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BgApiResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BluetoothAddressType;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.ConnectionStatusFlag;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;

/**
 * Tests connection-attempt state handling in {@link BlueGigaBridgeHandler}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class BlueGigaBridgeHandlerTest {

    @Test
    void wrongStateMeansNoGapProcedureIsActive() {
        assertTrue(BlueGigaBridgeHandler.isGapProcedureEnded(BgApiResponse.WRONG_STATE));
        assertTrue(BlueGigaBridgeHandler.isGapProcedureEnded(BgApiResponse.SUCCESS));
        assertFalse(BlueGigaBridgeHandler.isGapProcedureEnded(BgApiResponse.INVALID_PARAM));
    }

    @Test
    void activeConnectionAddressIsDetectedAsDuplicate() {
        BluetoothAddress activeAddress = new BluetoothAddress("E0:7D:EA:26:94:2F");
        BluetoothAddress otherAddress = new BluetoothAddress("58:93:D8:8A:F1:C2");

        assertTrue(BlueGigaBridgeHandler.isConnectionAttemptAlreadyActive(activeAddress, activeAddress));
        assertFalse(BlueGigaBridgeHandler.isConnectionAttemptAlreadyActive(otherAddress, activeAddress));
        assertFalse(BlueGigaBridgeHandler.isConnectionAttemptAlreadyActive(activeAddress, null));
    }

    @Test
    void clearConnectionAttemptUsesAddressValue() {
        BluetoothAddress trackedAddress = new BluetoothAddress("58:93:D8:8A:F1:C2");
        BluetoothAddress eventAddress = new BluetoothAddress("58:93:D8:8A:F1:C2");
        AtomicReference<@Nullable BluetoothAddress> connectingAddress = new AtomicReference<>(trackedAddress);

        assertTrue(BlueGigaBridgeHandler.clearConnectionAttempt(connectingAddress, eventAddress));
        assertNull(connectingAddress.get());
    }

    @Test
    void clearConnectionAttemptLeavesDifferentAddressInPlace() {
        BluetoothAddress trackedAddress = new BluetoothAddress("58:93:D8:8A:F1:C2");
        BluetoothAddress otherAddress = new BluetoothAddress("E0:7D:EA:26:94:2F");
        AtomicReference<@Nullable BluetoothAddress> connectingAddress = new AtomicReference<>(trackedAddress);

        assertFalse(BlueGigaBridgeHandler.clearConnectionAttempt(connectingAddress, otherAddress));
        assertEquals(trackedAddress, connectingAddress.get());
    }

    @Test
    @SuppressWarnings("null")
    void cancellationKeepsConnectionReservedUntilGapProcedureEnds() throws Exception {
        BlueGigaBridgeHandler handler = new BlueGigaBridgeHandler(mock(Bridge.class), mock(SerialPortManager.class));
        BluetoothAddress activeAddress = new BluetoothAddress("58:93:D8:8A:F1:C2");
        BluetoothAddress queuedAddress = new BluetoothAddress("E0:7D:EA:26:94:2F");
        BlueGigaTransactionManager transactionManager = mock(BlueGigaTransactionManager.class);
        BlueGigaEndProcedureResponse endResponse = mock(BlueGigaEndProcedureResponse.class);
        BlueGigaConnectDirectResponse connectResponse = mock(BlueGigaConnectDirectResponse.class);
        CountDownLatch endProcedureStarted = new CountDownLatch(1);
        CountDownLatch allowEndProcedure = new CountDownLatch(1);
        when(endResponse.getResult()).thenReturn(BgApiResponse.SUCCESS);
        when(connectResponse.getResult()).thenReturn(BgApiResponse.SUCCESS);
        doAnswer(invocation -> {
            BlueGigaCommand command = invocation.getArgument(0);
            if (command instanceof BlueGigaEndProcedureCommand) {
                endProcedureStarted.countDown();
                assertTrue(allowEndProcedure.await(5, TimeUnit.SECONDS));
                return endResponse;
            }
            return connectResponse;
        }).when(transactionManager).sendTransaction(any(BlueGigaCommand.class), any(), anyLong());

        setField(handler, "initComplete", true);
        setField(handler, "maxConnections", 1);
        setField(handler, "gapProcedureActive", true);
        setField(handler, "connectingAddress", new AtomicReference<>(activeAddress));
        setField(handler, "transactionManager", CompletableFuture.completedFuture(transactionManager));

        CompletableFuture<Boolean> cancellation = CompletableFuture
                .supplyAsync(() -> handler.cancelConnectionAttempt(activeAddress));
        try {
            assertTrue(endProcedureStarted.await(5, TimeUnit.SECONDS));
            assertEquals(activeAddress, ((AtomicReference<?>) getField(handler, "connectingAddress")).get());
            assertTrue(handler.bgConnect(queuedAddress, BluetoothAddressType.GAP_ADDRESS_TYPE_PUBLIC));

            allowEndProcedure.countDown();
            assertTrue(cancellation.get(5, TimeUnit.SECONDS));
            assertEquals(queuedAddress, ((AtomicReference<?>) getField(handler, "connectingAddress")).get());
        } finally {
            allowEndProcedure.countDown();
            handler.dispose();
        }
    }

    private static Object getField(Object instance, String name) throws ReflectiveOperationException {
        Field field = instance.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return Objects.requireNonNull(field.get(instance));
    }

    private static void setField(Object instance, String name, Object value) throws ReflectiveOperationException {
        Field field = instance.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }

    @Test
    void recordConnectionStatusIgnoresEventsWithoutConnectedFlag() {
        Map<Integer, BluetoothAddress> connections = new HashMap<>();
        BluetoothAddress address = new BluetoothAddress("58:93:D8:8A:F1:C2");

        assertFalse(BlueGigaBridgeHandler.recordConnectionStatus(connections, 0, address,
                Set.of(ConnectionStatusFlag.CONNECTION_PARAMETERS_CHANGE)));
        assertTrue(connections.isEmpty());
    }

    @Test
    void recordConnectionStatusTracksEstablishedConnection() {
        Map<Integer, BluetoothAddress> connections = new HashMap<>();
        BluetoothAddress address = new BluetoothAddress("58:93:D8:8A:F1:C2");

        assertTrue(BlueGigaBridgeHandler.recordConnectionStatus(connections, 0, address,
                Set.of(ConnectionStatusFlag.CONNECTION_CONNECTED)));
        assertEquals(Map.of(0, address), connections);
    }
}
