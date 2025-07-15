/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZonedDateTime;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.danfossairunit.internal.protocol.Parameter;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;

/**
 * The {@link DanfossAirUnit} class represents the air unit device and build the commands to be sent by
 * {@link DanfossAirUnitCommunicationController}
 * 
 * @author Ralf Duckstein - Initial contribution
 * @author Robert Bach - heavy refactorings
 * @author Jacob Laursen - Refactoring, bugfixes and enhancements
 */

@NonNullByDefault
public class DanfossAirUnit {

    private final CommunicationController communicationController;
    private final TimeZoneProvider timeZoneProvider;

    public DanfossAirUnit(CommunicationController communicationController, TimeZoneProvider timeZoneProvider) {
        this.communicationController = communicationController;
        this.timeZoneProvider = timeZoneProvider;
    }

    private boolean getBoolean(Parameter parameter) throws IOException {
        return communicationController.sendRobustRequest(parameter)[0] != 0;
    }

    private byte getByte(Parameter parameter) throws IOException {
        return communicationController.sendRobustRequest(parameter)[0];
    }

    private String getString(Parameter parameter) throws IOException {
        // length of the string is stored in the first byte
        byte[] result = communicationController.sendRobustRequest(parameter);
        return new String(result, 1, result[0], StandardCharsets.US_ASCII);
    }

    private void set(Parameter parameter, byte value) throws IOException {
        byte[] valueArray = { value };
        communicationController.sendRobustRequest(parameter, valueArray);
    }

    private short getShort(Parameter parameter) throws IOException, UnexpectedResponseValueException {
        byte[] result = communicationController.sendRobustRequest(parameter);
        if (result.length < 2) {
            throw new UnexpectedResponseValueException("Response too short: %d".formatted(result.length));
        }
        return (short) ((result[0] << 8) | (result[1] & 0xff));
    }

    private int getInt(Parameter parameter) throws IOException, UnexpectedResponseValueException {
        byte[] result = communicationController.sendRobustRequest(parameter);
        if (result.length < 4) {
            throw new UnexpectedResponseValueException("Response too short: %d".formatted(result.length));
        }
        return ((result[0] & 0xff) << 24) | ((result[1] & 0xff) << 16) | ((result[2] & 0xff) << 8) | (result[3] & 0xff);
    }

    private float getTemperature(Parameter parameter) throws IOException, UnexpectedResponseValueException {
        short shortTemp = getShort(parameter);
        float temp = ((float) shortTemp) / 100;
        if (temp <= -274 || temp > 100) {
            throw new UnexpectedResponseValueException("Invalid temperature: %s".formatted(temp));
        }
        return temp;
    }

    private Instant getTimestamp(Parameter parameter) throws IOException, UnexpectedResponseValueException {
        byte[] result = communicationController.sendRobustRequest(parameter);
        return asInstant(result);
    }

    private Instant asInstant(byte[] data) throws UnexpectedResponseValueException {
        if (data.length < 6) {
            throw new UnexpectedResponseValueException("Response too short: %d".formatted(data.length));
        }
        int second = data[0];
        int minute = data[1];
        int hour = data[2] & 0x1f;
        int day = data[3] & 0x1f;
        int month = data[4];
        int year = data[5] + 2000;
        try {
            return ZonedDateTime.of(year, month, day, hour, minute, second, 0, timeZoneProvider.getTimeZone())
                    .toInstant();
        } catch (DateTimeException e) {
            String msg = "Ignoring invalid timestamp %s.%s.%s %s:%s:%s".formatted(day, month, year, hour, minute,
                    second);
            throw new UnexpectedResponseValueException(msg, e);
        }
    }

    private static int asUnsignedByte(byte b) {
        return b & 0xFF;
    }

    private static float asPercentByte(byte b) {
        float f = asUnsignedByte(b);
        return f * 100 / 255;
    }

    public String getUnitName() throws IOException {
        return getString(Parameter.UNIT_NAME);
    }

    public String getUnitSerialNumber() throws IOException, UnexpectedResponseValueException {
        return String.valueOf(getShort(Parameter.UNIT_SERIAL));
    }

    public String getCCMSerialNumber() throws IOException, UnexpectedResponseValueException {
        return String.valueOf(getInt(Parameter.CCM_SERIAL_NUMBER));
    }

    public String getHardwareRevision() throws IOException, UnexpectedResponseValueException {
        return String.valueOf(getShort(Parameter.UNIT_HARDWARE_REVISION));
    }

    public String getSoftwareRevision() throws IOException, UnexpectedResponseValueException {
        return String.valueOf(getShort(Parameter.UNIT_SOFTWARE_REVISION));
    }

    public StringType getMode() throws IOException {
        return new StringType(Mode.values()[getByte(Parameter.MODE)].name());
    }

    public PercentType getManualFanStep() throws IOException, UnexpectedResponseValueException {
        byte value = getByte(Parameter.MANUAL_FAN_SPEED_STEP);
        if (value < 0 || value > 10) {
            throw new UnexpectedResponseValueException("Invalid fan step: %d".formatted(value));
        }
        return new PercentType(BigDecimal.valueOf(value * 10));
    }

    public QuantityType<Frequency> getSupplyFanSpeed() throws IOException, UnexpectedResponseValueException {
        return new QuantityType<>(BigDecimal.valueOf(getShort(Parameter.SUPPLY_FAN_SPEED)), Units.RPM);
    }

    public QuantityType<Frequency> getExtractFanSpeed() throws IOException, UnexpectedResponseValueException {
        return new QuantityType<>(BigDecimal.valueOf(getShort(Parameter.EXTRACT_FAN_SPEED)), Units.RPM);
    }

    public PercentType getSupplyFanStep() throws IOException {
        return new PercentType(BigDecimal.valueOf(getByte(Parameter.SUPPLY_FAN_STEP)));
    }

    public PercentType getExtractFanStep() throws IOException {
        return new PercentType(BigDecimal.valueOf(getByte(Parameter.EXTRACT_FAN_STEP)));
    }

    public OnOffType getBoost() throws IOException {
        return OnOffType.from(getBoolean(Parameter.BOOST));
    }

    public OnOffType getNightCooling() throws IOException {
        return OnOffType.from(getBoolean(Parameter.NIGHT_COOLING));
    }

    public OnOffType getDefrostStatus() throws IOException {
        return OnOffType.from(getBoolean(Parameter.DEFROST_STATUS));
    }

    public OnOffType getBypass() throws IOException {
        return OnOffType.from(getBoolean(Parameter.BYPASS));
    }

    public QuantityType<Dimensionless> getHumidity() throws IOException {
        BigDecimal value = BigDecimal.valueOf(asPercentByte(getByte(Parameter.HUMIDITY)));
        return new QuantityType<>(value.setScale(1, RoundingMode.HALF_UP), Units.PERCENT);
    }

    public QuantityType<Temperature> getRoomTemperature() throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(Parameter.ROOM_TEMPERATURE);
    }

    public QuantityType<Temperature> getRoomTemperatureCalculated()
            throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(Parameter.ROOM_TEMPERATURE_CALCULATED);
    }

    public QuantityType<Temperature> getOutdoorTemperature() throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(Parameter.OUTDOOR_TEMPERATURE);
    }

    public QuantityType<Temperature> getSupplyTemperature() throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(Parameter.SUPPLY_TEMPERATURE);
    }

    public QuantityType<Temperature> getExtractTemperature() throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(Parameter.EXTRACT_TEMPERATURE);
    }

    public QuantityType<Temperature> getExhaustTemperature() throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(Parameter.EXHAUST_TEMPERATURE);
    }

    private QuantityType<Temperature> getTemperatureAsDecimalType(Parameter parameter)
            throws IOException, UnexpectedResponseValueException {
        BigDecimal value = BigDecimal.valueOf(getTemperature(parameter));
        return new QuantityType<>(value.setScale(1, RoundingMode.HALF_UP), SIUnits.CELSIUS);
    }

    public DecimalType getBatteryLife() throws IOException {
        return new DecimalType(BigDecimal.valueOf(asUnsignedByte(getByte(Parameter.BATTERY_LIFE))));
    }

    public DecimalType getFilterLife() throws IOException {
        BigDecimal value = BigDecimal.valueOf(asPercentByte(getByte(Parameter.FILTER_LIFE)));
        return new DecimalType(value.setScale(1, RoundingMode.HALF_UP));
    }

    public DecimalType getFilterPeriod() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getByte(Parameter.FILTER_PERIOD)));
    }

    public DecimalType setFilterPeriod(Command cmd) throws IOException {
        return setNumberTypeRegister(cmd, Parameter.FILTER_PERIOD);
    }

    public DateTimeType getCurrentTime() throws IOException, UnexpectedResponseValueException {
        Instant timestamp = getTimestamp(Parameter.CURRENT_TIME);
        return new DateTimeType(timestamp);
    }

    public QuantityType<Time> getOperationTime() throws IOException, UnexpectedResponseValueException {
        return new QuantityType<>(getInt(Parameter.OPERATION_TIME), Units.MINUTE);
    }

    public DecimalType getPowerCycles() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getByte(Parameter.POWER_CYCLE_COUNTER)));
    }

    public PercentType setManualFanStep(Command cmd) throws IOException {
        return setPercentTypeRegister(cmd, Parameter.MANUAL_FAN_SPEED_STEP);
    }

    private DecimalType setNumberTypeRegister(Command cmd, Parameter parameter) throws IOException {
        if (cmd instanceof DecimalType decimalCommand) {
            byte value = (byte) decimalCommand.intValue();
            set(parameter, value);
        }
        return new DecimalType(BigDecimal.valueOf(getByte(parameter)));
    }

    private PercentType setPercentTypeRegister(Command cmd, Parameter parameter) throws IOException {
        if (cmd instanceof PercentType percentCommand) {
            byte value = (byte) ((percentCommand.intValue() + 5) / 10);
            set(parameter, value);
        }
        return new PercentType(BigDecimal.valueOf(getByte(parameter) * 10));
    }

    private OnOffType setOnOffTypeRegister(Command cmd, Parameter parameter) throws IOException {
        if (cmd instanceof OnOffType) {
            set(parameter, OnOffType.ON.equals(cmd) ? (byte) 1 : (byte) 0);
        }
        return OnOffType.from(getBoolean(parameter));
    }

    private StringType setStringTypeRegister(Command cmd, Parameter parameter) throws IOException {
        if (cmd instanceof StringType) {
            byte value = (byte) (Mode.valueOf(cmd.toString()).ordinal());
            set(parameter, value);
        }

        return new StringType(Mode.values()[getByte(parameter)].name());
    }

    public StringType setMode(Command cmd) throws IOException {
        return setStringTypeRegister(cmd, Parameter.MODE);
    }

    public OnOffType setBoost(Command cmd) throws IOException {
        return setOnOffTypeRegister(cmd, Parameter.BOOST);
    }

    public OnOffType setNightCooling(Command cmd) throws IOException {
        return setOnOffTypeRegister(cmd, Parameter.NIGHT_COOLING);
    }

    public OnOffType setBypass(Command cmd) throws IOException {
        return setOnOffTypeRegister(cmd, Parameter.BYPASS);
    }
}
