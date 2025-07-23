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
package org.openhab.binding.modbus.kermi.internal.dto;

import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;

import org.openhab.binding.modbus.kermi.internal.modbus.Data;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ValueBuffer;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@link PvDTO} Data object for Kermi Xcenter
 *
 * @author Kai Neuhaus - Initial contribution
 */
public class PvDTO implements Data {

    public OnOffType pvModulationActive;
    public QuantityType<Power> pvModulationPower;
    public QuantityType<Temperature> pvTargetTemperatureHeating;
    public QuantityType<Temperature> pvTargetTemperatureDrinkingwater;

    public PvDTO(byte[] bArray) {
        int modActive = ModbusBitUtilities.extractBit(bArray, 0);
        pvModulationActive = modActive == 0 ? OnOffType.OFF : OnOffType.ON;

        ValueBuffer wrap = ValueBuffer.wrap(bArray);
        // skip first bit -> modActive-Value
        wrap.position(2);

        pvModulationPower = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 0.1), Units.WATT);
        pvTargetTemperatureHeating = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 0.1), SIUnits.CELSIUS);
        pvTargetTemperatureDrinkingwater = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 0.1),
                SIUnits.CELSIUS);
    }
}
