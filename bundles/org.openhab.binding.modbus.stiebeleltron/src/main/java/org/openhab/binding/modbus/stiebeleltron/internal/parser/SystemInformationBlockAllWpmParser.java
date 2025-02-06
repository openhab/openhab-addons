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
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses modbus system information data of a WPM/WPM3/WPM3i compatible heat pump into a System Information Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
@NonNullByDefault
public class SystemInformationBlockAllWpmParser extends AbstractBaseParser {

    private boolean mWpmHeatPump = false;

    /**
     * Instances of this parser shall know if they handle a WPM heat pump with additional
     * information about heat pumps and circuits.
     *
     * @param boolean flag telling if the parse shall work on a WPM heat pump with additional data
     */
    public SystemInformationBlockAllWpmParser(boolean isWpmHeatPump) {
        mWpmHeatPump = isWpmHeatPump;
    }

    public SystemInformationBlockAllWpm parse(ModbusRegisterArray raw) {
        SystemInformationBlockAllWpm block = new SystemInformationBlockAllWpm();

        block.temperatureFe7 = extractInt16(raw, 0, (short) 0);
        block.temperatureFe7SetPoint = extractInt16(raw, 1, (short) 0);
        block.temperatureFek = extractInt16(raw, 2, (short) 0);
        block.temperatureFekSetPoint = extractInt16(raw, 3, (short) 0);
        block.humidityFek = extractInt16(raw, 4, (short) 0);
        block.dewpointFek = extractInt16(raw, 5, (short) 0);
        block.temperatureOutdoor = extractInt16(raw, 6, (short) 0);
        block.temperatureHc1 = extractInt16(raw, 7, (short) 0);

        // documents tell "HC1 Set Point temperature" of WPM3i would be on address 509, but on my WPM3i it is also on
        // 510
        block.temperatureHc1SetPoint = extractInt16(raw, 9, (short) 0);
        block.temperatureHc2 = extractInt16(raw, 10, (short) 0);
        block.temperatureHc2SetPoint = extractInt16(raw, 11, (short) 0);

        block.temperatureFlowHp = extractInt16(raw, 12, (short) 0);
        block.temperatureFlowNhz = extractInt16(raw, 13, (short) 0);
        block.temperatureFlow = extractInt16(raw, 14, (short) 0);
        block.temperatureReturn = extractInt16(raw, 15, (short) 0);
        block.temperatureFixedSetPoint = extractInt16(raw, 16, (short) 0);

        block.temperatureBuffer = extractInt16(raw, 17, (short) 0);
        block.temperatureBufferSetPoint = extractInt16(raw, 18, (short) 0);

        block.pressureHeating = extractInt16(raw, 19, (short) 0);
        block.flowRate = extractInt16(raw, 20, (short) 0);

        block.temperatureWater = extractInt16(raw, 21, (short) 0);
        block.temperatureWaterSetPoint = extractInt16(raw, 22, (short) 0);

        block.temperatureFanCooling = extractInt16(raw, 23, (short) 0);
        block.temperatureFanCoolingSetPoint = extractInt16(raw, 24, (short) 0);
        block.temperatureAreaCooling = extractInt16(raw, 25, (short) 0);
        block.temperatureAreaCoolingSetPoint = extractInt16(raw, 26, (short) 0);

        block.temperatureCollectorSolar = extractInt16(raw, 27, (short) 0);
        block.temperatureCylinderSolar = extractInt16(raw, 28, (short) 0);
        block.runtimeSolar = extractUInt16(raw, 29, 0);

        block.temperatureExtHeatSource = extractInt16(raw, 30, (short) 0);
        block.temperatureExtHeatSourceSetPoint = extractInt16(raw, 31, (short) 0);

        block.lowerHeatingLimit = extractInt16(raw, 32, (short) 0);
        block.lowerWaterLimit = extractInt16(raw, 33, (short) 0);

        block.runtimeExtHeatSource = extractUInt16(raw, 34, 0);

        block.temperatureSource = extractInt16(raw, 35, (short) 0);
        block.temperatureSourceMin = extractInt16(raw, 36, (short) 0);
        block.pressureSource = extractInt16(raw, 37, (short) 0);

        block.temperatureHotgas = extractInt16(raw, 38, (short) 0);
        block.pressureHigh = extractInt16(raw, 39, (short) 0);
        block.pressureLow = extractInt16(raw, 40, (short) 0);

        if (mWpmHeatPump) {
            block.hp1TemperatureReturn = extractInt16(raw, 41, (short) 0);
            block.hp1TemperatureFlow = extractInt16(raw, 42, (short) 0);
            block.hp1TemperatureHotgas = extractInt16(raw, 43, (short) 0);
            block.hp1PressureLow = extractInt16(raw, 44, (short) 0);
            block.hp1PressureMean = extractInt16(raw, 45, (short) 0);
            block.hp1PressureHigh = extractInt16(raw, 46, (short) 0);
            block.hp1FlowRate = extractInt16(raw, 47, (short) 0);
        }
        return block;
    }
}
