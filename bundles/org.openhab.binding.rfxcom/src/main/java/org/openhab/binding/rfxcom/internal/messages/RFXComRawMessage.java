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
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.RAW;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.config.RFXComRawDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageTooLongException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.util.HexUtils;

/**
 * RFXCOM data class for raw messages.
 *
 * @author James Hewitt-Thomas - Initial contribution, new addition to the PRO RFXCom firmware
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

    private RFXComRawDeviceConfiguration config;

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
        data[4] = (byte) config.repeat;

        ByteBuffer.wrap(data, 5, pulsesByteLen).asShortBuffer().put(pulses);

        return data;
    }

    @Override
    public String getDeviceId() {
        return "RAW";
    }

    @Override
    public void setConfig(RFXComDeviceConfiguration config) throws RFXComException {
        super.setConfig(config);
        this.config = (RFXComRawDeviceConfiguration) config;
    }

    @Override
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_RAW_MESSAGE:
                return new StringType(HexUtils.bytesToHex(rawMessage));

            case CHANNEL_RAW_PAYLOAD:
                byte[] payload = new byte[pulses.length * 2];
                ByteBuffer.wrap(payload).asShortBuffer().put(pulses);
                return new StringType(HexUtils.bytesToHex(payload));

            case CHANNEL_PULSES:
                return new StringType(IntStream.range(0, pulses.length)
                        .mapToObj(s -> Integer.toString(Short.toUnsignedInt(pulses[s])))
                        .collect(Collectors.joining(" ")));

            default:
                throw new RFXComUnsupportedChannelException("Nothing relevant for " + channelId);
        }
    }

    @Override
    public void setSubType(SubType subType) {
        this.subType = subType;
    }

    @Override
    public void setDeviceId(String deviceId) {
        // Nothing to do here
    }

    @Override
    public void convertFromState(String channelId, Type type)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        switch (channelId) {
            case CHANNEL_RAW_MESSAGE:
            case CHANNEL_RAW_PAYLOAD:
            case CHANNEL_PULSES:
                throw new RFXComUnsupportedChannelException("Cannot send on channel " + channelId);

            case CHANNEL_COMMAND:
                if (type instanceof OnOffType) {
                    if (type == OnOffType.ON) {
                        this.pulses = config.onPulsesArray;
                    } else {
                        this.pulses = config.offPulsesArray;
                    }
                } else if (type instanceof OpenClosedType) {
                    if (type == OpenClosedType.OPEN) {
                        this.pulses = config.openPulsesArray;
                    } else {
                        this.pulses = config.closedPulsesArray;
                    }
                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }

                if (this.pulses == null) {
                    throw new RFXComInvalidStateException(channelId, null,
                            "No pulses provided in the device configuration for command" + type);
                }

                break;

            default:
                throw new RFXComUnsupportedChannelException("Channel " + channelId + " is not relevant here");
        }
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }
}
