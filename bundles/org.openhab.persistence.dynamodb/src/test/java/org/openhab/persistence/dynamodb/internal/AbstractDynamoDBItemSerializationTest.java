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
import org.junit.jupiter.api.Test;
import org.openhab.core.items.Item;
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
import org.openhab.core.types.UnDefType;

/**
 * Test for AbstractDynamoDBItem.fromState and AbstractDynamoDBItem.asHistoricItem for all kind of states
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class AbstractDynamoDBItemSerializationTest {

    private final ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochSecond(400), ZoneId.systemDefault());

    /**
     * Generic function testing serialization of item state to internal format in DB. In other words, conversion of
     * Item with state to DynamoDBItem
     *
     * @param state item state
     * @param expectedState internal format in DB representing the item state
     * @return dynamo db item
     * @throws IOException
     */
    public DynamoDBItem<?> testStateGeneric(State state, Object expectedState) throws IOException {
        DynamoDBItem<?> dbItem = AbstractDynamoDBItem.fromState("item1", state, date);

        assertEquals("item1", dbItem.getName());
        assertEquals(date, dbItem.getTime());
        Object actualState = dbItem.getState();
        assertNotNull(actualState);
        Objects.requireNonNull(actualState);
        if (expectedState instanceof BigDecimal) {
            BigDecimal expectedRounded = DynamoDBBigDecimalItem.loseDigits(((BigDecimal) expectedState));
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
     * @param item parameter for DynamoDBItem.asHistoricItem
     * @param expectedState Expected state of the historic item. DecimalTypes are compared with reduced accuracy
     * @return
     * @throws IOException
     */
    public HistoricItem testAsHistoricGeneric(DynamoDBItem<?> dbItem, Item item, Object expectedState)
            throws IOException {
        HistoricItem historicItem = dbItem.asHistoricItem(item);

        assertEquals("item1", historicItem.getName());
        assertEquals(date, historicItem.getTimestamp());
        assertEquals(expectedState.getClass(), historicItem.getState().getClass());
        if (expectedState instanceof DecimalType) {
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

    @Test
    public void testUndefWithNumberItem() throws IOException {
        final DynamoDBItem<?> dbitem = testStateGeneric(UnDefType.UNDEF, "<org.openhab.core.types.UnDefType.UNDEF>");
        assertTrue(dbitem instanceof DynamoDBStringItem);
        testAsHistoricGeneric(dbitem, new NumberItem("foo"), UnDefType.UNDEF);
    }

    @Test
    public void testCallTypeWithCallItem() throws IOException {
        final DynamoDBItem<?> dbitem = testStateGeneric(new StringListType("origNum", "destNum"), "origNum,destNum");
        testAsHistoricGeneric(dbitem, new CallItem("foo"), new StringListType("origNum", "destNum"));
    }

    @Test
    public void testOpenClosedTypeWithContactItem() throws IOException {
        final DynamoDBItem<?> dbitemOpen = testStateGeneric(OpenClosedType.CLOSED, BigDecimal.ZERO);
        testAsHistoricGeneric(dbitemOpen, new ContactItem("foo"), OpenClosedType.CLOSED);

        final DynamoDBItem<?> dbitemClosed = testStateGeneric(OpenClosedType.OPEN, BigDecimal.ONE);
        testAsHistoricGeneric(dbitemClosed, new ContactItem("foo"), OpenClosedType.OPEN);
    }

    @Test
    public void testDateTimeTypeWithDateTimeItem() throws IOException {
        ZonedDateTime zdt = ZonedDateTime.parse("2016-05-01T13:46:00.050Z");
        DynamoDBItem<?> dbitem = testStateGeneric(new DateTimeType(zdt.toString()), "2016-05-01T13:46:00.050Z");
        testAsHistoricGeneric(dbitem, new DateTimeItem("foo"),
                new DateTimeType(zdt.withZoneSameInstant(ZoneId.systemDefault())));
    }

    @Test
    public void testDateTimeTypeWithStringItem() throws IOException {
        DynamoDBItem<?> dbitem = testStateGeneric(new DateTimeType(ZonedDateTime.parse("2016-05-01T13:46:00.050Z")),
                "2016-05-01T13:46:00.050Z");
        testAsHistoricGeneric(dbitem, new StringItem("foo"), new StringType("2016-05-01T13:46:00.050Z"));
    }

    @Test
    public void testDateTimeTypeLocalWithDateTimeItem() throws IOException {
        DynamoDBItem<?> dbitem = testStateGeneric(new DateTimeType("2016-07-17T19:38:07.050+0300"),
                "2016-07-17T16:38:07.050Z");

        ZonedDateTime expectedZdt = Instant.ofEpochMilli(1468773487050L).atZone(ZoneId.systemDefault());
        testAsHistoricGeneric(dbitem, new DateTimeItem("foo"), new DateTimeType(expectedZdt));
    }

    @Test
    public void testDateTimeTypeLocalWithStringItem() throws IOException {
        Instant instant = Instant.ofEpochMilli(1468773487050L); // GMT: Sun, 17 Jul 2016 16:38:07.050 GMT
        ZonedDateTime zdt = instant.atZone(TimeZone.getTimeZone("GMT+03:00").toZoneId());
        DynamoDBItem<?> dbitem = testStateGeneric(new DateTimeType(zdt), "2016-07-17T16:38:07.050Z");
        testAsHistoricGeneric(dbitem, new StringItem("foo"), new StringType("2016-07-17T16:38:07.050Z"));
    }

    @Test
    public void testPointTypeWithLocationItem() throws IOException {
        final PointType point = new PointType(new DecimalType(60.3), new DecimalType(30.2), new DecimalType(510.90));
        String expected = point.getLatitude().toBigDecimal().toString() + ","
                + point.getLongitude().toBigDecimal().toString() + "," + point.getAltitude().toBigDecimal().toString();
        DynamoDBItem<?> dbitem = testStateGeneric(point, expected);
        testAsHistoricGeneric(dbitem, new LocationItem("foo"), point);
    }

    @Test
    public void testDecimalTypeWithNumberItem() throws IOException {
        DynamoDBItem<?> dbitem = testStateGeneric(new DecimalType("3.2"), new BigDecimal("3.2"));
        testAsHistoricGeneric(dbitem, new NumberItem("foo"), new DecimalType("3.2"));
    }

    @Test
    public void testPercentTypeWithColorItem() throws IOException {
        DynamoDBItem<?> dbitem = testStateGeneric(new PercentType(new BigDecimal("3.2")), new BigDecimal("3.2"));
        testAsHistoricGeneric(dbitem, new ColorItem("foo"), new PercentType(new BigDecimal("3.2")));
    }

    @Test
    public void testPercentTypeWithDimmerItem() throws IOException {
        DynamoDBItem<?> dbitem = testStateGeneric(new PercentType(new BigDecimal("3.2")), new BigDecimal("3.2"));
        testAsHistoricGeneric(dbitem, new DimmerItem("foo"), new PercentType(new BigDecimal("3.2")));
    }

    @Test
    public void testPercentTypeWithRollerShutterItem() throws IOException {
        DynamoDBItem<?> dbitem = testStateGeneric(new PercentType(new BigDecimal("3.2")), new BigDecimal("3.2"));
        testAsHistoricGeneric(dbitem, new RollershutterItem("foo"), new PercentType(new BigDecimal("3.2")));
    }

    @Test
    public void testPercentTypeWithNumberItem() throws IOException {
        DynamoDBItem<?> dbitem = testStateGeneric(new PercentType(new BigDecimal("3.2")), new BigDecimal("3.2"));
        // note: comes back as DecimalType instead of the original PercentType
        testAsHistoricGeneric(dbitem, new NumberItem("foo"), new DecimalType(new BigDecimal("3.2")));
    }

    @Test
    public void testUpDownTypeWithRollershutterItem() throws IOException {
        // note: comes back as PercentType instead of the original UpDownType
        DynamoDBItem<?> dbItemDown = testStateGeneric(UpDownType.DOWN, BigDecimal.ZERO);
        testAsHistoricGeneric(dbItemDown, new RollershutterItem("foo"), new PercentType(BigDecimal.ZERO));

        DynamoDBItem<?> dbItemUp = testStateGeneric(UpDownType.UP, BigDecimal.ONE);
        testAsHistoricGeneric(dbItemUp, new RollershutterItem("foo"), new PercentType(BigDecimal.ONE));
    }

    @Test
    public void testStringTypeWithStringItem() throws IOException {
        DynamoDBItem<?> dbitem = testStateGeneric(new StringType("foo bar"), "foo bar");
        testAsHistoricGeneric(dbitem, new StringItem("foo"), new StringType("foo bar"));
    }

    @Test
    public void testOnOffTypeWithColorItem() throws IOException {
        DynamoDBItem<?> dbitemOff = testStateGeneric(OnOffType.OFF, BigDecimal.ZERO);
        testAsHistoricGeneric(dbitemOff, new ColorItem("foo"), new PercentType(BigDecimal.ZERO));

        DynamoDBItem<?> dbitemOn = testStateGeneric(OnOffType.ON, BigDecimal.ONE);
        testAsHistoricGeneric(dbitemOn, new ColorItem("foo"), new PercentType(BigDecimal.ONE));
    }

    @Test
    public void testOnOffTypeWithDimmerItem() throws IOException {
        DynamoDBItem<?> dbitemOff = testStateGeneric(OnOffType.OFF, BigDecimal.ZERO);
        testAsHistoricGeneric(dbitemOff, new DimmerItem("foo"), new PercentType(BigDecimal.ZERO));

        DynamoDBItem<?> dbitemOn = testStateGeneric(OnOffType.ON, BigDecimal.ONE);
        testAsHistoricGeneric(dbitemOn, new DimmerItem("foo"), new PercentType(BigDecimal.ONE));
    }

    @Test
    public void testOnOffTypeWithSwitchItem() throws IOException {
        DynamoDBItem<?> dbitemOff = testStateGeneric(OnOffType.OFF, BigDecimal.ZERO);
        testAsHistoricGeneric(dbitemOff, new SwitchItem("foo"), OnOffType.OFF);

        DynamoDBItem<?> dbitemOn = testStateGeneric(OnOffType.ON, BigDecimal.ONE);
        testAsHistoricGeneric(dbitemOn, new SwitchItem("foo"), OnOffType.ON);
    }

    @Test
    public void testHSBTypeWithColorItem() throws IOException {
        HSBType hsb = new HSBType(new DecimalType(1.5), new PercentType(new BigDecimal(2.5)),
                new PercentType(new BigDecimal(3.5)));
        DynamoDBItem<?> dbitem = testStateGeneric(hsb, "1.5,2.5,3.5");
        testAsHistoricGeneric(dbitem, new ColorItem("foo"), hsb);
    }
}
