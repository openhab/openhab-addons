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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link RRD4jPersistenceService}.
 *
 * @author Copilot - Initial contribution
 * @author Holger Friedrich - refactoring and additional tests
 */
@ExtendWith(MockitoExtension.class)
class RRD4jPersistenceServiceTest {
    private static final long STORAGE_TIMEOUT_MS = 20000; // 20 seconds for CI
    private static final long POLL_INTERVAL_MS = 250; // Check every 250ms

    private final Logger logger = LoggerFactory.getLogger(RRD4jPersistenceServiceTest.class);

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

    /**
     * Waits for data to be persisted by polling the database.
     * This is more robust than Thread.sleep() in CI environments with resource contention.
     *
     * @param itemName the name of the item to check
     * @param timeoutMs maximum time to wait in milliseconds
     * @throws InterruptedException if interrupted while waiting
     */
    private void waitForStorage(String itemName, long timeoutMs) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        int attempts = 0;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            attempts++;

            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(itemName);
            criteria.setPageSize(1);

            try {
                Iterable<HistoricItem> results = service.query(criteria);
                if (results.iterator().hasNext()) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    logger.info("Storage completed for '{}' after {}ms ({} attempts)", itemName, elapsed, attempts);
                    return; // Success!
                }
            } catch (Exception e) {
                // Query might fail if data not ready yet, continue polling
                logger.info("Query attempt {} failed: {}", attempts, e.getMessage());
            }

            Thread.sleep(POLL_INTERVAL_MS);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        fail(String.format("Data for item '%s' was not persisted within %dms (%d polling attempts).", itemName, elapsed,
                attempts));
    }

    private void configureNumberItem(String suffix) throws Exception {
        when(numberItem.getName()).thenReturn("TestNumber" + suffix);
        when(numberItem.getType()).thenReturn("Number");
        when(numberItem.getState()).thenReturn(new DecimalType(42.5));
        when(numberItem.getStateAs(DecimalType.class)).thenReturn(new DecimalType(42.5));
        when(itemRegistry.getItem("TestNumber" + suffix)).thenReturn(numberItem);
    }

    private void configureSwitchItem(String suffix) throws Exception {
        when(switchItem.getName()).thenReturn("TestSwitch" + suffix);
        when(switchItem.getType()).thenReturn("Switch");
        when(switchItem.getStateAs(DecimalType.class)).thenReturn(new DecimalType(1));
        when(itemRegistry.getItem("TestSwitch" + suffix)).thenReturn(switchItem);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (service != null) {
            service.deactivate();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void storeAndRetrieveNumberValue(boolean reloadAfterStore) throws Exception {
        configureNumberItem(reloadAfterStore ? "_PERSISTED" : "_MEMORY");

        // Store a value
        service.store(numberItem);

        if (reloadAfterStore) {
            service.deactivate();
            service = new RRD4jPersistenceService(itemRegistry, Map.of());
        }

        // Wait for background storage to complete
        waitForStorage(numberItem.getName(), STORAGE_TIMEOUT_MS);

        // Query the value back
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(numberItem.getName());
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        criteria.setPageSize(1);
        criteria.setPageNumber(0);

        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify the retrieved value
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals(numberItem.getName(), item.getName());
        assertEquals(new DecimalType(42.5), item.getState());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void storeAndRetrieveSwitchValue(boolean reloadAfterStore) throws Exception {
        configureSwitchItem(reloadAfterStore ? "_PERSISTED" : "_MEMORY");

        // Store a value
        service.store(switchItem);

        if (reloadAfterStore) {
            service.deactivate();
            service = new RRD4jPersistenceService(itemRegistry, Map.of());
        }

        // Wait for background storage to complete
        waitForStorage(switchItem.getName(), STORAGE_TIMEOUT_MS);

        // Query the value back
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(switchItem.getName());
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        criteria.setPageSize(1);
        criteria.setPageNumber(0);

        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify the retrieved value (converted back to OnOffType by toStateMapper)
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals(switchItem.getName(), item.getName());
        assertEquals(OnOffType.ON, item.getState());

        PersistedItem persistedItem = service.persistedItem(switchItem.getName(), null);
        assertNotNull(persistedItem);
        assertEquals(switchItem.getName(), persistedItem.getName());
        assertEquals(OnOffType.ON, persistedItem.getState());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void queryWithTimeRange(boolean reloadAfterStore) throws Exception {
        configureNumberItem(reloadAfterStore ? "_PERSISTED" : "_MEMORY");

        // Store a value
        service.store(numberItem);

        if (reloadAfterStore) {
            service.deactivate();
            service = new RRD4jPersistenceService(itemRegistry, Map.of());
        }

        // Wait for background storage to complete
        waitForStorage(numberItem.getName(), STORAGE_TIMEOUT_MS);

        // Query with time range
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(numberItem.getName());
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
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void storeAndRetrieveWithInvalidDBConfig(boolean reloadAfterStore) throws Exception {
        service = new RRD4jPersistenceService(itemRegistry, Map.of("something.invalid", "invalid/path/to/db"));

        configureNumberItem(reloadAfterStore ? "_PERSISTED" : "_MEMORY");

        // Store a value
        service.store(numberItem);

        if (reloadAfterStore) {
            service.deactivate();
            service = new RRD4jPersistenceService(itemRegistry, Map.of("something.invalid", "invalid/path/to/db"));
        }

        // Wait for background storage to complete
        waitForStorage(numberItem.getName(), STORAGE_TIMEOUT_MS);

        // Query the value back
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(numberItem.getName());
        criteria.setOrdering(FilterCriteria.Ordering.ASCENDING);
        criteria.setPageSize(1);
        criteria.setPageNumber(0);
        criteria.setBeginDate(ZonedDateTime.now(ZoneId.systemDefault()).minusHours(1));

        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify the retrieved value
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals(numberItem.getName(), item.getName());
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
