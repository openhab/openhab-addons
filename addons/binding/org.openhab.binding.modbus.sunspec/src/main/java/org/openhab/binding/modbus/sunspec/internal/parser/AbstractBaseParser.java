/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.sunspec.internal.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.io.transport.modbus.ModbusBitUtilities;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * Base class for parsers with some helper methods
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
public class AbstractBaseParser {

    /**
     * Extract an int16 value
     *
     * @param raw the register array to extract from
     * @param index the address of the field
     * @return the parsed value or null if the field is not implemented
     */
    protected Short extractInt16(@NonNull ModbusRegisterArray raw, int index) {
        short value = ModbusBitUtilities.extractStateFromRegisters(raw, index, ValueType.INT16).shortValue();
        if (value == (short) 0x8000) {
            return null;
        }
        return value;
    }

    /**
     * Extract an uint16 value
     *
     * @param raw the register array to extract from
     * @param index the address of the field
     * @return the parsed value or null if the field is not implemented
     */
    protected Integer extractUInt16(@NonNull ModbusRegisterArray raw, int index) {
        int value = ModbusBitUtilities.extractStateFromRegisters(raw, index, ValueType.UINT16).intValue();
        if (value == 0xffff) {
            return null;
        }
        return value;
    }

    /**
     * Extract an acc32 value
     *
     * @param raw the register array to extract from
     * @param index the address of the field
     * @return the parsed value or null if the field is not implemented
     */
    protected Long extractAcc32(@NonNull ModbusRegisterArray raw, int index) {
        long value = ModbusBitUtilities.extractStateFromRegisters(raw, index, ValueType.UINT32).longValue();
        if (value == 0) {
            return null;
        }
        return value;
    }

    /**
     * Extract a scale factor
     *
     * @param raw the register array to extract from
     * @param index the address of the field
     * @return the parsed value or null if the field is not implemented
     */
    protected Short extractSunSSF(@NonNull ModbusRegisterArray raw, int index) {
        short value = ModbusBitUtilities.extractStateFromRegisters(raw, index, ValueType.INT16).shortValue();
        if (value == (short) 0x8000) {
            return null;
        }
        return value;
    }
}
