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

import static org.openhab.binding.rfxcom.RFXComBindingConstants.CHANNEL_CHANNEL1_AMPS;
import static org.openhab.binding.rfxcom.RFXComBindingConstants.CHANNEL_CHANNEL2_AMPS;
import static org.openhab.binding.rfxcom.RFXComBindingConstants.CHANNEL_CHANNEL3_AMPS;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

/**
 * RFXCOM data class for current message.
 *
 * @author Ben Jones - Initial contribution
 * @author Pauli Anttila - for the Similar RFXComEnergyMessage code
 * @author Jordan Cook - Added support for CURRENT devices, such as OWL CM113
 * @author Martin van Wingerden - Updated CurrentMessage code to new style
 */
public class RFXComCurrentMessage extends RFXComBatteryDeviceMessage<RFXComCurrentMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        ELEC1(1);

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

    public RFXComCurrentMessage() {
        super(PacketType.CURRENT);
    }

    public RFXComCurrentMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + sensorId;
        str += ", Count = " + count;
        str += ", Channel 1 Amps = " + channel1Amps;
        str += ", Channel 2 Amps = " + channel2Amps;
        str += ", Channel 3 Amps = " + channel3Amps;
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

        channel1Amps = ((data[7] & 0xFF) << 8 | (data[8] & 0xFF)) / 10.0;
        channel2Amps = ((data[9] & 0xFF) << 8 | (data[10] & 0xFF)) / 10.0;
        channel3Amps = ((data[11] & 0xFF) << 8 | (data[12] & 0xFF)) / 10.0;

        signalLevel = (byte) ((data[13] & 0xF0) >> 4);
        batteryLevel = (byte) (data[13] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[14];

        data[0] = 0x0D;
        data[1] = PacketType.CURRENT.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);
        data[6] = count;

        data[7] = (byte) (((int) (channel1Amps * 10) >> 8) & 0xFF);
        data[8] = (byte) ((int) (channel1Amps * 10) & 0xFF);

        data[9] = (byte) (((int) (channel2Amps * 10) >> 8) & 0xFF);
        data[10] = (byte) ((int) (channel2Amps * 10) & 0xFF);

        data[11] = (byte) (((int) (channel3Amps * 10) >> 8) & 0xFF);
        data[12] = (byte) ((int) (channel3Amps * 10) & 0xFF);

        data[13] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

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
