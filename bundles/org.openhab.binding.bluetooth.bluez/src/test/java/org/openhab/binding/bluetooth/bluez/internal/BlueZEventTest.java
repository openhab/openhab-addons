/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEventListener;

/**
 *
 * @author Benjamin Lafois - Initial Contribution
 * @author Connor Petty - Added additional test cases
 */
public class BlueZEventTest {

    @Test
    public void testDbusPathParser0() {
        BlueZEvent event = new DummyBlueZEvent("/org/bluez/hci0/dsqdsq/ds/dd");
        assertEquals("hci0", event.getAdapterName());
        assertNull(event.getDevice());
    }

    @Test
    public void testDbusPathParser1() {
        BlueZEvent event = new DummyBlueZEvent("/org/bluez/hci0/dev_00_CC_3F_B2_7E_60");
        assertEquals("hci0", event.getAdapterName());
        assertEquals(new BluetoothAddress("00:CC:3F:B2:7E:60"), event.getDevice());
    }

    @Test
    public void testDbusPathParser2() {
        BlueZEvent event = new DummyBlueZEvent("/org/bluez/hci0/dev_A4_34_D9_ED_D3_74/service0026/char0027");
        assertEquals("hci0", event.getAdapterName());
        assertEquals(new BluetoothAddress("A4:34:D9:ED:D3:74"), event.getDevice());
    }

    @Test
    public void testDbusPathParser3() {
        BlueZEvent event = new DummyBlueZEvent("/org/bluez/hci0/dev_00_CC_3F_B2_7E_60/");
        assertEquals("hci0", event.getAdapterName());
        assertEquals(new BluetoothAddress("00:CC:3F:B2:7E:60"), event.getDevice());
    }

    @Test
    public void testDbusPathParser4() {
        BlueZEvent event = new DummyBlueZEvent("/org/bluez/hci0/dev_");
        assertEquals("hci0", event.getAdapterName());
        assertNull(event.getDevice());
    }

    @Test
    public void testDbusPathParser5() {
        BlueZEvent event = new DummyBlueZEvent("/org/bluez/hci0/dev_/");
        assertEquals("hci0", event.getAdapterName());
        assertNull(event.getDevice());
    }

    @Test
    public void testDbusPathParser6() {
        BlueZEvent event = new DummyBlueZEvent("/org/bluez/hci0");
        assertEquals("hci0", event.getAdapterName());
        assertNull(event.getDevice());
    }

    private static class DummyBlueZEvent extends BlueZEvent {

        public DummyBlueZEvent(String dbusPath) {
            super(dbusPath);
        }

        @Override
        public void dispatch(@NonNull BlueZEventListener listener) {
            listener.onDBusBlueZEvent(this);
        }
    }
}
