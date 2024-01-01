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
 * RFXCOM data class for temperature and humidity message.
 *
 * @author Marc SAUVEUR - Initial contribution
 * @author Pauli Anttila - Migrated for OH2
 */
public class RFXComRainMessage extends RFXComBatteryDeviceMessage<RFXComRainMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        RAIN1(1),
        RAIN2(2),
        RAIN3(3),
        RAIN4(4),
        RAIN5(5),
        RAIN6(6),
        RAIN7(7),
        RAIN8(8),
        RAIN9(9);

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
    public double rainRate;
    public double rainTotal;

    public RFXComRainMessage() {
        super(PacketType.RAIN);
    }

    public RFXComRainMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Rain rate = " + rainRate;
        str += ", Rain total = " + rainTotal;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

        rainRate = (short) ((data[6] & 0xFF) << 8 | (data[7] & 0xFF));
        if (subType == SubType.RAIN2) {
            rainRate *= 0.01;
        }

        if (subType == SubType.RAIN6) {
            rainTotal = (short) ((data[10] & 0xFF)) * 0.266;
        } else {
            rainTotal = (short) ((data[8] & 0xFF) << 8 | (data[9] & 0xFF) << 8 | (data[10] & 0xFF)) * 0.1;
        }

        signalLevel = (byte) ((data[11] & 0xF0) >> 4);
        batteryLevel = (byte) (data[11] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[12];

        data[0] = 0x0B;
        data[1] = RFXComBaseMessage.PacketType.RAIN.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);

        short rainR = (short) Math.abs(rainRate * 100);
        data[6] = (byte) ((rainR >> 8) & 0xFF);
        data[7] = (byte) (rainR & 0xFF);

        short rainT = (short) Math.abs(rainTotal * 10);
        data[8] = (byte) ((rainT >> 16) & 0xFF);
        data[9] = (byte) ((rainT >> 8) & 0xFF);
        data[10] = (byte) (rainT & 0xFF);

        data[11] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

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
            case CHANNEL_RAIN_RATE:
                return new DecimalType(rainRate);

            case CHANNEL_RAIN_TOTAL:
                return new DecimalType(rainTotal);

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
