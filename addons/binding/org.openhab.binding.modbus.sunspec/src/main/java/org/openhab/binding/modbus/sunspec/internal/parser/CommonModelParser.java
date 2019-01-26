/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.nio.charset.Charset;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.modbus.sunspec.internal.SunSpecBindingConstants;
import org.openhab.binding.modbus.sunspec.internal.block.CommonModelBlock;
import org.openhab.io.transport.modbus.ModbusBitUtilities;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for the Common Message block
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
public class CommonModelParser implements SunspecParser<CommonModelBlock> {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(CommonModelParser.class);

    @Override
    public @NonNull CommonModelBlock parse(@NonNull ModbusRegisterArray raw) {

        CommonModelBlock block = new CommonModelBlock();

        block.setSunSpecDID(ModbusBitUtilities.extractStateFromRegisters(raw, 0, ValueType.UINT16).intValue());

        block.setLength(ModbusBitUtilities.extractStateFromRegisters(raw, 1, ValueType.UINT16).intValue());

        if (block.getLength() + SunSpecBindingConstants.MODEL_HEADER_SIZE != raw.size()) {
            logger.warn("Short read on common block loding. Expected size: {}, got: {}",
                    block.getLength() + SunSpecBindingConstants.MODEL_HEADER_SIZE, raw.size());
            return block;
        }

        // parse manufacturer, model and version
        block.setManufacturer(
                ModbusBitUtilities.extractStringFromRegisters(raw, 2, 32, Charset.forName("UTF-8")).toString());
        block.setModel(ModbusBitUtilities.extractStringFromRegisters(raw, 18, 32, Charset.forName("UTF-8")).toString());
        block.setVersion(
                ModbusBitUtilities.extractStringFromRegisters(raw, 42, 16, Charset.forName("UTF-8")).toString());
        block.setSerialNumber(
                ModbusBitUtilities.extractStringFromRegisters(raw, 50, 32, Charset.forName("UTF-8")).toString());

        block.setDeviceAddress(ModbusBitUtilities.extractStateFromRegisters(raw, 66, ValueType.UINT16).intValue());

        return block;

    }

}
