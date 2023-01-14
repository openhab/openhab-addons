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
package org.openhab.binding.modbus.sbc.internal;

import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.*;

import java.math.BigDecimal;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusConstants;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.library.unit.Units;

/**
 * The {@link ALD1Registers} is responsible for defining Modbus registers and their units.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public enum ALD1Registers {
    // the following register numbers are 1-based. They need to be converted before sending them on the wire.
    TOTAL_ENERGY(0.01f, 28, UINT32, Units.KILOWATT_HOUR),
    PARTIAL_ENERGY(0.01f, 30, UINT32, Units.KILOWATT_HOUR), // only unidirectional meters
    FEEDING_BACK_ENERGY(0.01f, 30, UINT32, Units.KILOWATT_HOUR), // only bidirectional meters
    VOLTAGE(1, 36, UINT16, Units.VOLT),
    CURRENT(0.1f, 37, UINT16, Units.AMPERE),
    ACTIVE_POWER(10, 38, INT16, Units.WATT),
    REACTIVE_POWER(10, 39, INT16, Units.VAR),
    POWER_FACTOR(0.01f, 40, INT16, Units.ONE);

    private BigDecimal multiplier;
    private int registerNumber;
    private ModbusConstants.ValueType type;
    private Unit<?> unit;

    private ALD1Registers(float multiplier, int registerNumber, ValueType type, Unit<?> unit) {
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

    public ModbusConstants.ValueType getType() {
        return type;
    }
}
