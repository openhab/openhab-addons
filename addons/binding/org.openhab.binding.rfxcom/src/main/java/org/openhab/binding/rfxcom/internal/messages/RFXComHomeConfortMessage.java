/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for HomeConfort message.
 *
 * @author Mike Jagdis - Initial contribution
 */
public class RFXComHomeConfortMessage extends RFXComBaseMessage {

    public enum SubType {
        TEL_010(0);

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
        GROUP_OFF(2),
        GROUP_ON(3);

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

    private static final List<RFXComValueSelector> SUPPORTED_INPUT_VALUE_SELECTORS = Collections
            .singletonList(RFXComValueSelector.COMMAND);

    private static final List<RFXComValueSelector> SUPPORTED_OUTPUT_VALUE_SELECTORS = Collections
            .singletonList(RFXComValueSelector.COMMAND);

    public SubType subType;
    public int deviceId;
    public char houseCode;
    public byte unitCode;
    public Commands command;
    public byte signalLevel;

    public RFXComHomeConfortMessage() {
        packetType = PacketType.HOME_CONFORT;
    }

    public RFXComHomeConfortMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        return super.toString()
            + ", Sub type = " + subType
            + ", Device Id = " + getDeviceId()
            + ", Command = " + command
            + ", Signal level = " + signalLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        deviceId = (((data[4] << 8) | data[5]) << 8) | data[6];
        houseCode = (char) data[7];
        unitCode = data[8];
        command = Commands.fromByte(data[9]);
        if (command == Commands.GROUP_ON || command == Commands.GROUP_OFF) {
            unitCode = 0;
        }
        signalLevel = (byte) ((data[12] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {

        byte[] data = new byte[13];

        data[0] = 0x0C;
        data[1] = PacketType.HOME_CONFORT.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((deviceId >> 16) & 0xff);
        data[5] = (byte) ((deviceId >> 8) & 0xff);
        data[6] = (byte) (deviceId & 0xff);
        data[7] = (byte) houseCode;
        data[8] = unitCode;
        data[9] = command.toByte();
        data[10] = 0;
        data[11] = 0;
        data[12] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return deviceId + ID_DELIMITER + houseCode + ID_DELIMITER + unitCode;
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
                        state = OnOffType.OFF;
                        break;

                    case ON:
                    case GROUP_ON:
                        state = OnOffType.ON;
                        break;

                    default:
                        throw new RFXComException("Can't convert " + command + " to SwitchItem");
                }

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to SwitchItem");
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
        if (ids.length != 3) {
            throw new RFXComException("Invalid device id '" + deviceId + "'");
        }

        this.deviceId = Integer.parseInt(ids[0]);
        houseCode = ids[1].charAt(0);
        unitCode = Byte.parseByte(ids[2]);
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        switch (valueSelector) {
            case COMMAND:
                if (type instanceof OnOffType) {
                    if (unitCode == 0) {
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
        return SUPPORTED_INPUT_VALUE_SELECTORS;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return SUPPORTED_OUTPUT_VALUE_SELECTORS;
    }

}
