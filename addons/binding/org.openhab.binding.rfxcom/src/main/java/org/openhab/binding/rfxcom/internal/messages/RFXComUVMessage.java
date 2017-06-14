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
import org.eclipse.smarthome.core.types.UnDefType;

import static org.openhab.binding.rfxcom.RFXComBindingConstants.*;

import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for UV and temperature message.
 *
 * @author Damien Servant - OpenHAB1 version
 * @author Mike Jagdis - Initial contribution, OpenHAB2 version
 */
public class RFXComUVMessage extends RFXComBaseMessage {

    public enum SubType {
        UV1(1), // UVN128, UV138
        UV2(2), // UVN800
        UV3(3); // TFA

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
    public double uv;
    public double temperature;
    public byte signalLevel;
    public byte batteryLevel;

    public RFXComUVMessage() {
        packetType = PacketType.UV;
    }

    public RFXComUVMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        return super.toString()
            + ", Sub type = " + subType
            + ", Device Id = " + getDeviceId()
            + ", UV = " + uv
            + ", Temperature = " + temperature
            + ", Signal level = " + signalLevel
            + ", Battery level = " + batteryLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

        uv = (data[6] & 0xFF) * 0.1;

        temperature = (short) ((data[7] & 0x7F) << 8 | (data[8] & 0xFF)) * 0.1;
        if ((data[7] & 0x80) != 0) {
            temperature = -temperature;
        }

        signalLevel = (byte) ((data[9] & 0xF0) >> 4);
        batteryLevel = (byte) (data[9] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[10];

        data[0] = 0x09;
        data[1] = RFXComBaseMessage.PacketType.UV.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);

        data[6] = (byte) (((short) (uv * 10)) & 0xFF);

        short temp = (short) Math.abs(temperature * 10);
        data[7] = (byte) ((temp >> 8) & 0xFF);
        data[8] = (byte) (temp & 0xFF);
        if (temperature < 0) {
            data[7] |= 0x80;
        }

        data[9] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(String channelId) throws RFXComException {

        switch (channelId) {
            case CHANNEL_SIGNAL_LEVEL:
                return new DecimalType(signalLevel);

            case CHANNEL_BATTERY_LEVEL:
                return new DecimalType(batteryLevel);

            case CHANNEL_UV:
                return new DecimalType(uv);

            case CHANNEL_TEMPERATURE:
                return (subType == SubType.UV3 ? new DecimalType(temperature) : UnDefType.UNDEF);

            default:
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
