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
package org.openhab.binding.modbus.stiebeleltron.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.RuntimeBlockWpm3i;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses runtime data from modbus energy data set of a WPM3i compatible heat pump into an Runtime Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
@NonNullByDefault
public class RuntimeBlockParser extends AbstractBaseParser {

    public RuntimeBlockWpm3i parse(ModbusRegisterArray raw) {
        RuntimeBlockWpm3i block = new RuntimeBlockWpm3i();

        block.runtimeCompressorHeating = extractUInt16(raw, 16, (short) 0);
        block.runtimeCompressorHotwater = extractUInt16(raw, 17, (short) 0);
        block.runtimeCompressorCooling = extractUInt16(raw, 18, (short) 0);
        block.runtimeNhz1 = extractUInt16(raw, 19, (short) 0);
        block.runtimeNhz2 = extractUInt16(raw, 20, (short) 0);
        block.runtimeNhz12 = extractUInt16(raw, 21, (short) 0);
        return block;
    }
}
