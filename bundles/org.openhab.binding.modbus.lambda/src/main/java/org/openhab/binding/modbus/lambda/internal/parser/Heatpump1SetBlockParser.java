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
import org.openhab.binding.modbus.lambda.internal.dto.Heatpump1SetBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inlambda modbus data into a Heatpump1 Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
@NonNullByDefault
public class Heatpump1SetBlockParser extends AbstractBaseParser {

    public Heatpump1SetBlock parse(ModbusRegisterArray raw) {
        Heatpump1SetBlock block = new Heatpump1SetBlock();
        block.heatpump1seterrorquit = extractUInt16(raw, 0, (short) 0);
        return block;
    }
}
