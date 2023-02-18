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

package org.openhab.binding.pjlinkdevice.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link IPUtilsTest} class defines some static utility methods
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class IPUtilsTest {

    @Test
    public void checkSingleAddress() {
        String[] addresses = IPUtils.getAllIPv4Addresses("192.168.1.1/32");

        assertEquals(1, addresses.length);
        assertEquals("192.168.1.1", addresses[0]);
    }

    @Test
    public void checkValidRangeCount() {
        String[] addresses = IPUtils.getAllIPv4Addresses("192.168.1.0/24");

        assertEquals(254, addresses.length);
        assertEquals("192.168.1.1", addresses[0]);
        assertEquals("192.168.1.254", addresses[253]);
    }

    @Test
    public void checkValidLargeRangeCount() {
        String[] addresses = IPUtils.getAllIPv4Addresses("127.0.0.0/16");

        assertEquals(65534, addresses.length);
        assertEquals("127.0.0.1", addresses[0]);
        assertEquals("127.0.255.254", addresses[65533]);
    }

    @Test
    public void checkInValidRange() {
        String[] addresses = IPUtils.getAllIPv4Addresses("192.168.12/24");

        assertEquals(0, addresses.length);
    }

    @Test
    public void checkNoMask() {
        String[] addresses = IPUtils.getAllIPv4Addresses("192.168.12/");

        assertEquals(0, addresses.length);
    }

    @Test
    public void checkWrongSlash() {
        String[] addresses = IPUtils.getAllIPv4Addresses("192.168.12\\");

        assertEquals(0, addresses.length);
    }
}
