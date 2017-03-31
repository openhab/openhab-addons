/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for lighting1 message. See X10, ARC, etc..
 *
 * @author Evert van Es, Cycling Engineer - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComLighting1Message extends RFXComDeviceMessageImpl<RFXComLighting1Message.SubType> {

    public enum SubType implements ByteEnumWrapper {
        X10(0),
        ARC(1),
        AB400D(2),
        WAVEMAN(3),
        EMW200(4),
        IMPULS(5),
        RISINGSUN(6),
        PHILIPS(7),
        ENERGENIE(8),
        ENERGENIE_5(9),
        COCO(10),
        HQ_COCO20(11);

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
        DIM(2),
        BRIGHT(3),
        GROUP_OFF(5),
        GROUP_ON(6),
        CHIME(7);

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
    public char houseCode;
    public byte unitCode;
    public Commands command;
    public boolean group;

    private static byte[] lastUnit = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    public RFXComLighting1Message() {
        super(PacketType.LIGHTING1);
    }

    public RFXComLighting1Message(byte[] data) throws RFXComException {
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

        subType = ByteEnumUtil.fromByte(SubType.class, super.subType);
        houseCode = (char) data[4];
        command = ByteEnumUtil.fromByte(Commands.class, data[6]);

        if ((command == Commands.GROUP_ON) || (command == Commands.GROUP_OFF)) {
            unitCode = 0;
        } else if ((data[5] == 0) && ((command == Commands.DIM) || (command == Commands.BRIGHT))) {
            // SS13 switches broadcast DIM/BRIGHT to X0 and the dimmers ignore
            // the message unless the last X<n> ON they saw was for them. So we
            // redirect an incoming broadcast DIM/BRIGHT to the correct item
            // based on the last X<n> we saw or sent.
            unitCode = lastUnit[(int)houseCode - (int)'A'];
        } else {
            unitCode = data[5];
            if (command == Commands.ON) {
                lastUnit[(int)houseCode - (int)'A'] = unitCode;
            }
        }

        signalLevel = (byte) ((data[7] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {
        // Example data 07 10 01 00 42 01 01 70
        // 07 10 01 00 42 10 06 70

        byte[] data = new byte[8];

        data[0] = 0x07;
        data[1] = PacketType.LIGHTING1.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) houseCode;
        // When an SS13 sends a DIM/BRIGHT it broadcasts to the all-units code (X0)
        // and it is up to the dimmers to ignore the message if the last X<n> they
        // saw wasn't for them. Sending to an actual X<n> may or may not work.
        // It is untested against _any_ dimmer never mind all so we stick to doing
        // the same as an SS13. At least for now. Using an explicit X<n> would be
        // better (if it works) because X10 RF and PLM are lossy and different
        // modules may have seen different traffic.
        if ((command == Commands.DIM) || (command == Commands.BRIGHT)) {
            data[5] = 0;
        } else {
            data[5] = unitCode;
        }
        data[6] = command.toByte();
        data[7] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return houseCode + ID_DELIMITER + unitCode;
    }

    @Override
    public Command convertToCommand(String channelId) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                        return OnOffType.OFF;

                    case ON:
                    case GROUP_ON:
                        return OnOffType.ON;

                    case DIM:
                        return IncreaseDecreaseType.DECREASE;

                    case BRIGHT:
                        return IncreaseDecreaseType.INCREASE;

                    case CHIME:
                        return OnOffType.ON;

                    default:
                        throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + command);
                }

            default:
                return super.convertToCommand(channelId);
        }
    }

    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                    case DIM:
                        return OnOffType.OFF;

                    case ON:
                    case GROUP_ON:
                    case BRIGHT:
                    case CHIME:
                        return OnOffType.ON;

                    default:
                        throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + command);
                }

            case CHANNEL_COMMAND_STRING:
                return command == null ? UnDefType.UNDEF : StringType.valueOf(command.toString());

            case CHANNEL_CONTACT:
                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                    case DIM:
                        return OpenClosedType.CLOSED;

                    case ON:
                    case GROUP_ON:
                    case BRIGHT:
                    case CHIME:
                        return OpenClosedType.OPEN;

                    default:
                        throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + command);
                }

            default:
                throw new RFXComUnsupportedChannelException("Channel " + channelId + " is not relevant here");
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

        houseCode = ids[0].charAt(0);

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
                        command = (type == OnOffType.ON ? Commands.GROUP_ON : Commands.GROUP_OFF);

                    } else {
                        command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
                    }
                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            case CHANNEL_COMMAND_STRING:
                command = Commands.valueOf(type.toString().toUpperCase());
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
