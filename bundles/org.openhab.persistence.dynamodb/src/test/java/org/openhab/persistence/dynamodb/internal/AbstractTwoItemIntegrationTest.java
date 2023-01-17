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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Operator;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

/**
 * This is abstract class helping with integration testing the persistence service. Different kind of queries are tested
 * against actual dynamo db database.
 *
 *
 * Inheritor of this base class needs to store two states of one item in a static method annotated with @BeforeAll.
 * This
 * static
 * class should update the private static fields
 * beforeStore (date before storing anything), afterStore1 (after storing first item, but before storing second item),
 * afterStore2 (after storing second item). The item name must correspond to getItemName. The first state needs to be
 * smaller than the second state.
 *
 * To have more comprehensive tests, the inheritor class can define getQueryItemStateBetween to provide a value between
 * the two states. Null can be used to omit the additional tests.
 *
 *
 * See DimmerItemIntegrationTest for example how to use this base class.
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public abstract class AbstractTwoItemIntegrationTest extends BaseIntegrationTest {

    protected static ZonedDateTime beforeStore = ZonedDateTime.of(2100, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
    protected static ZonedDateTime afterStore1 = ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
    protected static ZonedDateTime afterStore2 = ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());

    protected abstract String getItemName();

    /**
     * State of the time item stored first, should be smaller than the second value
     *
     * @return
     */
    protected abstract State getFirstItemState();

    /**
     * State of the time item stored second, should be larger than the first value
     *
     * @return
     */
    protected abstract State getSecondItemState();

    /**
     * State that is between the first and second. Use null to omit extended tests using this value.
     *
     * @return
     */
    protected abstract @Nullable State getQueryItemStateBetween();

    protected void assertStateEquals(State expected, State actual) {
        assertEquals(expected, actual);
    }

    /**
     * Asserts that iterable contains correct items and nothing else
     *
     */
    protected void assertIterableContainsItems(Iterable<HistoricItem> iterable, boolean ascending) {
        Iterator<HistoricItem> iterator = iterable.iterator();
        assertTrue(iterator.hasNext());
        HistoricItem actual1 = iterator.next();
        assertTrue(iterator.hasNext());
        HistoricItem actual2 = iterator.next();
        assertFalse(iterator.hasNext());

        for (HistoricItem actual : new HistoricItem[] { actual1, actual2 }) {
            assertEquals(getItemName(), actual.getName());
        }
        HistoricItem storedFirst;
        HistoricItem storedSecond;
        if (ascending) {
            storedFirst = actual1;
            storedSecond = actual2;
        } else {
            storedFirst = actual2;
            storedSecond = actual1;
        }

        assertStateEquals(getFirstItemState(), storedFirst.getState());
        assertTrue(storedFirst.getTimestamp().toInstant().isBefore(afterStore1.toInstant()));
        assertTrue(storedFirst.getTimestamp().toInstant().isAfter(beforeStore.toInstant()));

        assertStateEquals(getSecondItemState(), storedSecond.getState());
        assertTrue(storedSecond.getTimestamp().toInstant().isBefore(afterStore2.toInstant()));
        assertTrue(storedSecond.getTimestamp().toInstant().isAfter(afterStore1.toInstant()));
    }

    @Test
    public void testQueryUsingName() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setItemName(getItemName());
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            assertIterableContainsItems(iterable, true);
        });
    }

    @Test
    public void testQueryUsingNameAndStart() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            assertIterableContainsItems(iterable, true);
        });
    }

    @Test
    public void testQueryUsingNameAndStartNoMatch() {
        waitForAssert(() -> {

            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(getItemName());
            criteria.setBeginDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            assertFalse(iterable.iterator().hasNext());
        });
    }

    @Test
    public void testQueryUsingNameAndEnd() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setItemName(getItemName());
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            assertIterableContainsItems(iterable, true);
        });
    }

    @Test
    public void testQueryUsingNameAndEndNoMatch() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(getItemName());
            criteria.setEndDate(beforeStore);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            assertFalse(iterable.iterator().hasNext());
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEnd() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            assertIterableContainsItems(iterable, true);
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndDesc() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOrdering(Ordering.DESCENDING);
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            assertIterableContainsItems(iterable, false);
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndWithNEQOperator() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOperator(Operator.NEQ);
            criteria.setState(getSecondItemState());
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            Iterator<HistoricItem> iterator = iterable.iterator();
            assertTrue(iterator.hasNext());
            HistoricItem actual1 = iterator.next();
            assertFalse(iterator.hasNext());
            assertStateEquals(getFirstItemState(), actual1.getState());
            assertTrue(actual1.getTimestamp().toInstant().isBefore(afterStore1.toInstant()));
            assertTrue(actual1.getTimestamp().toInstant().isAfter(beforeStore.toInstant()));
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndWithEQOperator() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOperator(Operator.EQ);
            criteria.setState(getFirstItemState());
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            Iterator<HistoricItem> iterator = iterable.iterator();
            assertTrue(iterator.hasNext());
            HistoricItem actual1 = iterator.next();
            assertFalse(iterator.hasNext());
            assertStateEquals(getFirstItemState(), actual1.getState());
            assertTrue(actual1.getTimestamp().toInstant().isBefore(afterStore1.toInstant()));
            assertTrue(actual1.getTimestamp().toInstant().isAfter(beforeStore.toInstant()));
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndWithLTOperator() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOperator(Operator.LT);
            criteria.setState(getSecondItemState());
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            Iterator<HistoricItem> iterator = iterable.iterator();
            assertTrue(iterator.hasNext());
            HistoricItem actual1 = iterator.next();
            assertFalse(iterator.hasNext());
            assertStateEquals(getFirstItemState(), actual1.getState());
            assertTrue(actual1.getTimestamp().toInstant().isBefore(afterStore1.toInstant()));
            assertTrue(actual1.getTimestamp().toInstant().isAfter(beforeStore.toInstant()));
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndWithLTOperatorNoMatch() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOperator(Operator.LT);
            criteria.setState(getFirstItemState());
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            Iterator<HistoricItem> iterator = iterable.iterator();
            assertFalse(iterator.hasNext());
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndWithLTEOperator() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOperator(Operator.LTE);
            criteria.setState(getFirstItemState());
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            Iterator<HistoricItem> iterator = iterable.iterator();
            assertTrue(iterator.hasNext());
            HistoricItem actual1 = iterator.next();
            assertFalse(iterator.hasNext());
            assertStateEquals(getFirstItemState(), actual1.getState());
            assertTrue(actual1.getTimestamp().toInstant().isBefore(afterStore1.toInstant()));
            assertTrue(actual1.getTimestamp().toInstant().isAfter(beforeStore.toInstant()));
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndWithGTOperator() {
        waitForAssert(() -> {
            // Skip for subclasses which have null "state between"
            assumeTrue(getQueryItemStateBetween() != null);

            FilterCriteria criteria = new FilterCriteria();
            criteria.setOperator(Operator.GT);
            criteria.setState(getQueryItemStateBetween());
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            Iterator<HistoricItem> iterator = iterable.iterator();
            assertTrue(iterator.hasNext());
            HistoricItem actual1 = iterator.next();
            assertFalse(iterator.hasNext());
            assertStateEquals(getSecondItemState(), actual1.getState());
            assertTrue(actual1.getTimestamp().toInstant().isBefore(afterStore2.toInstant()));
            assertTrue(actual1.getTimestamp().toInstant().isAfter(afterStore1.toInstant()));
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndWithGTOperatorNoMatch() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOperator(Operator.GT);
            criteria.setState(getSecondItemState());
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            Iterator<HistoricItem> iterator = iterable.iterator();
            assertFalse(iterator.hasNext());
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndWithGTEOperator() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOperator(Operator.GTE);
            criteria.setState(getSecondItemState());
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore2);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            Iterator<HistoricItem> iterator = iterable.iterator();
            assertTrue(iterator.hasNext());
            HistoricItem actual1 = iterator.next();
            assertFalse(iterator.hasNext());
            assertStateEquals(getSecondItemState(), actual1.getState());
            assertTrue(actual1.getTimestamp().toInstant().isBefore(afterStore2.toInstant()));
            assertTrue(actual1.getTimestamp().toInstant().isAfter(afterStore1.toInstant()));
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndFirst() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(afterStore1);
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);

            Iterator<HistoricItem> iterator = iterable.iterator();
            assertTrue(iterator.hasNext());
            HistoricItem actual1 = iterator.next();
            assertFalse(iterator.hasNext());
            assertStateEquals(getFirstItemState(), actual1.getState());
            assertTrue(actual1.getTimestamp().toInstant().isBefore(afterStore1.toInstant()));
            assertTrue(actual1.getTimestamp().toInstant().isAfter(beforeStore.toInstant()));
        });
    }

    @Test
    public void testQueryUsingNameAndStartAndEndNoMatch() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(getItemName());
            criteria.setBeginDate(beforeStore);
            criteria.setEndDate(beforeStore); // sic
            @SuppressWarnings("null")
            Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
            assertFalse(iterable.iterator().hasNext());
        });
    }
}
