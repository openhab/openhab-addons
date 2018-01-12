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
import static org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration.*;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFXCOM data class for lighting4 message.
 *
 * a Lighting4 Base command is composed of 24 bit DATA plus PULSE information
 *
 * DATA:
 * Code = 014554
 * S1- S24 = <0000 0001 0100 0101 0101> <0100>
 * first 20 are DeviceID last 4 are for Command
 *
 * PULSE:
 * default 350
 *
 * Tested on a PT2262 remote PlugIn module
 *
 * Example:
 *
 * Switch TESTout "TestOut" (All) {rfxcom=">83205.350:LIGHTING4.PT2262:Command"}
 * (SendCommand DeviceID(int).Pulse(int):LIGHTING4.Subtype:Command )
 *
 * Switch TESTin "TestIn" (All) {rfxcom="<83205:Command"}
 * (ReceiveCommand ON/OFF Command )
 *
 * @author Alessandro Ballini (ITA) - Initial contribution
 * @author Pauli Anttila
 * @author Martin van Wingerden - Extended support for more complex PT2262 devices
 */
public class RFXComLighting4Message extends RFXComDeviceMessageImpl<RFXComLighting4Message.SubType> {
    // this logger is used from a static context, so is static as well
    private static final Logger LOGGER = LoggerFactory.getLogger(RFXComLighting4Message.class);

    private static final byte DEFAULT_OFF_COMMAND_ID = Commands.OFF_4.toByte();
    private static final byte DEFAULT_ON_COMMAND_ID = Commands.ON_1.toByte();

    public enum SubType implements ByteEnumWrapper {
        PT2262(0);

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
        OFF_0(0, false),
        ON_1(1, true),
        OFF_2(2, false),
        ON_3(3, true),
        OFF_4(4, false),
        ON_5(5, true),
        ON_7(7, true),
        ON_9(9, true),
        ON_12(12, true),
        UNKNOWN(-1, false);

        private final int command;
        private final boolean on;

        Commands(int command, boolean on) {
            this.command = command;
            this.on = on;
        }

        @Override
        public byte toByte() {
            return (byte) command;
        }

        public boolean isOn() {
            return on;
        }

        public static Commands fromByte(int input) {
            for (Commands c : Commands.values()) {
                if (c.command == input) {
                    return c;
                }
            }
            LOGGER.info(
                    "A not completely supported command with value {} was received, we can send it but please report "
                            + "it as an issue including what the command means, this helps to extend the binding with better support.",
                    input);
            return UNKNOWN;
        }
    }

    private SubType subType;
    private int sensorId;
    private int pulse;
    private Commands command;
    private int commandId;
    private int offCommandId;
    private int onCommandId;

    public RFXComLighting4Message() {
        super(PacketType.LIGHTING4);
    }

    public RFXComLighting4Message(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Command = " + command + "(" + commandId + ")";
        str += ", Pulse = " + pulse;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 12 | (data[5] & 0xFF) << 4 | (data[6] & 0xF0) >> 4;

        commandId = (data[6] & 0x0F);
        command = Commands.fromByte(commandId);
        onCommandId = command.isOn() ? commandId : DEFAULT_ON_COMMAND_ID;
        offCommandId = command.isOn() ? DEFAULT_OFF_COMMAND_ID : commandId;

        pulse = (data[7] & 0xFF) << 8 | (data[8] & 0xFF);

        signalLevel = (byte) ((data[9] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {

        byte[] data = new byte[10];

        data[0] = 0x09;
        data[1] = PacketType.LIGHTING4.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        // SENSOR_ID + COMMAND
        data[4] = (byte) ((sensorId >> 12) & 0xFF);
        data[5] = (byte) ((sensorId >> 4) & 0xFF);
        data[6] = (byte) ((sensorId << 4 & 0xF0) | (commandId & 0x0F));

        // PULSE
        data[7] = (byte) (pulse >> 8 & 0xFF);
        data[8] = (byte) (pulse & 0xFF);

        // SIGNAL
        data[9] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {

        switch (channelId) {
            case CHANNEL_COMMAND:
            case CHANNEL_MOTION:
                return command.isOn() ? OnOffType.ON : OnOffType.OFF;

            case CHANNEL_CONTACT:
                return command.isOn() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;

            case CHANNEL_COMMAND_ID:
                return new DecimalType(commandId);

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
        sensorId = Integer.parseInt(deviceId);
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                if (type instanceof OnOffType) {
                    command = Commands.fromByte(type == OnOffType.ON ? onCommandId : offCommandId);
                    commandId = command.toByte();

                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            case CHANNEL_COMMAND_ID:
                if (type instanceof DecimalType) {
                    commandId = ((DecimalType) type).toBigDecimal().byteValue();
                    command = Commands.fromByte(commandId);

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
    public void addDevicePropertiesTo(DiscoveryResultBuilder discoveryResultBuilder) throws RFXComException {
        super.addDevicePropertiesTo(discoveryResultBuilder);
        discoveryResultBuilder.withProperty(PULSE_LABEL, pulse);
        discoveryResultBuilder.withProperty(ON_COMMAND_ID_LABEL, onCommandId);
        discoveryResultBuilder.withProperty(OFF_COMMAND_ID_LABEL, offCommandId);
    }

    @Override
    public void setConfig(RFXComDeviceConfiguration config) throws RFXComException {
        super.setConfig(config);
        this.pulse = config.pulse != null ? config.pulse : 350;
        this.onCommandId = valueOrDefault(config.onCommandId, DEFAULT_ON_COMMAND_ID);
        this.offCommandId = valueOrDefault(config.offCommandId, DEFAULT_OFF_COMMAND_ID);
    }

    private int valueOrDefault(Integer commandId, byte defaultValue) {
        if (commandId != null) {
            return commandId;
        }
        return defaultValue;
    }
}
