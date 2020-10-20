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
package org.openhab.binding.bluetooth.bluez.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEvent;

/**
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
public class BlueZEventTest {

    @Test
    public void testDbusPathParser0() {
        BlueZEvent event = new BlueZEvent("/org/bluez/hci0/dsqdsq/ds/dd", BlueZEvent.EventType.NAME);
        assertEquals("hci0", event.getAdapterName());
        assertNull(event.getDevice());
    }

    @Test
    public void testDbusPathParser1() {
        BlueZEvent event = new BlueZEvent("/org/bluez/hci0/dev_00_CC_3F_B2_7E_60", BlueZEvent.EventType.NAME);
        assertEquals("hci0", event.getAdapterName());
        assertEquals(new BluetoothAddress("00:CC:3F:B2:7E:60"), event.getDevice());
    }

    @Test
    public void testDbusPathParser2() {
        BlueZEvent event = new BlueZEvent("/org/bluez/hci0/dev_A4_34_D9_ED_D3_74/service0026/char0027",
                BlueZEvent.EventType.NAME);
        assertEquals("hci0", event.getAdapterName());
        assertEquals(new BluetoothAddress("A4:34:D9:ED:D3:74"), event.getDevice());
    }
}
