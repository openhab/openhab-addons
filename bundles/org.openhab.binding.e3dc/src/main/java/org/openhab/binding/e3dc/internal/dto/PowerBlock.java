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
package org.openhab.binding.e3dc.internal.dto;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.e3dc.internal.modbus.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PowerBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
public class PowerBlock implements Data {
    private final Logger logger = LoggerFactory.getLogger(PowerBlock.class);
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

    public PowerBlock(byte[] bArray) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < bArray.length; i++) {
            // logger.info("Byte {} is {}", i, bArray[i]);
            s.append(bArray[i] + " : ");
        }
        logger.info("PowerBlock: {}", s.toString());
        long pvPowerSupplyL = DataConverter.getInt32_swap(bArray, 0);
        pvPowerSupply = new QuantityType<Power>(pvPowerSupplyL, SmartHomeUnits.WATT);
        long batteryPower = DataConverter.getInt32_swap(bArray, 4);
        if (batteryPower < 0) {
            batteryPowerSupply = new QuantityType<Power>(batteryPower * -1, SmartHomeUnits.WATT);
            batteryPowerConsumption = new QuantityType<Power>(0, SmartHomeUnits.WATT);
        } else {
            batteryPowerSupply = new QuantityType<Power>(0, SmartHomeUnits.WATT);
            batteryPowerConsumption = new QuantityType<Power>(batteryPower, SmartHomeUnits.WATT);
        }
        long householdPowerConsumptionL = DataConverter.getInt32_swap(bArray, 8);
        householdPowerConsumption = new QuantityType<Power>(householdPowerConsumptionL, SmartHomeUnits.WATT);
        long gridPower = DataConverter.getInt32_swap(bArray, 12);
        if (gridPower < 0) {
            gridPowerConsumpition = new QuantityType<Power>(0, SmartHomeUnits.WATT);
            gridPowerSupply = new QuantityType<Power>(gridPower * -1, SmartHomeUnits.WATT);
        } else {
            gridPowerConsumpition = new QuantityType<Power>(gridPower, SmartHomeUnits.WATT);
            gridPowerSupply = new QuantityType<Power>(0, SmartHomeUnits.WATT);
        }
        logger.info("PV {} Battery {} Grid {} House {}", pvPowerSupplyL, batteryPower, gridPower,
                householdPowerConsumptionL);
        externalPowerSupply = new QuantityType<Power>(DataConverter.getInt32_swap(bArray, 16), SmartHomeUnits.WATT);
        wallboxPowerConsumption = new QuantityType<Power>(DataConverter.getInt32_swap(bArray, 20), SmartHomeUnits.WATT);
        wallboxPVPowerConsumption = new QuantityType<Power>(DataConverter.getInt32_swap(bArray, 24),
                SmartHomeUnits.WATT);
        autarky = new QuantityType<Dimensionless>(bArray[28], SmartHomeUnits.PERCENT);
        selfConsumption = new QuantityType<Dimensionless>(bArray[29], SmartHomeUnits.PERCENT);
        batterySOC = new QuantityType<Dimensionless>(DataConverter.getIntValue(bArray, 30), SmartHomeUnits.PERCENT);
    }
}
