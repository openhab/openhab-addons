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

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.CHANNEL_TEMPERATURE;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

/**
 * RFXCOM data class for temperature and humidity message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComTemperatureMessage extends RFXComBatteryDeviceMessage<RFXComTemperatureMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        TEMP1(1),
        TEMP2(2),
        TEMP3(3),
        TEMP4(4),
        TEMP5(5),
        TEMP6(6),
        TEMP7(7),
        TEMP8(8),
        TEMP9(9),
        TEMP10(10),
        TEMP11(11);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    public SubType subType;
    public int sensorId;
    public double temperature;

    public RFXComTemperatureMessage() {
        super(PacketType.TEMPERATURE);
    }

    public RFXComTemperatureMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Temperature = " + temperature;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

        temperature = (short) ((data[6] & 0x7F) << 8 | (data[7] & 0xFF)) * 0.1;
        if ((data[6] & 0x80) != 0) {
            temperature = -temperature;
        }

        signalLevel = (byte) ((data[8] & 0xF0) >> 4);
        batteryLevel = (byte) (data[8] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[9];

        data[0] = 0x08;
        data[1] = RFXComBaseMessage.PacketType.TEMPERATURE.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);

        short temp = (short) Math.abs(temperature * 10);
        data[6] = (byte) ((temp >> 8) & 0xFF);
        data[7] = (byte) (temp & 0xFF);
        if (temperature < 0) {
            data[6] |= 0x80;
        }

        data[8] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                return new DecimalType(temperature);

            default:
                return super.convertToState(channelId, config, deviceState);
        }
    }

    @Override
    public void setSubType(SubType subType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDeviceId(String deviceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void convertFromState(String channelId, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }
}
