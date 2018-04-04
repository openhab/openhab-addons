/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for temperature and humidity message.
 *
 * @author Marc SAUVEUR - Initial contribution
 * @author Pauli Anttila
 * @author Mike Jagdis - Support all available data from sensors
 */
public class RFXComWindMessage extends RFXComBatteryDeviceMessage<RFXComWindMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        WIND1(1),
        WIND2(2),
        WIND3(3),
        WIND4(4),
        WIND5(5),
        WIND6(6),
        WIND7(7);

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
    public double windDirection;
    public double windSpeed;
    public double avgWindSpeed;
    public double temperature;
    public double chillTemperature;

    public RFXComWindMessage() {
        super(PacketType.WIND);
    }

    public RFXComWindMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        return super.toString()
            + ", Sub type = " + subType
            + ", Device Id = " + getDeviceId()
            + ", Wind direction = " + windDirection
            + ", Wind gust = " + windSpeed
            + ", Average wind speed = " + avgWindSpeed
            + ", Temperature = " + temperature
            + ", Chill temperature = " + chillTemperature
            + ", Signal level = " + signalLevel
            + ", Battery level = " + batteryLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

        windDirection = (short) ((data[6] & 0xFF) << 8 | (data[7] & 0xFF));

        if (subType != SubType.WIND5) {
            avgWindSpeed = (short) ((data[8] & 0xFF) << 8 | (data[9] & 0xFF)) * 0.1;
        }

        windSpeed = (short) ((data[10] & 0xFF) << 8 | (data[11] & 0xFF)) * 0.1;

        if (subType == SubType.WIND4) {
            temperature = (short) ((data[12] & 0x7F) << 8 | (data[13] & 0xFF)) * 0.1;
            if ((data[12] & 0x80) != 0) {
                temperature = -temperature;
            }

            chillTemperature = (short) ((data[14] & 0x7F) << 8 | (data[15] & 0xFF)) * 0.1;
            if ((data[14] & 0x80) != 0) {
                chillTemperature = -chillTemperature;
            }
        }

        signalLevel = (byte) ((data[16] & 0xF0) >> 4);
        batteryLevel = (byte) (data[16] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[17];

        data[0] = 0x10;
        data[1] = PacketType.WIND.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);

        short absWindDirection = (short) Math.abs(windDirection);
        data[6] = (byte) ((absWindDirection >> 8) & 0xFF);
        data[7] = (byte) (absWindDirection & 0xFF);

        if (subType != SubType.WIND5) {
            int absAvgWindSpeedTimesTen = (short) Math.abs(avgWindSpeed) * 10;
            data[8] = (byte) ((absAvgWindSpeedTimesTen >> 8) & 0xFF);
            data[9] = (byte) (absAvgWindSpeedTimesTen & 0xFF);
        }

        int absWindSpeedTimesTen = (short) Math.abs(windSpeed) * 10;
        data[10] = (byte) ((absWindSpeedTimesTen >> 8) & 0xFF);
        data[11] = (byte) (absWindSpeedTimesTen & 0xFF);

        if (subType == SubType.WIND4) {
            int temp = (short) Math.abs(temperature) * 10;
            data[12] = (byte) ((temp >> 8) & 0x7F);
            data[13] = (byte) (temp & 0xFF);
            if (temperature < 0) {
                data[12] |= 0x80;
            }

            int chill = (short) Math.abs(chillTemperature) * 10;
            data[14] = (byte) ((chill >> 8) & 0x7F);
            data[15] = (byte) (chill & 0xFF);
            if (chillTemperature < 0) {
                data[14] |= 0x80;
            }
        }

        data[16] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_WIND_DIRECTION:
                return new DecimalType(windDirection);

            case CHANNEL_AVG_WIND_SPEED:
                return new DecimalType(avgWindSpeed);

            case CHANNEL_WIND_SPEED:
                return new DecimalType(windSpeed);

            case CHANNEL_TEMPERATURE:
                return new DecimalType(temperature);

            case CHANNEL_CHILL_TEMPERATURE:
                return new DecimalType(chillTemperature);

            default:
                return super.convertToState(channelId);
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
