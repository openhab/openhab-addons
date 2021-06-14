/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.RAW;

import java.nio.ByteBuffer;

import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageTooLongException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.util.HexUtils;

/**
 * RFXCOM data class for raw messages.
 *
 * @author James Hewitt-Thomas - New addition to the PRO RFXCom firmware
 */
public class RFXComRawMessage extends RFXComDeviceMessageImpl<RFXComRawMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        RAW_PACKET1(0x00),
        RAW_PACKET2(0x01),
        RAW_PACKET3(0x02),
        RAW_PACKET4(0x03),

        UNKNOWN(0xFF);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }

        public static SubType fromByte(int input) {
            for (SubType c : SubType.values()) {
                if (c.subType == input) {
                    return c;
                }
            }

            return SubType.UNKNOWN;
        }
    }

    public SubType subType;
    public byte repeat;
    public short[] pulses;

    public RFXComRawMessage() {
        super(RAW);
        pulses = new short[0];
    }

    public RFXComRawMessage(byte[] message) throws RFXComException {
        encodeMessage(message);
    }

    @Override
    public String toString() {
        String str = super.toString();

        str += ", Sub type = " + subType;

        return str;
    }

    @Override
    public void encodeMessage(byte[] message) throws RFXComException {
        super.encodeMessage(message);

        final int pulsesByteLen = rawMessage.length - 5;
        if (pulsesByteLen % 4 != 0) {
            throw new RFXComException("Incorrect byte length for pulses - must be divisible by 4");
        }

        subType = SubType.fromByte(super.subType);
        repeat = rawMessage[4];
        pulses = new short[pulsesByteLen / 2];
        ByteBuffer.wrap(rawMessage, 5, rawMessage.length - 5).asShortBuffer().get(pulses);
    }

    @Override
    public byte[] decodeMessage() throws RFXComException {
        if (pulses.length > 124) {
            throw new RFXComMessageTooLongException("Longest payload according to RFXtrx SDK is 124 shorts.");
        }

        final int pulsesByteLen = pulses.length * 2;
        byte[] data = new byte[5 + pulsesByteLen];

        data[0] = (byte) (data.length - 1);
        data[1] = RAW.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = repeat;

        ByteBuffer.wrap(data, 5, pulsesByteLen).asShortBuffer().put(pulses);

        return data;
    }

    @Override
    public String getDeviceId() {
        return "RAW";
    }

    @Override
    public State convertToState(String channelId, DeviceState deviceState) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_RAW_MESSAGE:
                return new StringType(HexUtils.bytesToHex(rawMessage));

            case CHANNEL_RAW_PAYLOAD:
                byte[] payload = new byte[pulses.length * 2];
                ByteBuffer.wrap(payload).asShortBuffer().put(pulses);
                return new StringType(HexUtils.bytesToHex(payload));

            default:
                throw new RFXComUnsupportedChannelException("Nothing relevant for " + channelId);
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
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_RAW_MESSAGE:
                if (type instanceof StringType) {
                    // TODO: Check the raw message for validity (length, no more than 124 shorts, multiple of 4 bytes in
                    // payload)
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " inot yet implemented");
                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }

            case CHANNEL_RAW_PAYLOAD:
                if (type instanceof StringType) {
                    // TODO: Check the payload for validity (no more than 124 shorts, multiple of 4 bytes
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " not yet implemented");
                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }

            default:
                throw new RFXComUnsupportedChannelException("Channel " + channelId + " is not relevant here");
        }
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }
}
