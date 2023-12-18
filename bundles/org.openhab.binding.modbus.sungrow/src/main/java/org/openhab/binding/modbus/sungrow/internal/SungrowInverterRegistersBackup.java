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
import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.INT32;
import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.UINT16;
import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.UINT32;

import java.math.BigDecimal;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.library.unit.Units;

/**
 * The {@link SungrowInverterRegistersBackup} is responsible for defining Modbus registers and their units.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public enum SungrowInverterRegistersBackup {

    // the following register numbers are 1-based. They need to be converted before sending them on the wire.
    NOMINAL_ACTIVE_POWER(100, 5001, UINT16, Units.WATT),
    DAILY_POWER_YIELDS(0.1f, 5003, UINT16, Units.KILOWATT_HOUR),
    TOTAL_POWER_YIELDS(1, 5004, UINT32, Units.KILOWATT_HOUR),
    TOTAL_RUNNING_TIME(0.1f, 5006, UINT32, Units.HOUR),
    INTERNAL_TEMPERATURE(0.1f, 5008, INT16, Units.KELVIN), // TODO: Umrechnung C in K + 273,15
    TOTAL_APPARENT_POWER(1, 5009, UINT32, Units.VOLT_AMPERE),
    MPPT1_VOLTAGE(0.1f, 5011, UINT16, Units.VOLT),
    MPPT1_CURRENT(0.1f, 5012, UINT16, Units.AMPERE),
    MPPT2_VOLTAGE(0.1f, 5013, UINT16, Units.VOLT),
    MPPT2_CURRENT(0.1f, 5014, UINT16, Units.AMPERE),
    MPPT3_VOLTAGE(0.1f, 5015, UINT16, Units.VOLT),
    MPPT3_CURRENT(0.1f, 5016, UINT16, Units.AMPERE),
    TOTAL_DC_POWER(1, 5017, UINT32, Units.WATT),
    PHASE_A_VOLTAGE(0.1f, 5019, UINT16, Units.VOLT),
    PHASE_B_VOLTAGE(0.1f, 5020, UINT16, Units.VOLT),
    PHASE_C_VOLTAGE(0.1f, 5021, UINT16, Units.VOLT),
    PHASE_A_CURRENT(0.1f, 5022, UINT16, Units.AMPERE),
    PHASE_B_CURRENT(0.1f, 5023, UINT16, Units.AMPERE),
    PHASE_C_CURRENT(0.1f, 5024, UINT16, Units.AMPERE),
    TOTAL_ACTIVE_POWER(1, 5031, UINT32, Units.WATT),
    TOTAL_REACTIVE_POWER(1, 5033, INT32, Units.VAR),
    POWER_FACTOR(0.001f, 5035, INT16, Units.ONE),
    GRID_FREQUENCY(0.1f, 5036, UINT16, Units.HERTZ),
    WORK_STATE_1(1, 5038, UINT16, Units.ONE),
    NOMINAL_REACTIVE_POWER(0.1f, 5049, UINT16, Units.KILOVAR),
    ARRAY_INSULATION_RESISTANCE(1000, 5071, UINT16, Units.OHM),
    ACTIVE_POWER_REGULATION_SETPOINT(1, 5077, UINT32, Units.WATT),
    REACTIVE_POWER_REGULATION_SETPOINT(1, 5079, INT32, Units.VAR),
    WORK_STATE_2(1, 5081, UINT32, Units.ONE),
    METER_POWER(1, 5083, INT32, Units.WATT),
    METER_PHASE_A_POWER(1, 5085, INT32, Units.WATT),
    METER_PHASE_B_POWER(1, 5087, INT32, Units.WATT),
    METER_PHASE_C_POWER(1, 5089, INT32, Units.WATT),
    LOAD_POWER(1, 5091, INT32, Units.WATT),
    DAILY_EXPORT_ENERGY(0.1f, 5093, UINT32, Units.KILOWATT_HOUR),
    TOTAL_EXPORT_ENERGY(0.1f, 5095, UINT32, Units.KILOWATT_HOUR),
    DAILY_IMPORT_ENERGY(0.1f, 5097, UINT32, Units.KILOWATT_HOUR),
    TOTAL_IMPORT_ENERGY(0.1f, 5099, UINT32, Units.KILOWATT_HOUR),
    DAILY_DIRECT_ENERGY_CONSUMPTION(0.1f, 5101, UINT32, Units.KILOWATT_HOUR),
    TOTAL_DIRECT_ENERGY_CONSUMPTION(0.1f, 5103, UINT32, Units.KILOWATT_HOUR),
    DAILY_RUNNING_TIME(1, 5113, UINT16, Units.MINUTE),
    MONTHLY_POWER_YIELDS(0.1f, 5128, UINT32, Units.KILOWATT_HOUR),
    TOTAL_POWER_YIELDS_FINE(0.1f, 5144, UINT32, Units.KILOWATT_HOUR),
    NEGATIVE_VOLTAGE_TO_GROUND(0.1f, 5146, INT16, Units.VOLT),
    BUS_VOLTAGE(0.1f, 5147, UINT16, Units.VOLT),
    GRID_FREQUENCY_FINE(0.01f, 5148, UINT16, Units.HERTZ),
    PID_WORK_STATE(1, 5150, UINT16, Units.ONE),
    PID_ALARM_CODE(1, 5151, UINT16, Units.ONE);

    private final BigDecimal multiplier;
    private final int registerNumber;
    private final ValueType type;
    private final Unit<?> unit;

    private SungrowInverterRegistersBackup(float multiplier, int registerNumber, ValueType type, Unit<?> unit) {
        this.multiplier = new BigDecimal(multiplier);
        this.registerNumber = registerNumber;
        this.type = type;
        this.unit = unit;
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
}
