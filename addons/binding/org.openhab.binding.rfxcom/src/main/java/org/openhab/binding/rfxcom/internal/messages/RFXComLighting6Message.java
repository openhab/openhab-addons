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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for lighting6 message. See Blyss.
 *
 * @author Damien Servant - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComLighting6Message extends RFXComDeviceMessageImpl<RFXComLighting6Message.SubType> {

    public enum SubType implements ByteEnumWrapper {
        BLYSS(0);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    public enum Commands implements ByteEnumWrapper {
        ON(0),
        OFF(1),
        GROUP_ON(2),
        GROUP_OFF(3);

        private final int command;

        Commands(int command) {
            this.command = command;
        }

        @Override
        public byte toByte() {
            return (byte) command;
        }
    }

    public SubType subType;
    public int sensorId;
    public char groupCode;
    public byte unitCode;
    public Commands command;

    public RFXComLighting6Message() {
        super(PacketType.LIGHTING6);
    }

    public RFXComLighting6Message(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Command = " + command;
        str += ", Signal level = " + signalLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
        groupCode = (char) data[6];
        unitCode = data[7];
        command = fromByte(Commands.class, data[8]);

        signalLevel = (byte) ((data[11] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {
        // Example data 0B 15 00 02 01 01 41 01 00 04 8E 00
        // 0B 15 00 02 01 01 41 01 01 04 8E 00

        byte[] data = new byte[12];

        data[0] = 0x0B;
        data[1] = RFXComBaseMessage.PacketType.LIGHTING6.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId >> 8) & 0xFF);
        data[5] = (byte) (sensorId & 0xFF);
        data[6] = (byte) groupCode;
        data[7] = unitCode;
        data[8] = command.toByte();
        data[9] = 0x00; // CmdSeqNbr1 - 0 to 4 - Useless for a Blyss Switch
        data[10] = 0x00; // CmdSeqNbr2 - 0 to 145 - Useless for a Blyss Switch
        data[11] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return sensorId + ID_DELIMITER + groupCode + ID_DELIMITER + unitCode;
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {

        switch (channelId) {
            case CHANNEL_COMMAND:
                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                        return OnOffType.OFF;

                    case ON:
                    case GROUP_ON:
                        return OnOffType.ON;

                    default:
                        throw new RFXComUnsupportedChannelException("Can't convert " + command + " for " + channelId);
                }

            case CHANNEL_CONTACT:
                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                        return OpenClosedType.CLOSED;

                    case ON:
                    case GROUP_ON:
                        return OpenClosedType.OPEN;

                    default:
                        throw new RFXComUnsupportedChannelException("Can't convert " + command + " for " + channelId);
                }

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
        String[] ids = deviceId.split("\\" + ID_DELIMITER);
        if (ids.length != 3) {
            throw new RFXComException("Invalid device id '" + deviceId + "'");
        }

        sensorId = Integer.parseInt(ids[0]);
        groupCode = ids[1].charAt(0);
        unitCode = Byte.parseByte(ids[2]);
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {

        switch (channelId) {
            case CHANNEL_COMMAND:
                if (type instanceof OnOffType) {
                    command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);

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
