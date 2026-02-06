/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.persistence.mapdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistedItem;
import org.openhab.persistence.mapdb.internal.MapDbPersistenceService;

/**
 * Tests for {@link MapDbPersistenceService}.
 *
 * @author Copilot - Initial contribution
 * @author Holger Friedrich - refactoring and additional tests
 */
@ExtendWith(MockitoExtension.class)
class MapDbPersistenceServiceTest {
    static final int STORAGE_TIMEOUT_MS = 1000;

    @Mock
    private ItemRegistry itemRegistry;

    @Mock
    private NumberItem numberItem;

    @Mock
    private SwitchItem switchItem;

    @Mock
    private StringItem stringItem;

    private MapDbPersistenceService service;

    @BeforeEach
    void setUp() throws Exception {
        // Create service and activate OSGi lifecycle manually for tests
        service = new MapDbPersistenceService();
        service.activate();
    }

    private void configureNumberItem() throws Exception {
        when(numberItem.getName()).thenReturn("TestNumber");
        when(numberItem.getState()).thenReturn(new DecimalType(42.5));
    }

    private void configureStringItem() throws Exception {
        when(stringItem.getName()).thenReturn("TestString");
        when(stringItem.getState()).thenReturn(new StringType("TestValue"));
    }

    private void configureSwitchItem() throws Exception {
        when(switchItem.getName()).thenReturn("TestSwitch");
        when(switchItem.getState()).thenReturn(OnOffType.ON);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (service != null) {
            service.deactivate();
        }
    }

    @Test
    void storeAndRetrieveNumberValue() throws Exception {
        configureNumberItem();

        // Store a value
        service.store(numberItem);

        // Wait for background storage to complete
        Thread.sleep(STORAGE_TIMEOUT_MS);

        // Query the value back
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName("TestNumber");
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        criteria.setPageSize(1);
        criteria.setPageNumber(0);

        // HistoricItem will anyway only return last stored value for MapDb, but this should work without errors
        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify the retrieved value
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals("TestNumber", item.getName());
        assertEquals(new DecimalType(42.5), item.getState());
    }

    @Test
    void storeAndRetrieveStringValue() throws Exception {
        configureStringItem();

        // Store a value
        service.store(stringItem);

        // Wait for background storage to complete
        Thread.sleep(STORAGE_TIMEOUT_MS);

        // Query the value back
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName("TestString");
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        criteria.setPageSize(1);
        criteria.setPageNumber(0);

        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify the retrieved value
        // HistoricItem will anyway only return last stored value for MapDb, but this should work without errors
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals("TestString", item.getName());
        assertEquals(new StringType("TestValue"), item.getState());

        PersistedItem persistedItem = service.persistedItem("TestString", null);
        assertNotNull(persistedItem);
        assertEquals("TestString", persistedItem.getName());
        assertEquals(new StringType("TestValue"), persistedItem.getState());
    }

    @Test
    void storeAndRetrieveSwitchValue() throws Exception {
        configureSwitchItem();

        // Store a value
        service.store(switchItem);

        // Wait for background storage to complete
        Thread.sleep(STORAGE_TIMEOUT_MS);

        // Query the value back
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName("TestSwitch");
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        criteria.setPageSize(1);
        criteria.setPageNumber(0);

        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify the retrieved value (converted back to OnOffType by toStateMapper)
        // HistoricItem will anyway only return last stored value for MapDb, but this should work without errors
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals("TestSwitch", item.getName());
        assertEquals(OnOffType.ON, item.getState());

        PersistedItem persistedItem = service.persistedItem("TestSwitch", null);
        assertNotNull(persistedItem);
        assertEquals("TestSwitch", persistedItem.getName());
        assertEquals(OnOffType.ON, persistedItem.getState());
    }

    @Test
    void queryWithTimeRange() throws Exception {
        configureNumberItem();

        // Store a value
        service.store(numberItem);

        // Wait for background storage to complete
        Thread.sleep(STORAGE_TIMEOUT_MS);

        // Query with time range
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName("TestNumber");
        criteria.setBeginDate(ZonedDateTime.now(ZoneId.systemDefault()).minusHours(1));
        criteria.setEndDate(ZonedDateTime.now(ZoneId.systemDefault()).plusHours(1));
        criteria.setOrdering(FilterCriteria.Ordering.ASCENDING);

        // HistoricItem will anyway only return last stored value for MapDb, but this should work without errors
        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify we got at least one result
        assertTrue(results.iterator().hasNext());

        // try to get a non-existing persisted item
        PersistedItem persistedItem = service.persistedItem("UnknownTestItem", null);
        assertNull(persistedItem);
    }

    @Test
    void serviceIdIsCorrect() throws Exception {
        MapDbPersistenceService simpleService = new MapDbPersistenceService();
        try {
            simpleService.activate();
            assertEquals("mapdb", simpleService.getId());
        } finally {
            simpleService.deactivate();
        }
    }

    @Test
    void labelIsCorrect() throws Exception {
        MapDbPersistenceService simpleService = new MapDbPersistenceService();
        try {
            simpleService.activate();
            assertEquals("MapDB", simpleService.getLabel(null));
        } finally {
            simpleService.deactivate();
        }
    }

    /*
     * Does not make much sense, MapDb stores only last value and creates a new DB on failures
     * void checkMapDbFormatCompatibility() throws Exception {
     * ...
     * }
     */
}
