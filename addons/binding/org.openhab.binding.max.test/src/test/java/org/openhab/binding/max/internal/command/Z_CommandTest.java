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

import org.junit.Test;

/**
 * Tests cases for {@link Z_Command}.
 *
 * @author Marcel Verpaalen - Initial version
 */
public class Z_CommandTest {

    @Test
    public void PrefixTest() {
        Z_Command scmd = new Z_Command(Z_Command.WakeUpType.DEVICE, "0b0da3", 30);

        String commandStr = scmd.getCommandString();
        String prefix = commandStr.substring(0, 2);

        assertEquals("z:", prefix);
    }

    @Test
    public void BaseCommandTest() {
        Z_Command scmd = new Z_Command(Z_Command.WakeUpType.DEVICE, "0b0da3", 30);
        String commandStr = scmd.getCommandString();
        assertEquals("z:1E,D,0b0da3" + '\r' + '\n', commandStr);
    }

    @Test
    public void WakeAllTest() {
        Z_Command scmd = new Z_Command(Z_Command.WakeUpType.ALL, "0b0da3", 60);
        String commandStr = scmd.getCommandString();
        assertEquals("z:3C,A" + '\r' + '\n', commandStr);

        scmd = Z_Command.wakeupAllDevices();
        commandStr = scmd.getCommandString();
        assertEquals("z:1E,A" + '\r' + '\n', commandStr);

        scmd = Z_Command.wakeupAllDevices(60);
        commandStr = scmd.getCommandString();
        assertEquals("z:3C,A" + '\r' + '\n', commandStr);
    }

    @Test
    public void WakeRoomTest() {
        Z_Command scmd = new Z_Command(Z_Command.WakeUpType.ROOM, "01", 30);
        String commandStr = scmd.getCommandString();
        assertEquals("z:1E,G,01" + '\r' + '\n', commandStr);

        scmd = Z_Command.wakeupRoom(1);
        commandStr = scmd.getCommandString();
        assertEquals("z:1E,G,01" + '\r' + '\n', commandStr);

        scmd = Z_Command.wakeupRoom(2, 60);
        commandStr = scmd.getCommandString();
        assertEquals("z:3C,G,02" + '\r' + '\n', commandStr);
    }

    @Test
    public void WakeDeviceTest() {

        Z_Command scmd = Z_Command.wakeupDevice("0b0da3");
        String commandStr = scmd.getCommandString();
        assertEquals("z:1E,D,0b0da3" + '\r' + '\n', commandStr);

        scmd = Z_Command.wakeupDevice("0b0da3", 60);
        commandStr = scmd.getCommandString();
        assertEquals("z:3C,D,0b0da3" + '\r' + '\n', commandStr);
    }
}
