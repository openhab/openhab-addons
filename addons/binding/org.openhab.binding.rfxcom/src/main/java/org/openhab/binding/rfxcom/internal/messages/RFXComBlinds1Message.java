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
import java.util.List;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for blinds1 message.
 *
 * @author Peter Janson / Pål Edman - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComBlinds1Message extends RFXComBaseMessage {

    public enum SubType {
        T0(0), // Hasta new/RollerTrol
        T1(1),
        T2(2),
        T3(3),
        T4(4), // Additional commands.
        T5(5), // MEDIA MOUNT have different direction commands than the rest!! Needs to be fixed.
        T6(6),
        T7(7),
        T8(8), // Chamberlain CS4330
        T9(9), // Sunpery/BTX
        T10(10), // Dolat DLM-1, Topstar
        T11(11), // ASP
        T12(12), // Confexx CNF24-2435
        T13(13); // Screenline

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
        OPEN(0), // MediaMount DOWN(0),
        CLOSE(1), // MediaMount UPP(1),
        STOP(2),
        CONFIRM(3),
        SET_LIMIT(4), // YR1326 SET_UPPER_LIMIT(4),
        SET_LOWER_LIMIT(5), // YR1326
        DELETE_LIMITS(6), // YR1326
        CHANGE_DIRECTON(7); // YR1326

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
            .asList(RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL, RFXComValueSelector.COMMAND);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
            .asList(RFXComValueSelector.SHUTTER);

    public SubType subType;
    public int sensorId;
    public byte unitCode;
    public Commands command;
    public byte signalLevel;
    public byte batteryLevel;

    public RFXComBlinds1Message() {
        packetType = PacketType.BLINDS1;
    }

    public RFXComBlinds1Message(byte[] data) throws RFXComException {
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
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);

        if (subType == SubType.T6) {
            sensorId = (data[4] & 0xFF) << 20 | (data[5] & 0xFF) << 12 | (data[6] & 0xFF) << 4 | (data[7] & 0xF0) >> 4;
            unitCode = (byte) (data[7] & 0x0F);
        } else {
            sensorId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
            unitCode = data[7];
        }

        command = Commands.fromByte(data[8]);

        signalLevel = (byte) ((data[9] & 0xF0) >> 4);
        batteryLevel = (byte) (data[9] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        // Example data
        // BLINDS1 09 19 00 06 00 B1 8F 01 00 70

        byte[] data = new byte[10];

        data[0] = 0x09;
        data[1] = RFXComBaseMessage.PacketType.BLINDS1.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        if (subType == SubType.T6) {
            data[4] = (byte) ((sensorId >>> 20) & 0xFF);
            data[5] = (byte) ((sensorId >>> 12) & 0xFF);
            data[6] = (byte) ((sensorId >>> 4) & 0xFF);
            data[7] = (byte) (((sensorId & 0x0F) << 4) | (unitCode & 0x0F));
        } else {
            data[4] = (byte) ((sensorId >> 16) & 0xFF);
            data[5] = (byte) ((sensorId >> 8) & 0xFF);
            data[6] = (byte) (sensorId & 0xFF);
            data[7] = unitCode;
        }

        data[8] = command.toByte();
        data[9] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return sensorId + ID_DELIMITER + unitCode;
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        State state = UnDefType.UNDEF;

        if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

                state = new DecimalType(signalLevel);

            } else if (valueSelector == RFXComValueSelector.BATTERY_LEVEL) {

                state = new DecimalType(batteryLevel);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
            }

        } else if (valueSelector.getItemClass() == RollershutterItem.class) {

            if (valueSelector == RFXComValueSelector.COMMAND) {

                switch (command) {
                    case CLOSE:
                        state = OpenClosedType.CLOSED;
                        break;

                    case OPEN:
                        state = OpenClosedType.OPEN;
                        break;

                    default:
                        break;
                }

            } else {
                throw new NumberFormatException("Can't convert " + valueSelector + " to RollershutterItem");
            }

        } else {

            throw new NumberFormatException("Can't convert " + valueSelector + " to " + valueSelector.getItemClass());

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
        unitCode = Byte.parseByte(ids[1]);
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        switch (valueSelector) {
            case SHUTTER:
                if (type instanceof OpenClosedType) {
                    command = (type == OpenClosedType.CLOSED ? Commands.CLOSE : Commands.OPEN);
                } else if (type instanceof UpDownType) {
                    command = (type == UpDownType.UP ? Commands.OPEN : Commands.CLOSE);
                } else if (type instanceof StopMoveType) {
                    command = Commands.STOP;

                } else {
                    throw new NumberFormatException("Can't convert " + type + " to Command");
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
