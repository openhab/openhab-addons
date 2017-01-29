/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

import java.util.Arrays;
import java.util.List;

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
 */
public class RFXComLighting4Message extends RFXComBaseMessage {

    public enum SubType {
        PT2262(0),

        UNKNOWN(255);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        SubType(byte subType) {
            this.subType = subType;
        }

        public byte toByte() {
            return (byte) subType;
        }

        public static SubType fromByte(int input) {
            for (SubType c : SubType.values()) {
                if (c.subType == input) {
                    return c;
                }
            }

            return SubType.UNKNOWN;
        }
    }

    public enum Commands {
        UNDEFINED_0(0),
        ON(1),
        UNDEFINED_2(2),
        UNDEFINED_3(3),
        OFF(4),

        UNKNOWN(255);

        private final int command;

        Commands(int command) {
            this.command = command;
        }

        Commands(byte command) {
            this.command = command;
        }

        public byte toByte() {
            return (byte) command;
        }

        public static Commands fromByte(int input) {
            for (Commands c : Commands.values()) {
                if (c.command == input) {
                    return c;
                }
            }
            return Commands.UNKNOWN;
        }
    }

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays
            .asList(RFXComValueSelector.COMMAND, RFXComValueSelector.SIGNAL_LEVEL);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
            .asList(RFXComValueSelector.COMMAND);

    public SubType subType = SubType.UNKNOWN;
    public int sensorId = 0;
    public Commands command = Commands.UNKNOWN;
    public int pulse = 0;
    public byte signalLevel = 0;

    public RFXComLighting4Message() {
        packetType = PacketType.LIGHTING4;
    }

    public RFXComLighting4Message(byte[] data) {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Command = " + command;
        str += ", Pulse = " + pulse;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        sensorId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF) >>> 4;

        int commandID = (data[6] & 0x0F); // 4 OFF - 1 ON
        command = Commands.fromByte(commandID);

        pulse = (data[7] & 0xFF) << 8 | (data[8] & 0xFF) << 0;

        signalLevel = (byte) ((data[9] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {

        byte[] data = new byte[11];

        data[0] = 0x0A;
        data[1] = RFXComBaseMessage.PacketType.LIGHTING4.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        // SENSORID + COMMAND
        data[4] = (byte) ((sensorId >> 16) & 0xFF);
        data[5] = (byte) ((sensorId >> 8) & 0xFF);
        data[6] = (byte) (((sensorId >> 4) & 0xFF) | command.ordinal() & 0x0F);

        // PULSE
        data[7] = (byte) ((pulse >> 8) & 0xFF);
        data[8] = (byte) (pulse & 0xFF);

        // SIGNAL
        data[9] = 0;

        // UNUSED
        data[10] = 0;

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        State state = UnDefType.UNDEF;

        // SWITCHITEM
        if (valueSelector.getItemClass() == SwitchItem.class) {

            if (valueSelector == RFXComValueSelector.COMMAND) {
                switch (command) {
                    case OFF:
                        state = OnOffType.OFF;
                        break;
                    case ON:
                        state = OnOffType.ON;
                        break;
                    default:
                        throw new RFXComException("Can't convert value " + command + " to COMMAND SwitchItem");
                }
            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to SwitchItem: not supported");
            }

            return state;

        } else if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

                state = new DecimalType(signalLevel);

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
        String[] ids = deviceId.split("\\" + ID_DELIMITER);
        if (ids.length != 2) {
            throw new RFXComException("Invalid device id '" + deviceId + "'");
        }

        sensorId = Integer.parseInt(ids[0]);
        pulse = Integer.parseInt(ids[1]);
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        switch (valueSelector) {

            case COMMAND:
                if (type instanceof OnOffType) {
                    command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
                } else {
                    throw new RFXComException("Can't convert " + type + " to Command");
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

        // try to find sub type by number
        try {
            return SubType.values()[Integer.parseInt(subType)];
        } catch (Exception e) {
            throw new RFXComException("Unknown sub type " + subType);
        }
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException {
        return supportedInputValueSelectors;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return supportedOutputValueSelectors;
    }

}
