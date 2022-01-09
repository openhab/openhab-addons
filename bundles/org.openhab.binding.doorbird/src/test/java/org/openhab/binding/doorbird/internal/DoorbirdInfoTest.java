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
package org.openhab.binding.doorbird.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.doorbird.internal.api.DoorbirdInfo;

/**
 * The {@link DoorbirdInfoTest} is responsible for testing the functionality
 * of Doorbird "info" message parsing.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class DoorbirdInfoTest {

    private final String infoWithControllerId =
    //@formatter:off
    "{" +
        "'BHA': {" +
            "'RETURNCODE': '1'," +
            "'VERSION': [{" +
                "'FIRMWARE': '000109'," +
                "'BUILD_NUMBER': '15120529'," +
                "'PRIMARY_MAC_ADDR': '1CCAE3711111'," +
                "'WIFI_MAC_ADDR': '1CCAE3799999'," +
                "'RELAYS': ['1', '2', 'gggaaa@1', 'gggaaa@2']," +
                "'DEVICE-TYPE': 'DoorBird D101'" +
            "}]" +
        "}" +
    "}";
    //@formatter:on

    private final String infoWithoutControllerId =
    //@formatter:off
    "{" +
        "'BHA': {" +
            "'RETURNCODE': '1'," +
            "'VERSION': [{" +
                "'FIRMWARE': '000109'," +
                "'BUILD_NUMBER': '15120529'," +
                "'PRIMARY_MAC_ADDR': '1CCAE3711111'," +
                "'WIFI_MAC_ADDR': '1CCAE3799999'," +
                "'RELAYS': ['1', '2']," +
                "'DEVICE-TYPE': 'DoorBird D101'" +
            "}]" +
        "}" +
    "}";
    //@formatter:on

    @Test
    public void testParsingWithoutControllerId() {
        DoorbirdInfo info = new DoorbirdInfo(infoWithoutControllerId);

        assertEquals("1", info.getReturnCode());
        assertEquals("000109", info.getFirmwareVersion());
        assertEquals("15120529", info.getBuildNumber());
        assertEquals("1CCAE3711111", info.getPrimaryMacAddress());
        assertEquals("1CCAE3799999", info.getWifiMacAddress());
        assertEquals("DoorBird D101", info.getDeviceType());

        assertTrue(info.getRelays().contains("1"));
        assertTrue(info.getRelays().contains("2"));
        assertFalse(info.getRelays().contains("3"));
    }

    @Test
    public void testGetControllerId() {
        DoorbirdInfo info = new DoorbirdInfo(infoWithControllerId);

        assertEquals("gggaaa", info.getControllerId(null));

        assertTrue(info.getRelays().contains("gggaaa@1"));
        assertTrue(info.getRelays().contains("gggaaa@2"));
        assertFalse(info.getRelays().contains("unknown"));
    }

    @Test
    public void testControllerIdIsNull() {
        DoorbirdInfo info = new DoorbirdInfo(infoWithoutControllerId);

        assertNull(info.getControllerId(null));
    }
}
