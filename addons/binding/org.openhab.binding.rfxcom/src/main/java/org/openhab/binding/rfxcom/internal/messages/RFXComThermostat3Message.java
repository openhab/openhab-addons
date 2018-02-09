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

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;

import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for thermostat3 message.
 * Mertik G6R-H4x gas heater
 *
 * @author Ruud Beukema - Initial contribution
 */
public class RFXComThermostat3Message extends RFXComDeviceMessageImpl<RFXComThermostat3Message.SubType> {
    public enum SubType implements ByteEnumWrapper {
        MERTIK__G6R_H4T1(0),
        MERTIK__G6R_H4TB__G6_H4T__G6R_H4T21_Z22(1),
        MERTIK__G6R_H4TD__G6R_H4T16(2),
        MERTIK__G6R_H4S_TRANSMIT_ONLY(3);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    /* Operating mode */
    public enum Commands implements ByteEnumWrapper {
        OFF(0),
        ON(1),
        UP(2),
        DOWN(3);
        
        // For now unimplemented commands
        // RUN_UP__2ND_OFF(4), // G6R_H4T1 and G6R_H4TB respectively only
        // RUN_DOWN__2ND_ON(5), // G6R_H4T1 and G6R_H4TB respectively only
        // STOP(6); // G6R_H4T1 only

        private final int command;

        Commands(int command) {
            this.command = command;
        }

        @Override
        public byte toByte() {
            return (byte) command;
        }

        public String toString(SubType subtype) {
            Commands _command = Commands.values()[this.command];
            switch (_command) {
                case OFF:
                    return new String("OFF");
                case ON:
                    return new String("ON");
                case UP:
                    return new String("UP");
                case DOWN:
                    return new String("DOWN");
                default:
                    return new String("Unknown command");
            }
        }
    }

    public SubType subType;
    public int unitId;
    public Commands command;

    public RFXComThermostat3Message() {
        super(PacketType.THERMOSTAT3);
    }

    public RFXComThermostat3Message(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Command = " + command.toString(subType);
        str += ", Signal level = " + signalLevel;

        return str;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(unitId);
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        unitId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
        command = fromByte(Commands.class, (data[7]));
        // Ignore filler byte at data[8]
        signalLevel = (byte) ((data[9] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() throws RFXComException {
        byte[] data = new byte[10];

        data[0] = 0x08;
        data[1] = RFXComBaseMessage.PacketType.THERMOSTAT3.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((unitId >> 16) & 0xFF);
        data[5] = (byte) ((unitId >> 8) & 0xFF);
        data[6] = (byte) (unitId & 0xFF);
        data[7] = command.toByte();
        data[8] = (byte) 0xFF; // filler
        data[9] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }
    
    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
		switch (channelId) {
	        case CHANNEL_COMMAND:
	            switch (command) {
	            	case OFF: return OnOffType.OFF;
					case ON: return OnOffType.ON;
					case UP: return OpenClosedType.CLOSED;
					case DOWN: return OpenClosedType.OPEN;
			        default:
		                    throw new RFXComUnsupportedChannelException("Can't convert " + command + " for " + channelId);
	            }
			default:
	            return super.convertToState(channelId);
        }
    }
    
    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_COMMAND:
                if (type instanceof OnOffType) {
                    command = (type == OnOffType.ON ? Commands.ON : Commands.OFF);
				} else if (type instanceof UpDownType) {
					command = (type == UpDownType.DOWN ? Commands.DOWN : Commands.UP);
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

    @Override
    public void setSubType(SubType subType) {
        this.subType = subType;
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new UnsupportedOperationException();
    }
}
