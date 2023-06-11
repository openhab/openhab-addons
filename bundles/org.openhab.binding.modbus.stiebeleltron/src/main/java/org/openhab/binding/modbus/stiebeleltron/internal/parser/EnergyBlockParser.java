/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inverter modbus data into an Energy Block
 *
 * @author Paul Frank - Initial contribution
 *
 */
@NonNullByDefault
public class EnergyBlockParser extends AbstractBaseParser {

    public EnergyBlock parse(ModbusRegisterArray raw) {
        EnergyBlock block = new EnergyBlock();

        block.productionHeatToday = extractUInt16(raw, 0, (short) 0);
        block.productionHeatTotalLow = extractUInt16(raw, 1, (short) 0);
        block.productionHeatTotalHigh = extractUInt16(raw, 2, (short) 0);
        block.productionWaterToday = extractUInt16(raw, 3, (short) 0);
        block.productionWaterTotalLow = extractUInt16(raw, 4, (short) 0);
        block.productionWaterTotalHigh = extractUInt16(raw, 5, (short) 0);

        block.consumptionHeatToday = extractUInt16(raw, 10, (short) 0);
        block.consumptionHeatTotalLow = extractUInt16(raw, 11, (short) 0);
        block.consumptionHeatTotalHigh = extractUInt16(raw, 12, (short) 0);
        block.consumptionWaterToday = extractUInt16(raw, 13, (short) 0);
        block.consumptionWaterTotalLow = extractUInt16(raw, 14, (short) 0);
        block.consumptionWaterTotalHigh = extractUInt16(raw, 15, (short) 0);
        return block;
    }
}
