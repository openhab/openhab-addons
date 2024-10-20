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
import org.openhab.binding.modbus.lambda.internal.dto.HeatingCircuit1Block;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses lambda
 * modbus data into a Heatpump1 Block
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 *
 */
@NonNullByDefault
public class HeatingCircuit1BlockParser extends AbstractBaseParser {

    public HeatingCircuit1Block parse(ModbusRegisterArray raw) {
        HeatingCircuit1Block block = new HeatingCircuit1Block();
        block.heatingcircuit1ErrorNumber = extractUInt16(raw, 0, (short) 0);
        block.heatingcircuit1OperatingState = extractUInt16(raw, 1, (short) 0);
        block.heatingcircuit1FlowLineTemperature = extractUInt16(raw, 2, (short) 0);
        block.heatingcircuit1ReturnLineTemperature = extractUInt16(raw, 3, (short) 0);
        block.heatingcircuit1RoomDeviceTemperature = extractUInt16(raw, 4, (short) 0);
        block.heatingcircuit1SetpointFlowLineTemperature = extractUInt16(raw, 5, (short) 0);
        block.heatingcircuit1OperatingMode = extractUInt16(raw, 6, (short) 0);
        return block;
    }
}
