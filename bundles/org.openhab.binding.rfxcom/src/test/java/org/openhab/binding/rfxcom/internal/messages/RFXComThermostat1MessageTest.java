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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComThermostat1MessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0940001B6B1816150270";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComThermostat1Message msg = (RFXComThermostat1Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(message);
        assertEquals(RFXComThermostat1Message.SubType.DIGIMAX, msg.subType, "SubType");
        assertEquals(27, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals("27416", msg.getDeviceId(), "Sensor Id");
        assertEquals(22, msg.temperature, "Temperature");
        assertEquals(21, msg.set, "Set point");
        assertEquals(RFXComThermostat1Message.Mode.HEATING, msg.mode, "Mode");
        assertEquals(RFXComThermostat1Message.Status.NO_DEMAND, msg.status, "Status");
        assertEquals((byte) 7, msg.signalLevel, "Signal Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
