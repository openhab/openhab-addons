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
package org.openhab.persistence.dynamodb.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.core.items.GenericItem;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

/**
 * Test for AbstractDynamoDBItem.fromState and AbstractDynamoDBItem.asHistoricItem for all kind of states
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class AbstractDynamoDBItemSerializationTest {

    private final ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochSecond(400), ZoneId.systemDefault());

    /**
     * Generic function testing serialization of GenericItem state to internal format in DB. In other words, conversion
     * of
     * GenericItem with state to DynamoDBItem
     *
     * @param legacy whether we have legacy
     * @param GenericItem item
     * @param stateOverride state
     * @param expectedState internal format in DB representing the GenericItem state
     * @return dynamo db item
     * @throws IOException
     */
    public DynamoDBItem<?> testSerializationToDTO(boolean legacy, GenericItem item, State stateOverride,
            Object expectedState) throws IOException {
        item.setState(stateOverride);
        DynamoDBItem<?> dbItem = legacy ? AbstractDynamoDBItem.fromStateLegacy(item, date)
                : AbstractDynamoDBItem.fromStateNew(item, date, null);

        assertEquals("foo", dbItem.getName());
        assertEquals(date, dbItem.getTime());
        Object actualState = dbItem.getState();
        assertNotNull(actualState);
        Objects.requireNonNull(actualState);
        if (expectedState instanceof BigDecimal decimal) {
            BigDecimal expectedRounded = DynamoDBBigDecimalItem.loseDigits(decimal);
            assertEquals(0, expectedRounded.compareTo((BigDecimal) actualState),
                    String.format("Expected state %s (%s but with some digits lost) did not match actual state %s",
                            expectedRounded, expectedState, actualState));
        } else {
            assertEquals(expectedState, actualState);
        }
        return dbItem;
    }

    /**
     * Test state deserialization, that is DynamoDBItem conversion to HistoricItem
     *
     * @param dbItem dynamo db item
     * @param GenericItem parameter for DynamoDBItem.asHistoricItem
     * @param expectedState Expected state of the historic item. DecimalTypes are compared with reduced accuracy
     * @return
     * @throws IOException
     */
    public HistoricItem testAsHistoricGeneric(DynamoDBItem<?> dbItem, GenericItem item, Object expectedState)
            throws IOException {
        HistoricItem historicItem = dbItem.asHistoricItem(item);
        assertNotNull(historicItem);
        assert historicItem != null; // getting rid off null pointer access warning
        assertEquals("foo", historicItem.getName());
        assertEquals(date, historicItem.getTimestamp());
        assertEquals(expectedState.getClass(), historicItem.getState().getClass());
        if (expectedState.getClass() == DecimalType.class) {
            // serialization loses accuracy, take this into consideration
            BigDecimal expectedRounded = DynamoDBBigDecimalItem
                    .loseDigits(((DecimalType) expectedState).toBigDecimal());
            BigDecimal actual = ((DecimalType) historicItem.getState()).toBigDecimal();
            assertEquals(0, expectedRounded.compareTo(actual),
                    String.format("Expected state %s (%s but with some digits lost) did not match actual state %s",
                            expectedRounded, expectedState, actual));
        } else {
            assertEquals(expectedState, historicItem.getState());
        }
        return historicItem;
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testCallTypeWithCallItemLegacy(boolean legacy) throws IOException {
        GenericItem item = new CallItem("foo");
        final DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, new StringListType("origNum", "destNum"),
                "origNum,destNum");
        testAsHistoricGeneric(dbitem, item, new StringListType("origNum", "destNum"));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testOpenClosedTypeWithContactItem(boolean legacy) throws IOException {
        GenericItem item = new ContactItem("foo");
        final DynamoDBItem<?> dbitemOpen = testSerializationToDTO(legacy, item, OpenClosedType.CLOSED, BigDecimal.ZERO);
        testAsHistoricGeneric(dbitemOpen, item, OpenClosedType.CLOSED);

        final DynamoDBItem<?> dbitemClosed = testSerializationToDTO(legacy, item, OpenClosedType.OPEN, BigDecimal.ONE);
        testAsHistoricGeneric(dbitemClosed, item, OpenClosedType.OPEN);
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testDateTimeTypeWithDateTimeItem(boolean legacy) throws IOException {
        GenericItem item = new DateTimeItem("foo");
        ZonedDateTime zdt = ZonedDateTime.parse("2016-05-01T13:46:00.050Z");
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, new DateTimeType(zdt.toString()),
                "2016-05-01T13:46:00.050Z");
        testAsHistoricGeneric(dbitem, item, new DateTimeType(zdt.withZoneSameInstant(ZoneId.systemDefault())));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testDateTimeTypeWithStringItem(boolean legacy) throws IOException {
        GenericItem item = new StringItem("foo");
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item,
                new DateTimeType(ZonedDateTime.parse("2016-05-01T13:46:00.050Z")), "2016-05-01T13:46:00.050Z");
        testAsHistoricGeneric(dbitem, item, new StringType("2016-05-01T13:46:00.050Z"));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testDateTimeTypeLocalWithDateTimeItem(boolean legacy) throws IOException {
        GenericItem item = new DateTimeItem("foo");
        ZonedDateTime expectedZdt = Instant.ofEpochMilli(1468773487050L).atZone(ZoneId.systemDefault());
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, new DateTimeType("2016-07-17T19:38:07.050+0300"),
                "2016-07-17T16:38:07.050Z");
        testAsHistoricGeneric(dbitem, item, new DateTimeType(expectedZdt));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testDateTimeTypeLocalWithStringItem(boolean legacy) throws IOException {
        GenericItem item = new StringItem("foo");
        Instant instant = Instant.ofEpochMilli(1468773487050L); // GMT: Sun, 17 Jul 2016 16:38:07.050 GMT
        ZonedDateTime zdt = instant.atZone(TimeZone.getTimeZone("GMT+03:00").toZoneId());
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, new DateTimeType(zdt),
                "2016-07-17T16:38:07.050Z");
        testAsHistoricGeneric(dbitem, item, new StringType("2016-07-17T16:38:07.050Z"));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testPointTypeWithLocationItem(boolean legacy) throws IOException {
        GenericItem item = new LocationItem("foo");
        final PointType point = new PointType(new DecimalType(60.3), new DecimalType(30.2), new DecimalType(510.90));
        String expected = point.getLatitude().toBigDecimal().toString() + ","
                + point.getLongitude().toBigDecimal().toString() + "," + point.getAltitude().toBigDecimal().toString();
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, point, expected);
        testAsHistoricGeneric(dbitem, item, point);
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testDecimalTypeWithNumberItem(boolean legacy) throws IOException {
        GenericItem item = new NumberItem("foo");
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, new DecimalType("3.2"), new BigDecimal("3.2"));
        testAsHistoricGeneric(dbitem, item, new DecimalType("3.2"));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testPercentTypeWithColorItem(boolean legacy) throws IOException {
        GenericItem item = new ColorItem("foo");
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, new PercentType(new BigDecimal("3.2")),
                "0,0,3.2");
        testAsHistoricGeneric(dbitem, item, new HSBType(DecimalType.ZERO, PercentType.ZERO, new PercentType("3.2")));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testPercentTypeWithDimmerItem(boolean legacy) throws IOException {
        GenericItem item = new DimmerItem("foo");
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, new PercentType(new BigDecimal("3.2")),
                new BigDecimal("3.2"));
        testAsHistoricGeneric(dbitem, item, new PercentType(new BigDecimal("3.2")));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testPercentTypeWithRollerShutterItem(boolean legacy) throws IOException {
        GenericItem item = new RollershutterItem("foo");
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, new PercentType(81), new BigDecimal("81"));
        testAsHistoricGeneric(dbitem, item, new PercentType(81));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testUpDownTypeWithRollershutterItem(boolean legacy) throws IOException {
        GenericItem item = new RollershutterItem("foo");
        // note: comes back as PercentType instead of the original UpDownType
        {
            // down == 1.0 = 100%
            State expectedDeserializedState = PercentType.HUNDRED;
            DynamoDBItem<?> dbItemDown = testSerializationToDTO(legacy, item, UpDownType.DOWN, new BigDecimal(100));
            testAsHistoricGeneric(dbItemDown, item, expectedDeserializedState);
            assertEquals(UpDownType.DOWN, expectedDeserializedState.as(UpDownType.class));
        }

        {
            // up == 0
            State expectedDeserializedState = PercentType.ZERO;
            DynamoDBItem<?> dbItemUp = testSerializationToDTO(legacy, item, UpDownType.UP, BigDecimal.ZERO);
            testAsHistoricGeneric(dbItemUp, item, expectedDeserializedState);
            assertEquals(UpDownType.UP, expectedDeserializedState.as(UpDownType.class));
        }
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testStringTypeWithStringItem(boolean legacy) throws IOException {
        GenericItem item = new StringItem("foo");
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, new StringType("foo bar"), "foo bar");
        testAsHistoricGeneric(dbitem, item, new StringType("foo bar"));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testOnOffTypeWithColorItem(boolean legacy) throws IOException {
        GenericItem item = new ColorItem("foo");
        DynamoDBItem<?> dbitemOff = testSerializationToDTO(legacy, item, OnOffType.OFF, "0,0,0");
        testAsHistoricGeneric(dbitemOff, item, HSBType.BLACK);

        DynamoDBItem<?> dbitemOn = testSerializationToDTO(legacy, item, OnOffType.ON, "0,0,100");
        testAsHistoricGeneric(dbitemOn, item, HSBType.WHITE);
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testOnOffTypeWithDimmerItem(boolean legacy) throws IOException {
        GenericItem item = new DimmerItem("foo");
        {
            State expectedDeserializedState = PercentType.ZERO;
            DynamoDBItem<?> dbitemOff = testSerializationToDTO(legacy, item, OnOffType.OFF, BigDecimal.ZERO);
            testAsHistoricGeneric(dbitemOff, item, expectedDeserializedState);
            assertEquals(OnOffType.OFF, expectedDeserializedState.as(OnOffType.class));
        }

        {
            State expectedDeserializedState = PercentType.HUNDRED;
            DynamoDBItem<?> dbitemOn = testSerializationToDTO(legacy, item, OnOffType.ON, new BigDecimal(100));
            testAsHistoricGeneric(dbitemOn, item, expectedDeserializedState);
            assertEquals(OnOffType.ON, expectedDeserializedState.as(OnOffType.class));
        }
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testOnOffTypeWithSwitchItem(boolean legacy) throws IOException {
        GenericItem item = new SwitchItem("foo");
        DynamoDBItem<?> dbitemOff = testSerializationToDTO(legacy, item, OnOffType.OFF, BigDecimal.ZERO);
        testAsHistoricGeneric(dbitemOff, item, OnOffType.OFF);

        DynamoDBItem<?> dbitemOn = testSerializationToDTO(legacy, item, OnOffType.ON, BigDecimal.ONE);
        testAsHistoricGeneric(dbitemOn, item, OnOffType.ON);
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testHSBTypeWithColorItem(boolean legacy) throws IOException {
        GenericItem item = new ColorItem("foo");
        HSBType hsb = new HSBType(new DecimalType(1.5), new PercentType(new BigDecimal(2.5)),
                new PercentType(new BigDecimal(3.5)));
        DynamoDBItem<?> dbitem = testSerializationToDTO(legacy, item, hsb, "1.5,2.5,3.5");
        testAsHistoricGeneric(dbitem, item, hsb);
    }
}
