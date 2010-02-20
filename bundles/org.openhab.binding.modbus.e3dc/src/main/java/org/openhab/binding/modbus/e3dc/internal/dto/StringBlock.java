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

import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;

/**
 * The {@link StringBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class StringBlock implements Data {
    public QuantityType<ElectricPotential> string1Volt;
    public QuantityType<ElectricPotential> string2Volt;
    public QuantityType<ElectricPotential> string3Volt;
    public QuantityType<ElectricCurrent> string1Ampere;
    public QuantityType<ElectricCurrent> string2Ampere;
    public QuantityType<ElectricCurrent> string3Ampere;
    public QuantityType<Power> string1Watt;
    public QuantityType<Power> string2Watt;
    public QuantityType<Power> string3Watt;

    /**
     * For decoding see Modbus Register Mapping Chapter 3.1.2 page 14-16
     *
     * @param bArray - Modbus Registers as bytes from 40096 to 40104
     */
    public StringBlock(byte[] bArray) {
        ByteBuffer wrap = ByteBuffer.wrap(bArray);
        // straight forward - for each String the values Volt, Ampere and then Watt. All unt16 = 2 bytes values
        string1Volt = QuantityType.valueOf(DataConverter.getUInt16Value(wrap), SmartHomeUnits.VOLT);
        string2Volt = QuantityType.valueOf(DataConverter.getUInt16Value(wrap), SmartHomeUnits.VOLT);
        string3Volt = QuantityType.valueOf(DataConverter.getUInt16Value(wrap), SmartHomeUnits.VOLT);
        // E3DC Modbus Spec chapter 3.1.2, page 16 - Ampere values shall be handled with factor 0.01
        string1Ampere = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 0.01), SmartHomeUnits.AMPERE);
        string2Ampere = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 0.01), SmartHomeUnits.AMPERE);
        string3Ampere = QuantityType.valueOf(DataConverter.getUDoubleValue(wrap, 0.01), SmartHomeUnits.AMPERE);
        string1Watt = QuantityType.valueOf(DataConverter.getUInt16Value(wrap), SmartHomeUnits.WATT);
        string2Watt = QuantityType.valueOf(DataConverter.getUInt16Value(wrap), SmartHomeUnits.WATT);
        string3Watt = QuantityType.valueOf(DataConverter.getUInt16Value(wrap), SmartHomeUnits.WATT);
    }
}
