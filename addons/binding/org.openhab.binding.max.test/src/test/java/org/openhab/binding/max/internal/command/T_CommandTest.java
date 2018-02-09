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

import org.apache.commons.net.util.Base64;
import org.junit.Test;
import org.openhab.binding.max.internal.Utils;

/**
 * Tests cases for {@link T_Command}.
 *
 * @author Marcel Verpaalen - Initial version
 */
public class T_CommandTest {

    @Test
    public void PrefixTest() {
        T_Command scmd = new T_Command("0f1d54", false);

        String commandStr = scmd.getCommandString();
        String prefix = commandStr.substring(0, 2);

        assertEquals("t:", prefix);

    }

    @Test
    public void BaseCommandTest() {
        T_Command scmd = new T_Command("0f1d54", false);

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
        T_Command scmd = new T_Command("0f1d54", false);
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
        T_Command scmd = new T_Command("0f1d54", true);
        String commandStr = scmd.getCommandString();

        assertEquals("t:01,1,Dx1U\r\n", commandStr);
    }
}
