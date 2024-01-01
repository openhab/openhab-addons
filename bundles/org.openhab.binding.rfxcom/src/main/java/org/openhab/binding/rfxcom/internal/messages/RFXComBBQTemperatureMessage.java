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
 * RFXCOM data class for BBQ temperature message.
 *
 * @author Mike Jagdis - Initial contribution
 */
public class RFXComBBQTemperatureMessage extends RFXComBatteryDeviceMessage<RFXComBBQTemperatureMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        BBQ1(1); // Maverick ET-732

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
        return super.toString() + ", Sub type = " + subType + ", Device Id = " + getDeviceId() + ", Food temperature = "
                + foodTemperature + ", BBQ temperature = " + bbqTemperature + ", Signal level = " + signalLevel
                + ", Battery level = " + batteryLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

        foodTemperature = (data[6] & 0x7F) << 8 | (data[7] & 0xFF);
        if ((data[6] & 0x80) != 0) {
            foodTemperature = -foodTemperature;
        }

        bbqTemperature = (data[8] & 0x7F) << 8 | (data[9] & 0xFF);
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
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        if (CHANNEL_FOOD_TEMPERATURE.equals(channelId)) {
            return new DecimalType(foodTemperature);
        } else if (CHANNEL_BBQ_TEMPERATURE.equals(channelId)) {
            return new DecimalType(bbqTemperature);
        } else {
            return super.convertToState(channelId, config, deviceState);
        }
    }

    @Override
    public void setSubType(SubType subType) {
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
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }
}
