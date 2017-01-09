/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * RFXCOM data class for current message.
 *
 * @author Unknown - Initial contribution
 * @author Pauli Anttila - for the Similar RFXComEnergyMessage code
 * @author Jordan Cook - Added support for CURRENT devices, such as OWL CM113
 */
public class RFXComCurrentMessage extends RFXComBaseMessage {

    public enum SubType {
        UNSUPPORTED(0),
        ELEC1(1),
        UNKNOWN(255);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        SubType(byte subType) {
            this.subType = subType;
        }

        public byte toByte() {
            return (byte) subType;
        }
    }

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays.asList(
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL, RFXComValueSelector.CHANNEL_1_AMPS,
            RFXComValueSelector.CHANNEL_2_AMPS, RFXComValueSelector.CHANNEL_3_AMPS);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays.asList();

    public SubType subType = SubType.ELEC1;
    public int sensorId = 0;
    public byte count = 0;
    public double channel1Amps = 0;
    public double channel2Amps = 0;
    public double channel3Amps = 0;
    public byte signalLevel = 0;
    public byte batteryLevel = 0;

    public RFXComCurrentMessage() {
        packetType = PacketType.CURRENT;
    }

    public RFXComCurrentMessage(byte[] data) {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Count = " + count;
        str += ", Channel 1 Amps = " + channel1Amps;
        str += ", Channel 2 Amps = " + channel2Amps;
        str += ", Channel 3 Amps = " + channel3Amps;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) {

        super.encodeMessage(data);

        try {
            subType = SubType.values()[super.subType];
        } catch (Exception e) {
            subType = SubType.UNKNOWN;
        }

        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
        count = data[6];

        channel1Amps = ((data[7] & 0xFF) << 8 | (data[8] & 0xFF)) /10.0;
        channel2Amps = ((data[9] & 0xFF) << 8 | (data[10] & 0xFF)) /10.0;
        channel3Amps = ((data[11] & 0xFF) << 8 | (data[12] & 0xFF)) /10.0;


        signalLevel = (byte) ((data[13] & 0xF0) >> 4);
        batteryLevel = (byte) (data[13] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[13];

        data[0] = 0x11;
        data[1] = RFXComBaseMessage.PacketType.ENERGY.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;

        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);
        data[6] = count;

        data[7] = (byte) (((byte)channel1Amps*10 >> 8) & 0xFF);
        data[8] = (byte) ((byte)channel1Amps*10 & 0xFF);

        data[9] = (byte) (((byte)channel2Amps*10 >> 8) & 0xFF);
        data[10] = (byte) ((byte)channel2Amps*10 & 0xFF);

        data[11] = (byte) (((byte)channel3Amps*10 >> 8) & 0xFF);
        data[12] = (byte) ((byte)channel3Amps*10 & 0xFF);

        data[13] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

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
            } else if (valueSelector == RFXComValueSelector.BATTERY_LEVEL) {
                state = new DecimalType(batteryLevel);
            } else if (valueSelector == RFXComValueSelector.CHANNEL_1_AMPS) {
                state = new DecimalType(channel1Amps);
            } else if (valueSelector == RFXComValueSelector.CHANNEL_2_AMPS) {
                state = new DecimalType(channel2Amps);
            } else if (valueSelector == RFXComValueSelector.CHANNEL_3_AMPS) {
                state = new DecimalType(channel3Amps);
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

        // try to find sub type by number
        try {
            return SubType.values()[Integer.parseInt(subType)];
        } catch (Exception e) {
            throw new RFXComException("Unknown sub type " + subType);
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
