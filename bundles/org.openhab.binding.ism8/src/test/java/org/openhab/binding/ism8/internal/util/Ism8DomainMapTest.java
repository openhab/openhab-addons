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
package org.openhab.binding.ism8.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.ism8.server.DataPointBool;
import org.openhab.binding.ism8.server.DataPointByteValue;
import org.openhab.binding.ism8.server.DataPointScaling;
import org.openhab.binding.ism8.server.DataPointValue;
import org.openhab.binding.ism8.server.IDataPoint;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Leo Siepel - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class Ism8DomainMapTest {

    @BeforeEach
    public void initialize() {
    }

    @Test
    public void mapDataPointValueToMessageSameUnit() {
        // arrange
        Command command = new QuantityType<>(40.5, SIUnits.CELSIUS);
        IDataPoint dataPoint = new DataPointValue(4, "9.001", "Datapoint_9.001");

        // act
        byte[] result = Ism8DomainMap.toISM8WriteData(dataPoint, command);

        // assert
        assertEquals(String.format("%.1f", 40.5), dataPoint.getValueText());
        assertEquals("0620F080001604000000F0C100040001000400020FE9", HexUtils.bytesToHex(result));
    }

    @Test
    public void mapDataPointValueToMessagOtherUnit() {
        // arrange
        Command command = new QuantityType<>(104.9, ImperialUnits.FAHRENHEIT);
        IDataPoint dataPoint = new DataPointValue(4, "9.001", "Datapoint_9.001");

        // act
        byte[] result = Ism8DomainMap.toISM8WriteData(dataPoint, command);

        // assert
        assertEquals(String.format("%.1f", 40.5), dataPoint.getValueText());
        assertEquals("0620F080001604000000F0C100040001000400020FE9", HexUtils.bytesToHex(result));
    }

    @Test
    public void mapDataPointBoolToMessage() {
        // arrange
        Command command = OnOffType.from(true);
        IDataPoint dataPoint = new DataPointBool(9, "1.001", "Datapoint_1.001");

        // act
        byte[] result = Ism8DomainMap.toISM8WriteData(dataPoint, command);

        // assert
        assertEquals("True", dataPoint.getValueText());
        assertEquals("0620F080001504000000F0C1000900010009000101", HexUtils.bytesToHex(result));
    }

    @Test
    public void mapDataPointScalingToMessage() {
        // arrange
        Command command = new QuantityType<Dimensionless>(13, Units.PERCENT);
        IDataPoint dataPoint = new DataPointScaling(3, "5.001", "Datapoint_5.001");

        // act
        byte[] result = Ism8DomainMap.toISM8WriteData(dataPoint, command);

        // assert
        assertEquals(String.format("%.1f", 13.0), dataPoint.getValueText());
        assertEquals("0620F080001504000000F0C1000300010003000121", HexUtils.bytesToHex(result));
    }

    @Test
    public void mapDataPointValueToOHState() {
        // arrange
        IDataPoint dataPoint = new DataPointValue(4, "9.001", "Datapoint_9.001");
        dataPoint.processData(HexUtils.hexToBytes("000403020FE9"));

        // act
        State result = Ism8DomainMap.toOpenHABState(dataPoint);

        // assert
        assertEquals(new QuantityType<Temperature>(40.49999909475446, SIUnits.CELSIUS), result);
    }

    @Test
    public void mapDataPointLongToOHState() {
        // arrange
        IDataPoint dataPoint = new DataPointByteValue(2, "20.102", "Datapoint_20.102");
        dataPoint.processData(HexUtils.hexToBytes("0002030101"));

        // act
        State result = Ism8DomainMap.toOpenHABState(dataPoint);

        // assert
        assertEquals(new QuantityType<Dimensionless>(1, Units.ONE), result);
    }
}
