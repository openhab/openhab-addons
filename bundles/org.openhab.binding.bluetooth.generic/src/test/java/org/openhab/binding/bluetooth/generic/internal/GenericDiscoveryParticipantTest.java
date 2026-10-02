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
package org.openhab.binding.bluetooth.generic.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;

/**
 * Verifies the connectability gating in {@link GenericDiscoveryParticipant}: a device that advertises as
 * connectable is surfaced as a generic thing straight from advertisement data (no connection needed), a
 * non-connectable beacon is declined (so discovery falls through to the beacon default), and a device whose
 * connectability is unknown keeps the legacy connect-to-fingerprint behavior.
 *
 * This locks the fix for "only beacon devices appear in the inbox": previously the participant unconditionally
 * required a connection, and on transports that refuse the discovery connect-probe every device was demoted to
 * a beacon.
 *
 * @author Vlad Kolotov - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenericDiscoveryParticipantTest {

    private final GenericDiscoveryParticipant participant = new GenericDiscoveryParticipant();

    private BluetoothDiscoveryDevice deviceWithConnectable(@Nullable Boolean connectable) {
        return device(connectable, null);
    }

    private BluetoothDiscoveryDevice device(@Nullable Boolean connectable, @Nullable String name) {
        BluetoothAdapter adapter = org.mockito.Mockito.mock(BluetoothAdapter.class);
        when(adapter.getUID()).thenReturn(new ThingUID("bluetooth:bluez:hci0"));
        BluetoothDiscoveryDevice device = org.mockito.Mockito.mock(BluetoothDiscoveryDevice.class);
        when(device.getAdapter()).thenReturn(adapter);
        when(device.getAddress()).thenReturn(new BluetoothAddress("12:34:56:78:9A:BC"));
        when(device.getConnectable()).thenReturn(connectable);
        when(device.getName()).thenReturn(name);
        // no manufacturer id -> no vendor suffix appended, so the label assertions test the name logic alone
        when(device.getManufacturerId()).thenReturn(null);
        return device;
    }

    @Test
    void connectableDeviceIsSurfacedWithoutConnection() {
        BluetoothDiscoveryDevice device = deviceWithConnectable(Boolean.TRUE);
        assertFalse(participant.requiresConnection(device), "connectable device must not require a connection");
        DiscoveryResult result = participant.createResult(device);
        assertNotNull(result, "connectable device must produce a generic discovery result");
    }

    @Test
    void nonConnectableBeaconIsDeclined() {
        BluetoothDiscoveryDevice device = deviceWithConnectable(Boolean.FALSE);
        assertFalse(participant.requiresConnection(device), "non-connectable device must not require a connection");
        assertNull(participant.createResult(device),
                "non-connectable beacon must be declined so discovery falls through to the beacon result");
    }

    @Test
    void unknownConnectabilityKeepsLegacyConnectProbe() {
        BluetoothDiscoveryDevice device = deviceWithConnectable(null);
        assertTrue(participant.requiresConnection(device),
                "unknown connectability must keep the legacy connect-to-fingerprint behavior");
        assertNotNull(participant.createResult(device), "unknown connectability still claims the device (as before)");
    }

    @Test
    void advertisedNameIsUsedAsLabel() {
        DiscoveryResult result = participant.createResult(device(Boolean.TRUE, "Living Room Speaker"));
        assertNotNull(result);
        assertEquals("Living Room Speaker", result.getLabel());
    }

    @Test
    void missingNameFallsBackToGenericLabel() {
        DiscoveryResult result = participant.createResult(device(Boolean.TRUE, null));
        assertNotNull(result);
        assertEquals("Generic Connectable Bluetooth Device", result.getLabel());
    }

    @Test
    void addressAsNameFallsBackToGenericLabel() {
        // some devices report their own address as the "name"; that is not a useful label
        DiscoveryResult result = participant.createResult(device(Boolean.TRUE, "12-34-56-78-9A-BC"));
        assertNotNull(result);
        assertEquals("Generic Connectable Bluetooth Device", result.getLabel());
    }
}
