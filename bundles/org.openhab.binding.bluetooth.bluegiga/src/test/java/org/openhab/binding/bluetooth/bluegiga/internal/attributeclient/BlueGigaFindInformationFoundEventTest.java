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
package org.openhab.binding.bluetooth.bluegiga.internal.attributeclient;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaFindInformationFoundEvent;

/**
 * Tests {@link BlueGigaFindInformationFoundEvent}.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class BlueGigaFindInformationFoundEventTest {

    @Test
    public void test16BitUUIDs() {
        final int[] data = { 0x80, 0x06, 0x04, 0x04, 0x02, 0x0A, 0x00, 0x02, 0x00, 0x28 };
        final String expectedUUID = "00002800-0000-1000-8000-00805F9B34FB";

        BlueGigaFindInformationFoundEvent event = new BlueGigaFindInformationFoundEvent(data);
        assertEquals(expectedUUID, event.getUuid().toString().toUpperCase());
        assertEquals(2, event.getConnection());
        assertEquals(10, event.getChrHandle());
    }

    @Test
    public void test32BitUUIDs() {
        final int[] data = { 0x80, 0x06, 0x04, 0x04, 0x02, 0x0A, 0x00, 0x04, 0x02, 0x01, 0x00, 0x28 };
        final String expectedUUID = "28000102-0000-1000-8000-00805F9B34FB";

        BlueGigaFindInformationFoundEvent event = new BlueGigaFindInformationFoundEvent(data);
        assertEquals(expectedUUID, event.getUuid().toString().toUpperCase());
        assertEquals(2, event.getConnection());
        assertEquals(10, event.getChrHandle());
    }

    @Test
    public void test128BitUUIDs() {
        final int[] data = { 0x80, 0x14, 0x04, 0x04, 0x02, 0x0D, 0x00, 0x10, 0xBA, 0x5C, 0xF7, 0x93, 0x3B, 0x12, 0xD3,
                0x89, 0xE4, 0x11, 0xE7, 0xAD, 0x68, 0x2A, 0x2E, 0xB4 };
        final String expectedUUID = "b42e2a68-ade7-11e4-89d3-123b93f75cba";

        BlueGigaFindInformationFoundEvent event = new BlueGigaFindInformationFoundEvent(data);
        assertEquals(expectedUUID, event.getUuid().toString().toLowerCase());
        assertEquals(2, event.getConnection());
        assertEquals(13, event.getChrHandle());
    }
}
