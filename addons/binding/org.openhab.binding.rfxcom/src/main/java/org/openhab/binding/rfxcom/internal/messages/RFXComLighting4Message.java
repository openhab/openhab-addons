/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.RFXComValueSelector.*;
import static org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
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
public class RFXComLighting4Message extends RFXComBaseMessage {
    // this loger is used from a static context, so is static as well
    private static final Logger LOGGER = LoggerFactory.getLogger(RFXComLighting4Message.class);

    private static final byte DEFAULT_OFF_COMMAND_ID = Commands.OFF_4.toByte();
    private static final byte DEFAULT_ON_COMMAND_ID = Commands.ON_1.toByte();

    public enum SubType {
        PT2262(0);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        public byte toByte() {
            return (byte) subType;
        }

        public static SubType fromByte(int input) throws RFXComUnsupportedValueException {
            for (SubType c : SubType.values()) {
                if (c.subType == input) {
                    return c;
                }
            }

            throw new RFXComUnsupportedValueException(SubType.class, input);
        }
    }

    public enum Commands {
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

    private static final List<RFXComValueSelector> SUPPORTED_INPUT_VALUE_SELECTORS = Arrays.asList(COMMAND, COMMAND_ID,
            SIGNAL_LEVEL);

    private static final List<RFXComValueSelector> SUPPORTED_OUTPUT_VALUE_SELECTORS = Arrays.asList(COMMAND,
            COMMAND_ID);

    private SubType subType;
    private int sensorId;
    private int pulse;
    private Commands command;
    private int commandId;
    private int offCommandId;
    private int onCommandId;
    private byte signalLevel;

    public RFXComLighting4Message() {
        packetType = PacketType.LIGHTING4;
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

        subType = SubType.fromByte(super.subType);
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
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        if (valueSelector.getItemClass() == SwitchItem.class) {
            if (valueSelector == COMMAND || valueSelector == MOTION) {
                return command.isOn() ? OnOffType.ON : OnOffType.OFF;
            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to SwitchItem: not supported");
            }
        } else if (valueSelector.getItemClass() == ContactItem.class) {
            if (valueSelector == RFXComValueSelector.CONTACT) {
                return command.isOn() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to SwitchItem: not supported");
            }
        } else if (valueSelector.getItemClass() == NumberItem.class) {
            if (valueSelector == SIGNAL_LEVEL) {
                return new DecimalType(signalLevel);
            } else if (valueSelector == COMMAND_ID) {
                return new DecimalType(commandId);
            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
            }
        }

        throw new RFXComException("Can't convert " + valueSelector + " to " + valueSelector.getItemClass());
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
        this.subType = ((SubType) subType);
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        sensorId = Integer.parseInt(deviceId);
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {
        switch (valueSelector) {
            case COMMAND:
                if (type instanceof OnOffType) {
                    command = Commands.fromByte(type == OnOffType.ON ? onCommandId : offCommandId);
                    commandId = command.toByte();
                } else {
                    throw new RFXComException("Can't convert " + type + " to Command");
                }
                break;
            case COMMAND_ID:
                if (type instanceof DecimalType) {
                    commandId = ((DecimalType) type).toBigDecimal().byteValue();
                    command = Commands.fromByte(commandId);
                } else {
                    throw new RFXComException("Can't convert " + type + " to CommandId");
                }
                break;
            default:
                throw new RFXComException("Can't convert " + type + " to " + valueSelector);
        }
    }

    @Override
    public Object convertSubType(String subType) throws RFXComException {
        for (SubType s : SubType.values()) {
            if (s.toString().equals(subType)) {
                return s;
            }
        }

        try {
            return SubType.fromByte(Integer.parseInt(subType));
        } catch (NumberFormatException e) {
            throw new RFXComUnsupportedValueException(SubType.class, subType);
        }
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException {
        return SUPPORTED_INPUT_VALUE_SELECTORS;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return SUPPORTED_OUTPUT_VALUE_SELECTORS;
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
