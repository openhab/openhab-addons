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
package org.openhab.binding.modbus.lambda.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.lambda.internal.dto.HeatpumpBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses lambda modbus data into a Heatpump Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
@NonNullByDefault
public class HeatpumpBlockParser extends AbstractBaseParser {

    public HeatpumpBlock parse(ModbusRegisterArray raw) {
        HeatpumpBlock block = new HeatpumpBlock();
        block.heatpumpErrorState = extractUInt16(raw, 0, (short) 0);
        block.heatpumpErrorNumber = extractInt16(raw, 1, (short) 0);
        block.heatpumpState = extractUInt16(raw, 2, (short) 0);
        block.heatpumpOperatingState = extractUInt16(raw, 3, (short) 0);
        block.heatpumpTFlow = extractInt16(raw, 4, (short) 0);
        block.heatpumpTReturn = extractInt16(raw, 5, (short) 0);
        block.heatpumpVolSink = extractInt16(raw, 6, (short) 0);
        block.heatpumpTEQin = extractInt16(raw, 7, (short) 0);
        block.heatpumpTEQout = extractInt16(raw, 8, (short) 0);
        block.heatpumpVolSource = extractInt16(raw, 9, (short) 0);
        block.heatpumpCompressorRating = extractUInt16(raw, 10, (short) 0);
        block.heatpumpQpHeating = extractInt16(raw, 11, (short) 0);
        block.heatpumpFIPowerConsumption = extractInt16(raw, 12, (short) 0);
        block.heatpumpCOP = extractInt16(raw, 13, (short) 0);
        block.heatpumpRequestPassword = extractUInt16(raw, 14, (short) 0);
        block.heatpumpRequestType = extractInt16(raw, 15, (short) 0);
        block.heatpumpRequestTFlow = extractInt16(raw, 16, (short) 0);
        block.heatpumpRequestTReturn = extractInt16(raw, 17, (short) 0);
        block.heatpumpRequestHeatSink = extractInt16(raw, 18, (short) 0);
        block.heatpumpRelaisState = extractInt16(raw, 19, (short) 0);
        block.heatpumpVdAE = extractInt32(raw, 20, (long) 0);
        block.heatpumpVdAQ = extractInt32(raw, 22, (long) 0);
        return block;
    }
}
