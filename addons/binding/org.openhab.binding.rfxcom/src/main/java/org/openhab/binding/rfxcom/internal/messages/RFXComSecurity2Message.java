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

import java.util.Arrays;

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for Security2 message.
 * (i.e. KEELOQ.)
 *
 * @author Mike Jagdis - Initial contribution
 */
public class RFXComSecurity2Message extends RFXComBatteryDeviceMessage<RFXComSecurity2Message.SubType> {

    public enum SubType implements ByteEnumWrapper {
        RAW_CLASSIC_KEELOQ(0),
        ROLLING_CODE_PACKET(1),
        RAW_AES_KEELOQ(2),
        RAW_CLASS_KEELOQ_WITH_REPEATS(3);

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
    public int buttonStatus;

    private final int BUTTON_0_BIT = 0x02;
    private final int BUTTON_1_BIT = 0x04;
    private final int BUTTON_2_BIT = 0x08;
    private final int BUTTON_3_BIT = 0x01;

    public RFXComSecurity2Message() {
        super(PacketType.SECURITY2);
    }

    public RFXComSecurity2Message(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        return super.toString() + ", Sub type = " + subType + ", Device Id = " + getDeviceId() + ", Button status = "
                + buttonStatus + ", Battery level = " + batteryLevel + ", Signal level = " + signalLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);

        sensorId = (data[11] & 0x0F) << 24 | (data[10] & 0xFF) << 16 | (data[9] & 0xFF) << 8 | (data[8] & 0xFF);

        buttonStatus = (data[11] & 0xF0) >> 4;

        batteryLevel = (byte) ((data[28] & 0xF0) >> 4);
        signalLevel = (byte) (data[28] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[29];

        Arrays.fill(data, (byte) 0);

        data[0] = 0x1C;
        data[1] = RFXComBaseMessage.PacketType.SECURITY2.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        data[8] = (byte) (sensorId & 0xFF);
        data[9] = (byte) ((sensorId >> 8) & 0xFF);
        data[10] = (byte) ((sensorId >> 16) & 0xFF);
        data[11] = (byte) ((buttonStatus & 0x0f) << 4 | (sensorId >> 24) & 0x0F);

        data[28] = (byte) (((batteryLevel & 0x0F) << 4) | (signalLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_CONTACT:
                return ((buttonStatus & BUTTON_0_BIT) == 0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

            case CHANNEL_CONTACT_1:
                return ((buttonStatus & BUTTON_1_BIT) == 0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

            case CHANNEL_CONTACT_2:
                return ((buttonStatus & BUTTON_2_BIT) == 0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

            case CHANNEL_CONTACT_3:
                return ((buttonStatus & BUTTON_3_BIT) == 0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

            default:
                return super.convertToState(channelId);
        }
    }

    @Override
    public void setSubType(SubType subType) {
        this.subType = subType;
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        sensorId = Integer.parseInt(deviceId);
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_CONTACT:
                if (type instanceof OpenClosedType) {
                    if (type == OpenClosedType.CLOSED) {
                        buttonStatus |= BUTTON_0_BIT;
                    } else {
                        buttonStatus &= ~BUTTON_0_BIT;
                    }

                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            case CHANNEL_CONTACT_1:
                if (type instanceof OpenClosedType) {
                    if (type == OpenClosedType.CLOSED) {
                        buttonStatus |= BUTTON_1_BIT;
                    } else {
                        buttonStatus &= ~BUTTON_1_BIT;
                    }

                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            case CHANNEL_CONTACT_2:
                if (type instanceof OpenClosedType) {
                    if (type == OpenClosedType.CLOSED) {
                        buttonStatus |= BUTTON_2_BIT;
                    } else {
                        buttonStatus &= ~BUTTON_2_BIT;
                    }

                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            case CHANNEL_CONTACT_3:
                if (type instanceof OpenClosedType) {
                    if (type == OpenClosedType.CLOSED) {
                        buttonStatus |= BUTTON_3_BIT;
                    } else {
                        buttonStatus &= ~BUTTON_3_BIT;
                    }

                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
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
