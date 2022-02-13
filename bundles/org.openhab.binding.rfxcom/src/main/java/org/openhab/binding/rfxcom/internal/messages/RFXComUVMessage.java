/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.core.types.UnDefType;

/**
 * RFXCOM data class for UV and temperature message.
 *
 * @author Damien Servant - OpenHAB1 version
 * @author Mike Jagdis - Initial contribution, OpenHAB2 version
 */
public class RFXComUVMessage extends RFXComBatteryDeviceMessage<RFXComUVMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        UV1(1), // UVN128, UV138
        UV2(2), // UVN800
        UV3(3); // TFA

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
    public double uv;
    public double temperature;

    public RFXComUVMessage() {
        super(PacketType.UV);
    }

    public RFXComUVMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        //@formatter:off
        return super.toString()
                + ", Sub type = " + subType
                + ", Device Id = " + getDeviceId()
                + ", UV = " + uv
                + ", Temperature = " + temperature
                + ", Signal level = " + signalLevel
                + ", Battery level = " + batteryLevel;
        //@formatter:on
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
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
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        switch (channelId) {
            case CHANNEL_UV:
                return new DecimalType(uv);

            case CHANNEL_TEMPERATURE:
                return (subType == SubType.UV3 ? new DecimalType(temperature) : UnDefType.UNDEF);

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
