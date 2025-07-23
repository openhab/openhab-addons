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

import org.openhab.binding.modbus.kermi.internal.modbus.Data;
import org.openhab.core.io.transport.modbus.ValueBuffer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * The {@link PowerDTO} Data object for Kermi Xcenter State Block
 *
 * @author Kai Neuhaus - Initial contribution
 */
public class PowerDTO implements Data {

    public DecimalType cop;
    public DecimalType copHeating;
    public DecimalType copDrinkingwater;
    public DecimalType copCooling;

    public QuantityType<Power> power;
    public QuantityType<Power> powerHeating;
    public QuantityType<Power> powerDrinkingwater;
    public QuantityType<Power> powerCooling;

    public QuantityType<Power> electricPower;
    public QuantityType<Power> electricPowerHeating;
    public QuantityType<Power> electricPowerDrinkingwater;
    public QuantityType<Power> electricPowerCooling;

    public PowerDTO(byte[] bArray) {
        ValueBuffer wrap = ValueBuffer.wrap(bArray);

        cop = new DecimalType(DataConverter.getUDoubleValue(wrap, 0.1));
        copHeating = new DecimalType(DataConverter.getUDoubleValue(wrap, 0.1));
        copDrinkingwater = new DecimalType(DataConverter.getUDoubleValue(wrap, 0.1));
        copCooling = new DecimalType(DataConverter.getUDoubleValue(wrap, 0.1));

        power = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 100), Units.WATT);
        powerHeating = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 100), Units.WATT);
        powerDrinkingwater = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 100), Units.WATT);
        powerCooling = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 100), Units.WATT);

        electricPower = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 100), Units.WATT);
        electricPowerHeating = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 100), Units.WATT);
        electricPowerDrinkingwater = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 100), Units.WATT);
        electricPowerCooling = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 100), Units.WATT);
    }
}
