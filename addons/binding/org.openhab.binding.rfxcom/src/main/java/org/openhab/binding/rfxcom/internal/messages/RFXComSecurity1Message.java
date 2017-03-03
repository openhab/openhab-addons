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
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for Security1 message.
 * (i.e. X10 Security, Visonic PowerCode, Meiantech, etc.)
 *
 * @author David Kalff - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComSecurity1Message extends RFXComBaseMessage {

    public enum SubType {
        X10_SECURITY(0),
        X10_SECURITY_MOTION(1),
        X10_SECURITY_REMOTE(2),
        KD101(3),
        VISONIC_POWERCODE_SENSOR_PRIMARY_CONTACT(4),
        VISONIC_POWERCODE_MOTION(5),
        VISONIC_CODESECURE(6),
        VISONIC_POWERCODE_SENSOR_AUX_CONTACT(7),
        MEIANTECH(8),
        SA30(9), // Also SA33
        RM174RF(10);

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

    public enum Status {
        NORMAL(0),
        NORMAL_DELAYED(1),
        ALARM(2),
        ALARM_DELAYED(3),
        MOTION(4),
        NO_MOTION(5),
        PANIC(6),
        END_PANIC(7),
        IR(8),
        ARM_AWAY(9),
        ARM_AWAY_DELAYED(10),
        ARM_HOME(11),
        ARM_HOME_DELAYED(12),
        DISARM(13),
        LIGHT_1_OFF(16),
        LIGHT_1_ON(17),
        LIGHT_2_OFF(18),
        LIGHT_2_ON(19),
        DARK_DETECTED(20),
        LIGHT_DETECTED(21),
        BATLOW(22),
        PAIR_KD101(23),
        NORMAL_TAMPER(128),
        NORMAL_DELAYED_TAMPER(129),
        ALARM_TAMPER(130),
        ALARM_DELAYED_TAMPER(131),
        MOTION_TAMPER(132),
        NO_MOTION_TAMPER(133);

        private final int status;

        Status(int status) {
            this.status = status;
        }

        public byte toByte() {
            return (byte) status;
        }

        public static Status fromByte(int input) throws RFXComUnsupportedValueException {
            for (Status status : Status.values()) {
                if (status.status == input) {
                    return status;
                }
            }

            throw new RFXComUnsupportedValueException(Status.class, input);
        }
    }

    /* Added item for ContactTypes */
    public enum Contact {
        NORMAL(0),
        NORMAL_DELAYED(1),
        ALARM(2),
        ALARM_DELAYED(3),
        NORMAL_TAMPER(128),
        NORMAL_DELAYED_TAMPER(129),
        ALARM_TAMPER(130),
        ALARM_DELAYED_TAMPER(131),

        UNKNOWN(255);

        private final int contact;

        Contact(int contact) {
            this.contact = contact;
        }

        public byte toByte() {
            return (byte) contact;
        }

        public static Contact fromByte(int input) {
            for (Contact status : Contact.values()) {
                if (status.contact == input) {
                    return status;
                }
            }

            return Contact.UNKNOWN;
        }
    }

    /* Added item for MotionTypes */
    public enum Motion {
        MOTION(4),
        NO_MOTION(5),
        MOTION_TAMPER(132),
        NO_MOTION_TAMPER(133),

        UNKNOWN(255);

        private final int motion;

        Motion(int motion) {
            this.motion = motion;
        }

        public byte toByte() {
            return (byte) motion;
        }

        public static Motion fromByte(int input) {
            for (Motion motion : Motion.values()) {
                if (motion.motion == input) {
                    return motion;
                }
            }

            return Motion.UNKNOWN;
        }
    }

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays.asList(
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL, RFXComValueSelector.STATUS,
            RFXComValueSelector.CONTACT, RFXComValueSelector.MOTION);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
            .asList(RFXComValueSelector.STATUS, RFXComValueSelector.CONTACT);

    public SubType subType;
    public int sensorId;
    public Status status;
    public byte batteryLevel;
    public byte signalLevel;
    public Contact contact;
    public Motion motion;

    public RFXComSecurity1Message() {
        packetType = PacketType.SECURITY1;
    }

    public RFXComSecurity1Message(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Status = " + status;
        str += ", Battery level = " + batteryLevel;
        str += ", Signal level = " + signalLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        sensorId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);

        status = Status.fromByte(data[7]);
        batteryLevel = (byte) ((data[8] & 0xF0) >> 4);
        signalLevel = (byte) (data[8] & 0x0F);

        contact = Contact.fromByte(data[7]);
        motion = Motion.fromByte(data[7]);
    }

    @Override
    public byte[] decodeMessage() {

        byte[] data = new byte[9];

        data[0] = 0x08;
        data[1] = RFXComBaseMessage.PacketType.SECURITY1.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId >> 16) & 0xFF);
        data[5] = (byte) ((sensorId >> 8) & 0xFF);
        data[6] = (byte) (sensorId & 0xFF);
        data[7] = status.toByte();
        data[8] = (byte) (((batteryLevel & 0x0F) << 4) | (signalLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        State state = UnDefType.UNDEF;

        if (valueSelector.getItemClass() == SwitchItem.class) {

            if (valueSelector == RFXComValueSelector.MOTION) {

                switch (status) {
                    case MOTION:
                        state = OnOffType.ON;
                        break;
                    case NO_MOTION:
                        state = OnOffType.OFF;
                        break;
                    default:
                        break;
                }

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to SwitchItem");
            }

        } else if (valueSelector.getItemClass() == ContactItem.class) {

            if (valueSelector == RFXComValueSelector.CONTACT) {

                switch (status) {

                    case NORMAL:
                        state = OpenClosedType.CLOSED;
                        break;
                    case NORMAL_DELAYED:
                        state = OpenClosedType.CLOSED;
                        break;
                    case ALARM:
                        state = OpenClosedType.OPEN;
                        break;
                    case ALARM_DELAYED:
                        state = OpenClosedType.OPEN;
                        break;
                    default:
                        break;

                }

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to ContactItem");
            }

        } else if (valueSelector.getItemClass() == StringItem.class) {

            if (valueSelector == RFXComValueSelector.STATUS) {

                state = new StringType(status.toString());

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to StringItem");
            }

        } else if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

                state = new DecimalType(signalLevel);

            } else if (valueSelector == RFXComValueSelector.BATTERY_LEVEL) {

                state = new DecimalType(batteryLevel);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to StringItem");
            }

        } else if (valueSelector.getItemClass() == DateTimeItem.class) {

            state = new DateTimeType();

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
        sensorId = Integer.parseInt(deviceId);
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        switch (valueSelector) {
            case COMMAND:
                if ((type instanceof OnOffType) && (subType == SubType.X10_SECURITY_REMOTE)) {
                    status = (type == OnOffType.ON ? Status.ARM_AWAY_DELAYED : Status.DISARM);
                } else {
                    throw new RFXComException("Can't convert " + type + " to Command");
                }
                break;

            case STATUS:
                if (type instanceof StringType) {
                    status = Status.valueOf(type.toString());
                } else {
                    throw new RFXComException("Can't convert " + type + " to Status");
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
