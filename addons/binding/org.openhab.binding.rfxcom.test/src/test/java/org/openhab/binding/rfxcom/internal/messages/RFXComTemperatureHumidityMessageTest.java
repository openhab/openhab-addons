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
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityMessage.HumidityStatus.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityMessage.SubType.*;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityMessage.HumidityStatus;

/**
 * Test for RFXCom-binding
 *
 * @author Ivan F. Martinez
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComTemperatureHumidityMessageTest {

    private void testMessage(String hexMsg, RFXComTemperatureHumidityMessage.SubType subType, int seqNbr, int sensorId,
            double temperature, int humidity, HumidityStatus humidityStatus, int signalLevel, int batteryLevel)
            throws RFXComException {
        byte[] binaryMessage = HexUtils.hexToBytes(hexMsg);
        final RFXComTemperatureHumidityMessage msg = (RFXComTemperatureHumidityMessage) RFXComMessageFactory
                .createMessage(binaryMessage);
        assertEquals("SubType", subType, msg.subType);
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", sensorId, msg.sensorId);
        assertEquals("Temperature", temperature, msg.temperature, 0.01);
        assertEquals("Humidity", humidity, msg.humidity);
        assertEquals("Humidity Status", humidityStatus, msg.humidityStatus);
        assertEquals("Signal Level", signalLevel, msg.signalLevel);
        assertEquals("Battery Level", batteryLevel, msg.batteryLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, HexUtils.bytesToHex(decoded));
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0A5201800F0201294C0349", TH1, 128, 3842, 29.7, 76, WET, 4, 9);
        testMessage("0A520211700200A72D0089", TH2, 17, 28674, 16.7, 45, NORMAL, 8, 9);
        testMessage("0A5205D42F000082590379", TH5, 212, 12032, 13, 89, WET, 7, 9);
    }

}
