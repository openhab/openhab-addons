/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.RFXComBindingConstants.CHANNEL_DATE_TIME;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * RFXCOM data class for Date and Time message.
 *
 * @author Damien Servant
 * @since 1.9.0
 */
public class RFXComDateTimeMessage extends RFXComBatteryDeviceMessage<RFXComDateTimeMessage.SubType> {

    public enum SubType implements ByteEnumWrapper {
        RTGR328N(1);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        @Override
        public byte toByte() {
            return (byte) subType;
        }
    }

    public SubType subType;
    public int sensorId;
    String dateTime;
    private int year;
    private int month;
    private int day;
    private int dayOfWeek;
    private int hour;
    private int minute;
    private int second;

    public RFXComDateTimeMessage() {
        super(PacketType.DATE_TIME);
    }

    public RFXComDateTimeMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = super.toString();

        str += ", Sub type = " + subType;
        str += ", Id = " + sensorId;
        str += ", Date Time = " + dateTime;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);

        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

        year = data[6] & 0xFF;
        month = data[7] & 0xFF;
        day = data[8] & 0xFF;
        dayOfWeek = data[9] & 0xFF;
        hour = data[10] & 0xFF;
        minute = data[11] & 0xFF;
        second = data[12] & 0xFF;

        dateTime = String.format("20%02d-%02d-%02dT%02d:%02d:%02d", year, month, day, hour, minute, second);

        signalLevel = (byte) ((data[13] & 0xF0) >> 4);
        batteryLevel = (byte) (data[13] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[14];

        data[0] = (byte) (data.length - 1);
        data[1] = RFXComBaseMessage.PacketType.DATE_TIME.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);
        data[6] = (byte) (year & 0x00FF);
        data[7] = (byte) (month & 0x00FF);
        data[8] = (byte) (day & 0x00FF);
        data[9] = (byte) (dayOfWeek & 0x00FF);
        data[10] = (byte) (hour & 0x00FF);
        data[11] = (byte) (minute & 0x00FF);
        data[12] = (byte) (second & 0x00FF);
        data[13] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public State convertToState(String channelId) throws RFXComUnsupportedChannelException {
        if (channelId.equals(CHANNEL_DATE_TIME)) {
            return new DateTimeType(dateTime);
        } else {
            return super.convertToState(channelId);
        }
    }

    @Override
    public void convertFromState(String channelId, Type type) throws RFXComUnsupportedChannelException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public void setDeviceId(String deviceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }

    @Override
    public void setSubType(SubType subType) {
        this.subType = subType;
    }
}
