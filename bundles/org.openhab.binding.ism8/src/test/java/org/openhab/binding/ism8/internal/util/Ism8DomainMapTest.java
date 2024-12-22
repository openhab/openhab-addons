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
import javax.measure.quantity.Pressure;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.ism8.server.DataPointBool;
import org.openhab.binding.ism8.server.DataPointByteValue;
import org.openhab.binding.ism8.server.DataPointIntegerValue;
import org.openhab.binding.ism8.server.DataPointLongValue;
import org.openhab.binding.ism8.server.DataPointScaling;
import org.openhab.binding.ism8.server.DataPointValue;
import org.openhab.binding.ism8.server.IDataPoint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
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
    public void mapDataPointBoolToOHState() {
        {
            // arrange
            IDataPoint dataPoint = new DataPointBool(1, "1.001", "Datapoint_1.001");
            dataPoint.processData(HexUtils.hexToBytes("0001030200"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            assertEquals(DecimalType.valueOf("0"), result);
            assertEquals(OnOffType.from(false), OnOffType.from(DecimalType.valueOf("0").toString()));
        }
        {
            // arrange
            IDataPoint dataPoint = new DataPointBool(1, "1.001", "Datapoint_1.001");
            dataPoint.processData(HexUtils.hexToBytes("0001030201"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            assertEquals(DecimalType.valueOf("1"), result);
            assertEquals(OnOffType.from(true), OnOffType.from(DecimalType.valueOf("1").toString()));
        }
        {
            // arrange
            IDataPoint dataPoint = new DataPointBool(1, "1.002", "Datapoint_1.002");
            dataPoint.processData(HexUtils.hexToBytes("0001030201"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            assertEquals(DecimalType.valueOf("1"), result);
            assertEquals(OnOffType.from(true), OnOffType.from(DecimalType.valueOf("1").toString()));
        }
        {
            // arrange
            IDataPoint dataPoint = new DataPointBool(1, "1.003", "Datapoint_1.003");
            dataPoint.processData(HexUtils.hexToBytes("0001030201"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            assertEquals(DecimalType.valueOf("1"), result);
            assertEquals(OnOffType.from(true), OnOffType.from(DecimalType.valueOf("1").toString()));
        }
        {
            // arrange
            IDataPoint dataPoint = new DataPointBool(1, "1.009", "Datapoint_1.009");
            dataPoint.processData(HexUtils.hexToBytes("0001030201"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            assertEquals(DecimalType.valueOf("1"), result);
            assertEquals(OnOffType.ON, OnOffType.from(DecimalType.valueOf("1").toString()));
            // DecimalType is compatible with Switch and Contact items, OH mapping is 0-off-closed and 1-on-open;
            // note that this is opposite to definition of KNX DPT 1.009
            assertEquals(OpenClosedType.CLOSED.as(DecimalType.class), DecimalType.valueOf("0"));
        }
    }

    @Test
    public void mapDataPointValueToOHState() {
        {
            // arrange
            IDataPoint dataPoint = new DataPointValue(4, "9.001", "Datapoint_9.001");
            dataPoint.processData(HexUtils.hexToBytes("000403020FE9"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            assertEquals(new QuantityType<>("40.49999909475446 °C"), result);
        }
        {
            // arrange
            IDataPoint dataPoint = new DataPointValue(4, "9.002", "Datapoint_9.002");
            dataPoint.processData(HexUtils.hexToBytes("000403020FE9"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            assertEquals(new QuantityType<>("40.49999909475446 K"), result);
        }
        {
            // arrange
            IDataPoint dataPoint = new DataPointValue(4, "9.006", "Datapoint_9.006");
            dataPoint.processData(HexUtils.hexToBytes("000403020FE9"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            // original unit is Pa, will be bar in OH -> divide by 10000, i.e. expect 0.0004049999909475446 bar
            // text ctor cannot be used, as it will result in scientific notation ..E-4,
            // double ctor returns a slightly different number caused by internal representation
            assertEquals(new QuantityType<Pressure>(0.00040500000473286946, Units.BAR), result);
        }
        {
            // arrange
            IDataPoint dataPoint = new DataPointValue(4, "9.024", "Datapoint_9.024");
            dataPoint.processData(HexUtils.hexToBytes("000403020FE9"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            // original unit is kW
            assertEquals(new QuantityType<>("40500 W"), result);
        }
        {
            // arrange
            IDataPoint dataPoint = new DataPointValue(4, "9.025", "Datapoint_9.025");
            dataPoint.processData(HexUtils.hexToBytes("000403020FE9"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            // original unit is l/h, i.e. divide by 60 to get l/min
            assertEquals(new QuantityType<>("0.6749999927706085 l/min"), result);
        }
    }

    @Test
    public void mapDataPointByteValueToOHState() {
        // arrange
        IDataPoint dataPoint = new DataPointByteValue(5, "5.010", "Datapoint_5.010");
        dataPoint.processData(HexUtils.hexToBytes("0005030243"));

        // act
        State result = Ism8DomainMap.toOpenHABState(dataPoint);

        // assert
        assertEquals(new QuantityType<Dimensionless>(0x43, Units.ONE), result);
    }

    @Test
    public void mapDataPointIntegerValueToOHState() {
        // arrange
        IDataPoint dataPoint = new DataPointIntegerValue(7, "7.001", "Datapoint_7.001");
        dataPoint.processData(HexUtils.hexToBytes("000703024321"));

        // act
        State result = Ism8DomainMap.toOpenHABState(dataPoint);

        // assert
        assertEquals(new QuantityType<Dimensionless>(0x4321, Units.ONE), result);
    }

    @Test
    public void mapDataPointLongValueToOHState() {
        {
            // arrange
            IDataPoint dataPoint = new DataPointLongValue(13, "13.002", "Datapoint_13.002");
            dataPoint.processData(HexUtils.hexToBytes("000D03027FFFFFFF"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            // value encoded above is max value 2147483647, scaling for 13.002 is 0.0001, unit m^3/h
            // -> expected is 214748.3647 m^3/h
            assertEquals(new QuantityType<>("214748.359275 m³/h"), result);
        }
        {
            // arrange
            IDataPoint dataPoint = new DataPointLongValue(13, "13.010", "Datapoint_13.010");
            dataPoint.processData(HexUtils.hexToBytes("000D03027FFFFFFF"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            // value encoded above is max value 2147483647
            assertEquals(new QuantityType<>("2147483647 Wh"), result);
        }
        {
            // arrange
            IDataPoint dataPoint = new DataPointLongValue(13, "13.013", "Datapoint_13.013");
            dataPoint.processData(HexUtils.hexToBytes("000D03027FFFFFFF"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            // value encoded above is max value 2147483647
            assertEquals(new QuantityType<>("2147483647 kWh"), result);
        }
    }

    @Test
    public void mapDataPointLongToOHState() {
        { // arrange
            IDataPoint dataPoint = new DataPointByteValue(20, "20.102", "Datapoint_20.102");
            dataPoint.processData(HexUtils.hexToBytes("0014030101"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            assertEquals(new QuantityType<Dimensionless>(1, Units.ONE), result);
        }
        { // arrange
            IDataPoint dataPoint = new DataPointByteValue(20, "20.103", "Datapoint_20.103");
            dataPoint.processData(HexUtils.hexToBytes("0014030101"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            assertEquals(new QuantityType<Dimensionless>(1, Units.ONE), result);
        }
        { // arrange
            IDataPoint dataPoint = new DataPointByteValue(20, "20.105", "Datapoint_20.105");
            dataPoint.processData(HexUtils.hexToBytes("0014030101"));

            // act
            State result = Ism8DomainMap.toOpenHABState(dataPoint);

            // assert
            assertEquals(new QuantityType<Dimensionless>(1, Units.ONE), result);
        }
    }
}
