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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityMessage.HumidityStatus.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityMessage.SubType.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityMessage.HumidityStatus;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Ivan F. Martinez - Initial contribution
 * @author Martin van Wingerden - Extended tests
 */
@NonNullByDefault
public class RFXComTemperatureHumidityMessageTest {

    private void testMessage(String hexMsg, RFXComTemperatureHumidityMessage.SubType subType, int seqNbr, int sensorId,
            double temperature, int humidity, HumidityStatus humidityStatus, int signalLevel, int batteryLevel)
            throws RFXComException {
        byte[] binaryMessage = HexUtils.hexToBytes(hexMsg);
        final RFXComTemperatureHumidityMessage msg = (RFXComTemperatureHumidityMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(binaryMessage);
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals(sensorId, msg.sensorId, "Sensor Id");
        assertEquals(temperature, msg.temperature, 0.01, "Temperature");
        assertEquals(humidity, msg.humidity, "Humidity");
        assertEquals(humidityStatus, msg.humidityStatus, "Humidity Status");
        assertEquals(signalLevel, msg.signalLevel, "Signal Level");
        assertEquals(batteryLevel, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0A5201800F0201294C0349", TH1, 128, 3842, 29.7, 76, WET, 4, 9);
        testMessage("0A520211700200A72D0089", TH2, 17, 28674, 16.7, 45, NORMAL, 8, 9);
        testMessage("0A5205D42F000082590379", TH5, 212, 12032, 13, 89, WET, 7, 9);
    }
}
