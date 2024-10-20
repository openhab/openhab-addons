/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.modbus.lambda.internal.dto.Boiler1Block;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses lambda modbus data into a Boiler1 Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
@NonNullByDefault
public class Boiler1BlockParser extends AbstractBaseParser {

    public Boiler1Block parse(ModbusRegisterArray raw) {
        Boiler1Block block = new Boiler1Block();
        block.boiler1ErrorNumber = extractUInt16(raw, 0, (short) 0);
        block.boiler1OperatingState = extractUInt16(raw, 1, (short) 0);
        block.boiler1ActualHighTemperature = extractUInt16(raw, 2, (short) 0);
        block.boiler1ActualLowTemperature = extractUInt16(raw, 3, (short) 0);
        return block;
    }
}
