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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
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

    private static final String NAME = "number";
    private static final int STATE_COUNT = 10;

    private static @Nullable ZonedDateTime storeStart;

    @BeforeAll
    public static void checkService() throws InterruptedException {
        String msg = "DynamoDB integration tests will be skipped. Did you specify AWS credentials for testing? "
                + "See BaseIntegrationTest for more details";
        if (service == null) {
            System.out.println(msg);
        }
        assumeTrue(service != null, msg);

        populateData();
    }

    public static void populateData() {
        storeStart = ZonedDateTime.now();

        NumberItem item = (NumberItem) ITEMS.get(NAME);

        for (int i = 0; i < STATE_COUNT; i++) {
            item.setState(new DecimalType(i));
            service.store(item);
        }
    }

    @Test
    public void testPagingFirstPage() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(NAME);
        criteria.setBeginDate(storeStart);
        criteria.setOrdering(Ordering.ASCENDING);
        criteria.setPageNumber(0);
        criteria.setPageSize(3);
        assertItemStates(BaseIntegrationTest.service.query(criteria), 0, 1, 2);
    }

    @Test
    public void testPagingSecondPage() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(NAME);
        criteria.setBeginDate(storeStart);
        criteria.setOrdering(Ordering.ASCENDING);
        criteria.setPageNumber(1);
        criteria.setPageSize(3);
        assertItemStates(BaseIntegrationTest.service.query(criteria), 3, 4, 5);
    }

    @Test
    public void testPagingPagePartialPage() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(NAME);
        criteria.setBeginDate(storeStart);
        criteria.setOrdering(Ordering.ASCENDING);
        criteria.setPageNumber(3);
        criteria.setPageSize(3);
        assertItemStates(BaseIntegrationTest.service.query(criteria), 9);
    }

    @Test
    public void testPagingPageOutOfBounds() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(NAME);
        criteria.setBeginDate(storeStart);
        criteria.setOrdering(Ordering.ASCENDING);
        criteria.setPageNumber(4);
        criteria.setPageSize(3);
        assertItemStates(BaseIntegrationTest.service.query(criteria)); // no results
    }

    @Test
    public void testPagingPage0Descending() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(NAME);
        criteria.setBeginDate(storeStart);
        criteria.setOrdering(Ordering.DESCENDING);
        criteria.setPageNumber(0);
        criteria.setPageSize(3);
        assertItemStates(BaseIntegrationTest.service.query(criteria), 9, 8, 7);
    }

    @Test
    public void testPagingPage0HugePageSize() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(NAME);
        criteria.setBeginDate(storeStart);
        criteria.setOrdering(Ordering.ASCENDING);
        criteria.setPageNumber(0);
        criteria.setPageSize(900);
        assertItemStates(BaseIntegrationTest.service.query(criteria), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    public void testPagingFirstPageWithFilter() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(NAME);
        criteria.setBeginDate(storeStart);
        criteria.setOrdering(Ordering.ASCENDING);
        criteria.setPageNumber(0);
        criteria.setPageSize(3);
        criteria.setOperator(Operator.GT);
        criteria.setState(new DecimalType(new BigDecimal(3)));
        assertItemStates(BaseIntegrationTest.service.query(criteria), 4, 5, 6);
    }

    private void assertItemStates(Iterable<HistoricItem> actualIterable, int... expected) {
        Iterator<HistoricItem> actualIterator = actualIterable.iterator();
        List<HistoricItem> got = new LinkedList<HistoricItem>();
        for (int expectedState : expected) {
            assertTrue(actualIterator.hasNext());
            HistoricItem actual = actualIterator.next();
            assertEquals(new DecimalType(expectedState), actual.getState());
            got.add(actual);
        }
        if (actualIterator.hasNext()) {
            fail("Did not expect any more items, but got at least this extra element: "
                    + actualIterator.next().toString() + ". Before this we got: " + Arrays.toString(got.toArray()));
        }
    }
}
