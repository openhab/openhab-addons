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
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for BBQ temperature message.
 *
 * @author Mike Jagdis - Initial contribution
 */
public class RFXComBBQTemperatureMessage extends RFXComBaseMessage {

    public enum SubType {
        BBQ1(1); // Maverick ET-732

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
            RFXComValueSelector.FOOD_TEMPERATURE, RFXComValueSelector.BBQ_TEMPERATURE,
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL);

    private static final List<RFXComValueSelector> SUPPORTED_OUTPUT_VALUE_SELECTORS = Collections.emptyList();

    public SubType subType;
    public int sensorId;
    public double foodTemperature;
    public double bbqTemperature;
    public byte signalLevel;
    public byte batteryLevel;

    public RFXComBBQTemperatureMessage() {
        packetType = PacketType.BBQ;
    }

    public RFXComBBQTemperatureMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        return super.toString()
            + ", Sub type = " + subType
            + ", Device Id = " + getDeviceId()
            + ", Food temperature = " + foodTemperature
            + ", BBQ temperature = " + bbqTemperature
            + ", Signal level = " + signalLevel
            + ", Battery level = " + batteryLevel;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

        foodTemperature = (double) ((data[6] & 0x7F) << 8 | (data[7] & 0xFF));
        if ((data[6] & 0x80) != 0) {
            foodTemperature = -foodTemperature;
        }

        bbqTemperature = (double) ((data[8] & 0x7F) << 8 | (data[9] & 0xFF));
        if ((data[8] & 0x80) != 0) {
            bbqTemperature = -bbqTemperature;
        }

        signalLevel = (byte) ((data[10] & 0xF0) >> 4);
        batteryLevel = (byte) (data[10] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[11];

        data[0] = 0x0A;
        data[1] = RFXComBaseMessage.PacketType.BBQ.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);

        short temp = (short) Math.abs(foodTemperature);
        data[6] = (byte) ((temp >> 8) & 0x7F);
        data[7] = (byte) (temp & 0xFF);
        if (foodTemperature < 0) {
            data[6] |= 0x80;
        }

        temp = (short) Math.abs(bbqTemperature);
        data[8] = (byte) ((temp >> 8) & 0x7F);
        data[9] = (byte) (temp & 0xFF);
        if (bbqTemperature < 0) {
            data[8] |= 0x80;
        }

        data[10] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        State state;

        if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

                state = new DecimalType(signalLevel);

            } else if (valueSelector == RFXComValueSelector.BATTERY_LEVEL) {

                state = new DecimalType(batteryLevel);

            } else if (valueSelector == RFXComValueSelector.FOOD_TEMPERATURE) {

                state = new DecimalType(foodTemperature);

            } else if (valueSelector == RFXComValueSelector.BBQ_TEMPERATURE) {

                state = new DecimalType(bbqTemperature);

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
        return SUPPORTED_INPUT_VALUE_SELECTORS;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return SUPPORTED_OUTPUT_VALUE_SELECTORS;
    }
}
