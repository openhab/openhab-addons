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
import org.openhab.binding.modbus.lambda.internal.dto.Boiler1MtBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses inverter modbus data into an Boiler1Mt Block -
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
@NonNullByDefault
public class Boiler1MtBlockParser extends AbstractBaseParser {
    private final Logger logger = LoggerFactory.getLogger(Boiler1MtBlockParser.class);

    public Boiler1MtBlock parse(ModbusRegisterArray raw) {
        logger.trace("Boiler1MtBlockParser");
        Boiler1MtBlock block = new Boiler1MtBlock();

        block.boiler1MaximumBoilerTemperature = extractUInt16(raw, 0, (short) 0);
        return block;
    }
}
