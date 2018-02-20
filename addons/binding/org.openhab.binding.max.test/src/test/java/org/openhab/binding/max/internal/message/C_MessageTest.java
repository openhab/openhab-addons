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
 * Tests cases for {@link C_Message}.
 *
 * @author Andreas Heil (info@aheil.de)
 * @author Marcel Verpaalen - OH2 Version and updates
 * @since 1.4.0
 */
public class C_MessageTest {

    public final String rawData = "C:0b0da3,0gsNowIBEABLRVEwNTQ0MjQyLCQ9CQcYAzAM/wBIYViRSP1ZFE0gTSBNIEUgRSBFIEUgRSBFIEhhWJFQ/VkVUSBRIFEgRSBFIEUgRSBFIEUgSFBYWkj+WRRNIE0gTSBFIEUgRSBFIEUgRSBIUFhaSP5ZFE0gTSBNIEUgRSBFIEUgRSBFIEhQWFpI/lkUTSBNIE0gRSBFIEUgRSBFIEUgSFBYWkj+WRRNIE0gTSBFIEUgRSBFIEUgRSBIUFhaSP5ZFE0gTSBNIEUgRSBFIEUgRSBFIA==";

    private C_Message message = null;

    @Before
    public void Before() {
        message = new C_Message(rawData);
    }

    @Test
    public void getMessageTypeTest() {

        MessageType messageType = ((Message) message).getType();

        assertEquals(MessageType.C, messageType);
    }

    @Test
    public void getRFAddressTest() {

        String rfAddress = message.getRFAddress();

        assertEquals("0b0da3", rfAddress);
    }

    @Test
    public void getDeviceTypeTest() {

        DeviceType deviceType = message.getDeviceType();

        assertEquals(DeviceType.HeatingThermostatPlus, deviceType);
    }

    @Test
    public void getSerialNumberTes() {
        String serialNumber = message.getSerialNumber();

        assertEquals("KEQ0544242", serialNumber);
    }
}