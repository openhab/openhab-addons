/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.nio.ByteBuffer;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;

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
        ByteBuffer wrap = ByteBuffer.wrap(bArray);

        // int32_swap value = 4 byte
        long pvPowerSupplyL = DataConverter.getInt32Swap(wrap);

        /*
         * int32_swap value don't provide negative values!
         * Positive value - Battery is charging = Power consumer
         * Negative value - Battery is discharging = Power supplier
         */
        pvPowerSupply = QuantityType.valueOf(pvPowerSupplyL, SmartHomeUnits.WATT);
        long batteryPower = DataConverter.getInt32Swap(wrap);
        if (batteryPower > 0) {
            // Battery is charging so Power is consumed by Battery
            batteryPowerSupply = QuantityType.valueOf(0, SmartHomeUnits.WATT);
            batteryPowerConsumption = QuantityType.valueOf(batteryPower, SmartHomeUnits.WATT);
        } else {
            // Battery is discharging so Power is provided by Battery
            batteryPowerSupply = QuantityType.valueOf(batteryPower * -1, SmartHomeUnits.WATT);
            batteryPowerConsumption = QuantityType.valueOf(0, SmartHomeUnits.WATT);
        }

        // int32_swap value = 4 byte
        long householdPowerConsumptionL = DataConverter.getInt32Swap(wrap);
        householdPowerConsumption = QuantityType.valueOf(householdPowerConsumptionL, SmartHomeUnits.WATT);

        /*
         * int32_swap value don't provide negative values!
         * Positive value - Power provided towards Grid = Power consumer
         * Negative value - Power requested from Grid = Power supplier
         */
        long gridPower = DataConverter.getInt32Swap(wrap);
        if (gridPower > 0) {
            // Power is provided by Grid
            gridPowerSupply = QuantityType.valueOf(gridPower, SmartHomeUnits.WATT);
            gridPowerConsumpition = QuantityType.valueOf(0, SmartHomeUnits.WATT);
        } else {
            // Power is consumed by Grid
            gridPowerConsumpition = QuantityType.valueOf(gridPower * -1, SmartHomeUnits.WATT);
            gridPowerSupply = QuantityType.valueOf(0, SmartHomeUnits.WATT);
        }

        // int32_swap value = 4 byte
        externalPowerSupply = QuantityType.valueOf(DataConverter.getInt32Swap(wrap), SmartHomeUnits.WATT);

        // int32_swap value = 4 byte
        wallboxPowerConsumption = QuantityType.valueOf(DataConverter.getInt32Swap(wrap), SmartHomeUnits.WATT);

        // int32_swap value = 4 byte
        wallboxPVPowerConsumption = QuantityType.valueOf(DataConverter.getInt32Swap(wrap), SmartHomeUnits.WATT);

        // unit8 + uint8 - one register with split value for Autarky & Self Consumption
        autarky = QuantityType.valueOf(wrap.get(), SmartHomeUnits.PERCENT);
        selfConsumption = QuantityType.valueOf(wrap.get(), SmartHomeUnits.PERCENT);

        // uint16 for Battery State of Charge
        batterySOC = QuantityType.valueOf(wrap.getShort(), SmartHomeUnits.PERCENT);
    }
}
