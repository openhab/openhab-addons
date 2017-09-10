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
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
 * RFXCOM data class for RFY (Somfy RTS) message.
 *
 * @author Jürgen Richtsfeld - Initial contribution
 * @author Pauli Anttila - Ported from OpenHAB1
 * @author Mike Jagdis - Added venetian support and sun+wind detector
 */
public class RFXComRfyMessage extends RFXComBaseMessage {

    public enum Commands {
        STOP(0x00),
        UP(0x01),
        DOWN(0x03),
        PROGRAM(0x07),
        UP_SHORT(0x0F),
        DOWN_SHORT(0x10),
        UP_LONG(0x11),
        DOWN_LONG(0x12),
        ENABLE_SUN_WIND_DETECTOR(0x13),
        DISABLE_SUN_DETECTOR(0x14);

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

    public enum SubType {
        RFY(0),
        RFY_EXT(1),
        RESERVED(2),
        ASA(3);

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

    private static final List<RFXComValueSelector> SUPPORTED_INPUT_VALUE_SELECTORS = Arrays
            .asList(RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.COMMAND);

    private static final List<RFXComValueSelector> SUPPORTED_OUTPUT_VALUE_SELECTORS = Arrays
            .asList(RFXComValueSelector.SHUTTER, RFXComValueSelector.VENETIAN_BLIND);

    private boolean sunDetector;

    public SubType subType;
    public int unitId;
    public byte unitCode;
    public Commands command;
    public byte signalLevel; // maximum 0xF

    public RFXComRfyMessage() {
        packetType = PacketType.RFY;

    }

    public RFXComRfyMessage(byte[] data) throws RFXComException {

        encodeMessage(data);
    }

    @Override
    public String toString() {
        return super.toString()
            + ", Sub type = " + subType
            + ", Unit Id = " + getDeviceId()
            + ", Unit Code = " + unitCode
            + ", Command = " + command
            + ", Signal level = " + signalLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = SubType.values()[super.subType];

        unitId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
        unitCode = data[7];

        command = Commands.fromByte(data[8]);
        signalLevel = (byte) ((data[12] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {
        final byte[] data = new byte[13];

        data[0] = 12;
        data[1] = RFXComBaseMessage.PacketType.RFY.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((unitId >> 16) & 0xFF);
        data[5] = (byte) ((unitId >> 8) & 0xFF);
        data[6] = (byte) (unitId & 0xFF);
        data[7] = unitCode;
        data[8] = command.toByte();
        data[12] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public String getDeviceId() {
        return unitId + ID_DELIMITER + unitCode;
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

        } else if (valueSelector.getItemClass() == RollershutterItem.class) {
            if (valueSelector == RFXComValueSelector.COMMAND) {

                switch (command) {
                    case DOWN:
                        state = OpenClosedType.CLOSED;
                        break;

                    case UP:
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

        this.unitId = Integer.parseInt(ids[0]);
        this.unitCode = Byte.parseByte(ids[1]);
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        switch (valueSelector) {
            case SHUTTER:
                if (type instanceof OpenClosedType) {
                    this.command = (type == OpenClosedType.CLOSED ? Commands.DOWN : Commands.UP);

                } else if (type instanceof UpDownType) {
                    this.command = (type == UpDownType.DOWN ? Commands.DOWN : Commands.UP);

                } else if (type instanceof StopMoveType) {
                    this.command = RFXComRfyMessage.Commands.STOP;

                } else {
                    throw new NumberFormatException("Can't convert " + type + " to Command");
                }
                break;

            case PROGRAM:
                if (type instanceof OnOffType && type == OnOffType.ON) {
                    this.command = Commands.PROGRAM;

                } else {
                    throw new NumberFormatException("Can't convert " + type + " to Command");
                }
                break;

            case SUN_WIND_DETECTOR:
                if (type instanceof OnOffType) {
                    this.command = (type == OnOffType.ON ? Commands.ENABLE_SUN_WIND_DETECTOR
                            : Commands.DISABLE_SUN_DETECTOR);

                } else {
                    throw new NumberFormatException("Can't convert " + type + " to Command");
                }
                break;

            case VENETIAN_BLIND:
                if (type instanceof OpenClosedType) {
                    this.command = (type == OpenClosedType.CLOSED ? Commands.DOWN_SHORT : Commands.UP_SHORT);

                } else if (type instanceof OnOffType) {
                    this.command = (type == OnOffType.ON ? Commands.DOWN_SHORT : Commands.UP_SHORT);

                } else if (type instanceof IncreaseDecreaseType) {
                    this.command = (type == IncreaseDecreaseType.INCREASE ? Commands.DOWN_LONG : Commands.UP_LONG);

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
        return SUPPORTED_INPUT_VALUE_SELECTORS;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return SUPPORTED_OUTPUT_VALUE_SELECTORS;
    }
}
