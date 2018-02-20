/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.max.internal.device.DeviceType;

/**
 * Tests cases for {@link N_Message}.
 * 
 * @author Marcel Verpaalen - Initial Version
 * @since 2.0
 */
public class N_MessageTest {

    public final String rawData = "N:Aw4VzExFUTAwMTUzNDD/";
    // public final String rawData = "N:AQe250tFUTAxNDUxNzL/";

    private N_Message message = null;

    @Before
    public void Before() {
        message = new N_Message(rawData);
    }

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