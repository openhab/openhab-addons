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

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.CHANNEL_COMMAND;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

/**
 * RFXCOM data class for HomeConfort message.
 *
 * @author Mike Jagdis - Initial contribution
 */
public class RFXComHomeConfortMessage extends RFXComDeviceMessageImpl<RFXComHomeConfortMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        TEL_010(0);

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
        OFF(0),
        ON(1),
        GROUP_OFF(2),
        GROUP_ON(3);

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
    public int deviceId;
    public char houseCode;
    public byte unitCode;
    public Commands command;

    public RFXComHomeConfortMessage() {
        super(PacketType.HOME_CONFORT);
    }

    public RFXComHomeConfortMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        return super.toString() + ", Sub type = " + subType + ", Device Id = " + getDeviceId() + ", Command = "
                + command + ", Signal level = " + signalLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        deviceId = (((data[4] << 8) | data[5]) << 8) | data[6];
        houseCode = (char) data[7];
        unitCode = data[8];
        command = fromByte(Commands.class, data[9]);
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
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        if (channelId.equals(CHANNEL_COMMAND)) {
            return OnOffType.from(command != Commands.OFF && command != Commands.GROUP_OFF);
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
        if (ids.length != 3) {
            throw new RFXComException("Invalid device id '" + deviceId + "'");
        }

        this.deviceId = Integer.parseInt(ids[0]);
        houseCode = ids[1].charAt(0);
        unitCode = Byte.parseByte(ids[2]);
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        if (CHANNEL_COMMAND.equals(channelId)) {
            if (type instanceof OnOffType) {
                if (unitCode == 0) {
                    command = (type == OnOffType.ON ? Commands.GROUP_ON : Commands.GROUP_OFF);

                } else {
                    command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
                }
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
