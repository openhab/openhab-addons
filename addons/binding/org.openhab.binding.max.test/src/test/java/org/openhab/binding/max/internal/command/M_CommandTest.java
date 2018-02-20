/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.device.DeviceConfiguration;
import org.openhab.binding.max.internal.device.RoomInformation;
import org.openhab.binding.max.internal.message.C_Message;

/**
 * Tests cases for {@link S_Command}.
 *
 * @author Marcel Verpaalen - Initial version
 */
public class M_CommandTest {

    private ArrayList<DeviceConfiguration> configurations = new ArrayList<DeviceConfiguration>();
    private ArrayList<Device> devices = new ArrayList<Device>();
    private ArrayList<RoomInformation> rooms;

    String deviceCMsg[] = {
            "C:0b0da3,0gsNowIBEABLRVEwNTQ0MjQyLCQ9CQcYAzAM/wBIYViRSP1ZFE0gTSBNIEUgRSBFIEUgRSBFIEhhWJFQ/VkVUSBRIFEgRSBFIEUgRSBFIEUgSFBYWkj+WRRNIE0gTSBFIEUgRSBFIEUgRSBIUFhaSP5ZFE0gTSBNIEUgRSBFIEUgRSBFIEhQWFpI/lkUTSBNIE0gRSBFIEUgRSBFIEUgSFBYWkj+WRRNIE0gTSBFIEUgRSBFIEUgRSBIUFhaSP5ZFE0gTSBNIEUgRSBFIEUgRSBFIA==",
            "C:08c1d6,0gjB1gEFGP9LRVEwNjQ5MzEyKyE9CQcYAzAM/wBEeFUgVSBVIFUgVSBVIEUgRSBFIEUgRSBFIER4VRZFIEUgRSBFIEUgRSBFIEUgRSBFIEUgRFFEYkTkTQ9FIEUgRSBFIEUgRSBFIEUgRSBEUURiRORND0UgRSBFIEUgRSBFIEUgRSBFIERRRGJE5E0PRSBFIEUgRSBFIEUgRSBFIEUgRFFEYkTkTQ9FIEUgRSBFIEUgRSBFIEUgRSBEUURiRORRGEUgRSBFIEUgRSBFIEUgRSBFIA==",
            "C:0e75f6,EQ519gQCEABLRVExMTA0Mzgw",
            "C:0f1d54,0g8dVAEAEKBMRVEwMTU1NTc4KiI9CQcYAzAM/wBESFUIRSBFIEUgRSBFIEUgRSBFIEUgRSBFIERIVQhFIEUgRSBFIEUgRSBFIEUgRSBFIEUgREhUbETMVRRFIEUgRSBFIEUgRSBFIEUgRSBESFRsRMxVFEUgRSBFIEUgRSBFIEUgRSBFIERIVGxEzFUURSBFIEUgRSBFIEUgRSBFIEUgREhUbETMVRRFIEUgRSBFIEUgRSBFIEUgRSBESFRsRMxVFEUgRSBFIEUgRSBFIEUgRSBFIA==" };

    private void prepareDevices() {
        // create a devices array
        for (String cMsg : deviceCMsg) {
            C_Message msg = new C_Message(cMsg);
            // DeviceConfiguration c = null;
            configurations.add(DeviceConfiguration.create(msg));
            Device di = Device.create(msg.getRFAddress(), configurations);
            devices.add(di);
        }
    }

    @Test
    public void PrefixTest() {

        prepareDevices();
        M_Command mcmd = new M_Command(devices);
        String commandStr = mcmd.getCommandString();

        String prefix = commandStr.substring(0, 2);
        assertEquals("m:", prefix);

    }

    @Test
    public void BaseCommandTest() {
        prepareDevices();

        M_Command mCmd = new M_Command(devices);
        rooms = new ArrayList<RoomInformation>(mCmd.getRooms());
        String commandStr = mCmd.getCommandString();
        assertEquals(
                "m:00,VgIDAQALDaMCAA519gUACMHWBAILDaNLRVEwNTQ0MjQyAAEBCMHWS0VRMDY0OTMxMgAFBA519ktFUTExMDQzODAAAgEPHVRMRVEwMTU1NTc4AAAB\r\n",
                commandStr);

    }

    @Test
    public void AddRoomsTest() {
        prepareDevices();

        M_Command mCmd = new M_Command(devices);
        rooms = new ArrayList<RoomInformation>(mCmd.getRooms());

        RoomInformation room = new RoomInformation(3, "testroom", "0f1d54");
        rooms.add(room);
        mCmd = new M_Command(devices, rooms);
        String commandStr = mCmd.getCommandString();
        assertEquals(
                "m:00,VgIEAQALDaMCAA519gMIdGVzdHJvb20PHVQFAAjB1gQCCw2jS0VRMDU0NDI0MgABAQjB1ktFUTA2NDkzMTIABQQOdfZLRVExMTA0MzgwAAIBDx1UTEVRMDE1NTU3OAAAAQ==\r\n",
                commandStr);
        devices.get(3).setRoomId(3);
        devices.get(3).setName("Testroom");

        mCmd = new M_Command(devices, rooms);
        commandStr = mCmd.getCommandString();

        assertEquals(
                "m:00,VgIEAQALDaMCAA519gMIdGVzdHJvb20PHVQFAAjB1gQCCw2jS0VRMDU0NDI0MgABAQjB1ktFUTA2NDkzMTIABQQOdfZLRVExMTA0MzgwAAIBDx1UTEVRMDE1NTU3OAhUZXN0cm9vbQMB\r\n",
                commandStr);

    }

}
