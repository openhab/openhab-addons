/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.modbus.e3dc.internal.dto;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;
import org.openhab.core.io.transport.modbus.ValueBuffer;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * The {@link PowerBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PowerBlock implements Data {
    public QuantityType<Power> pvPowerSupply;
    public QuantityType<Power> batteryPowerSupply;
    public QuantityType<Power> batteryPowerConsumption;
    public QuantityType<Power> householdPowerConsumption;
    public QuantityType<Power> gridPowerConsumpition;
    public QuantityType<Power> gridPowerSupply;
    public QuantityType<Power> externalPowerSupply;
    public QuantityType<Power> wallboxPowerConsumption;
    public QuantityType<Power> wallboxPVPowerConsumption;
    public QuantityType<Dimensionless> autarky;
    public QuantityType<Dimensionless> selfConsumption;
    public QuantityType<Dimensionless> batterySOC;

    /**
     * For decoding see Modbus Register Mapping Chapter 3.1.2 page 14
     *
     * @param bArray - Modbus Registers as bytes from 40067 to 40083
     */
    public PowerBlock(byte[] bArray) {
        // index handling to calculate the correct start index
        ValueBuffer wrap = ValueBuffer.wrap(bArray);

        // int32_swap value = 4 byte
        long pvPowerSupplyL = wrap.getSInt32Swap();

        /*
         * int32_swap value don't provide negative values!
         * Positive value - Battery is charging = Power consumer
         * Negative value - Battery is discharging = Power supplier
         */
        pvPowerSupply = QuantityType.valueOf(pvPowerSupplyL, Units.WATT);
        long batteryPower = wrap.getSInt32Swap();
        if (batteryPower > 0) {
            // Battery is charging so Power is consumed by Battery
            batteryPowerSupply = QuantityType.valueOf(0, Units.WATT);
            batteryPowerConsumption = QuantityType.valueOf(batteryPower, Units.WATT);
        } else {
            // Battery is discharging so Power is provided by Battery
            batteryPowerSupply = QuantityType.valueOf(batteryPower * -1, Units.WATT);
            batteryPowerConsumption = QuantityType.valueOf(0, Units.WATT);
        }

        // int32_swap value = 4 byte
        long householdPowerConsumptionL = wrap.getSInt32Swap();
        householdPowerConsumption = QuantityType.valueOf(householdPowerConsumptionL, Units.WATT);

        /*
         * int32_swap value don't provide negative values!
         * Positive value - Power provided towards Grid = Power consumer
         * Negative value - Power requested from Grid = Power supplier
         */
        long gridPower = wrap.getSInt32Swap();
        if (gridPower > 0) {
            // Power is provided by Grid
            gridPowerSupply = QuantityType.valueOf(gridPower, Units.WATT);
            gridPowerConsumpition = QuantityType.valueOf(0, Units.WATT);
        } else {
            // Power is consumed by Grid
            gridPowerConsumpition = QuantityType.valueOf(gridPower * -1, Units.WATT);
            gridPowerSupply = QuantityType.valueOf(0, Units.WATT);
        }

        // int32_swap value = 4 byte
        externalPowerSupply = QuantityType.valueOf(wrap.getSInt32Swap(), Units.WATT);

        // int32_swap value = 4 byte
        wallboxPowerConsumption = QuantityType.valueOf(wrap.getSInt32Swap(), Units.WATT);

        // int32_swap value = 4 byte
        wallboxPVPowerConsumption = QuantityType.valueOf(wrap.getSInt32Swap(), Units.WATT);

        // unit8 + uint8 - one register with split value for Autarky & Self Consumption
        autarky = QuantityType.valueOf(wrap.getSInt8(), Units.PERCENT);
        selfConsumption = QuantityType.valueOf(wrap.getSInt8(), Units.PERCENT);

        // uint16 for Battery State of Charge
        batterySOC = QuantityType.valueOf(wrap.getSInt16(), Units.PERCENT);
    }
}
