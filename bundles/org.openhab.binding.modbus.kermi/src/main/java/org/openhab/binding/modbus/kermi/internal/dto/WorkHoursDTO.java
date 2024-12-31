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

import javax.measure.quantity.Time;

import org.openhab.binding.modbus.kermi.internal.modbus.Data;
import org.openhab.core.io.transport.modbus.ValueBuffer;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * The {@link WorkHoursDTO} Data object for Kermi Xcenter State Block
 *
 * @author Kai Neuhaus - Initial contribution
 */
public class WorkHoursDTO implements Data {

    public QuantityType<Time> workHoursFan;
    public QuantityType<Time> workHoursStorageLoadingPump;
    public QuantityType<Time> workHoursCompressor;

    public WorkHoursDTO(byte[] bArray) {

        ValueBuffer wrap = ValueBuffer.wrap(bArray);

        workHoursFan = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 0.1), Units.HOUR);
        workHoursStorageLoadingPump = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 0.1), Units.HOUR);
        workHoursCompressor = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 0.1), Units.HOUR);
    }
}
