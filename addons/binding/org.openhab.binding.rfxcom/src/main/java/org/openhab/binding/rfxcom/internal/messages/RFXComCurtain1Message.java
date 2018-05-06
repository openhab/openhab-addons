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

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for curtain1 message. See Harrison.
 *
 * @author Evert van Es - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComCurtain1Message extends RFXComBatteryDeviceMessage<RFXComCurtain1Message.SubType> {

    public enum SubType implements ByteEnumWrapper {
        HARRISON(0);

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
        OPEN(0),
        CLOSE(1),
        STOP(2),
        PROGRAM(3);

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
    public char sensorId;
    public byte unitCode;
    public Commands command;

    public RFXComCurtain1Message() {
        super(PacketType.CURTAIN1);
    }

    public RFXComCurtain1Message(byte[] data) throws RFXComException {
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
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (char) data[4];
        unitCode = data[5];
        command = fromByte(Commands.class, data[6]);

        signalLevel = (byte) ((data[7] & 0xF0) >> 4);
        batteryLevel = (byte) ((data[7] & 0x0F));
    }

    @Override
    public byte[] decodeMessage() {
        // Example data 07 18 00 00 65 01 00 00
        // 07 18 00 00 65 02 00 00

        byte[] data = new byte[8];

        data[0] = 0x07;
        data[1] = 0x18;
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) sensorId;
        data[5] = unitCode;
        data[6] = command.toByte();
        data[7] = (byte) (((signalLevel & 0x0F) << 4) + batteryLevel);

        return data;
    }

    @Override
    public String getDeviceId() {
        return sensorId + ID_DELIMITER + unitCode;
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        if (channelId.equals(CHANNEL_COMMAND)) {
            return (command == Commands.CLOSE ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
        } else {
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
        if (ids.length != 2) {
            throw new RFXComException("Invalid device id '" + deviceId + "'");
        }

        sensorId = ids[0].charAt(0);
        unitCode = Byte.parseByte(ids[1]);
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        if (channelId.equals(CHANNEL_SHUTTER)) {
            if (type instanceof OpenClosedType) {
                command = (type == OpenClosedType.CLOSED ? Commands.CLOSE : Commands.OPEN);
            } else if (type instanceof UpDownType) {
                command = (type == UpDownType.UP ? Commands.CLOSE : Commands.OPEN);
            } else if (type instanceof StopMoveType) {
                command = Commands.STOP;
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
