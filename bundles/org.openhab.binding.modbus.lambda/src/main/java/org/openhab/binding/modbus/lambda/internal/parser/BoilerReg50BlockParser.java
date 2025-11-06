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
import org.openhab.binding.modbus.lambda.internal.dto.BoilerReg50Block;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses modbus data into an BoilerReg50 Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
@NonNullByDefault
public class BoilerReg50BlockParser extends AbstractBaseParser {
    public BoilerReg50Block parse(ModbusRegisterArray raw) {
        BoilerReg50Block block = new BoilerReg50Block();
        block.boilerMaximumBoilerTemperature = extractInt16(raw, 0, (short) 0);
        return block;
    }
}
