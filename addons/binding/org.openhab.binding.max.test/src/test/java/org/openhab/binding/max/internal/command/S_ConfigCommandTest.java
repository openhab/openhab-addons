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

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.max.internal.command.S_ConfigCommand.ConfigCommandType;

/**
 * Tests cases for {@link S_ConfigCommand}.
 *
 * @author Marcel Verpaalen - Initial Version
 * @since 2.0
 */
public class S_ConfigCommandTest {

    private final String rfTestAddress = "0e15cc";
    private final int testRoom = 2;

    private CubeCommand cubeCommand = null;

    @Before
    public void Before() {
        cubeCommand = new S_ConfigCommand(rfTestAddress, testRoom, ConfigCommandType.SetRoom);
    }

    @Test
    public void setRoomTest() {
        cubeCommand = new S_ConfigCommand(rfTestAddress, testRoom, ConfigCommandType.SetRoom);
        String commandString = cubeCommand.getCommandString();
        assertEquals("s:AAAiAAAADhXMAAI=\r\n", commandString);
    }

    @Test
    public void removeRoomTest() {
        cubeCommand = new S_ConfigCommand(rfTestAddress, 1, ConfigCommandType.RemoveRoom);
        String commandString = cubeCommand.getCommandString();
        assertEquals("s:AAAjAAAADhXMAAE=\r\n", commandString);
    }
}