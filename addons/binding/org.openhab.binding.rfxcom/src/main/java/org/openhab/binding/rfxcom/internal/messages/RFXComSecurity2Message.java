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
 * RFXCOM data class for Security2 message.
 * (i.e. KEELOQ.)
 *
 * @author Mike Jagdis - Initial contribution
 */
public class RFXComSecurity2Message extends RFXComBaseMessage {

    public enum SubType {
        RAW_CLASSIC_KEELOQ(0),
        ROLLING_CODE_PACKET(1),
        RAW_AES_KEELOQ(2),
        RAW_CLASS_KEELOQ_WITH_REPEATS(3);

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

    private static final List<RFXComValueSelector> SUPPORTED_INPUT_VALUE_SELECTORS = Arrays.asList(
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL, RFXComValueSelector.CONTACT,
            RFXComValueSelector.CONTACT_1, RFXComValueSelector.CONTACT_2, RFXComValueSelector.CONTACT_3);

    private static final List<RFXComValueSelector> SUPPORTED_OUTPUT_VALUE_SELECTORS = Arrays.asList(
            RFXComValueSelector.CONTACT, RFXComValueSelector.CONTACT_1, RFXComValueSelector.CONTACT_2,
            RFXComValueSelector.CONTACT_3);

    public SubType subType;
    public int sensorId;
    public int buttonStatus;
    public byte batteryLevel;
    public byte signalLevel;

    private final int BUTTON_0_BIT = 0x02;
    private final int BUTTON_1_BIT = 0x04;
    private final int BUTTON_2_BIT = 0x08;
    private final int BUTTON_3_BIT = 0x01;

    public RFXComSecurity2Message() {
        packetType = PacketType.SECURITY2;
    }

    public RFXComSecurity2Message(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        return super.toString() + ", Sub type = " + subType + ", Device Id = " + getDeviceId() + ", Button status = "
                + buttonStatus + ", Battery level = " + batteryLevel + ", Signal level = " + signalLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);

        sensorId = (data[11] & 0x0F) << 24 | (data[10] & 0xFF) << 16 | (data[9] & 0xFF) << 8 | (data[8] & 0xFF);

        buttonStatus = (data[11] & 0xF0) >> 4;

        batteryLevel = (byte) ((data[28] & 0xF0) >> 4);
        signalLevel = (byte) (data[28] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {

        byte[] data = new byte[29];

        Arrays.fill(data, (byte) 0);

        data[0] = 0x1C;
        data[1] = RFXComBaseMessage.PacketType.SECURITY2.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        data[8] = (byte) (sensorId & 0xFF);
        data[9] = (byte) ((sensorId >> 8) & 0xFF);
        data[10] = (byte) ((sensorId >> 16) & 0xFF);
        data[11] = (byte) ((buttonStatus & 0x0f) << 4 | (sensorId >> 24) & 0x0F);

        data[28] = (byte) (((batteryLevel & 0x0F) << 4) | (signalLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        State state = UnDefType.UNDEF;

        if (valueSelector.getItemClass() == ContactItem.class) {

            if (valueSelector == RFXComValueSelector.CONTACT) {
                state = ((buttonStatus & BUTTON_0_BIT) == 0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

            } else if (valueSelector == RFXComValueSelector.CONTACT_1) {
                state = ((buttonStatus & BUTTON_1_BIT) == 0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

            } else if (valueSelector == RFXComValueSelector.CONTACT_2) {
                state = ((buttonStatus & BUTTON_2_BIT) == 0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

            } else if (valueSelector == RFXComValueSelector.CONTACT_3) {
                state = ((buttonStatus & BUTTON_3_BIT) == 0) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to ContactItem");
            }

        } else if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {
                state = new DecimalType(signalLevel);

            } else if (valueSelector == RFXComValueSelector.BATTERY_LEVEL) {
                state = new DecimalType(batteryLevel);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
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
        sensorId = Integer.parseInt(deviceId);
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        switch (valueSelector) {
            case CONTACT:
                if (type instanceof OpenClosedType) {
                    if (type == OpenClosedType.CLOSED) {
                        buttonStatus |= BUTTON_0_BIT;
                    } else {
                        buttonStatus &= ~BUTTON_0_BIT;
                    }
                } else {
                    throw new RFXComException("Can't convert " + type + " to OpenClosedType");
                }
                break;

            case CONTACT_1:
                if (type instanceof OpenClosedType) {
                    if (type == OpenClosedType.CLOSED) {
                        buttonStatus |= BUTTON_1_BIT;
                    } else {
                        buttonStatus &= ~BUTTON_1_BIT;
                    }
                } else {
                    throw new RFXComException("Can't convert " + type + " to OpenClosedType");
                }
                break;

            case CONTACT_2:
                if (type instanceof OpenClosedType) {
                    if (type == OpenClosedType.CLOSED) {
                        buttonStatus |= BUTTON_2_BIT;
                    } else {
                        buttonStatus &= ~BUTTON_2_BIT;
                    }
                } else {
                    throw new RFXComException("Can't convert " + type + " to OpenClosedType");
                }
                break;

            case CONTACT_3:
                if (type instanceof OpenClosedType) {
                    if (type == OpenClosedType.CLOSED) {
                        buttonStatus |= BUTTON_3_BIT;
                    } else {
                        buttonStatus &= ~BUTTON_3_BIT;
                    }
                } else {
                    throw new RFXComException("Can't convert " + type + " to OpenClosedType");
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
