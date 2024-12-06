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
package org.openhab.binding.modbus.kermi.internal.dto;

import static org.openhab.binding.modbus.kermi.internal.modbus.KermiModbusConstans.ALARM_REG_SIZE;

import org.openhab.binding.modbus.kermi.internal.modbus.Data;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.library.types.OnOffType;

/**
 * The {@link AlarmDTO} Data object for Kermi Xcenter
 *
 * @author Kai Neuhaus - Initial contribution
 */
public class AlarmDTO implements Data {

    public OnOffType alarmIsActive;

    public AlarmDTO(byte[] bArray) {
        int status = ModbusBitUtilities.extractBit(bArray, ALARM_REG_SIZE);
        alarmIsActive = OnOffType.from(status != 0);
    }
}
