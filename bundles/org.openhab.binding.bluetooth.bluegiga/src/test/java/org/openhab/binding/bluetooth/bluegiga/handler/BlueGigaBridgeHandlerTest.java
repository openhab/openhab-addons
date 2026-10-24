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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BgApiResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.ConnectionStatusFlag;

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
