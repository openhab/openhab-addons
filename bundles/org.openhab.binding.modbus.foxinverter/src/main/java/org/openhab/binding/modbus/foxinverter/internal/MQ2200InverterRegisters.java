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
package org.openhab.binding.modbus.foxinverter.internal;

import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * The {@link MQ2200InverterRegisters} is responsible for defining Modbus registers and their units.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public enum MQ2200InverterRegisters {
    // this temperature is shown in the app
    BATTERY_TEMPERATURE(37617, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(SIUnits.CELSIUS), "battery-information"),

    METER_CONNECTED(38801, UINT16, BigDecimal.ONE, contactFactory(), "overview"),

    // seems not useful yet, always 0
    // Thing data protocolVersion "Protocol version" [ readStart="39000", readValueType="uint32" ] // always 0

    // status is 1 during start and shutdown, 4 during normal operation, 0x40 on fault
    // (poweroff cannot be read, as it will shut down Modbus communication)
    HIDDEN_STATUS1(39063, UINT16, BigDecimal.ONE, DecimalType::new, "overview"),
    // STATUS3 seems to use only bit0, but it does not indicate the grid status properly
    // grid outage could be detected via GRID_FREQUENCY register
    // STATUS_ON_GRID(39065, UINT32, BigDecimal.ONE, contactFactory(), "status"),
    HIDDEN_ALARM1(39067, UINT16, BigDecimal.ONE, DecimalType::new, ""),
    HIDDEN_ALARM2(39068, UINT16, BigDecimal.ONE, DecimalType::new, ""),
    HIDDEN_ALARM3(39069, UINT16, BigDecimal.ONE, DecimalType::new, ""),

    // NOTE: this list needs to be sorted by register number!
    MPPT1_VOLTAGE(39070, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "mppt-information"),
    MPPT1_CURRENT(39071, UINT16, ConversionConstants.DIV_BY_HUNDRED, quantityFactory(Units.AMPERE), "mppt-information"),
    MPPT2_VOLTAGE(39072, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "mppt-information"),
    MPPT2_CURRENT(39073, UINT16, ConversionConstants.DIV_BY_HUNDRED, quantityFactory(Units.AMPERE), "mppt-information"),
    MPPT3_VOLTAGE(39074, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "mppt-information"),
    MPPT3_CURRENT(39075, UINT16, ConversionConstants.DIV_BY_HUNDRED, quantityFactory(Units.AMPERE), "mppt-information"),
    MPPT4_VOLTAGE(39076, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "mppt-information"),
    MPPT4_CURRENT(39077, UINT16, ConversionConstants.DIV_BY_HUNDRED, quantityFactory(Units.AMPERE), "mppt-information"),

    PV_POWER(39118, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "mppt-information"),

    HIDDEN_GRID_FREQUENCY(39139, UINT16, new BigDecimal(BigInteger.ONE, 2), quantityFactory(Units.HERTZ),
            "grid-information"),

    PHASE_A_VOLTAGE(39123, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "grid-information"),
    PHASE_B_VOLTAGE(39124, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "grid-information"),
    PHASE_C_VOLTAGE(39125, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "grid-information"),

    // INVERTER_x_CURRENT 39126, 39128, 39130 seem not to work for MQ2200

    HIDDEN_INVERTER_POWER(39134, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "overview"),

    // seems not to work for MQ2200
    // REACTIVE_POWER(39136, INT32, BigDecimal.ONE, quantityFactory(Units.VAR), "overview"),
    // POWER_FACTOR(39138, INT16, ConversionConstants.DIV_BY_THOUSAND, DecimalType::new, "overview"),
    INTERNAL_TEMPERATURE(39141, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(SIUnits.CELSIUS), "overview"),

    TOTAL_PV_GENERATION(39149, UINT32, ConversionConstants.DIV_BY_HUNDRED, quantityFactory(Units.KILOWATT_HOUR),
            "overview"),
    DAILY_PV_GENERATION(39151, UINT32, ConversionConstants.DIV_BY_HUNDRED, quantityFactory(Units.KILOWATT_HOUR),
            "overview"),

    HOME_IMPORT_POWER(39162, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "overview"),
    GRID_EXPORT_POWER(39168, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "grid-information"),

    EPS_EXPORT_POWER(39216, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "emergency-power-supply"),

    BATTERY_VOLTAGE(39227, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "battery-information"),
    BATTERY_CURRENT(39228, INT32, ConversionConstants.DIV_BY_THOUSAND, quantityFactory(Units.AMPERE),
            "battery-information"),

    // prefer combined power over bat1 power
    BATTERY_CHARGING_POWER(39237, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "battery-information"),

    MPPT1_POWER(39279, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "mppt-information"),
    MPPT2_POWER(39281, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "mppt-information"),
    MPPT3_POWER(39283, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "mppt-information"),
    MPPT4_POWER(39285, INT32, BigDecimal.ONE, quantityFactory(Units.WATT), "mppt-information"),

    BATTERY_LEVEL(39424, UINT16, BigDecimal.ONE, percentFactory(), "battery-information"),

    // TODO implement remote control, registers 46001-46007

    BATTERY_MINIMUM_SOC(46609, UINT16, BigDecimal.ONE, percentFactory(), "battery-information"),
    BATTERY_MAXIMUM_SOC(46610, UINT16, BigDecimal.ONE, percentFactory(), "battery-information"),
    BATTERY_MINIMUM_SOC_ON_GRID(46611, UINT16, BigDecimal.ONE, percentFactory(), "battery-information"),

    // 0:off 2:on, special handling in MQ2200InverterHandler
    HIDDEN_EPS_OUTPUT(46613, UINT16, BigDecimal.ONE, DecimalType::new, "emergency-power-supply");

    // does not work, always returns 7
    // WORK_MODE(49203, UINT16, BigDecimal.ONE, DecimalType::new, "overview"),

    // some registers in between cannot be read and will make the Modbus request fail
    // BLOCKER(MQ2200InverterHandler.ENFORCE_NEW_REQUEST, UINT16, BigDecimal.ONE, DecimalType::new, ""),
    // does not change during manual shutdown using the button on the device
    // SYSTEM_POWER_STATE(49228, UINT16, BigDecimal.ONE, DecimalType::new, "overview"),
    // idle state seems to be 0
    // IDLE_STATE(49229, UINT16, BigDecimal.ONE, DecimalType::new, "overview"),
    // IDLE_LOAD_POWER_THRESHOLD(49230, UINT16, BigDecimal.ONE, DecimalType::new, "overview");

    private final BigDecimal multiplier;
    private final int registerNumber;
    private final ValueType type;

    private final Function<BigDecimal, BigDecimal> conversion;
    private final Function<BigDecimal, State> stateFactory;
    private final String channelGroup;

    MQ2200InverterRegisters(int registerNumber, ValueType type, BigDecimal multiplier,
            Function<BigDecimal, State> stateFactory, Function<BigDecimal, BigDecimal> conversion,
            String channelGroup) {
        this.multiplier = multiplier;
        this.registerNumber = registerNumber;
        this.type = type;
        this.conversion = conversion;
        this.stateFactory = stateFactory;
        this.channelGroup = channelGroup;
    }

    MQ2200InverterRegisters(int registerNumber, ValueType type, BigDecimal multiplier,
            Function<BigDecimal, State> stateFactory, String channelGroup) {
        this.multiplier = multiplier;
        this.registerNumber = registerNumber;
        this.type = type;
        this.conversion = Function.identity();
        this.stateFactory = stateFactory;
        this.channelGroup = channelGroup;
    }

    private static Function<BigDecimal, State> contactFactory() {
        return (BigDecimal value) -> value.intValue() == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
    }

    private static Function<BigDecimal, State> percentFactory() {
        return (BigDecimal value) -> new QuantityType<>(value, Units.PERCENT);
    }

    /**
     * Creates a Function that creates {@link QuantityType} states with the given {@link Unit}.
     *
     * @param unit {@link Unit} to be used for the value.
     * @return Function for value creation.
     */
    private static Function<BigDecimal, State> quantityFactory(Unit<?> unit) {
        return (BigDecimal value) -> new QuantityType<>(value, unit);
    }

    /**
     * Returns the Modbus register number.
     *
     * @return Modbus register number.
     */
    public int getRegisterNumber() {
        return registerNumber;
    }

    /**
     * Returns the {@link ValueType} for the channel.
     *
     * @return {@link ValueType} for the channel.
     */
    public ValueType getType() {
        return type;
    }

    /**
     * Returns the count of registers read to return the value of this register.
     *
     * @return register count.
     */
    public int getRegisterCount() {
        return this.type.getBits() / 16;
    }

    /**
     * Returns the channel group.
     *
     * @return channel group id.
     */
    public String getChannelGroup() {
        return channelGroup;
    }

    /**
     * Returns the channel name.
     *
     * @return the channel name.
     */
    public String getChannelName() {
        return this.name().toLowerCase().replace('_', '-');
    }

    /**
     * Creates the {@link State} for the given register value.
     *
     * @param registerValue the value for the channel.
     * @return {@link State} for the given value.
     */
    public State createState(DecimalType registerValue) {
        final BigDecimal scaledValue = registerValue.toBigDecimal().multiply(this.multiplier);

        final BigDecimal convertedValue = conversion.apply(scaledValue);
        return this.stateFactory.apply(convertedValue);
    }
}
