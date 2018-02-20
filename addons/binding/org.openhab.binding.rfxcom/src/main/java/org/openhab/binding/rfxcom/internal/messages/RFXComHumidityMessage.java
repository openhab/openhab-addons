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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for humidity message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComHumidityMessage extends RFXComBatteryDeviceMessage<RFXComHumidityMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        HUM1(1),
        HUM2(2),
        HUM3(3);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    public enum HumidityStatus implements ByteEnumWrapper {
        NORMAL(0),
        COMFORT(1),
        DRY(2),
        WET(3);

        private final int humidityStatus;

        HumidityStatus(int humidityStatus) {
            this.humidityStatus = humidityStatus;
        }

        @Override
        public byte toByte() {
            return (byte) humidityStatus;
        }
    }

    public SubType subType;
    public int sensorId;
    public byte humidity;
    public HumidityStatus humidityStatus;

    public RFXComHumidityMessage() {
        super(PacketType.HUMIDITY);
    }

    public RFXComHumidityMessage(byte[] data) throws RFXComException {
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
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);
        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
        humidity = data[6];
        humidityStatus = fromByte(HumidityStatus.class, data[7]);
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
        data[8] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        switch (channelId) {
            case CHANNEL_HUMIDITY:
                return new DecimalType(humidity);

            case CHANNEL_HUMIDITY_STATUS:
                return new StringType(humidityStatus.toString());

            default:
                return super.convertToState(channelId);
        }
    }

    @Override
    public void setSubType(SubType subType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }
}
