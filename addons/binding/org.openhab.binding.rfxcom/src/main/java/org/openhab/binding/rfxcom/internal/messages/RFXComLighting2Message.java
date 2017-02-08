/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.LIGHTING2;
import static org.openhab.binding.rfxcom.internal.messages.RFXComLighting2Message.Commands.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for lighting2 message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComLighting2Message extends RFXComBaseMessage {

    public enum SubType {
        AC(0),
        HOME_EASY_EU(1),
        ANSLUT(2),
        KAMBROOK(3);

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
        SET_LEVEL(2),
        GROUP_OFF(3),
        GROUP_ON(4),
        SET_GROUP_LEVEL(5);

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

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays.asList(
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.COMMAND, RFXComValueSelector.DIMMING_LEVEL,
            RFXComValueSelector.CONTACT);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
            .asList(RFXComValueSelector.COMMAND, RFXComValueSelector.DIMMING_LEVEL);

    public SubType subType;
    public int sensorId;
    public byte unitCode;
    public Commands command;
    public byte dimmingLevel;
    public byte signalLevel;
    public boolean group;

    public RFXComLighting2Message() {
        packetType = PacketType.LIGHTING2;
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

        subType = SubType.fromByte(super.subType);
        sensorId = (data[4] & 0xFF) << 24 | (data[5] & 0xFF) << 16 | (data[6] & 0xFF) << 8 | (data[7] & 0xFF);
        command = Commands.fromByte(data[9]);

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
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        State state = UnDefType.UNDEF;

        if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

                state = new DecimalType(signalLevel);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
            }

        } else if (valueSelector.getItemClass() == DimmerItem.class
                || valueSelector.getItemClass() == RollershutterItem.class) {

            if (valueSelector == RFXComValueSelector.DIMMING_LEVEL) {
                state = RFXComLighting2Message.getPercentTypeFromDimLevel(dimmingLevel);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to DimmerItem/RollershutterItem");
            }

        } else if (valueSelector.getItemClass() == SwitchItem.class) {

            if (valueSelector == RFXComValueSelector.COMMAND) {

                switch (command) {
                    case OFF:
                    case GROUP_OFF:
                        state = OnOffType.OFF;
                        break;

                    case ON:
                    case GROUP_ON:
                        state = OnOffType.ON;
                        break;

                    case SET_GROUP_LEVEL:
                    case SET_LEVEL:
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
                        state = OpenClosedType.CLOSED;
                        break;

                    case ON:
                    case GROUP_ON:
                        state = OpenClosedType.OPEN;
                        break;

                    case SET_GROUP_LEVEL:
                    case SET_LEVEL:
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

        sensorId = Integer.parseInt(ids[0]);

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
                        command = (type == OnOffType.ON ? GROUP_ON : GROUP_OFF);
                    } else {
                        command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
                    }
                    dimmingLevel = 0;
                } else {
                    throw new RFXComException("Can't convert " + type + " to Command");
                }
                break;

            case DIMMING_LEVEL:
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
