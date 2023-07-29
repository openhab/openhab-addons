/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.danfossairunit.internal.Commands.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    public DanfossAirUnit(CommunicationController communicationController) {
        this.communicationController = communicationController;
    }

    private boolean getBoolean(byte[] operation, byte[] register) throws IOException {
        return communicationController.sendRobustRequest(operation, register)[0] != 0;
    }

    private short getWord(byte[] operation, byte[] register) throws IOException {
        byte[] resultBytes = communicationController.sendRobustRequest(operation, register);
        return (short) ((resultBytes[0] << 8) | (resultBytes[1] & 0xFF));
    }

    private byte getByte(byte[] operation, byte[] register) throws IOException {
        return communicationController.sendRobustRequest(operation, register)[0];
    }

    private String getString(byte[] operation, byte[] register) throws IOException {
        // length of the string is stored in the first byte
        byte[] result = communicationController.sendRobustRequest(operation, register);
        return new String(result, 1, result[0], StandardCharsets.US_ASCII);
    }

    private void set(byte[] operation, byte[] register, byte value) throws IOException {
        byte[] valueArray = { value };
        communicationController.sendRobustRequest(operation, register, valueArray);
    }

    private short getShort(byte[] operation, byte[] register) throws IOException {
        byte[] result = communicationController.sendRobustRequest(operation, register);
        return (short) ((result[0] << 8) + (result[1] & 0xff));
    }

    private float getTemperature(byte[] operation, byte[] register)
            throws IOException, UnexpectedResponseValueException {
        short shortTemp = getShort(operation, register);
        float temp = ((float) shortTemp) / 100;
        if (temp <= -274 || temp > 100) {
            throw new UnexpectedResponseValueException(String.format("Invalid temperature: %s", temp));
        }
        return temp;
    }

    private ZonedDateTime getTimestamp(byte[] operation, byte[] register)
            throws IOException, UnexpectedResponseValueException {
        byte[] result = communicationController.sendRobustRequest(operation, register);
        return asZonedDateTime(result);
    }

    private ZonedDateTime asZonedDateTime(byte[] data) throws UnexpectedResponseValueException {
        int second = data[0];
        int minute = data[1];
        int hour = data[2] & 0x1f;
        int day = data[3] & 0x1f;
        int month = data[4];
        int year = data[5] + 2000;
        try {
            return ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.systemDefault());
        } catch (DateTimeException e) {
            String msg = String.format("Ignoring invalid timestamp %s.%s.%s %s:%s:%s", day, month, year, hour, minute,
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
        return getString(REGISTER_1_READ, UNIT_NAME);
    }

    public String getUnitSerialNumber() throws IOException {
        return String.valueOf(getShort(REGISTER_4_READ, UNIT_SERIAL));
    }

    public StringType getMode() throws IOException {
        return new StringType(Mode.values()[getByte(REGISTER_1_READ, MODE)].name());
    }

    public PercentType getManualFanStep() throws IOException, UnexpectedResponseValueException {
        byte value = getByte(REGISTER_1_READ, MANUAL_FAN_SPEED_STEP);
        if (value < 0 || value > 10) {
            throw new UnexpectedResponseValueException(String.format("Invalid fan step: %d", value));
        }
        return new PercentType(BigDecimal.valueOf(value * 10));
    }

    public DecimalType getSupplyFanSpeed() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getWord(REGISTER_4_READ, SUPPLY_FAN_SPEED)));
    }

    public DecimalType getExtractFanSpeed() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getWord(REGISTER_4_READ, EXTRACT_FAN_SPEED)));
    }

    public PercentType getSupplyFanStep() throws IOException {
        return new PercentType(BigDecimal.valueOf(getByte(REGISTER_4_READ, SUPPLY_FAN_STEP)));
    }

    public PercentType getExtractFanStep() throws IOException {
        return new PercentType(BigDecimal.valueOf(getByte(REGISTER_4_READ, EXTRACT_FAN_STEP)));
    }

    public OnOffType getBoost() throws IOException {
        return getBoolean(REGISTER_1_READ, BOOST) ? OnOffType.ON : OnOffType.OFF;
    }

    public OnOffType getNightCooling() throws IOException {
        return getBoolean(REGISTER_1_READ, NIGHT_COOLING) ? OnOffType.ON : OnOffType.OFF;
    }

    public OnOffType getBypass() throws IOException {
        return getBoolean(REGISTER_1_READ, BYPASS) ? OnOffType.ON : OnOffType.OFF;
    }

    public QuantityType<Dimensionless> getHumidity() throws IOException {
        BigDecimal value = BigDecimal.valueOf(asPercentByte(getByte(REGISTER_1_READ, HUMIDITY)));
        return new QuantityType<>(value.setScale(1, RoundingMode.HALF_UP), Units.PERCENT);
    }

    public QuantityType<Temperature> getRoomTemperature() throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(REGISTER_1_READ, ROOM_TEMPERATURE);
    }

    public QuantityType<Temperature> getRoomTemperatureCalculated()
            throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(REGISTER_0_READ, ROOM_TEMPERATURE_CALCULATED);
    }

    public QuantityType<Temperature> getOutdoorTemperature() throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(REGISTER_1_READ, OUTDOOR_TEMPERATURE);
    }

    public QuantityType<Temperature> getSupplyTemperature() throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(REGISTER_4_READ, SUPPLY_TEMPERATURE);
    }

    public QuantityType<Temperature> getExtractTemperature() throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(REGISTER_4_READ, EXTRACT_TEMPERATURE);
    }

    public QuantityType<Temperature> getExhaustTemperature() throws IOException, UnexpectedResponseValueException {
        return getTemperatureAsDecimalType(REGISTER_4_READ, EXHAUST_TEMPERATURE);
    }

    private QuantityType<Temperature> getTemperatureAsDecimalType(byte[] operation, byte[] register)
            throws IOException, UnexpectedResponseValueException {
        BigDecimal value = BigDecimal.valueOf(getTemperature(operation, register));
        return new QuantityType<>(value.setScale(1, RoundingMode.HALF_UP), SIUnits.CELSIUS);
    }

    public DecimalType getBatteryLife() throws IOException {
        return new DecimalType(BigDecimal.valueOf(asUnsignedByte(getByte(REGISTER_1_READ, BATTERY_LIFE))));
    }

    public DecimalType getFilterLife() throws IOException {
        BigDecimal value = BigDecimal.valueOf(asPercentByte(getByte(REGISTER_1_READ, FILTER_LIFE)));
        return new DecimalType(value.setScale(1, RoundingMode.HALF_UP));
    }

    public DecimalType getFilterPeriod() throws IOException {
        return new DecimalType(BigDecimal.valueOf(getByte(REGISTER_1_READ, FILTER_PERIOD)));
    }

    public DecimalType setFilterPeriod(Command cmd) throws IOException {
        return setNumberTypeRegister(cmd, FILTER_PERIOD);
    }

    public DateTimeType getCurrentTime() throws IOException, UnexpectedResponseValueException {
        ZonedDateTime timestamp = getTimestamp(REGISTER_1_READ, CURRENT_TIME);
        return new DateTimeType(timestamp);
    }

    public PercentType setManualFanStep(Command cmd) throws IOException {
        return setPercentTypeRegister(cmd, MANUAL_FAN_SPEED_STEP);
    }

    private DecimalType setNumberTypeRegister(Command cmd, byte[] register) throws IOException {
        if (cmd instanceof DecimalType) {
            byte value = (byte) ((DecimalType) cmd).intValue();
            set(REGISTER_1_WRITE, register, value);
        }
        return new DecimalType(BigDecimal.valueOf(getByte(REGISTER_1_READ, register)));
    }

    private PercentType setPercentTypeRegister(Command cmd, byte[] register) throws IOException {
        if (cmd instanceof PercentType) {
            byte value = (byte) ((((PercentType) cmd).intValue() + 5) / 10);
            set(REGISTER_1_WRITE, register, value);
        }
        return new PercentType(BigDecimal.valueOf(getByte(REGISTER_1_READ, register) * 10));
    }

    private OnOffType setOnOffTypeRegister(Command cmd, byte[] register) throws IOException {
        if (cmd instanceof OnOffType) {
            set(REGISTER_1_WRITE, register, OnOffType.ON.equals(cmd) ? (byte) 1 : (byte) 0);
        }
        return getBoolean(REGISTER_1_READ, register) ? OnOffType.ON : OnOffType.OFF;
    }

    private StringType setStringTypeRegister(Command cmd, byte[] register) throws IOException {
        if (cmd instanceof StringType) {
            byte value = (byte) (Mode.valueOf(cmd.toString()).ordinal());
            set(REGISTER_1_WRITE, register, value);
        }

        return new StringType(Mode.values()[getByte(REGISTER_1_READ, register)].name());
    }

    public StringType setMode(Command cmd) throws IOException {
        return setStringTypeRegister(cmd, MODE);
    }

    public OnOffType setBoost(Command cmd) throws IOException {
        return setOnOffTypeRegister(cmd, BOOST);
    }

    public OnOffType setNightCooling(Command cmd) throws IOException {
        return setOnOffTypeRegister(cmd, NIGHT_COOLING);
    }

    public OnOffType setBypass(Command cmd) throws IOException {
        return setOnOffTypeRegister(cmd, BYPASS);
    }
}
