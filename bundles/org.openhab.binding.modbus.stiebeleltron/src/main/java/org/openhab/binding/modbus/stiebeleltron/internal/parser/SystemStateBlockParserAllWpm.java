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
package org.openhab.binding.modbus.stiebeleltron.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemStateBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemStateControlAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemStateControlAllWpm.SystemStateFeatureKeys;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * Parses modbus system state data of a a WPM compatible heat pump into a System State Block
 *
 * @author Thomas Burri - Initial contribution
 *
 */
@NonNullByDefault
public class SystemStateBlockParserAllWpm extends AbstractBaseParser {

    public SystemStateBlockAllWpm parse(ModbusRegisterArray raw, SystemStateControlAllWpm control) {
        SystemStateBlockAllWpm block = new SystemStateBlockAllWpm();

        block.state = extractUInt16(raw, 0, 0);
        block.powerOff = extractUInt16(raw, 1, 0);
        if (control.featureAvailable(SystemStateFeatureKeys.OPERATING_STATUS)) {
            block.operatingStatus = extractUInt16(raw, 2, 0);
            if (block.operatingStatus == 32768) {
                control.setFeatureAvailable(SystemStateFeatureKeys.OPERATING_STATUS, false);
            }
        }
        block.faultStatus = extractUInt16(raw, 3, 0);

        // Stiebel Eltron Modbus User Manual tells type 6/unsigned short, but has range -4 to 0, so using extractInt16!
        block.busStatus = extractInt16(raw, 4, (short) 0);

        if (control.featureAvailable(SystemStateFeatureKeys.DEFROST_INITIATED)) {
            block.defrostInitiated = extractUInt16(raw, 5, 0);
            if (block.defrostInitiated == 32768) {
                control.setFeatureAvailable(SystemStateFeatureKeys.DEFROST_INITIATED, false);
            }
        }

        block.activeError = extractUInt16(raw, 6, 0);
        return block;
    }
}
