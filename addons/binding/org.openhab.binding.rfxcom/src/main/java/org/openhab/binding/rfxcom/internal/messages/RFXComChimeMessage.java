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
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for chime messages.
 *
 * @author Mike Jagdis
 */
public class RFXComChimeMessage extends RFXComBaseMessage {

    public enum SubType {
        BYRONSX(0),
        BYRONMP001(1),
        SELECTPLUS(2),
        SELECTPLUS3(3),
        ENVIVO(4);

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
            .asList(RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.CHIME_SOUND);

    private static final List<RFXComValueSelector> SUPPORTED_OUTPUT_VALUE_SELECTORS = Collections
            .singletonList(RFXComValueSelector.CHIME_SOUND);

    public SubType subType;
    public int sensorId;
    public int chimeSound;
    public byte signalLevel;

    public RFXComChimeMessage() {
        packetType = PacketType.CHIME;
    }

    public RFXComChimeMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Chime Sound = " + chimeSound;
        str += ", Signal level = " + signalLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);

        switch (subType) {
            case BYRONSX:
                sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
                chimeSound = data[6];
                break;
            case BYRONMP001:
            case SELECTPLUS:
            case SELECTPLUS3:
            case ENVIVO:
                sensorId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
                chimeSound = 1;
                break;
        }

        signalLevel = (byte) ((data[7] & 0xF0) >> 4);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[8];

        data[0] = 0x07;
        data[1] = packetType.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        switch (subType) {
            case BYRONSX:
                data[4] = (byte) ((sensorId & 0xFF00) >> 8);
                data[5] = (byte) (sensorId & 0x00FF);
                data[6] = (byte) chimeSound;
                break;
            case BYRONMP001:
            case SELECTPLUS:
            case SELECTPLUS3:
            case ENVIVO:
                data[4] = (byte) ((sensorId & 0xFF0000) >> 16);
                data[5] = (byte) ((sensorId & 0x00FF00) >> 8);
                data[6] = (byte) ((sensorId & 0x0000FF));
                break;
        }

        data[7] = (byte) ((signalLevel & 0x0F) << 4);

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

            } else if (valueSelector == RFXComValueSelector.CHIME_SOUND) {

                state = new DecimalType(chimeSound);

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
    public void setDeviceId(String sensorId) throws RFXComException {
        this.sensorId = Integer.parseInt(sensorId);
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        switch (valueSelector) {
            case CHIME_SOUND:
                if (type instanceof DecimalType) {
                    chimeSound = ((DecimalType) type).intValue();
                } else {
                    throw new RFXComException("Can't convert " + type + " to Command");
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
