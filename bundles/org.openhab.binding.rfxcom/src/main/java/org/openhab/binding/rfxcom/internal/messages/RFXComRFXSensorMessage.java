/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.rfxcom.internal.messages;

import static java.math.RoundingMode.*;
import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.messages.ByteEnumUtil.fromByte;

import java.math.BigDecimal;

import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add support for the rfx-sensor
 *
 * @author Martin van Wingerden - Initial contribution
 */
public class RFXComRFXSensorMessage extends RFXComDeviceMessageImpl<RFXComRFXSensorMessage.SubType> {
    private final Logger logger = LoggerFactory.getLogger(RFXComRFXSensorMessage.class);

    private static final BigDecimal PRESSURE_ADDITION = new BigDecimal("0.095");
    private static final BigDecimal PRESSURE_DIVIDER = new BigDecimal("0.0009");

    private static final BigDecimal HUMIDITY_VOLTAGE_SUBTRACTION = new BigDecimal("0.16");
    private static final BigDecimal HUMIDITY_VOLTAGE_DIVIDER = new BigDecimal("0.0062");
    private static final BigDecimal HUMIDITY_TEMPERATURE_CORRECTION = new BigDecimal("1.0546");
    private static final BigDecimal HUMIDITY_TEMPERATURE_MULTIPLIER = new BigDecimal("0.00216");

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public enum SubType implements ByteEnumWrapper {
        TEMPERATURE(0),
        A_D(1),
        VOLTAGE(2),
        MESSAGE(3);

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
    private Double temperature;
    private BigDecimal miliVoltageTimesTen;
    public byte signalLevel;

    public RFXComRFXSensorMessage() {
        super(PacketType.RFXSENSOR);
    }

    public RFXComRFXSensorMessage(byte[] data) throws RFXComException {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = super.toString();

        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Temperature = " + temperature;
        str += ", Voltage = " + getVoltage();
        str += ", Signal level = " + signalLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) throws RFXComException {
        super.encodeMessage(data);

        subType = fromByte(SubType.class, super.subType);

        sensorId = (data[4] & 0xFF);

        byte msg1 = data[5];
        byte msg2 = data[6];

        switch (subType) {
            case TEMPERATURE:
                encodeTemperatureMessage(msg1, msg2);
                break;
            case A_D:
            case VOLTAGE:
                encodeVoltageMessage(msg1, msg2);
                break;
            case MESSAGE:
                encodeStatusMessage(msg2);
                break;
        }

        signalLevel = (byte) ((data[7] & 0xF0) >> 4);
    }

    private void encodeTemperatureMessage(byte msg1, byte msg2) {
        temperature = (short) ((msg1 & 0x7F) << 8 | (msg2 & 0xFF)) * 0.01;
        if ((msg1 & 0x80) != 0) {
            temperature = -temperature;
        }
    }

    private void encodeVoltageMessage(byte msg1, byte msg2) {
        miliVoltageTimesTen = BigDecimal.valueOf((short) ((msg1 & 0xFF) << 8 | (msg2 & 0xFF)));
    }

    private void encodeStatusMessage(byte msg2) {
        // noop
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[8];

        data[0] = 0x07;
        data[1] = PacketType.RFXSENSOR.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) (sensorId & 0x00FF);

        if (subType == SubType.TEMPERATURE) {
            decodeTemperatureMessage(data);
        } else if (subType == SubType.A_D) {
            decodeVoltageMessage(data);
        } else if (subType == SubType.VOLTAGE) {
            decodeVoltageMessage(data);
        } else if (subType == SubType.MESSAGE) {
            decodeStatusMessage(data);
        }

        data[7] = (byte) ((signalLevel & 0x0F) << 4);

        return data;
    }

    @Override
    public void convertFromState(String channelId, Type type) {
        throw new UnsupportedOperationException();
    }

    private void decodeTemperatureMessage(byte[] data) {
        short temp = (short) Math.abs(temperature * 100);
        data[5] = (byte) ((temp >> 8) & 0xFF);
        data[6] = (byte) (temp & 0xFF);
        if (temperature < 0) {
            data[5] |= 0x80;
        }
    }

    private void decodeVoltageMessage(byte[] data) {
        short miliVoltageTimesTenShort = this.miliVoltageTimesTen.shortValueExact();
        data[5] = (byte) ((miliVoltageTimesTenShort >> 8) & 0xFF);
        data[6] = (byte) (miliVoltageTimesTenShort & 0xFF);
    }

    private void decodeStatusMessage(byte[] data) {
        logger.info("A status message was received {}", HexUtils.bytesToHex(data));
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public SubType convertSubType(String subType) throws RFXComUnsupportedValueException {
        return ByteEnumUtil.convertSubType(SubType.class, subType);
    }

    @Override
    public void setSubType(SubType subType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public State convertToState(String channelId, RFXComDeviceConfiguration config, DeviceState deviceState)
            throws RFXComUnsupportedChannelException, RFXComInvalidStateException {
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                return subType == SubType.TEMPERATURE ? getTemperature() : null;

            case CHANNEL_VOLTAGE:
                return subType == SubType.A_D ? handleVoltage() : null;

            case CHANNEL_REFERENCE_VOLTAGE:
                return subType == SubType.VOLTAGE ? handleVoltage() : null;

            case CHANNEL_HUMIDITY:
                return subType == SubType.A_D ? handleHumidity(deviceState) : null;

            case CHANNEL_PRESSURE:
                return subType == SubType.A_D ? handlePressure(deviceState) : null;

            default:
                return super.convertToState(channelId, config, deviceState);
        }
    }

    private State getTemperature() {
        if (temperature != null) {
            return new DecimalType(temperature);
        } else {
            return UnDefType.UNDEF;
        }
    }

    private State handleVoltage() {
        if (miliVoltageTimesTen != null) {
            return new DecimalType(getVoltage());
        } else {
            return UnDefType.UNDEF;
        }
    }

    private State handleHumidity(DeviceState deviceState) {
        DecimalType temperatureState = (DecimalType) deviceState.getLastState(CHANNEL_TEMPERATURE);
        Type referenceVoltageState = deviceState.getLastState(CHANNEL_REFERENCE_VOLTAGE);
        BigDecimal adVoltage = getVoltage();

        if (temperatureState == null || referenceVoltageState == null || adVoltage == null) {
            return null;
        }

        if (!(referenceVoltageState instanceof DecimalType)) {
            return UnDefType.UNDEF;
        }

        BigDecimal temperature = temperatureState.toBigDecimal();
        BigDecimal supplyVoltage = ((DecimalType) referenceVoltageState).toBigDecimal();

        // RH = (((A/D voltage / supply voltage) - 0.16) / 0.0062) / (1.0546 - 0.00216 * temperature)
        BigDecimal belowTheDivider = adVoltage.divide(supplyVoltage, 4, HALF_DOWN)
                .subtract(HUMIDITY_VOLTAGE_SUBTRACTION).divide(HUMIDITY_VOLTAGE_DIVIDER, 4, HALF_DOWN);
        BigDecimal underTheDivider = HUMIDITY_TEMPERATURE_CORRECTION
                .subtract(HUMIDITY_TEMPERATURE_MULTIPLIER.multiply(temperature));

        return new DecimalType(belowTheDivider.divide(underTheDivider, 4, HALF_DOWN));
    }

    private State handlePressure(DeviceState deviceState) {
        DecimalType referenceVoltageState = (DecimalType) deviceState.getLastState(CHANNEL_REFERENCE_VOLTAGE);
        BigDecimal adVoltage = getVoltage();

        if (referenceVoltageState == null || adVoltage == null) {
            return null;
        }

        BigDecimal supplyVoltage = referenceVoltageState.toBigDecimal();

        // hPa = ((A/D voltage / supply voltage) + 0.095) / 0.0009
        return new DecimalType((adVoltage.divide(supplyVoltage, 4, HALF_DOWN).add(PRESSURE_ADDITION))
                .divide(PRESSURE_DIVIDER, 4, HALF_DOWN));
    }

    private BigDecimal getVoltage() {
        if (miliVoltageTimesTen == null) {
            return null;
        }
        return miliVoltageTimesTen.divide(ONE_HUNDRED, 100, CEILING);
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new RFXComException("Not supported");
    }
}
