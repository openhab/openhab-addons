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
package org.openhab.persistence.dynamodb.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.GroupItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Operator;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class TestComplexItemsWithDifferentStateTypesTest extends BaseIntegrationTest {

    private static final HSBType HSB_STATE = new HSBType(DecimalType.valueOf("20"), new PercentType(30),
            new PercentType(40));
    private static final PercentType COLOR_PERCENT_STATE = new PercentType(22);
    private static final HSBType HSB_STATE_AFTER_PERCENT = new HSBType(DecimalType.valueOf("20"), new PercentType(30),
            COLOR_PERCENT_STATE);
    private static final OnOffType COLOR_ONOFF_STATE = OnOffType.ON;
    private static final HSBType HSB_STATE_AFTER_ONOFF = new HSBType(DecimalType.valueOf("20"), new PercentType(30),
            PercentType.HUNDRED);
    public static final boolean LEGACY_MODE = false;

    @BeforeAll
    @SuppressWarnings("null")
    public static void storeColorItemData() {
        ColorItem item = (ColorItem) ITEMS.get("color");
        try {
            item.setState(HSB_STATE);
            service.store(item);
            Thread.sleep(10);

            // percent
            item.setState(COLOR_PERCENT_STATE); // changes only the brightness
            service.store(item);
            Thread.sleep(10);

            // on/off
            item.setState(COLOR_ONOFF_STATE); // once again, changes the brightness
            service.store(item);
            Thread.sleep(10);

        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    @BeforeAll
    @SuppressWarnings("null")
    public static void storeRollershutterItemData() {
        RollershutterItem item = (RollershutterItem) ITEMS.get("rollershutter");
        try {
            item.setState(new PercentType(31));
            service.store(item);
            Thread.sleep(10);

            item.setState(UpDownType.DOWN);
            service.store(item);
            Thread.sleep(10);

            item.setState(new PercentType(32));
            service.store(item);
            Thread.sleep(10);

            item.setState(UpDownType.UP);
            service.store(item);
            Thread.sleep(10);

        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    @BeforeAll
    @SuppressWarnings("null")
    public static void storeDimmerItemData() {
        DimmerItem item = (DimmerItem) ITEMS.get("dimmer");
        try {
            Thread.sleep(10);
            item.setState(new PercentType(33));
            service.store(item);
            Thread.sleep(10);

            item.setState(OnOffType.OFF);
            service.store(item);
            Thread.sleep(10);

            item.setState(new PercentType(35));
            service.store(item);
            Thread.sleep(10);

            item.setState(OnOffType.ON);
            service.store(item);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    @BeforeAll
    @SuppressWarnings("null")
    public static void storeStringItemData() {
        StringItem item = (StringItem) ITEMS.get("string");
        try {
            Thread.sleep(10);
            item.setState(new StringType("mystring"));
            service.store(item);
            Thread.sleep(10);

            item.setState(DateTimeType.valueOf("2021-01-17T11:18:00+02:00"));
            service.store(item);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    @BeforeAll
    @SuppressWarnings("null")
    public static void storeTemperatureNumberItemData() {
        NumberItem item = (NumberItem) ITEMS.get("numberTemperature");
        assertEquals(TEMP_ITEM_UNIT, item.getUnit());
        try {
            Thread.sleep(10);
            item.setState(new QuantityType<>("2.0 °C"));
            service.store(item);

            Thread.sleep(10);
            item.setState(new QuantityType<>("2.0 K"));
            service.store(item);

            Thread.sleep(10);
            item.setState(new QuantityType<>("5.1 °F"));
            service.store(item);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    @BeforeAll
    @SuppressWarnings("null")
    public static void storeGroupTemperatureNumberItemData() {
        GroupItem item = (GroupItem) ITEMS.get("groupNumberTemperature");
        try {
            Thread.sleep(10);
            item.setState(new QuantityType<>("3.0 °C"));
            service.store(item);

            Thread.sleep(10);
            item.setState(new QuantityType<>("3.0 K"));
            service.store(item);

            Thread.sleep(10);
            item.setState(new QuantityType<>("5.1 °F"));
            service.store(item);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    @BeforeAll
    @SuppressWarnings("null")
    public static void storeDimensionlessNumberItemData() {
        NumberItem item = (NumberItem) ITEMS.get("numberDimensionless");
        assertEquals(DIMENSIONLESS_ITEM_UNIT, item.getUnit());
        try {
            Thread.sleep(10);
            item.setState(new QuantityType<>("2 %"));
            service.store(item);

            Thread.sleep(10);
            item.setState(new QuantityType<>("3.5"));
            service.store(item);

            Thread.sleep(10);
            item.setState(new QuantityType<>("510 ppm"));
            service.store(item);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    @BeforeAll
    @SuppressWarnings("null")
    public static void storeDummyGroupItemData() {
        GroupItem item = (GroupItem) ITEMS.get("groupDummy");
        try {
            Thread.sleep(10);
            item.setState(new QuantityType<>("2 %")); // Will not write anything
            service.store(item);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    @Test
    public void testColorItem() {
        waitForAssert(() -> {
            assertQueryAll("color", new HSBType[] { HSB_STATE, HSB_STATE_AFTER_PERCENT, HSB_STATE_AFTER_ONOFF });
        });
    }

    @Test
    public void testRollershutter() {
        // when querying, UP/DOWN are returned as PercentType
        assertEquals(PercentType.HUNDRED, UpDownType.DOWN.as(PercentType.class));
        assertEquals(PercentType.ZERO, UpDownType.UP.as(PercentType.class));
        waitForAssert(() -> {
            assertQueryAll("rollershutter",
                    new State[] { new PercentType(31), PercentType.HUNDRED, new PercentType(32), PercentType.ZERO });
        });
    }

    @Test
    public void testDimmer() {
        // when querying, ON/OFF are returned as PercentType
        assertEquals(PercentType.HUNDRED, OnOffType.ON.as(PercentType.class));
        assertEquals(PercentType.ZERO, OnOffType.OFF.as(PercentType.class));
        waitForAssert(() -> {
            assertQueryAll("dimmer",
                    new State[] { new PercentType(33), PercentType.ZERO, new PercentType(35), PercentType.HUNDRED });
        });
    }

    @Test
    public void testString() {
        waitForAssert(() -> {
            assertQueryAll("string",
                    new State[] { new StringType("mystring"), new StringType("2021-01-17T09:18:00.000Z") });
        });
    }

    @Test
    public void testTemperatureItemNumber() {
        waitForAssert(() -> {
            assertQueryAll("numberTemperature",
                    new State[] { new QuantityType<>("2.0 °C"), /* 2 K = -271.15 °C */ new QuantityType<>("-271.15 °C"),
                            new QuantityType<>("-14.9444444444444444444444444444444 °C") });
            assertQueryFilterByState("numberTemperature", Operator.GT, new QuantityType<>("-20 °C"), new State[] {
                    new QuantityType<>("2.0 °C"), new QuantityType<>("-14.9444444444444444444444444444444 °C") });
            assertQueryFilterByState("numberTemperature", Operator.LT, new QuantityType<>("273.15 K"), new State[] {
                    new QuantityType<>("-271.15 °C"), new QuantityType<>("-14.9444444444444444444444444444444 °C") });
            assertQueryFilterByState("numberTemperature", Operator.EQ, new QuantityType<>("5.1 °F"),
                    new State[] { new QuantityType<>("-14.9444444444444444444444444444444 °C") });
            assertQueryFilterByState("numberTemperature", Operator.EQ, new QuantityType<>("5.1 m/s"), new State[] {});
        });
    }

    @Test
    public void testGroupTemperatureItemNumber() {
        waitForAssert(() -> {
            assertQueryAll("groupNumberTemperature",
                    new State[] { new QuantityType<>("3.0 °C"), /* 3 K = -270.15 °C */ new QuantityType<>("-270.15 °C"),
                            new QuantityType<>("-14.9444444444444444444444444444444 °C") });
            assertQueryFilterByState("groupNumberTemperature", Operator.GT, new QuantityType<>("-20 °C"), new State[] {
                    new QuantityType<>("3.0 °C"), new QuantityType<>("-14.9444444444444444444444444444444 °C") });
            assertQueryFilterByState("groupNumberTemperature", Operator.LT, new QuantityType<>("273.15 K"),
                    new State[] { new QuantityType<>("-270.15 °C"),
                            new QuantityType<>("-14.9444444444444444444444444444444 °C") });
            assertQueryFilterByState("groupNumberTemperature", Operator.EQ, new QuantityType<>("5.1 °F"),
                    new State[] { new QuantityType<>("-14.9444444444444444444444444444444 °C") });
            assertQueryFilterByState("groupNumberTemperature", Operator.EQ, new QuantityType<>("5.1 m/s"),
                    new State[] {});
        });
    }

    @Test
    public void testGroupDummyItem() {
        // only with the fast local server
        assumeTrue(hasFakeServer());
        try {
            // 5 seconds should be enough that any writes go through
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            fail(e.getMessage());
            return;
        }
        // We expect no results
        assertQueryAll("groupDummy", new State[] {});
    }

    @Test
    public void testDimensionlessItemNumber() {
        waitForAssert(() -> {
            assertQueryAll("numberDimensionless", new State[] { /* 2 % */ new QuantityType<>("0.02"),
                    new QuantityType<>("3.5"), new QuantityType<>("510").divide(new BigDecimal(1_000_000)) });
        });
    }

    private void assertQueryAll(String item, State[] expectedStates) {
        assertQueryFilterByState(item, null, null, expectedStates);
    }

    private void assertQueryFilterByState(String item, @Nullable Operator operator, @Nullable State state,
            State[] expectedStates) {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setOrdering(Ordering.ASCENDING);
        criteria.setItemName(item);
        if (operator != null) {
            criteria.setOperator(operator);
        }
        if (state != null) {
            criteria.setState(state);
        }
        @SuppressWarnings("null")
        Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
        List<State> actualStatesList = new ArrayList<>();
        iterable.forEach(i -> actualStatesList.add(i.getState()));
        State[] actualStates = actualStatesList.toArray(new State[0]);
        assertArrayEquals(expectedStates, actualStates, Arrays.toString(actualStates));
    }
}
