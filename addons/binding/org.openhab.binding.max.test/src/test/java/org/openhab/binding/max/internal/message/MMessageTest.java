/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.max.internal.device.DeviceInformation;
import org.openhab.binding.max.internal.device.DeviceType;
import org.openhab.binding.max.internal.device.RoomInformation;

/**
 * Tests cases for {@link MMessage}.
 *
 * @author Marcel Verpaalen - Initial version
 */
public class MMessageTest {

    public static final String RAW_DATA = "M:00,01,VgIFAQhiYWRrYW1lcgsNowIMU3R1ZGVlcmthbWVyB7bnAwlXb29ua2FtZXIL6aIEDFN6b25qYSBLYW1lcgjDSQUGWm9sZGVyCMHWCAILDaNLRVEwNTQ0MjQyEUJhZGthbWVyIFJhZGlhdG9yAQEHtudLRVEwMTQ1MTcyFVJhZGlhdG9yIFN0dWRlZXJrYW1lcgIDDhXMTEVRMDAxNTM0MBlXYWxsIFRoZXJtb3N0YXQgV29vbmthbWVyAwEL6aJLRVE5MDE1NDMyG1BsdWcgQWRhcHRlciBNdXVydmVyd2FybWluZwMFBDNvSkVRMDM4MDg3OBdFY28gU3dpdGNoIFN0dWRlZXJrYW1lcgAEDnX2S0VRMTEwNDM4MBpXaW5kb3cgU2Vuc29yIFN0dWRlZXJrYW1lcgIBCMNJS0VRMDY0ODk0ORJUaGVybW9zdGFhdCBTem9uamEEAQjB1ktFUTA2NDkzMTIRU3R1ZGVlcmthbWVyIElybWEFAQ==";

    private MMessage message;

    @Before
    public void before() {
        message = new MMessage(RAW_DATA);
    }

    @Test
    public void getMessageTypeTest() {
        MessageType messageType = ((Message) message).getType();
        assertEquals(MessageType.M, messageType);
    }

    @Test
    public void deviceInformationTest() {
        List<DeviceInformation> allDevicesInformation = message.devices;

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
        List<DeviceInformation> allDevicesInformation = message.devices;
        DeviceInformation deviceInformation = allDevicesInformation.get(1);
        assertEquals(DeviceType.HeatingThermostat, deviceInformation.getDeviceType());
    }

    @Test
    public void deviceInformationTypeTest2() {
        List<DeviceInformation> allDevicesInformation = message.devices;
        DeviceInformation deviceInformation = allDevicesInformation.get(2);
        assertEquals(DeviceType.WallMountedThermostat, deviceInformation.getDeviceType());
    }

    @Test
    public void deviceInformationTypeTest3() {
        List<DeviceInformation> allDevicesInformation = message.devices;
        DeviceInformation deviceInformation = allDevicesInformation.get(4);
        assertEquals(DeviceType.EcoSwitch, deviceInformation.getDeviceType());
    }

    @Test
    public void deviceInformationTypeTest4() {
        List<DeviceInformation> allDevicesInformation = message.devices;
        DeviceInformation deviceInformation = allDevicesInformation.get(5);
        assertEquals(DeviceType.ShutterContact, deviceInformation.getDeviceType());
    }

    @Test
    public void roomInformationTest() {
        List<RoomInformation> roomInformation = message.rooms;

        assertEquals(5, roomInformation.size());
        assertEquals("badkamer", roomInformation.get(0).getName());
    }

}
