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
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for Security1 message.
 * (i.e. X10 Security, Visonic PowerCode, Meiantech, etc.)
 *
 * @author David Kalff - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComSecurity1Message extends RFXComBatteryDeviceMessage<RFXComSecurity1Message.SubType> {

    public enum SubType implements ByteEnumWrapper {
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

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    public enum Status implements ByteEnumWrapper {
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

        @Override
        public byte toByte() {
            return (byte) status;
        }
    }

    /* Added item for ContactTypes */
    public enum Contact implements ByteEnumWrapper {
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

        @Override
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
    public enum Motion implements ByteEnumWrapper {
        MOTION(4),
        NO_MOTION(5),
        MOTION_TAMPER(132),
        NO_MOTION_TAMPER(133),

        UNKNOWN(255);

        private final int motion;

        Motion(int motion) {
            this.motion = motion;
        }

        @Override
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

    public SubType subType;
    public int sensorId;
    public Status status;
    public Contact contact;
    public Motion motion;

    public RFXComSecurity1Message() {
        super(PacketType.SECURITY1);
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

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);

        status = fromByte(Status.class, data[7]);
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
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {

        switch (channelId) {
            case CHANNEL_MOTION:
                switch (status) {
                    case MOTION:
                        return OnOffType.ON;
                    case NO_MOTION:
                        return OnOffType.OFF;
                    default:
                        throw new RFXComUnsupportedChannelException("Can't convert " + status + " for " + channelId);
                }

            case CHANNEL_CONTACT:
                switch (status) {
                    case NORMAL:
                        return OpenClosedType.CLOSED;
                    case NORMAL_DELAYED:
                        return OpenClosedType.CLOSED;
                    case ALARM:
                        return OpenClosedType.OPEN;
                    case ALARM_DELAYED:
                        return OpenClosedType.OPEN;
                    default:
                        throw new RFXComUnsupportedChannelException("Can't convert " + status + " for " + channelId);
                }

            case CHANNEL_STATUS:
                return new StringType(status.toString());

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
                if ((type instanceof OnOffType) && (subType == SubType.X10_SECURITY_REMOTE)) {
                    status = (type == OnOffType.ON ? Status.ARM_AWAY_DELAYED : Status.DISARM);

                } else {
                    throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
                }
                break;

            case CHANNEL_STATUS:
                if (type instanceof StringType) {
                    status = Status.valueOf(type.toString());

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
