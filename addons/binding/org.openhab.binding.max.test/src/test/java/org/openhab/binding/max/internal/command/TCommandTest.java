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

import org.apache.commons.net.util.Base64;
import org.junit.Test;
import org.openhab.binding.max.internal.Utils;

/**
 * Tests cases for {@link TCommand}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class TCommandTest {

    @Test
    public void PrefixTest() {
        TCommand scmd = new TCommand("0f1d54", false);

        String commandStr = scmd.getCommandString();
        String prefix = commandStr.substring(0, 2);

        assertEquals("t:", prefix);
    }

    @Test
    public void BaseCommandTest() {
        TCommand scmd = new TCommand("0f1d54", false);

        String commandStr = scmd.getCommandString();

        String base64Data = commandStr.split(",")[2];
        byte[] bytes = Base64.decodeBase64(base64Data.getBytes());
        int[] data = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            data[i] = bytes[i] & 0xFF;
        }
        String decodedString = Utils.toHex(data);

        assertEquals("t:01,0,Dx1U\r\n", commandStr);
        assertEquals("0F1D54", decodedString);
    }

    @Test
    public void AddRoomTest() {
        TCommand scmd = new TCommand("0f1d54", false);
        scmd.addRoom("0b0da3");

        String commandStr = scmd.getCommandString();

        String base64Data = commandStr.split(",")[2];
        byte[] bytes = Base64.decodeBase64(base64Data.getBytes());
        int[] data = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            data[i] = bytes[i] & 0xFF;
        }
        String decodedString = Utils.toHex(data);

        assertEquals("t:02,0,Cw2jDx1U\r\n", commandStr);
        assertEquals("0B0DA30F1D54", decodedString);
    }

    @Test
    public void ForceModeTest() {
        TCommand scmd = new TCommand("0f1d54", true);
        String commandStr = scmd.getCommandString();

        assertEquals("t:01,1,Dx1U\r\n", commandStr);
    }
}
