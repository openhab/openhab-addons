/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Operator;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class PagingIntegrationTest extends BaseIntegrationTest {

    public static final boolean LEGACY_MODE = false;
    private static final String NAME = "number";
    private static final int STATE_COUNT = 10;

    private static @Nullable ZonedDateTime storeStart;

    @SuppressWarnings("null")
    @BeforeAll
    public static void populateData() {
        storeStart = ZonedDateTime.now();

        NumberItem item = (NumberItem) ITEMS.get(NAME);
        for (int i = 0; i < STATE_COUNT; i++) {
            item.setState(new DecimalType(i));
            try {
                // Add some delay to enforce different timestamps in ms accuracy
                Thread.sleep(5);
            } catch (InterruptedException e) {
                fail("Interrupted");
                return;
            }
            service.store(item);
        }
    }

    @SuppressWarnings("null")
    @Test
    public void testPagingFirstPage() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(NAME);
            criteria.setBeginDate(storeStart);
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setPageNumber(0);
            criteria.setPageSize(3);
            assertItemStates(BaseIntegrationTest.service.query(criteria), 0, 1, 2);
        });
    }

    @SuppressWarnings("null")
    @Test
    public void testPagingSecondPage() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(NAME);
            criteria.setBeginDate(storeStart);
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setPageNumber(1);
            criteria.setPageSize(3);
            assertItemStates(BaseIntegrationTest.service.query(criteria), 3, 4, 5);
        });
    }

    @SuppressWarnings("null")
    @Test
    public void testPagingPagePartialPage() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(NAME);
            criteria.setBeginDate(storeStart);
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setPageNumber(3);
            criteria.setPageSize(3);
            assertItemStates(BaseIntegrationTest.service.query(criteria), 9);
        });
    }

    @SuppressWarnings("null")
    @Test
    public void testPagingPageOutOfBounds() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(NAME);
            criteria.setBeginDate(storeStart);
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setPageNumber(4);
            criteria.setPageSize(3);
            assertItemStates(BaseIntegrationTest.service.query(criteria)); // no results
        });
    }

    @SuppressWarnings("null")
    @Test
    public void testPagingPage0Descending() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(NAME);
            criteria.setBeginDate(storeStart);
            criteria.setOrdering(Ordering.DESCENDING);
            criteria.setPageNumber(0);
            criteria.setPageSize(3);
            assertItemStates(BaseIntegrationTest.service.query(criteria), 9, 8, 7);
        });
    }

    @SuppressWarnings("null")
    @Test
    public void testPagingPage0HugePageSize() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(NAME);
            criteria.setBeginDate(storeStart);
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setPageNumber(0);
            criteria.setPageSize(900);
            assertItemStates(BaseIntegrationTest.service.query(criteria), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        });
    }

    @SuppressWarnings("null")
    @Test
    public void testPagingFirstPageWithFilter() {
        waitForAssert(() -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(NAME);
            criteria.setBeginDate(storeStart);
            criteria.setOrdering(Ordering.ASCENDING);
            criteria.setPageNumber(0);
            criteria.setPageSize(3);
            criteria.setOperator(Operator.GT);
            criteria.setState(new DecimalType(new BigDecimal(3)));
            assertItemStates(BaseIntegrationTest.service.query(criteria), 4, 5, 6);
        });
    }

    private void assertItemStates(Iterable<HistoricItem> actualIterable, int... expected) {
        Iterator<HistoricItem> actualIterator = actualIterable.iterator();
        List<DecimalType> expectedStates = new ArrayList<>();
        List<DecimalType> actualStates = new ArrayList<>();
        for (int expectedState : expected) {
            assertTrue(actualIterator.hasNext());
            HistoricItem actual = actualIterator.next();
            expectedStates.add(new DecimalType(expectedState));
            actualStates.add((DecimalType) actual.getState());
        }
        assertEquals(expectedStates, actualStates);
        assertFalse(actualIterator.hasNext());
    }
}
