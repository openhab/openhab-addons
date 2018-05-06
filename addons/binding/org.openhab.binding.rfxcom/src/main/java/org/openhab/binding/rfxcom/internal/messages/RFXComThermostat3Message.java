/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.RFXComBindingConstants.CHANNEL_COMMAND;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;
import static org.openhab.binding.rfxcom.internal.messages.RFXComThermostat3Message.SubType.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for thermostat3message.
 *
 * Mertik G6R-XXX Thermostat RF sensor operational
 *
 * @author Sander Biesenbeek - Initial contribution
 * @author Ruud Beukema - Initial contribution (parallel development)
 * @author Martin van Wingerden - Joined contribution of Sander & Ruud
 */
public class RFXComThermostat3Message extends RFXComDeviceMessageImpl<RFXComThermostat3Message.SubType> {
    public enum SubType implements ByteEnumWrapper {
        MERTIK__G6R_H4T1(0),
        MERTIK__G6R_H4TB__G6R_H4T__G6R_H4T21_Z22(1),
        MERTIK__G6R_H4TD__G6R_H4T16(2),
        MERTIK__G6R_H4S_TRANSMIT_ONLY(3);

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
        OFF(0),
        ON(1),
        UP(2),
        DOWN(3),
        RUN_UP(4, MERTIK__G6R_H4T1),
        SECOND_OFF(4, MERTIK__G6R_H4TB__G6R_H4T__G6R_H4T21_Z22),
        RUN_DOWN(5, MERTIK__G6R_H4T1),
        SECOND_ON(5, MERTIK__G6R_H4TB__G6R_H4T__G6R_H4T21_Z22),
        STOP(6, MERTIK__G6R_H4T1);

        private final int command;
        private final List<SubType> supportedBySubTypes;

        Commands(int command) {
            this(command, SubType.values());
        }

        Commands(int command, SubType... supportedBySubTypes) {
            this.command = command;
            this.supportedBySubTypes = Arrays.asList(supportedBySubTypes);
        }

        @Override
        public byte toByte() {
            return (byte) command;
        }

        public static Commands fromByte(int input, SubType subType) throws RFXComUnsupportedValueException {
            for (Commands c : Commands.values()) {
                if (c.command == input && c.supportedBySubTypes.contains(subType)) {
                    return c;
                }
            }

            throw new RFXComUnsupportedValueException(Commands.class, input);
        }
    }

    public SubType subType;
    private int unitId;
    public Commands command;
    private byte commandId;

    public RFXComThermostat3Message() {
        super(PacketType.THERMOSTAT3);
    }

    public RFXComThermostat3Message(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Command = " + command + "(" + commandId + ")";
        str += ", Signal level = " + signalLevel;

        return str;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(unitId);
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        unitId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
        commandId = data[7];
        command = Commands.fromByte(commandId, subType);
        signalLevel = (byte) ((data[8] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[9];

        data[0] = 0x08;
        data[1] = RFXComBaseMessage.PacketType.THERMOSTAT3.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((unitId >> 16) & 0xFF);
        data[5] = (byte) ((unitId >> 8) & 0xFF);
        data[6] = (byte) (unitId & 0xFF);
        data[7] = command.toByte();
        data[8] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                switch (command) {
                    case OFF:
                    case SECOND_OFF:
                        return OnOffType.OFF;
                    case ON:
                    case SECOND_ON:
                        return OnOffType.ON;
                    case UP:
                        return UpDownType.UP;
                    case DOWN:
                        return UpDownType.DOWN;
                    default:
                        throw new RFXComUnsupportedChannelException("Can't convert " + command + " for " + channelId);
                }
            default:
                return super.convertToState(channelId);
        }
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                if (type instanceof OnOffType) {
                    command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
                } else if (type instanceof UpDownType) {
                    command = (type == UpDownType.UP ? Commands.UP : Commands.DOWN);
                } else if (type instanceof OpenClosedType) {
                    command = (type == OpenClosedType.CLOSED ? Commands.ON : Commands.OFF);
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

    @Override
    public void setSubType(SubType subType) {
        this.subType = subType;
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        this.unitId = Integer.parseInt(deviceId);
    }
}