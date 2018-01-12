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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for thermostat1 message.
 * Digimax 210 Thermostat RF sensor operational
 *
 * @author Les Ashworth - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComThermostat1Message extends RFXComDeviceMessageImpl<RFXComThermostat1Message.SubType> {

    public enum SubType implements ByteEnumWrapper {
        DIGIMAX(0),
        DIGIMAX_SHORT(1);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    /* Added item for ContactTypes */
    public enum Status implements ByteEnumWrapper {
        NO_STATUS(0),
        DEMAND(1),
        NO_DEMAND(2),
        INITIALIZING(3);

        private final int status;

        Status(int status) {
            this.status = status;
        }

        @Override
        public byte toByte() {
            return (byte) status;
        }
    }

    /* Operating mode */
    public enum Mode implements ByteEnumWrapper {
        HEATING(0),
        COOLING(1);

        private final int mode;

        Mode(int mode) {
            this.mode = mode;
        }

        @Override
        public byte toByte() {
            return (byte) mode;
        }
    }

    public SubType subType;
    public int sensorId;
    public byte temperature;
    public byte set;
    public Mode mode;
    public Status status;

    public RFXComThermostat1Message() {
        super(PacketType.THERMOSTAT1);
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

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
        temperature = data[6];
        set = data[7];
        mode = fromByte(Mode.class, (data[8] & 0xF0) >> 7);

        status = fromByte(Status.class, data[8] & 0x03);
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
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {

        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                return new DecimalType(temperature);

            case CHANNEL_SET_POINT:
                return new DecimalType(set);

            case CHANNEL_CONTACT:
                switch (status) {
                    case DEMAND:
                        return OpenClosedType.CLOSED;
                    case NO_DEMAND:
                        return OpenClosedType.OPEN;
                    default:
                        return UnDefType.UNDEF;
                }

            default:
                return super.convertToState(channelId);
        }
    }

    @Override
    public void setSubType(SubType subType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }
}
