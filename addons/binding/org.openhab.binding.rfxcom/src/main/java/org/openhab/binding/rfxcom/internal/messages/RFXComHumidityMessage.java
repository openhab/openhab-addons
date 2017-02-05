/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

import java.util.Arrays;
import java.util.List;

/**
 * RFXCOM data class for humidity message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComHumidityMessage extends RFXComBaseMessage {

    public enum SubType {
        HUM1(1),
        HUM2(2),

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

        public static SubType fromByte(int input) {
            for (SubType c : SubType.values()) {
                if (c.subType == input) {
                    return c;
                }
            }

            return SubType.UNKNOWN;
        }
    }

    public enum HumidityStatus {
        NORMAL(0),
        COMFORT(1),
        DRY(2),
        WET(3),

        UNKNOWN(255);

        private final int humidityStatus;

        HumidityStatus(int humidityStatus) {
            this.humidityStatus = humidityStatus;
        }

        HumidityStatus(byte humidityStatus) {
            this.humidityStatus = humidityStatus;
        }

        public byte toByte() {
            return (byte) humidityStatus;
        }

        public static HumidityStatus fromByte(int input) {
            for (HumidityStatus value : HumidityStatus.values()) {
                if (value.humidityStatus == input) {
                    return value;
                }
            }

            return HumidityStatus.UNKNOWN;
        }
    }

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays.asList(
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL, RFXComValueSelector.HUMIDITY,
            RFXComValueSelector.HUMIDITY_STATUS);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays.asList();

    public SubType subType = SubType.UNKNOWN;
    public int sensorId = 0;
    public byte humidity = 0;
    public HumidityStatus humidityStatus = HumidityStatus.UNKNOWN;
    public byte signalLevel = 0;
    public byte batteryLevel = 0;

    public RFXComHumidityMessage() {
        packetType = PacketType.HUMIDITY;
    }

    public RFXComHumidityMessage(byte[] data) {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Humidity = " + humidity;
        str += ", Humidity status = " + humidityStatus;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
        humidity = data[6];
        humidityStatus = HumidityStatus.fromByte(data[7]);
        signalLevel = (byte) ((data[8] & 0xF0) >> 4);
        batteryLevel = (byte) (data[8] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[9];

        data[0] = 0x08;
        data[1] = RFXComBaseMessage.PacketType.HUMIDITY.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);
        data[6] = humidity;
        data[7] = humidityStatus.toByte();
        data[8] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));// Janne

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

            } else if (valueSelector == RFXComValueSelector.HUMIDITY) {

                state = new DecimalType(humidity);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
            }

        } else if (valueSelector.getItemClass() == StringItem.class) {

            if (valueSelector == RFXComValueSelector.HUMIDITY_STATUS) {

                state = new StringType(humidityStatus.toString());

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to StringItem");
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
