/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityBarometricMessage.ForecastStatus.RAIN;
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityBarometricMessage.HumidityStatus.DRY;
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityBarometricMessage.SubType.THB2;

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
public class RFXComTemperatureHumidityBarometricMessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0D54020EE90000C9270203E70439";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComTemperatureHumidityBarometricMessage msg = (RFXComTemperatureHumidityBarometricMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(message);
        assertEquals(THB2, msg.subType, "SubType");
        assertEquals(14, msg.seqNbr, "Seq Number");
        assertEquals("59648", msg.getDeviceId(), "Sensor Id");
        assertEquals(20.1, msg.temperature, 0.01, "Temperature");
        assertEquals(39, msg.humidity, "Humidity");
        assertEquals(DRY, msg.humidityStatus, "Humidity status");
        assertEquals(999.0, msg.pressure, 0.001, "Barometer");
        assertEquals(RAIN, msg.forecastStatus, "Forecast");
        assertEquals(3, msg.signalLevel, "Signal Level");
        assertEquals(9, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
