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
package org.openhab.binding.modbus.solaxx3mic.internal;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.io.transport.modbus.ModbusBitUtilities;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * The {@link InverterStatus} describes
 * possible values for an inverter's status field
 *
 * @author Stanislaw Wawszczak - Initial contribution
 */
@NonNullByDefault
public final class ModbusParser {

    /**
     * Extract an optional int16 value
     *
     * @param raw the register array to extract from
     * @param index the address of the field
     * @return the parsed value or empty if the field is not implemented
     */
    public static Optional<Short> extractOptionalInt16(ModbusRegisterArray raw, int index) {
        return ModbusBitUtilities.extractStateFromRegisters(raw, index, ValueType.INT16).map(DecimalType::shortValue);
    }

    /**
     * Extract a mandatory int16 value
     *
     * @param raw the register array to extract from
     * @param index the address of the field
     * @param def the default value
     * @return the parsed value or the default if the field is not implemented
     */
    public static Short extractInt16(ModbusRegisterArray raw, int index, short def) {
        return extractOptionalInt16(raw, index).orElse(def);
    }

    /**
     * Extract an optional uint16 value
     *
     * @param raw the register array to extract from
     * @param index the address of the field
     * @return the parsed value or empty if the field is not implemented
     */
    public static Optional<Integer> extractOptionalUInt16(ModbusRegisterArray raw, int index) {
        return ModbusBitUtilities.extractStateFromRegisters(raw, index, ValueType.UINT16).map(DecimalType::intValue);
    }

    /**
     * Extract a mandatory uint16 value
     *
     * @param raw the register array to extract from
     * @param index the address of the field
     * @param def the default value
     * @return the parsed value or the default if the field is not implemented
     */
    public static Integer extractUInt16(ModbusRegisterArray raw, int index, int def) {
        return extractOptionalUInt16(raw, index).orElse(def);
    }

    /**
     * Extract an optional acc32 value
     *
     * @param raw the register array to extract from
     * @param index the address of the field
     * @return the parsed value or empty if the field is not implemented
     */
    public static Optional<Long> extractOptionalInt32(ModbusRegisterArray raw, int index) {
        return ModbusBitUtilities.extractStateFromRegisters(raw, index, ValueType.INT32).map(DecimalType::longValue);
    }

    /**
     * Extract a mandatory acc32 value
     *
     * @param raw the register array to extract from
     * @param index the address of the field
     * @param def the default value
     * @return the parsed value or default if the field is not implemented
     */
    public static Long extractInt32(ModbusRegisterArray raw, int index, long def) {
        return extractOptionalInt32(raw, index).orElse(def);
    }
}
