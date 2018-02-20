/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.RFXComBindingConstants.CHANNEL_CHIME_SOUND;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for chime messages.
 *
 * @author Mike Jagdis
 */
public class RFXComChimeMessage extends RFXComDeviceMessageImpl<RFXComChimeMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        BYRONSX(0),
        BYRONMP001(1),
        SELECTPLUS(2),
        SELECTPLUS3(3),
        ENVIVO(4);

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
    public int chimeSound;

    public RFXComChimeMessage() {
        super(PacketType.CHIME);
    }

    public RFXComChimeMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Chime Sound = " + chimeSound;
        str += ", Signal level = " + signalLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);

        switch (subType) {
            case BYRONSX:
                sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
                chimeSound = data[6];
                break;
            case BYRONMP001:
            case SELECTPLUS:
            case SELECTPLUS3:
            case ENVIVO:
                sensorId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
                chimeSound = 1;
                break;
        }

        signalLevel = (byte) ((data[7] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[8];

        data[0] = 0x07;
        data[1] = getPacketType().toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        switch (subType) {
            case BYRONSX:
                data[4] = (byte) ((sensorId & 0xFF00) >> 8);
                data[5] = (byte) (sensorId & 0x00FF);
                data[6] = (byte) chimeSound;
                break;
            case BYRONMP001:
            case SELECTPLUS:
            case SELECTPLUS3:
            case ENVIVO:
                data[4] = (byte) ((sensorId & 0xFF0000) >> 16);
                data[5] = (byte) ((sensorId & 0x00FF00) >> 8);
                data[6] = (byte) ((sensorId & 0x0000FF));
                break;
        }

        data[7] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        if (CHANNEL_CHIME_SOUND.equals(channelId)) {
            return new DecimalType(chimeSound);
        } else {
            return super.convertToState(channelId);
        }
    }

    @Override
    public void setSubType(SubType subType) {
        this.subType = subType;
    }

    @Override
    public void setDeviceId(String sensorId) throws RFXComException {
        this.sensorId = Integer.parseInt(sensorId);
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        if (CHANNEL_CHIME_SOUND.equals(channelId)) {
            if (type instanceof DecimalType) {
                chimeSound = ((DecimalType) type).intValue();
            } else {
                throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
            }
        } else {
            throw new RFXComUnsupportedChannelException("Channel " + channelId + " is not relevant here");
        }
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }
}
