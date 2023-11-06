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
import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComRFXSensorMessage.SubType.*;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComRFXSensorMessageTest {
    private final MockDeviceState mockedDeviceState = new MockDeviceState();

    private void testMessage(String hexMsg, RFXComRFXSensorMessage.SubType subType, int seqNbr, String deviceId,
            @Nullable Double temperature, @Nullable Double voltage, @Nullable Double referenceVoltage,
            @Nullable Double expectedPressure, @Nullable Double expectedHumidity, int signalLevel,
            DeviceState deviceState) throws RFXComException {
        final RFXComRFXSensorMessage msg = (RFXComRFXSensorMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(DatatypeConverter.parseHexBinary(hexMsg));
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals(deviceId, msg.getDeviceId(), "Sensor Id");
        assertEquals(signalLevel, msg.signalLevel, "Signal Level");
        assertEquals(temperature, getMessageTemperature(msg, deviceState), "Temperature");
        assertEquals(voltage, getChannelAsDouble(CHANNEL_VOLTAGE, msg, deviceState), "Voltage");
        assertEquals(referenceVoltage, getChannelAsDouble(CHANNEL_REFERENCE_VOLTAGE, msg, deviceState),
                "Reference Voltage");
        assertEquals(expectedHumidity, getChannelAsDouble(CHANNEL_HUMIDITY, msg, deviceState), "Humidity");
        assertEquals(expectedPressure, getChannelAsDouble(CHANNEL_PRESSURE, msg, deviceState), "Pressure");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, DatatypeConverter.printHexBinary(decoded), "Message converted back");
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0770000008080270", TEMPERATURE, 0, "8", 20.5d, null, null, null, null, 7, mockedDeviceState);
        testMessage("0770000208809650", TEMPERATURE, 2, "8", -1.5d, null, null, null, null, 5, mockedDeviceState);
        testMessage("077002010801F270", VOLTAGE, 1, "8", null, null, 4.98, null, null, 7, mockedDeviceState);
        testMessage("077001020800F470", A_D, 2, "8", null, 2.44, null, null, null, 7, mockedDeviceState);
    }

    @Test
    public void testPressure() throws RFXComException {
        MockDeviceState deviceState = new MockDeviceState();
        deviceState.set(CHANNEL_REFERENCE_VOLTAGE, new DecimalType(4.98));

        testMessage("077001020800F470", A_D, 2, "8", null, 2.44, null, 650.0, null, 7, deviceState);
    }

    @Test
    public void testHumidity() throws RFXComException {
        MockDeviceState deviceState = new MockDeviceState();
        deviceState.set(CHANNEL_TEMPERATURE, new DecimalType(20.5));
        deviceState.set(CHANNEL_REFERENCE_VOLTAGE, new DecimalType(4.98));

        testMessage("077001020800F470", A_D, 2, "8", null, 2.44, null, 650.0, 52.6821, 7, deviceState);
    }

    private @Nullable Double getMessageTemperature(RFXComRFXSensorMessage msg, DeviceState deviceState)
            throws RFXComException {
        return getChannelAsDouble(CHANNEL_TEMPERATURE, msg, deviceState);
    }

    private @Nullable Double getChannelAsDouble(String channelId, RFXComRFXSensorMessage msg, DeviceState deviceState)
            throws RFXComException {
        return getStateAsDouble(msg.convertToState(channelId, null, deviceState));
    }

    private @Nullable Double getStateAsDouble(State state) {
        if (state instanceof DecimalType decimalCommand) {
            return decimalCommand.doubleValue();
        } else {
            return null;
        }
    }
}
