/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.io.transport.modbus.ModbusConstants.ValueType.*;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SmartHomeUnits;
import org.openhab.io.transport.modbus.ModbusConstants;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;

/**
 * The {@link ALD1Registers} is responsible for defining Modbus registers and their units.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public enum ALD1Registers {
    TOTAL_ENERGY(0.01f, 28, UINT32, SmartHomeUnits.KILOWATT_HOUR),
    PARTIAL_ENERGY(0.01f, 30, UINT32, SmartHomeUnits.KILOWATT_HOUR),
    FEEDING_BACK_ENERGY(0.01f, 30, UINT32, SmartHomeUnits.KILOWATT_HOUR),
    VOLTAGE(1, 36, UINT16, SmartHomeUnits.VOLT),
    CURRENT(0.1f, 37, UINT16, SmartHomeUnits.AMPERE),
    ACTIVE_POWER(10, 38, INT16, SmartHomeUnits.WATT),
    REACTIVE_POWER(10, 39, INT16, SmartHomeUnits.VAR),
    POWER_FACTOR(0.01f, 40, UINT16, SmartHomeUnits.ONE);

    private float multiplier;
    private int registerAddress;
    private ModbusConstants.ValueType type;
    private Unit<?> unit;

    private ALD1Registers(float multiplier, int registerAddress, ValueType type, Unit<?> unit) {
        this.multiplier = multiplier;
        this.registerAddress = registerAddress;
        this.type = type;
        this.unit = unit;
    }

    public Unit<?> getUnit() {
        return unit;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public int getRegisterAddress() {
        return registerAddress;
    }

    public ModbusConstants.ValueType getType() {
        return type;
    }
}
