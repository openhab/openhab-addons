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
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.LIGHTING2;
import static org.openhab.binding.rfxcom.internal.messages.RFXComLighting2Message.Commands.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for lighting2 message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComLighting2Message extends RFXComDeviceMessageImpl<RFXComLighting2Message.SubType> {

    public enum SubType implements ByteEnumWrapper {
        AC(0),
        HOME_EASY_EU(1),
        ANSLUT(2),
        KAMBROOK(3);

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
        SET_LEVEL(2),
        GROUP_OFF(3),
        GROUP_ON(4),
        SET_GROUP_LEVEL(5);

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
    public byte unitCode;
    public Commands command;
    public byte dimmingLevel;
    public boolean group;

    public RFXComLighting2Message() {
        super(PacketType.LIGHTING2);
    }

    public RFXComLighting2Message(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Command = " + command;
        str += ", Dim level = " + dimmingLevel;
        str += ", Signal level = " + signalLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 24 | (data[5] & 0xFF) << 16 | (data[6] & 0xFF) << 8 | (data[7] & 0xFF);
        command = fromByte(Commands.class, data[9]);

        if ((command == Commands.GROUP_ON) || (command == Commands.GROUP_OFF)) {
            unitCode = 0;
        } else {
            unitCode = data[8];
        }

        dimmingLevel = data[10];
        signalLevel = (byte) ((data[11] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {

        byte[] data = new byte[12];

        data[0] = 0x0B;
        data[1] = LIGHTING2.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId >> 24) & 0xFF);
        data[5] = (byte) ((sensorId >> 16) & 0xFF);
        data[6] = (byte) ((sensorId >> 8) & 0xFF);
        data[7] = (byte) (sensorId & 0xFF);

        data[8] = unitCode;
        data[9] = command.toByte();
        data[10] = dimmingLevel;
        data[11] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return sensorId + ID_DELIMITER + unitCode;
    }

    /**
     * Convert a 0-15 scale value to a percent type.
     *
     * @param pt
     *            percent type to convert
     * @return converted value 0-15
     */
    public static int getDimLevelFromPercentType(PercentType pt) {
        return pt.toBigDecimal().multiply(BigDecimal.valueOf(15))
                .divide(PercentType.HUNDRED.toBigDecimal(), 0, BigDecimal.ROUND_UP).intValue();
    }

    /**
     * Convert a 0-15 scale value to a percent type.
     *
     * @param value
     *            percent type to convert
     * @return converted value 0-15
     */
    public static PercentType getPercentTypeFromDimLevel(int value) {
        value = Math.min(value, 15);

        return new PercentType(BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(15), 0, BigDecimal.ROUND_UP).intValue());
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {

        switch (channelId) {
            case CHANNEL_DIMMING_LEVEL:
                return RFXComLighting2Message.getPercentTypeFromDimLevel(dimmingLevel);

            case CHANNEL_COMMAND:
                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                        return OnOffType.OFF;

                    case ON:
                    case GROUP_ON:
                        return OnOffType.ON;

                    case SET_GROUP_LEVEL:
                    case SET_LEVEL:
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

                    case SET_GROUP_LEVEL:
                    case SET_LEVEL:
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
        if (ids.length != 2) {
            throw new RFXComException("Invalid device id '" + deviceId + "'");
        }

        sensorId = Integer.parseInt(ids[0]);

        // Get unitcode, 0 means group
        unitCode = Byte.parseByte(ids[1]);
        if (unitCode == 0) {
            unitCode = 1;
            group = true;
        }
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {

        switch (channelId) {
            case CHANNEL_COMMAND:
                if (type instanceof OnOffType) {
                    if (group) {
                        command = (type == OnOffType.ON ? GROUP_ON : GROUP_OFF);

                    } else {
                        command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
                    }

                    dimmingLevel = 0;
                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            case CHANNEL_DIMMING_LEVEL:
                if (type instanceof OnOffType) {
                    command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
                    dimmingLevel = 0;

                } else if (type instanceof PercentType) {
                    command = Commands.SET_LEVEL;
                    dimmingLevel = (byte) getDimLevelFromPercentType((PercentType) type);

                    if (dimmingLevel == 0) {
                        command = Commands.OFF;
                    }
                } else if (type instanceof IncreaseDecreaseType) {
                    command = Commands.SET_LEVEL;
                    // Evert: I do not know how to get previous object state...
                    dimmingLevel = 5;

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
