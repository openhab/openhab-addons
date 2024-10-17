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

import javax.measure.quantity.Temperature;

import org.openhab.binding.modbus.kermi.internal.modbus.Data;
import org.openhab.core.io.transport.modbus.ValueBuffer;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;

/**
 * The {@link EnergySourceDTO} Data object for Kermi Xcenter
 *
 * @author Kai Neuhaus - Initial contribution
 */
public class EnergySourceDTO implements Data {

    public QuantityType<Temperature> exitTemperature;
    public QuantityType<Temperature> incomingTemperature;
    public QuantityType<Temperature> outsideTemperature;

    public EnergySourceDTO(byte[] bArray) {
        ValueBuffer wrap = ValueBuffer.wrap(bArray);
        exitTemperature = QuantityType.valueOf(DataConverter.getSDoubleValue(wrap, 0.1), SIUnits.CELSIUS);
        incomingTemperature = QuantityType.valueOf(DataConverter.getSDoubleValue(wrap, 0.1), SIUnits.CELSIUS);
        outsideTemperature = QuantityType.valueOf(DataConverter.getSDoubleValue(wrap, 0.1), SIUnits.CELSIUS);
    }
}
