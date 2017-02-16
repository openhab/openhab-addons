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

import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for thermostat1 message.
 * Digimax 210 Thermostat RF sensor operational
 *
 * @author Les Ashworth - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComThermostat1Message extends RFXComBaseMessage {

    public enum SubType {
        DIGIMAX(0),
        DIGIMAX_SHORT(1);

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

    /* Added item for ContactTypes */
    public enum Status {
        NO_STATUS(0),
        DEMAND(1),
        NO_DEMAND(2),
        INITIALIZING(3);

        private final int status;

        Status(int status) {
            this.status = status;
        }

        public byte toByte() {
            return (byte) status;
        }

        public static Status fromByte(int input) throws RFXComUnsupportedValueException {
            for (Status contact : Status.values()) {
                if (contact.status == input) {
                    return contact;
                }
            }

            throw new RFXComUnsupportedValueException(Status.class, input);
        }
    }

    /* Operating mode */
    public enum Mode {
        HEATING(0),
        COOLING(1);

        private final int mode;

        Mode(int mode) {
            this.mode = mode;
        }

        public byte toByte() {
            return (byte) mode;
        }

        public static Mode fromByte(int input) throws RFXComUnsupportedValueException {
            for (Mode mode : Mode.values()) {
                if (mode.mode == input) {
                    return mode;
                }
            }

            throw new RFXComUnsupportedValueException(Mode.class, input);
        }
    }

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays.asList(
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.TEMPERATURE, RFXComValueSelector.SET_POINT,
            RFXComValueSelector.CONTACT);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays.asList();

    public SubType subType;
    public int sensorId;
    public byte temperature;
    public byte set;
    public Mode mode;
    public Status status;
    public byte signalLevel;

    public RFXComThermostat1Message() {
        packetType = PacketType.THERMOSTAT1;
    }

    public RFXComThermostat1Message(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Temperature = " + temperature;
        str += ", Set = " + set;
        str += ", Mode = " + mode;
        str += ", Status = " + status;
        str += ", Signal level = " + signalLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
        temperature = data[6];
        set = data[7];
        mode = Mode.fromByte((data[8] & 0xF0) >> 7);

        status = Status.fromByte(data[8] & 0x03);
        signalLevel = (byte) ((data[9] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[10];

        data[0] = 0x09;
        data[1] = RFXComBaseMessage.PacketType.THERMOSTAT1.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);
        data[6] = (temperature);
        data[7] = (set);
        data[8] = (byte) ((mode.toByte() << 7) | (status.toByte() & 0xFF));
        data[9] = (byte) (signalLevel << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        State state = UnDefType.UNDEF;

        if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

                state = new DecimalType(signalLevel);

            } else if (valueSelector == RFXComValueSelector.TEMPERATURE) {

                state = new DecimalType(temperature);

            } else if (valueSelector == RFXComValueSelector.SET_POINT) {

                state = new DecimalType(set);

            } else {
                throw new NumberFormatException("Can't convert " + valueSelector + " to NumberItem");
            }

        } else if (valueSelector.getItemClass() == ContactItem.class) {
            if (valueSelector == RFXComValueSelector.CONTACT) {
                switch (status) {
                    case DEMAND:
                        state = OpenClosedType.CLOSED;
                        break;
                    case NO_DEMAND:
                        state = OpenClosedType.OPEN;
                        break;
                    default:
                        break;
                }
            }
        }

        else {
            throw new NumberFormatException("Can't convert " + valueSelector + " to " + valueSelector.getItemClass());
        }

        return state;
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        throw new RFXComException("Not supported");
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