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
/**
* Parses lambda modbus data into a Solar Block
*
* @author Paul Frank - Initial contribution
* @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
*
*/

package org.openhab.binding.modbus.lambda.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.lambda.internal.dto.SolarBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parser for solar thermic component data from modbus registers
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
@NonNullByDefault
public class SolarBlockParser extends AbstractBaseParser {

    public SolarBlock parse(ModbusRegisterArray registers) {
        SolarBlock block = new SolarBlock();
        block.solarErrorNumber = extractUInt16(registers, 0, 0);
        block.solarOperatingState = extractUInt16(registers, 1, 0);
        block.solarCollectorTemperature = extractInt16(registers, 2, (short) 0);
        // Two different versions for usage in different configurations of the manufacturer
        block.solarBuffer1Temperature = extractInt16(registers, 3, (short) 0);
        block.solarBuffer2Temperature = extractUInt16(registers, 4, 0);

        return block;
    }
}
