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
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inverter modbus data into a SystemB Information lock
 *
 * @author Paul Frank - Initial contribution
 *
 */
@NonNullByDefault
public class SystemInfromationBlockParser extends AbstractBaseParser {

    public SystemInformationBlock parse(ModbusRegisterArray raw) {
        SystemInformationBlock block = new SystemInformationBlock();

        block.temperatureFek = extractInt16(raw, 2, (short) 0);
        block.temperatureFekSetPoint = extractInt16(raw, 3, (short) 0);
        block.humidityFek = extractInt16(raw, 4, (short) 0);
        block.dewpointFek = extractInt16(raw, 5, (short) 0);
        block.temperatureOutdoor = extractInt16(raw, 6, (short) 0);
        block.temperatureHk1 = extractInt16(raw, 7, (short) 0);
        block.temperatureHk1SetPoint = extractInt16(raw, 9, (short) 0);
        block.temperatureSupply = extractInt16(raw, 12, (short) 0);
        block.temperatureReturn = extractInt16(raw, 15, (short) 0);
        block.temperatureWater = extractInt16(raw, 21, (short) 0);
        block.temperatureWaterSetPoint = extractInt16(raw, 22, (short) 0);
        block.temperatureSource = extractInt16(raw, 35, (short) 0);
        return block;
    }
}
