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
package org.openhab.binding.modbus.stiebeleltron.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeControlAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeControlAllWpm.EnergyRuntimeFeatureKeys;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeControlAllWpm.EnergyRuntimeHpFeature;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeControlAllWpm.EnergyRuntimeHpFeatureKeys;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses runtime data from modbus energy data set of a WPM compatible heat pump into an Runtime Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
@NonNullByDefault
public class EnergyRuntimeBlockAllWpmParser extends AbstractBaseParser {

    public EnergyRuntimeBlockAllWpm parse(ModbusRegisterArray raw, EnergyRuntimeControlAllWpm control,
            int heatpumpCount) {
        EnergyRuntimeBlockAllWpm block = new EnergyRuntimeBlockAllWpm(heatpumpCount);

        // Common Production
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

        // Common Consumption
        block.consumptionHeatToday = extractUInt16(raw, 10, (short) 0);
        block.consumptionHeatTotalLow = extractUInt16(raw, 11, (short) 0);
        block.consumptionHeatTotalHigh = extractUInt16(raw, 12, (short) 0);
        block.consumptionWaterToday = extractUInt16(raw, 13, (short) 0);
        block.consumptionWaterTotalLow = extractUInt16(raw, 14, (short) 0);
        block.consumptionWaterTotalHigh = extractUInt16(raw, 15, (short) 0);

        // Common Runtime
        if (control.featureAvailable(EnergyRuntimeFeatureKeys.COMMON_RUNTIMES)) {
            block.runtimeCompressorHeating = extractUInt16(raw, 16, (short) 0);
            if (block.runtimeCompressorHeating == 32768) {
                control.setFeatureAvailable(EnergyRuntimeFeatureKeys.COMMON_RUNTIMES, false);
            } else {
                block.runtimeCompressorHotwater = extractUInt16(raw, 17, (short) 0);
                block.runtimeNhz1 = extractUInt16(raw, 19, (short) 0);
                block.runtimeNhz2 = extractUInt16(raw, 20, (short) 0);
                block.runtimeNhz12 = extractUInt16(raw, 21, (short) 0);
            }
        }

        if (control.featureAvailable(EnergyRuntimeFeatureKeys.COMMON_COOLING_RUNTIME)) {
            block.runtimeCompressorCooling = extractUInt16(raw, 18, (short) 0);
            if (block.runtimeCompressorCooling == 32768) {
                control.setFeatureAvailable(EnergyRuntimeFeatureKeys.COMMON_COOLING_RUNTIME, false);
            }
        }

        for (int idx = 0; idx < heatpumpCount; idx++) {
            block.heatPumps[idx].productionHeatToday = extractUInt16(raw, 22 + 26 * idx, (short) 0);
            block.heatPumps[idx].productionHeatTotalLow = extractUInt16(raw, 23 + 26 * idx, (short) 0);
            block.heatPumps[idx].productionHeatTotalHigh = extractUInt16(raw, 24 + 26 * idx, (short) 0);
            block.heatPumps[idx].productionWaterToday = extractUInt16(raw, 25 + 26 * idx, (short) 0);
            block.heatPumps[idx].productionWaterTotalLow = extractUInt16(raw, 26 + 26 * idx, (short) 0);
            block.heatPumps[idx].productionWaterTotalHigh = extractUInt16(raw, 27 + 26 * idx, (short) 0);
            block.heatPumps[idx].productionNhzHeatingTotalLow = extractUInt16(raw, 28 + 26 * idx, (short) 0);
            block.heatPumps[idx].productionNhzHeatingTotalHigh = extractUInt16(raw, 29 + 26 * idx, (short) 0);
            block.heatPumps[idx].productionNhzHotwaterTotalLow = extractUInt16(raw, 30 + 26 * idx, (short) 0);
            block.heatPumps[idx].productionNhzHotwaterTotalHigh = extractUInt16(raw, 31 + 26 * idx, (short) 0);

            block.heatPumps[idx].consumptionHeatToday = extractUInt16(raw, 32 + 26 * idx, (short) 0);
            block.heatPumps[idx].consumptionHeatTotalLow = extractUInt16(raw, 33 + 26 * idx, (short) 0);
            block.heatPumps[idx].consumptionHeatTotalHigh = extractUInt16(raw, 34 + 26 * idx, (short) 0);
            block.heatPumps[idx].consumptionWaterToday = extractUInt16(raw, 35 + 26 * idx, (short) 0);
            block.heatPumps[idx].consumptionWaterTotalLow = extractUInt16(raw, 36 + 26 * idx, (short) 0);
            block.heatPumps[idx].consumptionWaterTotalHigh = extractUInt16(raw, 37 + 26 * idx, (short) 0);

            EnergyRuntimeHpFeature hpFeaturesObj = control.hpEgRtList[idx];
            if (hpFeaturesObj.featureAvailable(EnergyRuntimeHpFeatureKeys.RUNTIMES)) {
                block.heatPumps[idx].runtimeCompressor1Heating = extractUInt16(raw, 38 + 26 * idx, (short) 0);
                if (block.heatPumps[idx].runtimeCompressor1Heating == 32768) {
                    hpFeaturesObj.setFeatureAvailable(EnergyRuntimeHpFeatureKeys.RUNTIMES, false);
                } else {
                    block.heatPumps[idx].runtimeCompressor2Heating = extractUInt16(raw, 39 + 26 * idx, (short) 0);
                    block.heatPumps[idx].runtimeCompressor12Heating = extractUInt16(raw, 40 + 26 * idx, (short) 0);
                    block.heatPumps[idx].runtimeCompressor1Hotwater = extractUInt16(raw, 41 + 26 * idx, (short) 0);
                    block.heatPumps[idx].runtimeCompressor2Hotwater = extractUInt16(raw, 42 + 26 * idx, (short) 0);
                    block.heatPumps[idx].runtimeCompressor12Hotwater = extractUInt16(raw, 43 + 26 * idx, (short) 0);
                }
            }
            if (hpFeaturesObj.featureAvailable(EnergyRuntimeHpFeatureKeys.COOLING_RUNTIME)) {
                block.heatPumps[idx].runtimeCompressorCooling = extractUInt16(raw, 44 + 26 * idx, (short) 0);
                if (block.heatPumps[idx].runtimeCompressorCooling == 32768) {
                    hpFeaturesObj.setFeatureAvailable(EnergyRuntimeHpFeatureKeys.COOLING_RUNTIME, false);
                }
            }

            if (hpFeaturesObj.featureAvailable(EnergyRuntimeHpFeatureKeys.NHZ_RUNTIMES)) {
                block.heatPumps[idx].runtimeNhz1 = extractUInt16(raw, 45 + 26 * idx, (short) 0);
                if (block.heatPumps[idx].runtimeNhz1 == 32768) {
                    hpFeaturesObj.setFeatureAvailable(EnergyRuntimeHpFeatureKeys.NHZ_RUNTIMES, false);
                } else {
                    block.heatPumps[idx].runtimeNhz2 = extractUInt16(raw, 46 + 26 * idx, (short) 0);
                    block.heatPumps[idx].runtimeNhz12 = extractUInt16(raw, 47 + 26 * idx, (short) 0);
                }
            }
        }
        return block;
    }
}
