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

import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Power;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.e3dc.internal.modbus.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link StringBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
public class StringBlock implements Data {
    private final Logger logger = LoggerFactory.getLogger(StringBlock.class);

    public QuantityType<ElectricPotential> string1Volt;
    public QuantityType<ElectricPotential> string2Volt;
    public QuantityType<ElectricPotential> string3Volt;
    public QuantityType<ElectricCurrent> string1Ampere;
    public QuantityType<ElectricCurrent> string2Ampere;
    public QuantityType<ElectricCurrent> string3Ampere;
    public QuantityType<Power> string1Watt;
    public QuantityType<Power> string2Watt;
    public QuantityType<Power> string3Watt;

    public StringBlock(byte[] bArray) {
        string1Volt = new QuantityType<ElectricPotential>(DataConverter.getIntValue(bArray, 0), SmartHomeUnits.VOLT);
        string2Volt = new QuantityType<ElectricPotential>(DataConverter.getIntValue(bArray, 2), SmartHomeUnits.VOLT);
        string3Volt = new QuantityType<ElectricPotential>(DataConverter.getIntValue(bArray, 4), SmartHomeUnits.VOLT);
        string1Ampere = new QuantityType<ElectricCurrent>(DataConverter.getIntValue(bArray, 6), SmartHomeUnits.AMPERE);
        string2Ampere = new QuantityType<ElectricCurrent>(DataConverter.getIntValue(bArray, 8), SmartHomeUnits.AMPERE);
        string3Ampere = new QuantityType<ElectricCurrent>(DataConverter.getIntValue(bArray, 10), SmartHomeUnits.AMPERE);
        string1Watt = new QuantityType<Power>(DataConverter.getIntValue(bArray, 10), SmartHomeUnits.WATT);
        string2Watt = new QuantityType<Power>(DataConverter.getIntValue(bArray, 10), SmartHomeUnits.WATT);
        string3Watt = new QuantityType<Power>(DataConverter.getIntValue(bArray, 10), SmartHomeUnits.WATT);
    }
}
