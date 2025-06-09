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
import org.openhab.binding.modbus.lambda.internal.dto.SolarBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parser for solar thermic component data from modbus registers
 */
@NonNullByDefault
public class SolarBlockParser extends AbstractBaseParser {

    public SolarBlock parse(ModbusRegisterArray registers) {
        SolarBlock block = new SolarBlock();

        // Parse registers according to the modbus description document
        // Base address is 4000, registers are mapped as follows:
        // 4000: Error number (uint16)
        // 4001: Operating state (uint16)
        // 4002: Collector temperature (int16, scaled by 10)
        // 4003: Storage temperature (int16, scaled by 10)
        // 4004: Pump speed (uint16, %)
        // 4005-4006: Heat quantity (uint32, scaled by 10)
        // 4007: Power output (uint16)
        // 4008: Operating hours (uint16)

        block.solarErrorNumber = extractUInt16(registers, 0, 0);
        block.solarOperatingState = extractUInt16(registers, 1, 0);
        block.solarCollectorTemperature = extractInt16(registers, 2, (short)0);
        block.solarStorageTemperature = extractInt16(registers, 3, (short)0);
        block.solarPumpSpeed = extractUInt16(registers, 4, 0);
        block.solarHeatQuantity = extractUInt32(registers, 5, 0).intValue();
        block.solarPowerOutput = extractUInt16(registers, 7, 0);
        block.solarOperatingHours = extractUInt16(registers, 8, 0);

        return block;
    }
}