/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

/**
 * RFXCOM data class for blinds1 message.
 *
 * @author Peter Janson / Pål Edman - Initial contribution
 * @author Pauli Anttila - Migration to OH2
 * @author Fabien Le Bars - Added support for Cherubini blinds
 */
public class RFXComBlinds1Message extends RFXComBatteryDeviceMessage<RFXComBlinds1Message.SubType> {

    public enum SubType implements ByteEnumWrapper {
        T0(0), // Hasta new/RollerTrol
        T1(1), // Hasta Old
        T2(2), // A-OK RF01
        T3(3), // A-OK AC114/AC123/Motorlux
        T4(4), // Raex YR1326
        T5(5), // MEDIA MOUNT have different direction commands than the rest!! Needs to be fixed.
        T6(6), // DC106/Rohrmotor24-RMF/Yooda/Dooya/ESMO/Brel/Quitidom
        T7(7), // Forest
        T8(8), // Chamberlain CS4330
        T9(9), // Sunpery/BTX
        T10(10), // Dolat DLM-1, Topstar
        T11(11), // ASP
        T12(12), // Confexx CNF24-2435
        T13(13), // Screenline
        T14(14), // Hualite
        T15(15), // Motostar
        T16(16), // Zemismart
        T17(17), // Gaposa
        T18(18), // Cherubini
        T19(19), // Louvolite One Touch Vogue motor
        T20(20); // OZRoll

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    public enum Commands implements ByteEnumWrapper {
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

        @Override
        public byte toByte() {
            return (byte) command;
        }
    }

    public SubType subType;
    public int sensorId;
    public byte unitCode;
    public Commands command;

    public RFXComBlinds1Message() {
        super(PacketType.BLINDS1);
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

        subType = fromByte(SubType.class, super.subType);

        if (subType == SubType.T6 || subType == SubType.T7 || subType == SubType.T9) {
            sensorId = (data[4] & 0xFF) << 20 | (data[5] & 0xFF) << 12 | (data[6] & 0xFF) << 4 | (data[7] & 0xF0) >> 4;
            unitCode = (byte) (data[7] & 0x0F);
        } else {
            sensorId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
            unitCode = data[7];
        }

        command = fromByte(Commands.class, data[8]);

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
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        if (CHANNEL_COMMAND.equals(channelId)) {
            return (command == Commands.CLOSE ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
        } else {
            return super.convertToState(channelId, config, deviceState);
        }
    }

    @Override
    public void setSubType(SubType subType) {
        this.subType = subType;
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
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        if (CHANNEL_SHUTTER.equals(channelId)) {
            if (type instanceof OpenClosedType) {
                command = (type == OpenClosedType.CLOSED ? Commands.CLOSE : Commands.OPEN);
            } else if (type instanceof UpDownType) {
                command = (type == UpDownType.UP ? Commands.OPEN : Commands.CLOSE);
            } else if (type instanceof StopMoveType) {
                command = Commands.STOP;
            } else {
                throw new RFXComUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
            }
        } else {
            throw new RFXComUnsupportedChannelException("Channel " + channelId + " is not relevant here");
        }
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }
}
