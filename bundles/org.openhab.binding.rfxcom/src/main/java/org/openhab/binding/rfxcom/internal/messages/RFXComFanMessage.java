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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

import java.util.Arrays;
import java.util.List;

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;
import static org.openhab.binding.rfxcom.internal.messages.RFXComFanMessage.SubType.*;

/**
 * RFXCOM data class for fan message.
 *
 * @author Martin van Wingerden - initial contribution
 */
public class RFXComFanMessage extends RFXComDeviceMessageImpl<RFXComFanMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        SF01(0),
        CVE_RFT(1),
        LUCCI_AIR_FAN(2),
        SEAV_TXS4(3),
        WESTINGHOUSE_7226640(4),
        LUCCI_AIR_DC(5),
        CASAFAN(6),
        FT1211R(7),
        FALMEC(8),
        LUCCI_AIR_DCII(9);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    public enum Commands implements ByteEnumWrapperWithSupportedSubTypes<SubType> {
        HI(1, WESTINGHOUSE_7226640, CASAFAN, LUCCI_AIR_FAN),
        MED(2, WESTINGHOUSE_7226640, CASAFAN, LUCCI_AIR_FAN),
        LOW(3, WESTINGHOUSE_7226640, CASAFAN, LUCCI_AIR_FAN),
        OFF(4, WESTINGHOUSE_7226640, CASAFAN, LUCCI_AIR_FAN),
        LIGHT(5, WESTINGHOUSE_7226640, CASAFAN, LUCCI_AIR_FAN),

        FALMEC_POWER_OFF(1, 0, FALMEC),
        FALMEC_SPEED_1(2, 1, FALMEC),
        FALMEC_SPEED_2(3, 2, FALMEC),
        FALMEC_SPEED_3(4, 3, FALMEC),
        FALMEC_SPEED_4(5, 4, FALMEC),
        FALMEC_TIMER_1(6, FALMEC),
        FALMEC_TIMER_2(7, FALMEC),
        FALMEC_TIMER_3(8, FALMEC),
        FALMEC_TIMER_4(9, FALMEC),
        FALMEC_LIGHT_ON(10, FALMEC),
        FALMEC_LIGHT_OFF(11, FALMEC),

        FT1211R_POWER(1, 0, FT1211R),
        FT1211R_LIGHT(2, FT1211R),
        FT1211R_SPEED_1(3, 1, FT1211R),
        FT1211R_SPEED_2(4, 2, FT1211R),
        FT1211R_SPEED_3(5, 3, FT1211R),
        FT1211R_SPEED_4(6, 4, FT1211R),
        FT1211R_SPEED_5(7, 5, FT1211R),
        FT1211R_FORWARD_REVERSE(8, FT1211R),
        FT1211R_TIMER_1H(9, FT1211R),
        FT1211R_TIMER_4H(10, FT1211R),
        FT1211R_TIMER_8H(11, FT1211R);

        private final int command;
        private final Integer speed;
        private final List<SubType> supportedBySubTypes;

        Commands(int command, SubType... supportedSubType) {
            this(command, null, supportedSubType);
        }

        Commands(int command, Integer speed, SubType... supportedSubType) {
            this.command = command;
            this.speed = speed;
            this.supportedBySubTypes = Arrays.asList(supportedSubType);
        }

        @Nullable
        public static Commands bySpeed(SubType subType, int speed) {
            for (Commands value : values()) {
                if (value.supportedBySubTypes.contains(subType) && value.speed == speed) {
                    return value;
                }
            }
            return null;
        }

        @Override
        public byte toByte() {
            return (byte) command;
        }

        public Integer getSpeed() {
            return speed;
        }

        @Override
        public List<SubType> supportedBySubTypes() {
            return supportedBySubTypes;
        }
    }

    private static final List<SubType> GENERIC_SUB_TYPES = Arrays.asList(WESTINGHOUSE_7226640, CASAFAN, LUCCI_AIR_FAN);

    private static final List<Commands> LIGHT_ON_COMMANDS = Arrays.asList(Commands.LIGHT, Commands.FALMEC_LIGHT_ON);
    private static final List<Commands> ON_COMMANDS = Arrays.asList(Commands.HI, Commands.MED, Commands.LOW,
            Commands.FALMEC_SPEED_1, Commands.FALMEC_SPEED_2, Commands.FALMEC_SPEED_3, Commands.FALMEC_SPEED_4);
    private static final List<Commands> OFF_COMMANDS = Arrays.asList(Commands.OFF, Commands.FALMEC_POWER_OFF);

    private SubType subType;
    private int sensorId;
    private Commands command;


    public RFXComFanMessage() {
        super(PacketType.FAN);
    }

    public RFXComFanMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public PacketType getPacketType() {
        switch (subType) {
            case LUCCI_AIR_FAN:
            case CASAFAN:
            case WESTINGHOUSE_7226640:
                return PacketType.FAN;
            case SF01:
                return PacketType.FAN_SF01;
            case CVE_RFT:
                return PacketType.FAN_ITHO;
            case SEAV_TXS4:
                return PacketType.FAN_SEAV;
            case LUCCI_AIR_DC:
                return PacketType.FAN_LUCCI_DC;
            case FT1211R:
                return PacketType.FAN_FT1211R;
            case FALMEC:
                return PacketType.FAN_FALMEC;
            case LUCCI_AIR_DCII:
                return PacketType.FAN_LUCCI_DCII;
        }
        return super.getPacketType();
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
        command = fromByte(Commands.class, data[7], subType);

        signalLevel = (byte) (data[8] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[9];

        data[0] = 0x08;
        data[1] = PacketType.FAN.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        data[4] = (byte) ((sensorId >> 16) & 0xFF);
        data[5] = (byte) ((sensorId >> 8) & 0xFF);
        data[6] = (byte) (sensorId & 0xFF);

        data[7] = command.toByte();

        data[8] = (byte) (signalLevel & 0x0F);

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
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
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_FAN_LIGHT:
                return handleLightChannel();

            case CHANNEL_FAN_SPEED:
                return handleFanSpeedChannel();

            case CHANNEL_COMMAND:
                return handleCommandChannel();

            default:
                return super.convertToState(channelId);
        }
    }

    private State handleLightChannel() {
        if (LIGHT_ON_COMMANDS.contains(command)) {
            return OnOffType.ON;
        } else if (command == Commands.FALMEC_LIGHT_OFF) {
            return OnOffType.OFF;
        } else {
            return UnDefType.UNDEF;
        }
    }

    private State handleFanSpeedChannel() {
        switch (command) {
            case HI:
            case MED:
            case LOW:
            case OFF:
                return StringType.valueOf(command.toString());

            case FALMEC_POWER_OFF:
            case FALMEC_SPEED_1:
            case FALMEC_SPEED_2:
            case FALMEC_SPEED_3:
            case FALMEC_SPEED_4:
            case FT1211R_POWER:
            case FT1211R_SPEED_1:
            case FT1211R_SPEED_2:
            case FT1211R_SPEED_3:
            case FT1211R_SPEED_4:
            case FT1211R_SPEED_5:
                return new DecimalType(command.getSpeed());

            default:
                return null;
        }
    }

    private State handleCommandChannel() {
        if (ON_COMMANDS.contains(command)) {
            return OnOffType.ON;
        } else if (OFF_COMMANDS.contains(command)) {
            return OnOffType.OFF;
        } else {
            return null;
        }
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                command = handleCommand(channelId, type);
                break;

            case CHANNEL_FAN_SPEED:
                command = handleFanSpeedCommand(channelId, type);
                break;

            case CHANNEL_FAN_LIGHT:
                command = handleFanLightCommand(channelId, type);
                break;

            default:
                throw new RFXComUnsupportedChannelException("Channel " + channelId + " is not relevant here");
        }
    }

    private Commands handleCommand(String channelId, Type type) throws RFXComUnsupportedChannelException {
        if (type instanceof OnOffType) {
            if (GENERIC_SUB_TYPES.contains(subType)) {
                return (type == OnOffType.ON ? Commands.MED : Commands.OFF);
            } else if (subType == FALMEC) {
                return (type == OnOffType.ON ? Commands.FALMEC_SPEED_2 : Commands.FALMEC_POWER_OFF);
            }
        }
        throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
    }

    private Commands handleFanSpeedCommand(String channelId, Type type) throws RFXComUnsupportedChannelException {
        if (type instanceof StringType) {
            String stringCommand = type.toString();
            switch (stringCommand) {
                case "HI":
                case "MED":
                case "LOW":
                case "OFF":
                    return Commands.valueOf(stringCommand);
            }
        } else if (type instanceof DecimalType) {
            Commands speedCommand = Commands.bySpeed(subType, ((DecimalType) type).intValue());
            if (speedCommand != null) {
                return speedCommand;
            }
        }
        throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
    }

    private Commands handleFanLightCommand(String channelId, Type type) throws RFXComUnsupportedChannelException {
        if (type == OnOffType.ON) {
            switch (subType) {
                case LUCCI_AIR_FAN:
                case CASAFAN:
                case WESTINGHOUSE_7226640:
                    return Commands.LIGHT;

                case FALMEC:
                    return Commands.FALMEC_LIGHT_ON;
            }
        } else if (type == OnOffType.OFF && subType == FALMEC) {
            return Commands.FALMEC_LIGHT_OFF;
        }
        throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
    }
}