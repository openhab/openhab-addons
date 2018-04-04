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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for temperature, humidity and barometric message.
 *
 * @author Damien Servant
 * @author Martin van Wingerden - ported to openHAB 2.0
 * @since 1.9.0
 */
public class RFXComTemperatureHumidityBarometricMessage
        extends RFXComBatteryDeviceMessage<RFXComTemperatureHumidityBarometricMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        THB1(1), // BTHR918, BTHGN129
        THB2(2); // BTHR918N, BTHR968

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

    public enum ForecastStatus implements ByteEnumWrapper {
        NO_INFO_AVAILABLE(0),
        SUNNY(1),
        PARTLY_CLOUDY(2),
        CLOUDY(3),
        RAIN(4);

        private final int forecastStatus;

        ForecastStatus(int forecastStatus) {
            this.forecastStatus = forecastStatus;
        }

        @Override
        public byte toByte() {
            return (byte) forecastStatus;
        }
    }

    public SubType subType;
    public int sensorId;
    public double temperature;
    public byte humidity;
    public HumidityStatus humidityStatus;
    public double pressure;
    public ForecastStatus forecastStatus;

    public RFXComTemperatureHumidityBarometricMessage() {
        super(PacketType.TEMPERATURE_HUMIDITY_BAROMETRIC);
    }

    public RFXComTemperatureHumidityBarometricMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + sensorId;
        str += ", Temperature = " + temperature;
        str += ", Humidity = " + humidity;
        str += ", Humidity status = " + humidityStatus;
        str += ", Pressure = " + pressure;
        str += ", Forecast = " + forecastStatus;
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

        pressure = (data[10] & 0xFF) << 8 | (data[11] & 0xFF);
        forecastStatus = fromByte(ForecastStatus.class, data[12]);

        signalLevel = (byte) ((data[13] & 0xF0) >> 4);
        batteryLevel = (byte) (data[13] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[14];

        data[0] = 0x0D;
        data[1] = RFXComBaseMessage.PacketType.TEMPERATURE_HUMIDITY_BAROMETRIC.toByte();
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

        temp = (short) (pressure);
        data[10] = (byte) ((temp >> 8) & 0xFF);
        data[11] = (byte) (temp & 0xFF);

        data[12] = forecastStatus.toByte();

        data[13] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                return new DecimalType(temperature);

            case CHANNEL_HUMIDITY:
                return new DecimalType(humidity);

            case CHANNEL_PRESSURE:
                return new DecimalType(pressure);

            case CHANNEL_HUMIDITY_STATUS:
                return new StringType(humidityStatus.toString());

            case CHANNEL_FORECAST:
                return new StringType(forecastStatus.toString());

            default:
                return super.convertToState(channelId);
        }
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
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
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new UnsupportedOperationException();
    }
}
