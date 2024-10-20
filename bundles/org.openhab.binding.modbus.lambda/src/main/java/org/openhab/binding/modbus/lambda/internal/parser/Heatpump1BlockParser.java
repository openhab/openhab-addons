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
import org.openhab.binding.modbus.lambda.internal.dto.Heatpump1Block;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses inlambda modbus data into a Heatpump1 Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
@NonNullByDefault
public class Heatpump1BlockParser extends AbstractBaseParser {

    public Heatpump1Block parse(ModbusRegisterArray raw) {
        Heatpump1Block block = new Heatpump1Block();
        block.heatpump1ErrorState = extractUInt16(raw, 0, (short) 0);
        block.heatpump1ErrorNumber = extractUInt16(raw, 1, (short) 0);
        block.heatpump1State = extractUInt16(raw, 2, (short) 0);
        block.heatpump1OperatingState = extractUInt16(raw, 3, (short) 0);
        block.heatpump1TFlow = extractUInt16(raw, 4, (short) 0);
        block.heatpump1TReturn = extractUInt16(raw, 5, (short) 0);
        block.heatpump1VolSink = extractUInt16(raw, 6, (short) 0);
        block.heatpump1TEQin = extractUInt16(raw, 7, (short) 0);
        block.heatpump1TEQout = extractUInt16(raw, 8, (short) 0);
        block.heatpump1VolSource = extractUInt16(raw, 9, (short) 0);
        block.heatpump1CompressorRating = extractUInt16(raw, 10, (short) 0);
        block.heatpump1QpHeating = extractUInt16(raw, 11, (short) 0);
        block.heatpump1FIPowerConsumption = extractUInt16(raw, 12, (short) 0);
        block.heatpump1COP = extractUInt16(raw, 13, (short) 0);
        return block;
    }
}
