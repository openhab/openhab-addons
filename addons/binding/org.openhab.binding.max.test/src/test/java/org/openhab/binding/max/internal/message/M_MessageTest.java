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

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.max.internal.device.DeviceInformation;
import org.openhab.binding.max.internal.device.DeviceType;
import org.openhab.binding.max.internal.device.RoomInformation;

/**
 * Tests cases for {@link M_Message}.
 *
 * @author Marcel Verpaalen - Initial version
 * @since 2.0
 */
public class M_MessageTest {

    public final String rawData = "M:00,01,VgIFAQhiYWRrYW1lcgsNowIMU3R1ZGVlcmthbWVyB7bnAwlXb29ua2FtZXIL6aIEDFN6b25qYSBLYW1lcgjDSQUGWm9sZGVyCMHWCAILDaNLRVEwNTQ0MjQyEUJhZGthbWVyIFJhZGlhdG9yAQEHtudLRVEwMTQ1MTcyFVJhZGlhdG9yIFN0dWRlZXJrYW1lcgIDDhXMTEVRMDAxNTM0MBlXYWxsIFRoZXJtb3N0YXQgV29vbmthbWVyAwEL6aJLRVE5MDE1NDMyG1BsdWcgQWRhcHRlciBNdXVydmVyd2FybWluZwMFBDNvSkVRMDM4MDg3OBdFY28gU3dpdGNoIFN0dWRlZXJrYW1lcgAEDnX2S0VRMTEwNDM4MBpXaW5kb3cgU2Vuc29yIFN0dWRlZXJrYW1lcgIBCMNJS0VRMDY0ODk0ORJUaGVybW9zdGFhdCBTem9uamEEAQjB1ktFUTA2NDkzMTIRU3R1ZGVlcmthbWVyIElybWEFAQ==";

    private M_Message message = null;

    @Before
    public void Before() {
        message = new M_Message(rawData);
    }

    @Test
    public void getMessageTypeTest() {

        MessageType messageType = ((Message) message).getType();

        assertEquals(MessageType.M, messageType);
    }

    @Test
    public void deviceInformationTest() {

        ArrayList<DeviceInformation> allDevicesInformation = message.devices;

        assertEquals(8, allDevicesInformation.size());

        DeviceInformation deviceInformation = allDevicesInformation.get(0);
        assertEquals("Badkamer Radiator", deviceInformation.getName());
        assertEquals("0B0DA3", deviceInformation.getRFAddress());
        assertEquals(1, deviceInformation.getRoomId());
        assertEquals("KEQ0544242", deviceInformation.getSerialNumber());
        assertEquals(DeviceType.HeatingThermostatPlus, deviceInformation.getDeviceType());

    }

    @Test
    public void deviceInformationTypeTest1() {

        ArrayList<DeviceInformation> allDevicesInformation = message.devices;
        DeviceInformation deviceInformation = allDevicesInformation.get(1);
        assertEquals(DeviceType.HeatingThermostat, deviceInformation.getDeviceType());
    }

    @Test
    public void deviceInformationTypeTest2() {

        ArrayList<DeviceInformation> allDevicesInformation = message.devices;
        DeviceInformation deviceInformation = allDevicesInformation.get(2);
        assertEquals(DeviceType.WallMountedThermostat, deviceInformation.getDeviceType());
    }

    @Test
    public void deviceInformationTypeTest3() {

        ArrayList<DeviceInformation> allDevicesInformation = message.devices;
        DeviceInformation deviceInformation = allDevicesInformation.get(4);
        assertEquals(DeviceType.EcoSwitch, deviceInformation.getDeviceType());
    }

    @Test
    public void deviceInformationTypeTest4() {

        ArrayList<DeviceInformation> allDevicesInformation = message.devices;
        DeviceInformation deviceInformation = allDevicesInformation.get(5);
        assertEquals(DeviceType.ShutterContact, deviceInformation.getDeviceType());
    }

    @Test
    public void roomInformationTest() {

        ArrayList<RoomInformation> roomInformation = message.rooms;

        assertEquals(5, roomInformation.size());
        assertEquals("badkamer", roomInformation.get(0).getName());

    }

}
