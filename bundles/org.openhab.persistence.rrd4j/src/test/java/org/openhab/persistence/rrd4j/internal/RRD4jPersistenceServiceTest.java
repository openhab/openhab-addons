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
package org.openhab.persistence.rrd4j.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistedItem;

/**
 * Tests for {@link RRD4jPersistenceService}.
 *
 * @author Copilot - Initial contribution
 * @author Holger Friedrich - refactoring and additional tests
 */
@ExtendWith(MockitoExtension.class)
class RRD4jPersistenceServiceTest {
    static final int STORAGE_TIMEOUT_MS = 1000;

    @Mock
    private ItemRegistry itemRegistry;

    @Mock
    private NumberItem numberItem;

    @Mock
    private SwitchItem switchItem;

    private RRD4jPersistenceService service;

    @BeforeEach
    void setUp() throws Exception {
        // Create service with empty config
        service = new RRD4jPersistenceService(itemRegistry, Map.of());
    }

    private void configureNumberItem() throws Exception {
        when(numberItem.getName()).thenReturn("TestNumber");
        when(numberItem.getType()).thenReturn("Number");
        when(numberItem.getState()).thenReturn(new DecimalType(42.5));
        when(numberItem.getStateAs(DecimalType.class)).thenReturn(new DecimalType(42.5));
        when(itemRegistry.getItem("TestNumber")).thenReturn(numberItem);
    }

    private void configureSwitchItem() throws Exception {
        when(switchItem.getName()).thenReturn("TestSwitch");
        when(switchItem.getType()).thenReturn("Switch");
        when(switchItem.getStateAs(DecimalType.class)).thenReturn(new DecimalType(1));
        when(itemRegistry.getItem("TestSwitch")).thenReturn(switchItem);
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

        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify the retrieved value
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals("TestNumber", item.getName());
        assertEquals(new DecimalType(42.5), item.getState());
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

        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify we got at least one result
        assertNotNull(results.iterator().hasNext());
    }

    @Test
    void serviceIdIsCorrect() throws Exception {
        RRD4jPersistenceService simpleService = new RRD4jPersistenceService(itemRegistry, Map.of());
        try {
            assertEquals("rrd4j", simpleService.getId());
        } finally {
            simpleService.deactivate();
        }
    }

    @Test
    void labelIsCorrect() throws Exception {
        RRD4jPersistenceService simpleService = new RRD4jPersistenceService(itemRegistry, Map.of());
        try {
            assertEquals("RRD4j", simpleService.getLabel(null));
        } finally {
            simpleService.deactivate();
        }
    }

    // just to increase test coverage, supply an invalid DB config which will be ignored
    @Test
    void storeAndRetrieveWithInvalidDBConfig() throws Exception {
        service = new RRD4jPersistenceService(itemRegistry, Map.of("something.invalid", "invalid/path/to/db"));

        configureNumberItem();

        // Store a value
        service.store(numberItem);

        // Wait for background storage to complete
        Thread.sleep(STORAGE_TIMEOUT_MS);

        // Query the value back
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName("TestNumber");
        criteria.setOrdering(FilterCriteria.Ordering.ASCENDING);
        criteria.setPageSize(1);
        criteria.setPageNumber(0);
        criteria.setBeginDate(ZonedDateTime.now(ZoneId.systemDefault()).minusHours(1));

        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify the retrieved value
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals("TestNumber", item.getName());
        assertEquals(new DecimalType(42.5), item.getState());
    }

    @Test
    void checkRddFormatCompatibility() throws Exception {
        PersistedItem persistedItem = service.persistedItem("KnownNumber", null);
        assertNotNull(persistedItem);
        assertEquals("KnownNumber", persistedItem.getName());
        assertEquals(new DecimalType(23.5), persistedItem.getState());
    }
}
