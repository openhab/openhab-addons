/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;

import static org.openhab.binding.rfxcom.RFXComBindingConstants.*;

import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for temperature and humidity message.
 *
 * @author Marc SAUVEUR - Initial contribution
 * @author Pauli Anttila
 * @author Mike Jagdis - Support all available data from sensors
 */
public class RFXComWindMessage extends RFXComBaseMessage {

    public enum SubType {
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

        public byte toByte() {
            return (byte) subType;
        }

        public static SubType fromByte(int input) throws RFXComUnsupportedValueException {
            for (SubType c : SubType.values()) {
                if (c.subType == input) {
                    return c;
                }
            }

            throw new RFXComUnsupportedValueException(SubType.class, input);
        }
    }

    public SubType subType;
    public int sensorId;
    public double windDirection;
    public double windSpeed;
    public double avgWindSpeed;
    public double temperature;
    public double chillTemperature;
    public byte signalLevel;
    public byte batteryLevel;

    public RFXComWindMessage() {
        packetType = PacketType.WIND;
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

        subType = SubType.fromByte(super.subType);
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
    public State convertToState(String channelId) throws RFXComException {

        if (channelId == CHANNEL_SIGNAL_LEVEL) {
            return new DecimalType(signalLevel);

        } else if (channelId == CHANNEL_BATTERY_LEVEL) {
            return new DecimalType(batteryLevel);

        } else if (channelId == CHANNEL_WIND_DIRECTION) {
            return new DecimalType(windDirection);

        } else if (channelId == CHANNEL_AVG_WIND_SPEED) {
            return new DecimalType(avgWindSpeed);

        } else if (channelId == CHANNEL_WIND_SPEED) {
            return new DecimalType(windSpeed);

        } else if (channelId == CHANNEL_TEMPERATURE) {
            return new DecimalType(temperature);

        } else if (channelId == CHANNEL_CHILL_TEMPERATURE) {
            return new DecimalType(chillTemperature);

        } else {
            throw new RFXComException("Nothing relevant for " + channelId);
        }
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public Object convertSubType(String subType) throws RFXComException {

        for (SubType s : SubType.values()) {
            if (s.toString().equals(subType)) {
                return s;
            }
        }

        try {
            return SubType.fromByte(Integer.parseInt(subType));
        } catch (NumberFormatException e) {
            throw new RFXComUnsupportedValueException(SubType.class, subType);
        }
    }
}
