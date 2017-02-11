/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for lighting1 message. See X10, ARC, etc..
 *
 * @author Evert van Es, Cycling Engineer - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComLighting1Message extends RFXComBaseMessage {

    public enum SubType {
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

        public byte toByte() {
            return (byte) command;
        }

        public static Commands fromByte(int input) throws RFXComUnsupportedValueException {
            for (Commands c : Commands.values()) {
                if (c.command == input) {
                    return c;
                }
            }

            throw new RFXComUnsupportedValueException(Commands.class, input);
        }
    }

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays
            .asList(RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.COMMAND, RFXComValueSelector.CONTACT);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Collections
            .singletonList(RFXComValueSelector.COMMAND);

    public SubType subType;
    public char houseCode;
    public byte unitCode;
    public Commands command;
    public byte signalLevel;
    public boolean group;

    public RFXComLighting1Message() {
        packetType = PacketType.LIGHTING1;
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

        subType = SubType.fromByte(super.subType);
        houseCode = (char) data[4];
        command = Commands.fromByte(data[6]);

        if ((command == Commands.GROUP_ON) || (command == Commands.GROUP_OFF)) {
            unitCode = 0;
        } else {
            unitCode = data[5];
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
        data[5] = unitCode;
        data[6] = command.toByte();
        data[7] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return houseCode + ID_DELIMITER + unitCode;
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        State state;

        if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

                state = new DecimalType(signalLevel);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
            }

        } else if (valueSelector.getItemClass() == SwitchItem.class) {

            if (valueSelector == RFXComValueSelector.COMMAND) {

                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                    case DIM:
                        state = OnOffType.OFF;
                        break;

                    case ON:
                    case GROUP_ON:
                    case BRIGHT:
                        state = OnOffType.ON;
                        break;

                    case CHIME:
                        state = OnOffType.ON;
                        break;

                    default:
                        throw new RFXComException("Can't convert " + command + " to SwitchItem");
                }

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to SwitchItem");
            }

        } else if (valueSelector.getItemClass() == ContactItem.class) {

            if (valueSelector == RFXComValueSelector.CONTACT) {

                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                    case DIM:
                        state = OpenClosedType.CLOSED;
                        break;

                    case ON:
                    case GROUP_ON:
                    case BRIGHT:
                        state = OpenClosedType.OPEN;
                        break;

                    case CHIME:
                        state = OpenClosedType.OPEN;
                        break;

                    default:
                        throw new RFXComException("Can't convert " + command + " to ContactItem");
                }

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to ContactItem");
            }

        } else {

            throw new RFXComException("Can't convert " + valueSelector + " to " + valueSelector.getItemClass());

        }

        return state;
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

        houseCode = ids[0].charAt(0);

        // Get unitcode, 0 means group
        unitCode = Byte.parseByte(ids[1]);
        if (unitCode == 0) {
            unitCode = 1;
            group = true;
        }
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        switch (valueSelector) {
            case COMMAND:
                if (type instanceof OnOffType) {
                    if (group) {
                        command = (type == OnOffType.ON ? Commands.GROUP_ON : Commands.GROUP_OFF);
                    } else {
                        command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
                    }
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

        try {
            return SubType.fromByte(Integer.parseInt(subType));
        } catch (NumberFormatException e) {
            throw new RFXComUnsupportedValueException(SubType.class, subType);
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
