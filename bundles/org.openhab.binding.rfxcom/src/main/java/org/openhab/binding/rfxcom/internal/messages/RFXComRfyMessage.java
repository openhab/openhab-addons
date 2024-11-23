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
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

/**
 * RFXCOM data class for RFY (Somfy RTS) message.
 *
 * @author Jürgen Richtsfeld - Initial contribution
 * @author Pauli Anttila - Ported from OpenHAB1
 * @author Mike Jagdis - Added venetian support and sun+wind detector
 */
public class RFXComRfyMessage extends RFXComDeviceMessageImpl<RFXComRfyMessage.SubType> {

    public enum Commands implements ByteEnumWrapper {
        STOP(0x00),
        UP(0x01),
        DOWN(0x03),
        PROGRAM(0x07),
        UP_SHORT(0x0F),
        DOWN_SHORT(0x10),
        UP_LONG(0x11),
        DOWN_LONG(0x12),
        ENABLE_SUN_WIND_DETECTOR(0x13),
        DISABLE_SUN_DETECTOR(0x14);

        private final int command;

        Commands(int command) {
            this.command = command;
        }

        @Override
        public byte toByte() {
            return (byte) command;
        }
    }

    public enum SubType implements ByteEnumWrapper {
        RFY(0),
        RFY_EXT(1),
        RESERVED(2),
        ASA(3);

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
    public int unitId;
    public byte unitCode;
    public Commands command;

    public RFXComRfyMessage() {
        super(PacketType.RFY);
    }

    public RFXComRfyMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        return super.toString() + ", Sub type = " + subType + ", Unit Id = " + getDeviceId() + ", Unit Code = "
                + unitCode + ", Command = " + command + ", Signal level = " + signalLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);

        unitId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
        unitCode = data[7];

        command = fromByte(Commands.class, data[8]);
        signalLevel = (byte) ((data[12] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {
        final byte[] data = new byte[13];

        data[0] = 12;
        data[1] = RFXComBaseMessage.PacketType.RFY.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((unitId >> 16) & 0xFF);
        data[5] = (byte) ((unitId >> 8) & 0xFF);
        data[6] = (byte) (unitId & 0xFF);
        data[7] = unitCode;
        data[8] = command.toByte();
        data[12] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return unitId + ID_DELIMITER + unitCode;
    }

    @Override
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                return (command == Commands.DOWN ? OpenClosedType.CLOSED : OpenClosedType.OPEN);

            default:
                return super.convertToState(channelId, config, deviceState);
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

        this.unitId = Integer.parseInt(ids[0]);
        this.unitCode = Byte.parseByte(ids[1]);
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_SHUTTER:
                if (type instanceof OpenClosedType) {
                    this.command = (type == OpenClosedType.CLOSED ? Commands.DOWN : Commands.UP);

                } else if (type instanceof UpDownType) {
                    this.command = (type == UpDownType.DOWN ? Commands.DOWN : Commands.UP);

                } else if (type instanceof StopMoveType) {
                    this.command = Commands.STOP;

                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            case CHANNEL_PROGRAM:
                if (type == OnOffType.ON) {
                    this.command = Commands.PROGRAM;
                } else {
                    throw new RFXComUnsupportedChannelException("Can't convert " + type + " to Command");
                }
                break;

            case CHANNEL_SUN_WIND_DETECTOR:
                if (type instanceof OnOffType) {
                    this.command = (type == OnOffType.ON ? Commands.ENABLE_SUN_WIND_DETECTOR
                            : Commands.DISABLE_SUN_DETECTOR);
                } else {
                    throw new RFXComUnsupportedChannelException("Can't convert " + type + " to Command");
                }
                break;

            case CHANNEL_VENETIAN_BLIND:
                if (type instanceof OpenClosedType) {
                    this.command = (type == OpenClosedType.CLOSED ? Commands.DOWN_SHORT : Commands.UP_SHORT);

                } else if (type instanceof OnOffType) {
                    this.command = (type == OnOffType.ON ? Commands.DOWN_SHORT : Commands.UP_SHORT);

                } else if (type instanceof IncreaseDecreaseType) {
                    this.command = (type == IncreaseDecreaseType.INCREASE ? Commands.DOWN_LONG : Commands.UP_LONG);

                } else {
                    throw new RFXComUnsupportedChannelException("Can't convert " + type + " to Command");
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
