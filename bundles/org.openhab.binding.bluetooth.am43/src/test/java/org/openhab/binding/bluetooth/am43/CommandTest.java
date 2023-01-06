/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.am43;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.am43.internal.command.ControlCommand;
import org.openhab.binding.bluetooth.am43.internal.command.GetAllCommand;
import org.openhab.binding.bluetooth.am43.internal.data.ControlAction;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class CommandTest {

    @Test
    public void findAllCommandTest() {
        byte[] expected = HexUtils.hexToBytes("00ff00009aa701013d");
        byte[] actual = new GetAllCommand().getRequest();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void controlStopCommandTest() {
        byte[] expected = HexUtils.hexToBytes("00ff00009a0a01cc5d");
        byte[] actual = new ControlCommand(ControlAction.STOP).getRequest();

        assertArrayEquals(expected, actual);
    }

    @Test
    public void controlOpenCommandTest() {
        byte[] expected = HexUtils.hexToBytes("00ff00009a0a01dd4c");
        byte[] actual = new ControlCommand(ControlAction.OPEN).getRequest();

        assertArrayEquals(expected, actual);
    }
}
