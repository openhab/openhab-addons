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
package org.openhab.binding.modbus.stiebeleltron.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemStateBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inverter modbus data into a System State Block
 *
 * @author Paul Frank - Initial contribution
 *
 */
@NonNullByDefault
public class SystemStateBlockParser extends AbstractBaseParser {

    public SystemStateBlock parse(ModbusRegisterArray raw) {
        SystemStateBlock block = new SystemStateBlock();

        block.state = extractUInt16(raw, 0, 0);
        return block;
    }
}
