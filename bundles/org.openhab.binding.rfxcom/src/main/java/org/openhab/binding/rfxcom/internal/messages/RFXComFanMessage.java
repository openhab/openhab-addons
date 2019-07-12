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
        LIGHT(5, WESTINGHOUSE_7226640, CASAFAN, LUCCI_AIR_FAN);

        private final int command;
        private final List<SubType> supportedBySubTypes;

        Commands(int command, SubType... supportedSubType) {
            this.command = command;
            this.supportedBySubTypes = Arrays.asList(supportedSubType);
        }

        @Override
        public byte toByte() {
            return (byte) command;
        }

        @Override
        public List<SubType> supportedBySubTypes() {
            return supportedBySubTypes;
        }
    }

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
                return handleCommandChannel(channelId);

            default:
                return super.convertToState(channelId);
        }
    }

    private State handleFanSpeedChannel() {
        switch (command) {
            case HI:
            case MED:
            case LOW:
            case OFF:
                return StringType.valueOf(command.toString());

            default:
                return null;
        }
    }

    private State handleLightChannel() {
        if (command == Commands.LIGHT) {
            return OnOffType.ON;
        } else {
            return UnDefType.UNDEF;
        }
    }

    private State handleCommandChannel(String channelId) {
        switch (command) {
            case HI:
            case MED:
            case LOW:
                return OnOffType.ON;

            case OFF:
                return OnOffType.OFF;

            default:
                return null;
        }
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                if (type instanceof OnOffType) {
                    command = (type == OnOffType.ON ? Commands.MED : Commands.OFF);
                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            case CHANNEL_FAN_SPEED:
                handleFanSpeedCommand(channelId, type);
                break;

            case CHANNEL_FAN_LIGHT:
                if (type == OnOffType.ON) {
                    command = Commands.LIGHT;
                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            default:
                throw new RFXComUnsupportedChannelException("Channel " + channelId + " is not relevant here");
        }
    }

    private void handleFanSpeedCommand(String channelId, Type type) throws RFXComUnsupportedChannelException {
        if (type instanceof StringType) {
            String stringCommand = type.toString();
            switch (stringCommand) {
                case "HI":
                case "MED":
                case "LOW":
                case "OFF":
                    command = Commands.valueOf(stringCommand);
                    break;
                default:
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
            }
        }
    }
}