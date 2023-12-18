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
package org.openhab.binding.modbus.sungrow.internal;

import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.INT16;
import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.INT32_SWAP;
import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.UINT16;
import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.UINT32_SWAP;

import java.math.BigDecimal;
import java.util.function.Function;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.library.unit.Units;

/**
 * The {@link SungrowInverterRegisters} is responsible for defining Modbus registers and their units.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public enum SungrowInverterRegisters {

    // the following register numbers are 1-based. They need to be converted before sending them on the wire.
    DAILY_OUTPUT_ENERGY(5003, UINT16, 0.1f, Units.KILOWATT_HOUR),
    TOTAL_OUTPUT_ENERGY(5004, UINT32_SWAP, 0.1f, Units.KILOWATT_HOUR),
    INTERNAL_TEMPERATURE(5008, INT16, 0.1f, Units.KELVIN, ConversionConstants.CELSIUS_TO_KELVIN),
    MPPT1_VOLTAGE(5011, UINT16, 0.1f, Units.VOLT),
    MPPT1_CURRENT(5012, UINT16, 0.1f, Units.AMPERE),
    MPPT2_VOLTAGE(5013, UINT16, 0.1f, Units.VOLT),
    MPPT2_CURRENT(5014, UINT16, 0.1f, Units.AMPERE),
    TOTAL_DC_POWER(5017, UINT32_SWAP, 1, Units.WATT),
    PHASE_A_VOLTAGE(5019, UINT16, 0.1f, Units.VOLT),
    PHASE_B_VOLTAGE(5020, UINT16, 0.1f, Units.VOLT),
    PHASE_C_VOLTAGE(5021, UINT16, 0.1f, Units.VOLT),

    REACTIVE_POWER(5033, INT32_SWAP, 1, Units.VAR),
    POWER_FACTOR(5035, INT16, 0.001f, Units.ONE),
    GRID_FREQUENCY(5036, UINT16, 0.01f, Units.HERTZ),

    /**
     * Not working
     * EXPORT_LIMIT_MIN(10, 5622, UINT16, Units.WATT),
     * EXPORT_LIMIT_MAX(10, 5623, UINT16, Units.WATT),
     * BDC_RATED_POWER(100, 5628, UINT16, Units.WATT),
     * MAX_CHARGING_CURRENT(1, 5635, UINT16, Units.AMPERE),
     * MAX_DISCHARGING_CURRENT(1, 5636, UINT16, Units.AMPERE),
     * PV_POWER_TODAY(1, 6100, UINT16, Units.WATT),
     * DAILY_PV_ENERGY_YIELDS(1, 6196, UINT16, Units.KILOWATT_HOUR),
     * MONTHLY_PV_ENERGY_YIELDS(1, 9227, UINT16, Units.KILOWATT_HOUR),
     */

    SYSTEM_STATE(13000, UINT16, 1, Units.ONE),
    RUNNING_STATE(13001, UINT16, 1, Units.ONE),
    DAILY_PV_GENERATION(13002, UINT16, 0.1f, Units.KILOWATT_HOUR),
    TOTAL_PV_GENERATION(13003, UINT32_SWAP, 0.1f, Units.KILOWATT_HOUR),
    DAILY_EXPORT_POWER_FROM_PV(13005, UINT16, 100, Units.WATT),
    TOTAL_EXPORT_ENERGY_FROM_PV(13006, UINT32_SWAP, 0.1f, Units.KILOWATT_HOUR),
    LOAD_POWER(13008, INT32_SWAP, 1, Units.WATT),
    EXPORT_POWER(13010, INT32_SWAP, 1, Units.WATT),
    DAILY_BATTERY_CHARGE(13012, UINT16, 0.1f, Units.KILOWATT_HOUR),
    TOTAL_BATTERY_CHARGE(13013, UINT32_SWAP, 0.1f, Units.KILOWATT_HOUR),
    CO2_REDUCTION(13015, UINT32_SWAP, 0.1f, tech.units.indriya.unit.Units.KILOGRAM),
    DAILY_DIRECT_ENERGY_CONSUMPTION(13017, UINT16, 0.1f, Units.KILOWATT_HOUR),
    TOTAL_DIRECT_ENERGY_CONSUMPTION(13018, UINT32_SWAP, 0.1f, Units.KILOWATT_HOUR),
    BATTERY_VOLTAGE(13020, UINT16, 0.1f, Units.VOLT),
    BATTERY_CURRENT(13021, UINT16, 0.1f, Units.AMPERE),
    BATTERY_POWER(13022, UINT16, 1, Units.WATT),
    BATTERY_LEVEL(13023, UINT16, 0.1f, Units.PERCENT),
    BATTERY_HEALTHY(13024, UINT16, 0.1f, Units.PERCENT),
    BATTERY_TEMPERATUR(13025, INT16, 0.1f, Units.KELVIN, ConversionConstants.CELSIUS_TO_KELVIN),
    DAILY_BATTERY_DISCHARGE_ENERGY(13026, UINT16, 0.1f, Units.KILOWATT_HOUR),
    TOTAL_BATTERY_DISCHARGE_ENERGY(13027, UINT32_SWAP, 0.1f, Units.KILOWATT_HOUR),
    SELF_CONSUMPTION_TODAY(13029, UINT16, 0.1f, Units.PERCENT),
    GRID_STATE(13030, UINT16, 1, Units.ONE),
    PHASE_A_CURRENT(13031, INT16, 0.1f, Units.AMPERE),
    PHASE_B_CURRENT(13032, INT16, 0.1f, Units.AMPERE),
    PHASE_C_CURRENT(13033, INT16, 0.1f, Units.AMPERE),
    TOTAL_ACTIVE_POWER(13034, INT32_SWAP, 1, Units.WATT),
    DAILY_IMPORT_ENERGY(13036, UINT16, 0.1f, Units.KILOWATT_HOUR),
    TOTAL_IMPORT_ENERGY(13037, UINT32_SWAP, 0.1f, Units.KILOWATT_HOUR),
    BATTERY_CAPACITY(13039, UINT16, 0.1f, Units.KILOWATT_HOUR),
    DAILY_CHARGE_ENERGY(13040, UINT16, 0.1f, Units.KILOWATT_HOUR),
    TOTAL_CHARGE_ENERGY(13041, UINT32_SWAP, 0.1f, Units.KILOWATT_HOUR),
    DRM_STATE(13043, UINT16, 1, Units.ONE),

    DAILY_EXPORT_ENERGY(13045, UINT16, 0.1f, Units.KILOWATT_HOUR),
    TOTAL_EXPORT_ENERGY(13046, UINT32_SWAP, 0.1f, Units.KILOWATT_HOUR),

    INVERTER_ALARM(13050, UINT32_SWAP, 1, Units.ONE),
    GRID_SIDE_FAULT(13052, UINT32_SWAP, 1, Units.ONE),
    SYSTEM_FAULT_1(13054, UINT32_SWAP, 1, Units.ONE),
    SYSTEM_FAULT_2(13056, UINT32_SWAP, 1, Units.ONE),
    DC_SIDE_FAULT(13058, UINT32_SWAP, 1, Units.ONE),
    PERMANENT_FAULT(13060, UINT32_SWAP, 1, Units.ONE),
    BDC_SIDE_FAULT(13062, UINT32_SWAP, 1, Units.ONE),
    BDC_SIDE_PERMANENT_FAULT(13064, UINT32_SWAP, 1, Units.ONE),
    BATTERY_FAULT(13066, UINT32_SWAP, 1, Units.ONE),
    BATTERY_ALARM(13068, UINT32_SWAP, 1, Units.ONE),
    BMS_ALARM_1(13070, UINT32_SWAP, 1, Units.ONE),
    BMS_PROTECTION(13072, UINT32_SWAP, 1, Units.ONE),
    BMS_FAULT_1(13074, UINT32_SWAP, 1, Units.ONE),
    BMS_FAULT_2(13076, UINT32_SWAP, 1, Units.ONE),
    BMS_ALARM_2(13078, UINT32_SWAP, 1, Units.ONE);

    private final BigDecimal multiplier;
    private final int registerNumber;
    private final ValueType type;
    private final Unit<?> unit;

    private final Function<BigDecimal, BigDecimal> conversion;

    SungrowInverterRegisters(int registerNumber, ValueType type, float multiplier, Unit<?> unit,
            Function<BigDecimal, BigDecimal> conversion) {
        this.multiplier = new BigDecimal(multiplier);
        this.registerNumber = registerNumber;
        this.type = type;
        this.unit = unit;
        this.conversion = conversion;
    }

    private SungrowInverterRegisters(int registerNumber, ValueType type, float multiplier, Unit<?> unit) {
        this.multiplier = new BigDecimal(multiplier);
        this.registerNumber = registerNumber;
        this.type = type;
        this.unit = unit;
        this.conversion = Function.identity();
    }

    public Unit<?> getUnit() {
        return unit;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }

    public ValueType getType() {
        return type;
    }

    /**
     * Returns the count of registers read to return the value of this register.
     */
    public int getRegisterCount() {
        return this.type.getBits() / 16;
    }

    /**
     * Returns the value conversion.
     */
    public Function<BigDecimal, BigDecimal> getConversion() {
        return conversion;
    }
}
