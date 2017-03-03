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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for Current and Energy message.
 *
 * @author Damien Servant
 * @since 1.9.0
 */
public class RFXComCurrentEnergyMessage extends RFXComBaseMessage {
    private static final float TOTAL_USAGE_CONVERSION_FACTOR = 223.666F;

    public enum SubType {
        ELEC4(1); // OWL - CM180i

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

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays.asList(
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL, RFXComValueSelector.CHANNEL1_AMPS,
            RFXComValueSelector.CHANNEL2_AMPS, RFXComValueSelector.CHANNEL3_AMPS, RFXComValueSelector.TOTAL_USAGE);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays.asList();

    public SubType subType;
    public int sensorId;
    public byte count;
    public double channel1Amps;
    public double channel2Amps;
    public double channel3Amps;
    public double totalUsage;
    public byte signalLevel;
    public byte batteryLevel;

    public RFXComCurrentEnergyMessage() {
        packetType = PacketType.CURRENT_ENERGY;
    }

    public RFXComCurrentEnergyMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Id = " + sensorId;
        str += ", Count = " + count;
        str += ", Channel1 Amps = " + channel1Amps;
        str += ", Channel2 Amps = " + channel2Amps;
        str += ", Channel3 Amps = " + channel3Amps;
        str += ", Total Usage = " + totalUsage;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
        count = data[6];

        // Current = Field / 10
        channel1Amps = ((data[7] & 0xFF) << 8 | (data[8] & 0xFF)) / 10.0;
        channel2Amps = ((data[9] & 0xFF) << 8 | (data[10] & 0xFF)) / 10.0;
        channel3Amps = ((data[11] & 0xFF) << 8 | (data[12] & 0xFF)) / 10.0;

        totalUsage = ((long) (data[13] & 0xFF) << 40 | (long) (data[14] & 0xFF) << 32 | (data[15] & 0xFF) << 24
                | (data[16] & 0xFF) << 16 | (data[17] & 0xFF) << 8 | (data[18] & 0xFF));
        totalUsage = totalUsage / TOTAL_USAGE_CONVERSION_FACTOR;

        signalLevel = (byte) ((data[19] & 0xF0) >> 4);
        batteryLevel = (byte) (data[19] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[20];

        data[0] = (byte) (data.length - 1);
        data[1] = RFXComBaseMessage.PacketType.CURRENT_ENERGY.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);
        data[6] = count;

        data[7] = (byte) (((int) (channel1Amps * 10.0) >> 8) & 0xFF);
        data[8] = (byte) ((int) (channel1Amps * 10.0) & 0xFF);
        data[9] = (byte) (((int) (channel2Amps * 10.0) >> 8) & 0xFF);
        data[10] = (byte) ((int) (channel2Amps * 10.0) & 0xFF);
        data[11] = (byte) (((int) (channel3Amps * 10.0) >> 8) & 0xFF);
        data[12] = (byte) ((int) (channel3Amps * 10.0) & 0xFF);

        long totalUsageLoc = (long) (totalUsage * TOTAL_USAGE_CONVERSION_FACTOR);

        data[13] = (byte) ((totalUsageLoc >> 40) & 0xFF);
        data[14] = (byte) ((totalUsageLoc >> 32) & 0xFF);
        data[15] = (byte) ((totalUsageLoc >> 24) & 0xFF);
        data[16] = (byte) ((totalUsageLoc >> 16) & 0xFF);
        data[17] = (byte) ((totalUsageLoc >> 8) & 0xFF);
        data[18] = (byte) (totalUsageLoc & 0xFF);

        data[19] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {
        State state;
        if (valueSelector.getItemClass() == NumberItem.class) {
            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {
                state = new DecimalType(signalLevel);
            } else if (valueSelector == RFXComValueSelector.BATTERY_LEVEL) {
                state = new DecimalType(batteryLevel);
            } else if (valueSelector == RFXComValueSelector.CHANNEL1_AMPS) {
                state = new DecimalType(channel1Amps);
            } else if (valueSelector == RFXComValueSelector.CHANNEL2_AMPS) {
                state = new DecimalType(channel2Amps);
            } else if (valueSelector == RFXComValueSelector.CHANNEL3_AMPS) {
                state = new DecimalType(channel3Amps);
            } else if (valueSelector == RFXComValueSelector.TOTAL_USAGE) {
                state = new DecimalType(totalUsage);
            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
            }
        } else {
            throw new RFXComException("Can't convert " + valueSelector + " to " + valueSelector.getItemClass());
        }
        return state;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException {
        return supportedInputValueSelectors;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return supportedOutputValueSelectors;
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public Object convertSubType(String subType) throws RFXComException {

        for (RFXComBlinds1Message.SubType s : RFXComBlinds1Message.SubType.values()) {
            if (s.toString().equals(subType)) {
                return s;
            }
        }

        // try to find sub type by number
        try {
            return RFXComBlinds1Message.SubType.fromByte(Integer.parseInt(subType));
        } catch (NumberFormatException e) {
            throw new RFXComException("Unknown sub type " + subType);
        }
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
        this.subType = (SubType) subType;
    }
}