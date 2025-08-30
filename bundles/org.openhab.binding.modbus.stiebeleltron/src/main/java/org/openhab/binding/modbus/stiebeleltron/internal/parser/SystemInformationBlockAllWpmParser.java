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
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationControlAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationControlAllWpm.SysInfoFeatureKeys;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationControlAllWpm.SysInfoHpFeature;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationControlAllWpm.SysInfoHpFeaturelKeys;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses modbus system information data of a WPM compatible heat pump into a System Information Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
@NonNullByDefault
public class SystemInformationBlockAllWpmParser extends AbstractBaseParser {

    public SystemInformationBlockAllWpm parse(ModbusRegisterArray raw, SystemInformationControlAllWpm control,
            int nrOfWpms) {
        SystemInformationBlockAllWpm block = new SystemInformationBlockAllWpm(nrOfWpms);

        if (control.featureAvailable(SysInfoFeatureKeys.FE7)) {
            block.temperatureFe7 = extractInt16(raw, 0, (short) -32768);
            if (block.temperatureFe7 == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.FE7, false);
            } else {
                block.temperatureFe7SetPoint = extractInt16(raw, 1, (short) 0);
            }
        }

        if (control.featureAvailable(SysInfoFeatureKeys.FEK)) {
            block.temperatureFek = extractInt16(raw, 2, (short) -32768);
            if (block.temperatureFek == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.FEK, false);
            } else {
                block.temperatureFekSetPoint = extractInt16(raw, 3, (short) 0);
                block.humidityFek = extractInt16(raw, 4, (short) 0);
                block.dewpointFek = extractInt16(raw, 5, (short) 0);
            }
        }
        block.temperatureOutdoor = extractInt16(raw, 6, (short) 0);
        block.temperatureHc1 = extractInt16(raw, 7, (short) 0);

        block.temperatureHc1SetPoint = extractInt16(raw, 9, (short) 0);

        // documents tell "HC1 Setpoint temperature" of WPM3i would be on address 509, but on my WPM3i it is also on
        // 510
        block.temperatureHc1SetPoint = extractInt16(raw, 9, (short) -32768);
        if (block.temperatureHc1SetPoint == -32768) {
            block.temperatureHc1SetPoint = extractInt16(raw, 8, (short) 0);
        }

        if (control.featureAvailable(SysInfoFeatureKeys.HC2)) {
            block.temperatureHc2 = extractInt16(raw, 10, (short) -32768);
            if (block.temperatureHc2 == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.HC2, false);
            } else {
                block.temperatureHc2SetPoint = extractInt16(raw, 11, (short) 0);
            }
        }

        block.temperatureFlowHp = extractInt16(raw, 12, (short) 0);
        block.temperatureFlowNhz = extractInt16(raw, 13, (short) 0);
        if (control.featureAvailable(SysInfoFeatureKeys.TEMP_FLOW)) {
            block.temperatureFlow = extractInt16(raw, 14, (short) -32768);
            if (block.temperatureFlow == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.TEMP_FLOW, false);
            }
        }
        if (control.featureAvailable(SysInfoFeatureKeys.TEMP_RETURN)) {
            block.temperatureReturn = extractInt16(raw, 15, (short) -32768);
            if (block.temperatureReturn == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.TEMP_RETURN, false);
            }
        }
        block.temperatureFixedSetPoint = extractInt16(raw, 16, (short) 0);
        // Check for OFF value (0x9000)-> use -1 as 0 is in valid range 0..90
        if (block.temperatureFixedSetPoint == -28672) {
            block.temperatureFixedSetPoint = -1;
        }

        block.temperatureBuffer = extractInt16(raw, 17, (short) 0);
        block.temperatureBufferSetPoint = extractInt16(raw, 18, (short) 0);

        block.pressureHeating = extractInt16(raw, 19, (short) 0);
        block.flowRate = extractInt16(raw, 20, (short) 0);

        block.temperatureWater = extractInt16(raw, 21, (short) 0);
        block.temperatureWaterSetPoint = extractInt16(raw, 22, (short) 0);

        if (control.featureAvailable(SysInfoFeatureKeys.FAN_COOLING)) {
            block.temperatureFanCooling = extractInt16(raw, 23, (short) -32768);
            if (block.temperatureFanCooling == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.FAN_COOLING, false);
            } else {
                block.temperatureFanCoolingSetPoint = extractInt16(raw, 24, (short) 0);
            }
        }
        if (control.featureAvailable(SysInfoFeatureKeys.AREA_COOLING)) {
            block.temperatureAreaCooling = extractInt16(raw, 25, (short) -32768);
            if (block.temperatureAreaCooling == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.AREA_COOLING, false);
            } else {
                block.temperatureAreaCoolingSetPoint = extractInt16(raw, 26, (short) 0);
            }
        }

        if (control.featureAvailable(SysInfoFeatureKeys.SOLAR_THERMAL)) {
            block.temperatureCollectorSolar = extractInt16(raw, 27, (short) -32768);
            if (block.temperatureCollectorSolar == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.SOLAR_THERMAL, false);
            } else {
                block.temperatureCylinderSolar = extractInt16(raw, 28, (short) 0);
                block.runtimeSolar = extractUInt16(raw, 29, 0);
            }
        }

        if (control.featureAvailable(SysInfoFeatureKeys.EXTERNAL_HEATING)) {
            block.temperatureExtHeatSource = extractInt16(raw, 30, (short) -32768);
            if (block.temperatureExtHeatSource == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.EXTERNAL_HEATING, false);
            } else {
                block.temperatureExtHeatSourceSetPoint = extractInt16(raw, 31, (short) 0);
                block.runtimeExtHeatSource = extractUInt16(raw, 34, 0);
            }
        }

        if (control.featureAvailable(SysInfoFeatureKeys.LOWER_LIMITS)) {
            block.lowerHeatingLimit = extractInt16(raw, 32, (short) -32768);
            if (block.lowerHeatingLimit == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.LOWER_LIMITS, false);
            } else {
                block.lowerWaterLimit = extractInt16(raw, 33, (short) 0);
                // Check for OFF value (0x9000)-> use -410, which returns -41; valid range is -40..40
                if (block.lowerHeatingLimit == -28672) {
                    block.lowerHeatingLimit = -410;
                }
                if (block.lowerWaterLimit == -28672) {
                    block.lowerWaterLimit = -410;
                }
            }
        }

        if (control.featureAvailable(SysInfoFeatureKeys.SOURCE_VALUES)) {
            block.temperatureSource = extractInt16(raw, 35, (short) -32768);
            if (block.temperatureSource == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.SOURCE_VALUES, false);
            } else {
                block.temperatureSourceMin = extractInt16(raw, 36, (short) 0);
                block.pressureSource = extractInt16(raw, 37, (short) 0);
            }
        }

        if (control.featureAvailable(SysInfoFeatureKeys.HOTGAS)) {
            block.temperatureHotgas = extractInt16(raw, 38, (short) -32768);
            if (block.temperatureHotgas == -32768) {
                control.setFeatureAvailable(SysInfoFeatureKeys.HOTGAS, false);
            } else {
                block.pressureHigh = extractInt16(raw, 39, (short) 0);
                block.pressureLow = extractInt16(raw, 40, (short) 0);
            }
        }

        for (int idx = 0; idx < nrOfWpms; idx++) {
            SysInfoHpFeature lObj = control.hpSysInfoList[idx];
            if (lObj.available(SysInfoHpFeaturelKeys.HP_TEMPERATURE_RETURN)) {
                block.heatPumps[idx].temperatureReturn = extractInt16(raw, 41 + 7 * idx, (short) -32768);
                if (block.heatPumps[idx].temperatureReturn == -32768) {
                    lObj.setAvailable(SysInfoHpFeaturelKeys.HP_TEMPERATURE_RETURN, false);
                }
            }

            if (lObj.available(SysInfoHpFeaturelKeys.HP_TEMPERATURE_FLOW)) {
                block.heatPumps[idx].temperatureFlow = extractInt16(raw, 42 + 7 * idx, (short) -32768);
                if (block.heatPumps[idx].temperatureFlow == -32768) {
                    lObj.setAvailable(SysInfoHpFeaturelKeys.HP_TEMPERATURE_FLOW, false);
                }
            }

            if (lObj.available(SysInfoHpFeaturelKeys.HP_TEMPERATURE_HOTGAS)) {
                block.heatPumps[idx].temperatureHotgas = extractInt16(raw, 43 + 7 * idx, (short) -32768);
                if (block.heatPumps[idx].temperatureHotgas == -32768) {
                    lObj.setAvailable(SysInfoHpFeaturelKeys.HP_TEMPERATURE_HOTGAS, false);
                }
            }

            if (lObj.available(SysInfoHpFeaturelKeys.HP_PRESSURE_LOW)) {
                block.heatPumps[idx].pressureLow = extractInt16(raw, 44 + 7 * idx, (short) -32768);
                if (block.heatPumps[idx].pressureLow == -32768) {
                    lObj.setAvailable(SysInfoHpFeaturelKeys.HP_PRESSURE_LOW, false);
                }
            }

            if (lObj.available(SysInfoHpFeaturelKeys.HP_PRESSURE_MEAN)) {
                block.heatPumps[idx].pressureMean = extractInt16(raw, 45 + 7 * idx, (short) -32768);
                if (block.heatPumps[idx].pressureMean == -32768) {
                    lObj.setAvailable(SysInfoHpFeaturelKeys.HP_PRESSURE_MEAN, false);
                }
            }

            if (lObj.available(SysInfoHpFeaturelKeys.HP_PRESSURE_HIGH)) {
                block.heatPumps[idx].pressureHigh = extractInt16(raw, 46 + 7 * idx, (short) -32768);
                if (block.heatPumps[idx].pressureHigh == -32768) {
                    lObj.setAvailable(SysInfoHpFeaturelKeys.HP_PRESSURE_HIGH, false);
                }
            }

            if (lObj.available(SysInfoHpFeaturelKeys.HP_FLOW_RATE)) {
                block.heatPumps[idx].flowRate = extractInt16(raw, 47 + 7 * idx, (short) -32768);
                if (block.heatPumps[idx].flowRate == -32768) {
                    lObj.setAvailable(SysInfoHpFeaturelKeys.HP_FLOW_RATE, false);
                }
            }
        }

        return block;
    }
}
