/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.max.internal.Utils;
import org.openhab.binding.max.internal.device.ThermostatModeType;

/**
 * Tests cases for {@link SCommand}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class SCommandTest {

    @Test
    public void prefixTest() {
        SCommand scmd = new SCommand("0b0da3", 1, ThermostatModeType.MANUAL, 20.0);
        String commandStr = scmd.getCommandString();
        String prefix = commandStr.substring(0, 2);

        assertEquals("s:", prefix);
    }

    @Test
    public void baseCommandTest() {
        SCommand scmd = new SCommand("0b0da3", 1, ThermostatModeType.MANUAL, 20.0);

        String commandStr = scmd.getCommandString();

        String base64Data = commandStr.substring(2).trim();
        byte[] bytes = Base64.getDecoder().decode(base64Data.getBytes(StandardCharsets.UTF_8));

        int[] data = new int[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            data[i] = bytes[i] & 0xFF;
        }

        String decodedString = Utils.toHex(data);
        assertEquals("s:AARAAAAACw2jAWg=\r\n", commandStr);
        assertEquals("0004400000000B0DA30168", decodedString);
    }

    @Test
    public void boostModeTest() {
        SCommand scmd = new SCommand("0b0da3", 1, ThermostatModeType.BOOST, 21.0);
        String commandStr = scmd.getCommandString();
        assertEquals("s:AARAAAAACw2jAeo=\r\n", commandStr);
    }

    @Test
    public void autoModeTest() {
        SCommand scmd = new SCommand("0b0da3", 1, ThermostatModeType.AUTOMATIC, 0);
        String commandStr = scmd.getCommandString();
        assertEquals("s:AARAAAAACw2jAQA=\r\n", commandStr);
    }
}
