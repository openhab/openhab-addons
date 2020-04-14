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
package org.openhab.binding.bluetooth.dbusbluez.internal;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openhab.binding.bluetooth.BluetoothAddress;

/**
 *
 * @author blafois
 *
 */
public class DBusBlueZUtilsTest {

    @Test
    public void testDbusPathParser0() {

        // /org/bluez/hci0/dev_A4_34_D9_ED_D3_74/service0026/char0027

        BluetoothAddress addr = DBusBlueZUtils.dbusPathToMac(null);

        assertNull(addr);

        addr = DBusBlueZUtils.dbusPathToMac("/org/bluez/dsqdq/dsqdsq/ds/dd");
        assertNull(addr);

        addr = DBusBlueZUtils.dbusPathToMac("/org/bluez/hci0/dev_00!CC!3F!B2!7E!60");
        assertNull(addr);

    }

    @Test
    public void testDbusPathParser1() {

        // /org/bluez/hci0/dev_00_CC_3F_B2_7E_60

        BluetoothAddress addr1 = DBusBlueZUtils.dbusPathToMac("/org/bluez/hci0/dev_00_CC_3F_B2_7E_60");

        assertNotNull(addr1);
        assertEquals(new BluetoothAddress("00:cc:3f:b2:7e:60".toUpperCase()), addr1);

    }

    @Test
    public void testDbusPathParser2() {

        // /org/bluez/hci0/dev_A4_34_D9_ED_D3_74/service0026/char0027

        BluetoothAddress addr2 = DBusBlueZUtils
                .dbusPathToMac("/org/bluez/hci0/dev_A4_34_D9_ED_D3_74/service0026/char0027");

        assertNotNull(addr2);
        assertEquals(new BluetoothAddress("a4:34:d9:ed:d3:74".toUpperCase()), addr2);

    }

}
