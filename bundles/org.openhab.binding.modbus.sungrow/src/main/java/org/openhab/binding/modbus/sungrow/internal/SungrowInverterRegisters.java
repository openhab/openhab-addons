/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal;

import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.INT16;
import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.INT32_SWAP;
import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.UINT16;
import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.UINT32_SWAP;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * The {@link SungrowInverterRegisters} is responsible for defining Modbus registers and their units.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public enum SungrowInverterRegisters {

    // the following register numbers are 1-based. They need to be converted before sending them on the wire.
    // Registers are duplicate to DAILY_PV_GENERATION / TOTAL_PV_GENERATION
    // DAILY_OUTPUT_ENERGY(5003, UINT16, ConversionConstants.DIV_BY_TEN, quantityTypeFactory(Units.KILOWATT_HOUR),
    // TOTAL_OUTPUT_ENERGY(5004, UINT32_SWAP, ConversionConstants.DIV_BY_TEN, quantityTypeFactory(Units.KILOWATT_HOUR),

    INTERNAL_TEMPERATURE(5008, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KELVIN),
            ConversionConstants.CELSIUS_TO_KELVIN, "overview"),
    MPPT1_VOLTAGE(5011, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "mppt-information"),
    MPPT1_CURRENT(5012, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.AMPERE), "mppt-information"),
    MPPT2_VOLTAGE(5013, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "mppt-information"),
    MPPT2_CURRENT(5014, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.AMPERE), "mppt-information"),
    TOTAL_DC_POWER(5017, UINT32_SWAP, BigDecimal.ONE, quantityFactory(Units.WATT), "overview"),
    PHASE_A_VOLTAGE(5019, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "overview"),
    PHASE_B_VOLTAGE(5020, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "overview"),
    PHASE_C_VOLTAGE(5021, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "overview"),

    REACTIVE_POWER(5033, INT32_SWAP, BigDecimal.ONE, quantityFactory(Units.VAR), "overview"),
    POWER_FACTOR(5035, INT16, new BigDecimal(BigInteger.ONE, 3), DecimalType::new, "overview"),
    GRID_FREQUENCY(5036, UINT16, new BigDecimal(BigInteger.ONE, 2), quantityFactory(Units.HERTZ), "overview"),

    /*
     * Not working
     * EXPORT_LIMIT_MIN(10, 5622, UINT16, quantityTypeFactory(Units.WATT)),
     * EXPORT_LIMIT_MAX(10, 5623, UINT16, quantityTypeFactory(Units.WATT)),
     * BDC_RATED_POWER(100, 5628, UINT16, quantityTypeFactory(Units.WATT)),
     * MAX_CHARGING_CURRENT(1, 5635, UINT16, quantityTypeFactory(Units.AMPERE)),
     * MAX_DISCHARGING_CURRENT(1, 5636, UINT16, quantityTypeFactory(Units.AMPERE)),
     * PV_POWER_TODAY(1, 6100, UINT16, quantityTypeFactory(Units.WATT)),
     * DAILY_PV_ENERGY_YIELDS(1, 6196, UINT16, quantityTypeFactory(Units.KILOWATT_HOUR)),
     * MONTHLY_PV_ENERGY_YIELDS(1, 9227, UINT16, quantityTypeFactory(Units.KILOWATT_HOUR)),
     */

    /**
     * Registers return invalid values.
     * SYSTEM_STATE(13000, UINT16, 1, quantityTypeFactory(Units.ONE)),
     * RUNNING_STATE(13001, UINT16, 1, quantityTypeFactory(Units.ONE)),
     */

    DAILY_PV_GENERATION(13002, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "overview"),
    TOTAL_PV_GENERATION(13003, UINT32_SWAP, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "overview"),
    DAILY_EXPORT_ENERGY_FROM_PV(13005, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "grid-information"),
    TOTAL_EXPORT_ENERGY_FROM_PV(13006, UINT32_SWAP, ConversionConstants.DIV_BY_TEN,
            quantityFactory(Units.KILOWATT_HOUR), "grid-information"),
    LOAD_POWER(13008, INT32_SWAP, BigDecimal.ONE, quantityFactory(Units.WATT), "load-information"),
    EXPORT_POWER(13010, INT32_SWAP, BigDecimal.ONE, quantityFactory(Units.WATT), "grid-information"),
    DAILY_BATTERY_CHARGE(13012, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "battery-information"),
    TOTAL_BATTERY_CHARGE(13013, UINT32_SWAP, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "battery-information"),
    /*
     * Not working
     * CO2_REDUCTION(13015, UINT32_SWAP, ConversionConstants.DIV_BY_TEN, tech.units.indriya.unit.Units.KILOGRAM),
     */
    DAILY_DIRECT_ENERGY_CONSUMPTION(13017, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "load-information"),
    TOTAL_DIRECT_ENERGY_CONSUMPTION(13018, UINT32_SWAP, ConversionConstants.DIV_BY_TEN,
            quantityFactory(Units.KILOWATT_HOUR), "load-information"),
    BATTERY_VOLTAGE(13020, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.VOLT), "battery-information"),
    BATTERY_CURRENT(13021, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.AMPERE),
            "battery-information"),
    BATTERY_POWER(13022, UINT16, BigDecimal.ONE, quantityFactory(Units.WATT), "battery-information"),
    BATTERY_LEVEL(13023, UINT16, ConversionConstants.DIV_BY_TEN, PercentType::new, "battery-information"),
    BATTERY_HEALTHY(13024, UINT16, ConversionConstants.DIV_BY_TEN, PercentType::new, "battery-information"),
    BATTERY_TEMPERATURE(13025, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KELVIN),
            ConversionConstants.CELSIUS_TO_KELVIN, "battery-information"),
    DAILY_BATTERY_DISCHARGE_ENERGY(13026, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "battery-information"),
    TOTAL_BATTERY_DISCHARGE_ENERGY(13027, UINT32_SWAP, ConversionConstants.DIV_BY_TEN,
            quantityFactory(Units.KILOWATT_HOUR), "battery-information"),
    SELF_CONSUMPTION_TODAY(13029, UINT16, ConversionConstants.DIV_BY_TEN, PercentType::new, "load-information"),
    // Not working
    // GRID_STATE(13030, UINT16, 1, quantityTypeFactory(Units.ONE, "grid-information"),
    PHASE_A_CURRENT(13031, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.AMPERE), "overview"),
    PHASE_B_CURRENT(13032, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.AMPERE), "overview"),
    PHASE_C_CURRENT(13033, INT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.AMPERE), "overview"),
    TOTAL_ACTIVE_POWER(13034, INT32_SWAP, BigDecimal.ONE, quantityFactory(Units.WATT), "overview"),
    DAILY_IMPORT_ENERGY(13036, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "grid-information"),
    TOTAL_IMPORT_ENERGY(13037, UINT32_SWAP, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "grid-information"),
    BATTERY_CAPACITY(13039, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "battery-information"),
    DAILY_CHARGE_ENERGY(13040, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "battery-information"),
    TOTAL_CHARGE_ENERGY(13041, UINT32_SWAP, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "battery-information"),
    // DRM_STATE(13043, UINT16, 1, quantityTypeFactory(Units.ONE, "channelGroup"),

    DAILY_EXPORT_ENERGY(13045, UINT16, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "grid-information"),
    TOTAL_EXPORT_ENERGY(13046, UINT32_SWAP, ConversionConstants.DIV_BY_TEN, quantityFactory(Units.KILOWATT_HOUR),
            "grid-information");

    /*
     * Status Registers -not known if working so not implemented yet.
     *
     * 
     * INVERTER_ALARM(13050, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * GRID_SIDE_FAULT(13052, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * SYSTEM_FAULT_1(13054, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * SYSTEM_FAULT_2(13056, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * DC_SIDE_FAULT(13058, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * PERMANENT_FAULT(13060, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * BDC_SIDE_FAULT(13062, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * BDC_SIDE_PERMANENT_FAULT(13064, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * BATTERY_FAULT(13066, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * BATTERY_ALARM(13068, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * BMS_ALARM_1(13070, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * BMS_PROTECTION(13072, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * BMS_FAULT_1(13074, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * BMS_FAULT_2(13076, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE),
     * BMS_ALARM_2(13078, UINT32_SWAP, 1, quantityTypeFactory(Units.ONE);
     */

    private final BigDecimal multiplier;
    private final int registerNumber;
    private final ValueType type;

    private final Function<BigDecimal, BigDecimal> conversion;
    private final Function<BigDecimal, State> stateFactory;
    private final String channelGroup;

    SungrowInverterRegisters(int registerNumber, ValueType type, BigDecimal multiplier,
            Function<BigDecimal, State> stateFactory, Function<BigDecimal, BigDecimal> conversion,
            String channelGroup) {
        this.multiplier = multiplier;
        this.registerNumber = registerNumber;
        this.type = type;
        this.conversion = conversion;
        this.stateFactory = stateFactory;
        this.channelGroup = channelGroup;
    }

    SungrowInverterRegisters(int registerNumber, ValueType type, BigDecimal multiplier,
            Function<BigDecimal, State> stateFactory, String channelGroup) {
        this.multiplier = multiplier;
        this.registerNumber = registerNumber;
        this.type = type;
        this.conversion = Function.identity();
        this.stateFactory = stateFactory;
        this.channelGroup = channelGroup;
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
     * Returns the modbus register number.
     *
     * @return modbus register number.
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
     * @return {@link State] for the given value.
     */
    public State createState(DecimalType registerValue) {
        final BigDecimal scaledValue = registerValue.toBigDecimal().multiply(this.multiplier);

        final BigDecimal convertedValue = conversion.apply(scaledValue);
        return this.stateFactory.apply(convertedValue);
    }
}
