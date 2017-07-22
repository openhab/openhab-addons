/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

import static org.openhab.binding.rfxcom.RFXComBindingConstants.CHANNEL_BBQ_TEMPERATURE;
import static org.openhab.binding.rfxcom.RFXComBindingConstants.CHANNEL_FOOD_TEMPERATURE;

/**
 * RFXCOM data class for BBQ temperature message.
 *
 * @author Mike Jagdis - Initial contribution
 */
public class RFXComBBQTemperatureMessage extends RFXComBatteryDeviceMessage {

    public enum SubType {
        BBQ1(1); // Maverick ET-732

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
    public double foodTemperature;
    public double bbqTemperature;
    public byte signalLevel;
    public byte batteryLevel;

    public RFXComBBQTemperatureMessage() {
        super(PacketType.BBQ);
    }

    public RFXComBBQTemperatureMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        return super.toString()
            + ", Sub type = " + subType
            + ", Device Id = " + getDeviceId()
            + ", Food temperature = " + foodTemperature
            + ", BBQ temperature = " + bbqTemperature
            + ", Signal level = " + signalLevel
            + ", Battery level = " + batteryLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

        foodTemperature = (double) ((data[6] & 0x7F) << 8 | (data[7] & 0xFF));
        if ((data[6] & 0x80) != 0) {
            foodTemperature = -foodTemperature;
        }

        bbqTemperature = (double) ((data[8] & 0x7F) << 8 | (data[9] & 0xFF));
        if ((data[8] & 0x80) != 0) {
            bbqTemperature = -bbqTemperature;
        }

        signalLevel = (byte) ((data[10] & 0xF0) >> 4);
        batteryLevel = (byte) (data[10] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[11];

        data[0] = 0x0A;
        data[1] = RFXComBaseMessage.PacketType.BBQ.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);

        short temp = (short) Math.abs(foodTemperature);
        data[6] = (byte) ((temp >> 8) & 0x7F);
        data[7] = (byte) (temp & 0xFF);
        if (foodTemperature < 0) {
            data[6] |= 0x80;
        }

        temp = (short) Math.abs(bbqTemperature);
        data[8] = (byte) ((temp >> 8) & 0x7F);
        data[9] = (byte) (temp & 0xFF);
        if (bbqTemperature < 0) {
            data[8] |= 0x80;
        }

        data[10] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        if (CHANNEL_FOOD_TEMPERATURE.equals(channelId)) {
            return new DecimalType(foodTemperature);
        } else if (CHANNEL_BBQ_TEMPERATURE.equals(channelId)) {
            return new DecimalType(bbqTemperature);
        } else {
            return super.convertToState(channelId);
        }
    }

    @Override
    public void setSubType(Object subType)  {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setDeviceId(String deviceId) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void convertFromState(String channelId, Type type) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object convertSubType(String subType) throws RFXComUnsupportedValueException {

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
