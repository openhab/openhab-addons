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
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

/**
 * RFXCOM data class for temperature and humidity message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComTemperatureHumidityMessage
        extends RFXComBatteryDeviceMessage<RFXComTemperatureHumidityMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        TH1(1),
        TH2(2),
        TH3(3),
        TH4(4),
        TH5(5),
        TH6(6),
        TH7(7),
        TH8(8),
        TH9(9),
        TH10(10),
        TH11(11),
        TH12(12),
        TH13(13),
        TH14(14);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    public enum HumidityStatus implements ByteEnumWrapper {
        NORMAL(0),
        COMFORT(1),
        DRY(2),
        WET(3);

        private final int humidityStatus;

        HumidityStatus(int humidityStatus) {
            this.humidityStatus = humidityStatus;
        }

        @Override
        public byte toByte() {
            return (byte) humidityStatus;
        }
    }

    public SubType subType;
    public int sensorId;
    public double temperature;
    public byte humidity;
    public HumidityStatus humidityStatus;

    public RFXComTemperatureHumidityMessage() {
        super(PacketType.TEMPERATURE_HUMIDITY);
    }

    public RFXComTemperatureHumidityMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Temperature = " + temperature;
        str += ", Humidity = " + humidity;
        str += ", Humidity status = " + humidityStatus;
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

        humidity = data[8];
        humidityStatus = fromByte(HumidityStatus.class, data[9]);

        signalLevel = (byte) ((data[10] & 0xF0) >> 4);
        batteryLevel = (byte) (data[10] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[11];

        data[0] = 0x0A;
        data[1] = RFXComBaseMessage.PacketType.TEMPERATURE_HUMIDITY.toByte();
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

        data[8] = humidity;
        data[9] = humidityStatus.toByte();
        data[10] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

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

            case CHANNEL_HUMIDITY:
                return new DecimalType(humidity);

            case CHANNEL_HUMIDITY_STATUS:
                return new StringType(humidityStatus.toString());

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
