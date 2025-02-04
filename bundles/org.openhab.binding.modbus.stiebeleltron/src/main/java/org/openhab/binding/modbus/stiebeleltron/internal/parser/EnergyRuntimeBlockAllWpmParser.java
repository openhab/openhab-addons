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
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeBlockAllWpm;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses runtime data from modbus energy data set of a WPM3i compatible heat pump into an Runtime Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
@NonNullByDefault
public class EnergyRuntimeBlockAllWpmParser extends AbstractBaseParser {

    private boolean mWpmWpm3HeatPump = false;

    /**
     * Instances of this parser shall know if they handle a WPMsystem/WPM3 heat pump with additional
     * information about heat pumps and circuits.
     *
     * @param boolean flag telling if the parse shall work on a WPMsystem/WPM3 heat pump with additional data
     */
    public EnergyRuntimeBlockAllWpmParser(boolean isWpmHeatPump) {
        mWpmWpm3HeatPump = isWpmHeatPump;
    }

    public EnergyRuntimeBlockAllWpm parse(ModbusRegisterArray raw) {
        EnergyRuntimeBlockAllWpm block = new EnergyRuntimeBlockAllWpm();

        block.productionHeatToday = extractUInt16(raw, 0, (short) 0);
        block.productionHeatTotalLow = extractUInt16(raw, 1, (short) 0);
        block.productionHeatTotalHigh = extractUInt16(raw, 2, (short) 0);
        block.productionWaterToday = extractUInt16(raw, 3, (short) 0);
        block.productionWaterTotalLow = extractUInt16(raw, 4, (short) 0);
        block.productionWaterTotalHigh = extractUInt16(raw, 5, (short) 0);

        block.productionNhzHeatingTotalLow = extractUInt16(raw, 6, (short) 0);
        block.productionNhzHeatingTotalHigh = extractUInt16(raw, 7, (short) 0);
        block.productionNhzHotwaterTotalLow = extractUInt16(raw, 8, (short) 0);
        block.productionNhzHotwaterTotalHigh = extractUInt16(raw, 9, (short) 0);

        block.consumptionHeatToday = extractUInt16(raw, 10, (short) 0);
        block.consumptionHeatTotalLow = extractUInt16(raw, 11, (short) 0);
        block.consumptionHeatTotalHigh = extractUInt16(raw, 12, (short) 0);
        block.consumptionWaterToday = extractUInt16(raw, 13, (short) 0);
        block.consumptionWaterTotalLow = extractUInt16(raw, 14, (short) 0);
        block.consumptionWaterTotalHigh = extractUInt16(raw, 15, (short) 0);

        if (mWpmWpm3HeatPump) {
            block.hp1ProductionHeatToday = extractUInt16(raw, 22, (short) 0);
            block.hp1ProductionHeatTotalLow = extractUInt16(raw, 23, (short) 0);
            block.hp1ProductionHeatTotalHigh = extractUInt16(raw, 24, (short) 0);
            block.hp1ProductionWaterToday = extractUInt16(raw, 25, (short) 0);
            block.hp1ProductionWaterTotalLow = extractUInt16(raw, 26, (short) 0);
            block.hp1ProductionWaterTotalHigh = extractUInt16(raw, 27, (short) 0);
            block.hp1ProductionNhzHeatingTotalLow = extractUInt16(raw, 28, (short) 0);
            block.hp1ProductionNhzHeatingTotalHigh = extractUInt16(raw, 29, (short) 0);
            block.hp1ProductionNhzHotwaterTotalLow = extractUInt16(raw, 30, (short) 0);
            block.hp1ProductionNhzHotwaterTotalHigh = extractUInt16(raw, 31, (short) 0);

            block.hp1ConsumptionHeatToday = extractUInt16(raw, 32, (short) 0);
            block.hp1ConsumptionHeatTotalLow = extractUInt16(raw, 33, (short) 0);
            block.hp1ConsumptionHeatTotalHigh = extractUInt16(raw, 34, (short) 0);
            block.hp1ConsumptionWaterToday = extractUInt16(raw, 35, (short) 0);
            block.hp1ConsumptionWaterTotalLow = extractUInt16(raw, 36, (short) 0);
            block.hp1ConsumptionWaterTotalHigh = extractUInt16(raw, 37, (short) 0);

            block.hp1RuntimeCompressor1Heating = extractUInt16(raw, 38, (short) 0);
            block.hp1RuntimeCompressor2Heating = extractUInt16(raw, 39, (short) 0);
            block.hp1RuntimeCompressor12Heating = extractUInt16(raw, 40, (short) 0);
            block.hp1RuntimeCompressor1Hotwater = extractUInt16(raw, 41, (short) 0);
            block.hp1RuntimeCompressor2Hotwater = extractUInt16(raw, 42, (short) 0);
            block.hp1RuntimeCompressor12Hotwater = extractUInt16(raw, 43, (short) 0);
            block.hp1RuntimeCompressorCooling = extractUInt16(raw, 44, (short) 0);

            block.runtimeNhz1 = extractUInt16(raw, 45, (short) 0);
            block.runtimeNhz2 = extractUInt16(raw, 46, (short) 0);
            block.runtimeNhz12 = extractUInt16(raw, 47, (short) 0);
        } else {
            block.runtimeCompressorHeating = extractUInt16(raw, 16, (short) 0);
            block.runtimeCompressorHotwater = extractUInt16(raw, 17, (short) 0);
            block.runtimeCompressorCooling = extractUInt16(raw, 18, (short) 0);
            block.runtimeNhz1 = extractUInt16(raw, 19, (short) 0);
            block.runtimeNhz2 = extractUInt16(raw, 20, (short) 0);
            block.runtimeNhz12 = extractUInt16(raw, 21, (short) 0);
        }
        return block;
    }
}
