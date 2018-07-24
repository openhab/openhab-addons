/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.THERMOSTAT3;
import static org.openhab.binding.rfxcom.internal.messages.RFXComThermostat3Message.SubType.MERTIK__G6R_H4TB__G6R_H4T__G6R_H4T21_Z22;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
public class RFXComThermostat3MessageTest {
    @Test
    public void checkForSupportTest() throws RFXComException {
        RFXComMessageFactory.createMessage(THERMOSTAT3);
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComThermostat3Message message = (RFXComThermostat3Message) RFXComMessageFactory.createMessage(THERMOSTAT3);

        message.subType = RFXComThermostat3Message.SubType.MERTIK__G6R_H4S_TRANSMIT_ONLY;
        message.command = RFXComThermostat3Message.Commands.ON;

        RFXComTestHelper.basicBoundaryCheck(THERMOSTAT3, message);
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "08420101019FAB0280";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComThermostat3Message msg = (RFXComThermostat3Message) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", MERTIK__G6R_H4TB__G6R_H4T__G6R_H4T21_Z22, msg.subType);
        assertEquals("Seq Number", 1, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", "106411", msg.getDeviceId());
        assertEquals("Command", RFXComThermostat3Message.Commands.UP, msg.command);
        assertEquals("Signal Level", (byte) 8, msg.signalLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMessage, HexUtils.bytesToHex(decoded));
    }
    // TODO please add tests for real messages
}
