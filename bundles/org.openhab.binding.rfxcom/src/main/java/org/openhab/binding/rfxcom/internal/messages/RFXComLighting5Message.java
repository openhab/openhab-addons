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
import static org.openhab.binding.rfxcom.internal.messages.RFXComLighting5Message.SubType.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

/**
 * RFXCOM data class for lighting5 message.
 *
 * @author Paul Hampson, Neil Renaud - Initial contribution
 * @author Pauli Anttila - Migrated to OH2
 * @author Martin van Wingerden - added support for IT and some other subtypes
 */
public class RFXComLighting5Message extends RFXComDeviceMessageImpl<RFXComLighting5Message.SubType> {

    public enum SubType implements ByteEnumWrapper {
        LIGHTWAVERF(0),
        EMW100(1),
        BBSB_NEW(2),
        MDREMOTE(3),
        CONRAD_RSL2(4),
        LIVOLO(5),
        RGB_TRC02(6),
        AOKE(7),
        RGB_TRC02_2(8),
        EURODOMEST(9),
        LIVOLO_APPLIANCE(10),
        MDREMOTE_107(12),
        AVANTEK(14),
        IT(15),
        MDREMOTE_108(16),
        KANGTAI(17);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    /**
     * Note: for the lighting5 commands, some command are only supported for certain sub types and
     * command-bytes might even have a different meaning for another sub type.
     *
     * If no sub types are specified for a command, its supported by all sub types.
     * An example is the command OFF which is represented by the byte 0x00 for all subtypes.
     *
     * Otherwise the list of sub types after the command-bytes indicates the sub types
     * which support this command with this byte.
     * Example byte value 0x03 means GROUP_ON for IT and some others while it means MOOD1 for LIGHTWAVERF
     */
    public enum Commands implements ByteEnumWrapperWithSupportedSubTypes<SubType> {
        OFF(0x00),
        ON(0x01),
        GROUP_OFF(0x02, LIGHTWAVERF, BBSB_NEW, CONRAD_RSL2, EURODOMEST, AVANTEK, IT, KANGTAI),
        LEARN(0x02, EMW100),
        GROUP_ON(0x03, BBSB_NEW, CONRAD_RSL2, EURODOMEST, AVANTEK, IT, KANGTAI),
        MOOD1(0x03, LIGHTWAVERF),
        MOOD2(0x04, LIGHTWAVERF),
        MOOD3(0x05, LIGHTWAVERF),
        MOOD4(0x06, LIGHTWAVERF),
        MOOD5(0x07, LIGHTWAVERF),
        RESERVED1(0x08, LIGHTWAVERF),
        RESERVED2(0x09, LIGHTWAVERF),
        UNLOCK(0x0A, LIGHTWAVERF),
        LOCK(0x0B, LIGHTWAVERF),
        ALL_LOCK(0x0C, LIGHTWAVERF),
        CLOSE_RELAY(0x0D, LIGHTWAVERF),
        STOP_RELAY(0x0E, LIGHTWAVERF),
        OPEN_RELAY(0x0F, LIGHTWAVERF),
        SET_LEVEL(0x10, LIGHTWAVERF, IT),
        COLOUR_PALETTE(0x11, LIGHTWAVERF),
        COLOUR_TONE(0x12, LIGHTWAVERF),
        COLOUR_CYCLE(0x13, LIGHTWAVERF),
        TOGGLE_1(0x01, LIVOLO_APPLIANCE),
        TOGGLE_2(0x02, LIVOLO_APPLIANCE),
        TOGGLE_3(0x03, LIVOLO_APPLIANCE),
        TOGGLE_4(0x04, LIVOLO_APPLIANCE),
        TOGGLE_5(0x07, LIVOLO_APPLIANCE),
        TOGGLE_6(0x0B, LIVOLO_APPLIANCE),
        TOGGLE_7(0x06, LIVOLO_APPLIANCE),
        TOGGLE_8(0x0A, LIVOLO_APPLIANCE),
        TOGGLE_9(0x05, LIVOLO_APPLIANCE);

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
        public List<SubType> supportedBySubTypes() {
            return supportedBySubTypes;
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

    public RFXComLighting5Message() {
        super(PacketType.LIGHTING5);
    }

    public RFXComLighting5Message(byte[] data) throws RFXComException {
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

        sensorId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
        unitCode = data[7];

        command = fromByte(Commands.class, data[8], subType);

        dimmingLevel = data[9];
        signalLevel = (byte) ((data[10] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[11];

        data[0] = 0x0A;
        data[1] = RFXComBaseMessage.PacketType.LIGHTING5.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId >> 16) & 0xFF);
        data[5] = (byte) ((sensorId >> 8) & 0xFF);
        data[6] = (byte) (sensorId & 0xFF);

        data[7] = unitCode;
        data[8] = command.toByte();
        data[9] = dimmingLevel;
        data[10] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return sensorId + ID_DELIMITER + unitCode;
    }

    /**
     * Convert a 0-31 scale value to a percent type.
     *
     * @param pt percent type to convert
     * @return converted value 0-31
     */
    public static int getDimLevelFromPercentType(PercentType pt) {
        return pt.toBigDecimal().multiply(BigDecimal.valueOf(31))
                .divide(PercentType.HUNDRED.toBigDecimal(), 0, RoundingMode.UP).intValue();
    }

    /**
     * Convert a 0-31 scale value to a percent type.
     *
     * @param value percent type to convert
     * @return converted value 0-31
     */
    public static PercentType getPercentTypeFromDimLevel(int value) {
        value = Math.min(value, 31);

        return new PercentType(BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(31), 0, RoundingMode.UP).intValue());
    }

    @Override
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        switch (channelId) {
            case CHANNEL_MOOD:
                switch (command) {
                    case GROUP_OFF:
                        return new DecimalType(0);
                    case MOOD1:
                        return new DecimalType(1);
                    case MOOD2:
                        return new DecimalType(2);
                    case MOOD3:
                        return new DecimalType(3);
                    case MOOD4:
                        return new DecimalType(4);
                    case MOOD5:
                        return new DecimalType(5);
                    default:
                        throw new RFXComUnsupportedChannelException(
                                "Unexpected mood command: " + command + " for " + channelId);
                }

            case CHANNEL_DIMMING_LEVEL:
                return RFXComLighting5Message.getPercentTypeFromDimLevel(dimmingLevel);

            case CHANNEL_COMMAND:
                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                        return OnOffType.OFF;

                    case ON:
                    case GROUP_ON:
                        return OnOffType.ON;

                    case SET_LEVEL:
                    default:
                        throw new RFXComUnsupportedChannelException("Can't convert " + command + " for " + channelId);
                }

            case CHANNEL_COMMAND_STRING:
                return command == null ? UnDefType.UNDEF : StringType.valueOf(command.toString());

            case CHANNEL_CONTACT:
                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                        return OpenClosedType.CLOSED;

                    case ON:
                    case GROUP_ON:
                        return OpenClosedType.OPEN;

                    case SET_LEVEL:
                    default:
                        throw new RFXComUnsupportedChannelException("Can't convert " + command + " for " + channelId);
                }

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

        sensorId = Integer.parseInt(ids[0]);
        unitCode = Byte.parseByte(ids[1]);
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                if (type instanceof OnOffType) {
                    command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
                    dimmingLevel = 0;

                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            case CHANNEL_COMMAND_STRING:
                command = Commands.valueOf(type.toString().toUpperCase());
                break;

            case CHANNEL_DIMMING_LEVEL:
                if (type instanceof OnOffType) {
                    command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
                    dimmingLevel = 0;

                } else if (type instanceof PercentType percentCommand) {
                    command = Commands.SET_LEVEL;
                    dimmingLevel = (byte) getDimLevelFromPercentType(percentCommand);

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
