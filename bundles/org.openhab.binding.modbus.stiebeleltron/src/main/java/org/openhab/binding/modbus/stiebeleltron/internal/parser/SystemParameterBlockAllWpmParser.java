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
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemParameterBlockAllWpm;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses modbus system parameter data of a WPM/WPM3/WPM3i compatible heat pump into a System Parameter Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
@NonNullByDefault
public class SystemParameterBlockAllWpmParser extends AbstractBaseParser {

    public SystemParameterBlockAllWpm parse(ModbusRegisterArray raw) {
        SystemParameterBlockAllWpm block = new SystemParameterBlockAllWpm();

        block.operationMode = extractUInt16(raw, 0, 0);
        block.comfortTemperatureHeating = extractInt16(raw, 1, (short) 0);
        block.ecoTemperatureHeating = extractInt16(raw, 2, (short) 0);
        block.heatingCurveRiseHc1 = extractInt16(raw, 3, (short) 0);
        block.comfortTemperatureHeatingHc2 = extractInt16(raw, 4, (short) 0);
        block.ecoTemperatureHeatingHc2 = extractInt16(raw, 5, (short) 0);
        block.heatingCurveRiseHc2 = extractInt16(raw, 6, (short) 0);
        block.fixedValueOperation = extractInt16(raw, 7, (short) 0);
        block.dualModeTemperatureHeating = extractInt16(raw, 8, (short) 0);
        block.comfortTemperatureWater = extractInt16(raw, 9, (short) 0);
        block.ecoTemperatureWater = extractInt16(raw, 10, (short) 0);
        block.hotwaterStages = extractUInt16(raw, 11, 0);
        block.hotwaterDualModeTemperature = extractInt16(raw, 12, (short) 0);
        block.flowTemperatureAreaCooling = extractInt16(raw, 13, (short) 0);
        block.flowTemperatureHysteresisAreaCooling = extractInt16(raw, 14, (short) 0);
        block.roomTemperatureAreaCooling = extractInt16(raw, 15, (short) 0);
        block.flowTemperatureFanCooling = extractInt16(raw, 16, (short) 0);
        block.flowTemperatureHysteresisFanCooling = extractInt16(raw, 17, (short) 0);
        block.roomTemperatureFanCooling = extractInt16(raw, 18, (short) 0);
        block.reset = extractUInt16(raw, 19, 0);
        block.restartIsg = extractUInt16(raw, 20, 0);

        return block;
    }
}
