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
package org.openhab.binding.max.internal.message;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.max.internal.device.DeviceType;

/**
 * Tests cases for {@link NMessage}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class NMessageTest {

    public static final String RAW_DATA = "N:Aw4VzExFUTAwMTUzNDD/";
    // public final String rawData = "N:AQe250tFUTAxNDUxNzL/";

    private NMessage message = new NMessage(RAW_DATA);

    @Test
    public void getMessageTypeTest() {
        MessageType messageType = ((Message) message).getType();
        assertEquals(MessageType.N, messageType);
    }

    @Test
    public void getRFAddressTest() {
        String rfAddress = message.getRfAddress();

        assertEquals("0E15CC", rfAddress);
    }

    @Test
    public void getSerialNumberTest() {
        String serialNumber = message.getSerialNumber();

        assertEquals("LEQ0015340", serialNumber);
    }

    @Test
    public void getDeviceTypeTest() {
        DeviceType deviceType = message.getDeviceType();

        assertEquals(DeviceType.WallMountedThermostat, deviceType);
    }
}
