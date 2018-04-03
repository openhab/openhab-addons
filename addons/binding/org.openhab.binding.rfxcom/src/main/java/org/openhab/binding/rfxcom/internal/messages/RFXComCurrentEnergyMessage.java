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
 * RFXCOM data class for Current and Energy message.
 *
 * @author Damien Servant
 * @since 1.9.0
 */
public class RFXComCurrentEnergyMessage extends RFXComBatteryDeviceMessage<RFXComCurrentEnergyMessage.SubType> {
    private static final float TOTAL_USAGE_CONVERSION_FACTOR = 223.666F;

    public enum SubType implements ByteEnumWrapper {
        ELEC4(1); // OWL - CM180i

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
    public byte count;
    public double channel1Amps;
    public double channel2Amps;
    public double channel3Amps;
    public double totalUsage;

    public RFXComCurrentEnergyMessage() {
        super(PacketType.CURRENT_ENERGY);
    }

    public RFXComCurrentEnergyMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Id = " + sensorId;
        str += ", Count = " + count;
        str += ", Channel1 Amps = " + channel1Amps;
        str += ", Channel2 Amps = " + channel2Amps;
        str += ", Channel3 Amps = " + channel3Amps;
        str += ", Total Usage = " + totalUsage;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
        count = data[6];

        // Current = Field / 10
        channel1Amps = ((data[7] & 0xFF) << 8 | (data[8] & 0xFF)) / 10.0;
        channel2Amps = ((data[9] & 0xFF) << 8 | (data[10] & 0xFF)) / 10.0;
        channel3Amps = ((data[11] & 0xFF) << 8 | (data[12] & 0xFF)) / 10.0;

        totalUsage = ((long) (data[13] & 0xFF) << 40 | (long) (data[14] & 0xFF) << 32 | (data[15] & 0xFF) << 24
                | (data[16] & 0xFF) << 16 | (data[17] & 0xFF) << 8 | (data[18] & 0xFF));
        totalUsage = totalUsage / TOTAL_USAGE_CONVERSION_FACTOR;

        signalLevel = (byte) ((data[19] & 0xF0) >> 4);
        batteryLevel = (byte) (data[19] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[20];

        data[0] = (byte) (data.length - 1);
        data[1] = RFXComBaseMessage.PacketType.CURRENT_ENERGY.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);
        data[6] = count;

        data[7] = (byte) (((int) (channel1Amps * 10.0) >> 8) & 0xFF);
        data[8] = (byte) ((int) (channel1Amps * 10.0) & 0xFF);
        data[9] = (byte) (((int) (channel2Amps * 10.0) >> 8) & 0xFF);
        data[10] = (byte) ((int) (channel2Amps * 10.0) & 0xFF);
        data[11] = (byte) (((int) (channel3Amps * 10.0) >> 8) & 0xFF);
        data[12] = (byte) ((int) (channel3Amps * 10.0) & 0xFF);

        long totalUsageLoc = (long) (totalUsage * TOTAL_USAGE_CONVERSION_FACTOR);

        data[13] = (byte) ((totalUsageLoc >> 40) & 0xFF);
        data[14] = (byte) ((totalUsageLoc >> 32) & 0xFF);
        data[15] = (byte) ((totalUsageLoc >> 24) & 0xFF);
        data[16] = (byte) ((totalUsageLoc >> 16) & 0xFF);
        data[17] = (byte) ((totalUsageLoc >> 8) & 0xFF);
        data[18] = (byte) (totalUsageLoc & 0xFF);

        data[19] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_CHANNEL1_AMPS:
                return new DecimalType(channel1Amps);

            case CHANNEL_CHANNEL2_AMPS:
                return new DecimalType(channel2Amps);

            case CHANNEL_CHANNEL3_AMPS:
                return new DecimalType(channel3Amps);

            case CHANNEL_TOTAL_USAGE:
                return new DecimalType(totalUsage);

            default:
                return super.convertToState(channelId);
        }
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
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

    @Override
    public void setSubType(SubType subType) {
        this.subType = subType;
    }
}
