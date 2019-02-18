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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 */
public class RFXComThermostat1MessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0940001B6B1816150270";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComThermostat1Message msg = (RFXComThermostat1Message) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", RFXComThermostat1Message.SubType.DIGIMAX, msg.subType);
        assertEquals("Seq Number", 27, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", "27416", msg.getDeviceId());
        assertEquals("Temperature", 22, msg.temperature);
        assertEquals("Set point", 21, msg.set);
        assertEquals("Mode", RFXComThermostat1Message.Mode.HEATING, msg.mode);
        assertEquals("Status", RFXComThermostat1Message.Status.NO_DEMAND, msg.status);
        assertEquals("Signal Level", (byte) 7, msg.signalLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMessage, HexUtils.bytesToHex(decoded));
    }
}
