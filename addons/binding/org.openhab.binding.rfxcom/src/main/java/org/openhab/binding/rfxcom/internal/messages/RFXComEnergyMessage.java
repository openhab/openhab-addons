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
 * RFXCOM data class for energy message.
 *
 * @author Unknown - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComEnergyMessage extends RFXComBaseMessage {

    private static final double TOTAL_USAGE_CONVERSION_FACTOR = 223.666d;
    private static final double WATTS_TO_AMPS_CONVERSION_FACTOR = 230d;

    public enum SubType {
        ELEC2(1),
        ELEC3(2);

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
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL, RFXComValueSelector.INSTANT_POWER,
            RFXComValueSelector.TOTAL_USAGE, RFXComValueSelector.INSTANT_AMPS, RFXComValueSelector.TOTAL_AMP_HOUR);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays.asList();

    public SubType subType;
    public int sensorId;
    public byte count;
    public double instantAmp;
    public double totalAmpHour;
    public double instantPower;
    public double totalUsage;
    public byte signalLevel;
    public byte batteryLevel;

    public RFXComEnergyMessage() {
        packetType = PacketType.ENERGY;
    }

    public RFXComEnergyMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Count = " + count;
        str += ", Instant Amps = " + instantAmp;
        str += ", Total Amp Hours = " + totalAmpHour;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;
        str += ", Instant Power = " + instantPower;
        str += ", Total Usage = " + totalUsage;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
        count = data[6];

        // all usage is reported in Watts based on 230V
        instantPower = ((data[7] & 0xFF) << 24 | (data[8] & 0xFF) << 16 | (data[9] & 0xFF) << 8 | (data[10] & 0xFF));
        totalUsage = ((long) (data[11] & 0xFF) << 40 | (long) (data[12] & 0xFF) << 32 | (data[13] & 0xFF) << 24
                | (data[14] & 0xFF) << 16 | (data[15] & 0xFF) << 8 | (data[16] & 0xFF)) / TOTAL_USAGE_CONVERSION_FACTOR;

        // convert to amps so external code can determine the watts based on local voltage
        instantAmp = instantPower / WATTS_TO_AMPS_CONVERSION_FACTOR;
        totalAmpHour = totalUsage / WATTS_TO_AMPS_CONVERSION_FACTOR;

        signalLevel = (byte) ((data[17] & 0xF0) >> 4);
        batteryLevel = (byte) (data[17] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[18];

        data[0] = 0x11;
        data[1] = RFXComBaseMessage.PacketType.ENERGY.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);
        data[6] = count;

        // convert our 'amp' values back into Watts since this is what comes back
        long instantUsage = (long) (instantAmp * WATTS_TO_AMPS_CONVERSION_FACTOR);
        long totalUsage = (long) (totalAmpHour * WATTS_TO_AMPS_CONVERSION_FACTOR * TOTAL_USAGE_CONVERSION_FACTOR);

        data[7] = (byte) ((instantUsage >> 24) & 0xFF);
        data[8] = (byte) ((instantUsage >> 16) & 0xFF);
        data[9] = (byte) ((instantUsage >> 8) & 0xFF);
        data[10] = (byte) (instantUsage & 0xFF);

        data[11] = (byte) ((totalUsage >> 40) & 0xFF);
        data[12] = (byte) ((totalUsage >> 32) & 0xFF);
        data[13] = (byte) ((totalUsage >> 24) & 0xFF);
        data[14] = (byte) ((totalUsage >> 16) & 0xFF);
        data[15] = (byte) ((totalUsage >> 8) & 0xFF);
        data[16] = (byte) (totalUsage & 0xFF);

        data[17] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

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

            } else if (valueSelector == RFXComValueSelector.INSTANT_POWER) {

                state = new DecimalType(instantPower);

            } else if (valueSelector == RFXComValueSelector.TOTAL_USAGE) {

                state = new DecimalType(totalUsage);

            } else if (valueSelector == RFXComValueSelector.INSTANT_AMPS) {

                state = new DecimalType(instantAmp);

            } else if (valueSelector == RFXComValueSelector.TOTAL_AMP_HOUR) {

                state = new DecimalType(totalAmpHour);

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
        return supportedInputValueSelectors;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return supportedOutputValueSelectors;
    }

}
