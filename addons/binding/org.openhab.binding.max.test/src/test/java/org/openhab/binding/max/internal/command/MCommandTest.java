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
package org.openhab.binding.max.internal.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.device.DeviceConfiguration;
import org.openhab.binding.max.internal.device.RoomInformation;
import org.openhab.binding.max.internal.message.CMessage;

/**
 * Tests cases for {@link MCommand}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MCommandTest {

    private List<DeviceConfiguration> configurations = new ArrayList<>();
    private List<Device> devices = new ArrayList<>();
    private List<RoomInformation> rooms;

    String deviceCMsg[] = {
            "C:0b0da3,0gsNowIBEABLRVEwNTQ0MjQyLCQ9CQcYAzAM/wBIYViRSP1ZFE0gTSBNIEUgRSBFIEUgRSBFIEhhWJFQ/VkVUSBRIFEgRSBFIEUgRSBFIEUgSFBYWkj+WRRNIE0gTSBFIEUgRSBFIEUgRSBIUFhaSP5ZFE0gTSBNIEUgRSBFIEUgRSBFIEhQWFpI/lkUTSBNIE0gRSBFIEUgRSBFIEUgSFBYWkj+WRRNIE0gTSBFIEUgRSBFIEUgRSBIUFhaSP5ZFE0gTSBNIEUgRSBFIEUgRSBFIA==",
            "C:08c1d6,0gjB1gEFGP9LRVEwNjQ5MzEyKyE9CQcYAzAM/wBEeFUgVSBVIFUgVSBVIEUgRSBFIEUgRSBFIER4VRZFIEUgRSBFIEUgRSBFIEUgRSBFIEUgRFFEYkTkTQ9FIEUgRSBFIEUgRSBFIEUgRSBEUURiRORND0UgRSBFIEUgRSBFIEUgRSBFIERRRGJE5E0PRSBFIEUgRSBFIEUgRSBFIEUgRFFEYkTkTQ9FIEUgRSBFIEUgRSBFIEUgRSBEUURiRORRGEUgRSBFIEUgRSBFIEUgRSBFIA==",
            "C:0e75f6,EQ519gQCEABLRVExMTA0Mzgw",
            "C:0f1d54,0g8dVAEAEKBMRVEwMTU1NTc4KiI9CQcYAzAM/wBESFUIRSBFIEUgRSBFIEUgRSBFIEUgRSBFIERIVQhFIEUgRSBFIEUgRSBFIEUgRSBFIEUgREhUbETMVRRFIEUgRSBFIEUgRSBFIEUgRSBESFRsRMxVFEUgRSBFIEUgRSBFIEUgRSBFIERIVGxEzFUURSBFIEUgRSBFIEUgRSBFIEUgREhUbETMVRRFIEUgRSBFIEUgRSBFIEUgRSBESFRsRMxVFEUgRSBFIEUgRSBFIEUgRSBFIA==" };

    private void prepareDevices() {
        // create a devices array
        for (String cMsg : deviceCMsg) {
            CMessage msg = new CMessage(cMsg);
            // DeviceConfiguration c = null;
            configurations.add(DeviceConfiguration.create(msg));
            Device di = Device.create(msg.getRFAddress(), configurations);
            devices.add(di);
        }
    }

    @Test
    public void PrefixTest() {
        prepareDevices();
        MCommand mcmd = new MCommand(devices);
        String commandStr = mcmd.getCommandString();

        String prefix = commandStr.substring(0, 2);
        assertEquals("m:", prefix);
    }

    @Test
    public void BaseCommandTest() {
        prepareDevices();

        MCommand mCmd = new MCommand(devices);
        rooms = new ArrayList<>(mCmd.getRooms());
        String commandStr = mCmd.getCommandString();
        assertEquals(
                "m:00,VgIDAQALDaMCAA519gUACMHWBAILDaNLRVEwNTQ0MjQyAAEBCMHWS0VRMDY0OTMxMgAFBA519ktFUTExMDQzODAAAgEPHVRMRVEwMTU1NTc4AAAB\r\n",
                commandStr);
    }

    @Test
    public void AddRoomsTest() {
        prepareDevices();

        MCommand mCmd = new MCommand(devices);
        rooms = new ArrayList<>(mCmd.getRooms());

        RoomInformation room = new RoomInformation(3, "testroom", "0f1d54");
        rooms.add(room);
        mCmd = new MCommand(devices, rooms);
        String commandStr = mCmd.getCommandString();
        assertEquals(
                "m:00,VgIEAQALDaMCAA519gMIdGVzdHJvb20PHVQFAAjB1gQCCw2jS0VRMDU0NDI0MgABAQjB1ktFUTA2NDkzMTIABQQOdfZLRVExMTA0MzgwAAIBDx1UTEVRMDE1NTU3OAAAAQ==\r\n",
                commandStr);
        devices.get(3).setRoomId(3);
        devices.get(3).setName("Testroom");

        mCmd = new MCommand(devices, rooms);
        commandStr = mCmd.getCommandString();

        assertEquals(
                "m:00,VgIEAQALDaMCAA519gMIdGVzdHJvb20PHVQFAAjB1gQCCw2jS0VRMDU0NDI0MgABAQjB1ktFUTA2NDkzMTIABQQOdfZLRVExMTA0MzgwAAIBDx1UTEVRMDE1NTU3OAhUZXN0cm9vbQMB\r\n",
                commandStr);
    }

}
