/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemParameterBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inverter modbus data into a System Parameter Block
 *
 * @author Paul Frank - Initial contribution
 *
 */
@NonNullByDefault
public class SystemParameterBlockParser extends AbstractBaseParser {

    public SystemParameterBlock parse(ModbusRegisterArray raw) {
        SystemParameterBlock block = new SystemParameterBlock();

        block.operationMode = extractUInt16(raw, 0, 0);
        block.comfortTemperatureHeating = extractInt16(raw, 1, (short) 0);
        block.ecoTemperatureHeating = extractInt16(raw, 2, (short) 0);
        block.comfortTemperatureWater = extractInt16(raw, 9, (short) 0);
        block.ecoTemperatureWater = extractInt16(raw, 10, (short) 0);
        return block;
    }
}
