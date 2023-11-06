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
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureMessage.SubType.*;

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
public class RFXComTemperatureMessageTest {
    private void testMessage(String hexMsg, RFXComTemperatureMessage.SubType subType, int seqNbr, String deviceId,
            double temperature, int signalLevel, int bateryLevel) throws RFXComException {
        final RFXComTemperatureMessage msg = (RFXComTemperatureMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals(deviceId, msg.getDeviceId(), "Sensor Id");
        assertEquals(temperature, msg.temperature, 0.001, "Temperature");
        assertEquals(signalLevel, msg.signalLevel, "Signal Level");
        assertEquals(bateryLevel, msg.batteryLevel, "Battery");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("08500110000180BC69", TEMP1, 16, "1", -18.8d, 6, 9);
        testMessage("0850021DFB0100D770", TEMP2, 29, "64257", 21.5d, 7, 0);
        testMessage("08500502770000D389", TEMP5, 2, "30464", 21.1d, 8, 9);
        testMessage("0850091A00C3800689", TEMP9, 26, "195", -0.6d, 8, 9);
        testMessage("0850097200C300E089", TEMP9, 114, "195", 22.4d, 8, 9);
    }
}
