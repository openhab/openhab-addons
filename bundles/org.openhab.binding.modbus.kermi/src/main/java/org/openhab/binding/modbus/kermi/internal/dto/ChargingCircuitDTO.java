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

import static org.openhab.core.library.unit.Units.LITRE_PER_MINUTE;

import javax.measure.quantity.Temperature;

import org.openhab.binding.modbus.kermi.internal.modbus.Data;
import org.openhab.core.io.transport.modbus.ValueBuffer;
import org.openhab.core.library.dimension.VolumetricFlowRate;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;

/**
 * The {@link ChargingCircuitDTO} Data object for Kermi Xcenter
 *
 * @author Kai Neuhaus - Initial contribution
 */
public class ChargingCircuitDTO implements Data {

    public QuantityType<Temperature> flowTemperature;
    public QuantityType<Temperature> returnFlowTemperature;
    public QuantityType<VolumetricFlowRate> flowSpeed;

    public ChargingCircuitDTO(byte[] bArray) {
        ValueBuffer wrap = ValueBuffer.wrap(bArray);
        flowTemperature = QuantityType.valueOf(DataConverter.getSDoubleValue(wrap, 0.1), SIUnits.CELSIUS);
        returnFlowTemperature = QuantityType.valueOf(DataConverter.getSDoubleValue(wrap, 0.1), SIUnits.CELSIUS);
        flowSpeed = QuantityType.valueOf(DataConverter.getSDoubleValue(wrap, 0.1), LITRE_PER_MINUTE);
    }
}
