/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests cases for {@link ZCommand}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class ZCommandTest {

    @Test
    public void prefixTest() {
        ZCommand scmd = new ZCommand(ZCommand.WakeUpType.DEVICE, "0b0da3", 30);

        String commandStr = scmd.getCommandString();
        String prefix = commandStr.substring(0, 2);

        assertEquals("z:", prefix);
    }

    @Test
    public void baseCommandTest() {
        ZCommand scmd = new ZCommand(ZCommand.WakeUpType.DEVICE, "0b0da3", 30);
        String commandStr = scmd.getCommandString();
        assertEquals("z:1E,D,0b0da3" + '\r' + '\n', commandStr);
    }

    @Test
    public void wakeAllTest() {
        ZCommand scmd = new ZCommand(ZCommand.WakeUpType.ALL, "0b0da3", 60);
        String commandStr = scmd.getCommandString();
        assertEquals("z:3C,A" + '\r' + '\n', commandStr);

        scmd = ZCommand.wakeupAllDevices();
        commandStr = scmd.getCommandString();
        assertEquals("z:1E,A" + '\r' + '\n', commandStr);

        scmd = ZCommand.wakeupAllDevices(60);
        commandStr = scmd.getCommandString();
        assertEquals("z:3C,A" + '\r' + '\n', commandStr);
    }

    @Test
    public void wakeRoomTest() {
        ZCommand scmd = new ZCommand(ZCommand.WakeUpType.ROOM, "01", 30);
        String commandStr = scmd.getCommandString();
        assertEquals("z:1E,G,01" + '\r' + '\n', commandStr);

        scmd = ZCommand.wakeupRoom(1);
        commandStr = scmd.getCommandString();
        assertEquals("z:1E,G,01" + '\r' + '\n', commandStr);

        scmd = ZCommand.wakeupRoom(2, 60);
        commandStr = scmd.getCommandString();
        assertEquals("z:3C,G,02" + '\r' + '\n', commandStr);
    }

    @Test
    public void wakeDeviceTest() {
        ZCommand scmd = ZCommand.wakeupDevice("0b0da3");
        String commandStr = scmd.getCommandString();
        assertEquals("z:1E,D,0b0da3" + '\r' + '\n', commandStr);

        scmd = ZCommand.wakeupDevice("0b0da3", 60);
        commandStr = scmd.getCommandString();
        assertEquals("z:3C,D,0b0da3" + '\r' + '\n', commandStr);
    }
}
