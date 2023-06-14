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
package org.openhab.binding.modbus.sunspec.internal.parser;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sunspec.internal.SunSpecConstants;
import org.openhab.binding.modbus.sunspec.internal.dto.CommonModelBlock;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for the Common Message block
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
@NonNullByDefault
public class CommonModelParser extends AbstractBaseParser implements SunspecParser<CommonModelBlock> {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(CommonModelParser.class);

    @Override
    public CommonModelBlock parse(ModbusRegisterArray raw) {
        CommonModelBlock block = new CommonModelBlock();

        block.sunSpecDID = extractUInt16(raw, 0, 0);

        block.length = extractUInt16(raw, 1, raw.size() - SunSpecConstants.MODEL_HEADER_SIZE);

        if (block.length + SunSpecConstants.MODEL_HEADER_SIZE != raw.size()) {
            logger.warn("Short read on common block loding. Expected size: {}, got: {}",
                    block.length + SunSpecConstants.MODEL_HEADER_SIZE, raw.size());
            return block;
        }

        // parse manufacturer, model and version
        block.manufacturer = ModbusBitUtilities.extractStringFromRegisters(raw, 2, 32, StandardCharsets.UTF_8);
        block.model = ModbusBitUtilities.extractStringFromRegisters(raw, 18, 32, StandardCharsets.UTF_8);
        block.version = ModbusBitUtilities.extractStringFromRegisters(raw, 42, 16, StandardCharsets.UTF_8);
        block.serialNumber = ModbusBitUtilities.extractStringFromRegisters(raw, 50, 32, StandardCharsets.UTF_8);

        block.deviceAddress = extractUInt16(raw, 66, 1);

        return block;
    }
}
