/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.lambda.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.lambda.internal.dto.BufferBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses lambda modbus data into a Buffer Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
@NonNullByDefault
public class BufferBlockParser extends AbstractBaseParser {

    public BufferBlock parse(ModbusRegisterArray raw) {
        BufferBlock block = new BufferBlock();
        block.bufferErrorNumber = extractInt16(raw, 0, (short) 0);
        block.bufferOperatingState = extractUInt16(raw, 1, (short) 0);
        block.bufferActualHighTemperature = extractInt16(raw, 2, (short) 0);
        block.bufferActualLowTemperature = extractInt16(raw, 3, (short) 0);
        block.bufferActualModbusTemperature = extractInt16(raw, 4, (short) 0);
        block.bufferRequestType = extractInt16(raw, 5, (short) 0);
        block.bufferrequestFlowLineTemperature = extractInt16(raw, 6, (short) 0);
        block.bufferrequestReturnLineTemperature = extractInt16(raw, 7, (short) 0);
        block.bufferrequestHeatSinkTemperature = extractInt16(raw, 8, (short) 0);
        block.bufferrequestHeatingCapacity = extractInt16(raw, 9, (short) 0);
        return block;
    }
}
