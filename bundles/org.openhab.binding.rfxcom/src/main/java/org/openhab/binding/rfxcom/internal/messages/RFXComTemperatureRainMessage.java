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

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
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
 * RFXCOM data class for Temperature and Rain message.
 *
 * @author Damien Servant - Initial contribution
 * @author Martin van Wingerden - ported to openHAB 2.0
 */
public class RFXComTemperatureRainMessage extends RFXComBatteryDeviceMessage<RFXComTemperatureRainMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        WS1200(1);

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
    public double rainTotal;

    public RFXComTemperatureRainMessage() {
        super(PacketType.TEMPERATURE_RAIN);
    }

    public RFXComTemperatureRainMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + sensorId;
        str += ", Temperature = " + temperature;
        str += ", Rain total = " + rainTotal;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

        temperature = ((data[6] & 0x7F) << 8 | (data[7] & 0xFF)) * 0.1;
        if ((data[6] & 0x80) != 0) {
            temperature = -temperature;
        }
        rainTotal = ((data[8] & 0xFF) << 8 | (data[9] & 0xFF)) * 0.1;
        signalLevel = (byte) ((data[10] & 0xF0) >> 4);
        batteryLevel = (byte) (data[10] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[11];

        data[0] = (byte) (data.length - 1);
        data[1] = RFXComBaseMessage.PacketType.TEMPERATURE_RAIN.toByte();
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

        short rainT = (short) Math.abs(rainTotal * 10);
        data[8] = (byte) ((rainT >> 8) & 0xFF);
        data[9] = (byte) (rainT & 0xFF);
        data[10] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                return new DecimalType(temperature);

            case CHANNEL_RAIN_TOTAL:
                return new DecimalType(rainTotal);

            default:
                return super.convertToState(channelId, config, deviceState);
        }
    }

    @Override
    public void convertFromState(String channelId, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }

    @Override
    public void setSubType(SubType subType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDeviceId(String deviceId) {
        throw new UnsupportedOperationException();
    }
}
