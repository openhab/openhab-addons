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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link MapDbPersistenceService}.
 *
 * @author Copilot - Initial contribution
 * @author Holger Friedrich - refactoring and additional tests
 */
@ExtendWith(MockitoExtension.class)
class MapDbPersistenceServiceTest {
    private static final long STORAGE_TIMEOUT_MS = 20000; // 20 seconds for CI
    private static final long POLL_INTERVAL_MS = 250; // Check every 250ms

    private final Logger logger = LoggerFactory.getLogger(MapDbPersistenceServiceTest.class);

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
        when(numberItem.getState()).thenReturn(new DecimalType(42.5));
    }

    private void configureStringItem(String suffix) throws Exception {
        when(stringItem.getName()).thenReturn("TestString" + suffix);
        when(stringItem.getState()).thenReturn(new StringType("TestValue"));
    }

    private void configureSwitchItem(String suffix) throws Exception {
        when(switchItem.getName()).thenReturn("TestSwitch" + suffix);
        when(switchItem.getState()).thenReturn(OnOffType.ON);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (service != null) {
            service.deactivate();
        }
    }

    // Test storing and retrieving a number value, with and without service reload in between.
    // With reloadAfterStore=true, this verifies that data is correctly persisted and can be retrieved
    // even after a restart.
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void storeAndRetrieveNumberValue(boolean reloadAfterStore) throws Exception {
        logger.debug("Starting storeAndRetrieveNumberValue with reloadAfterStore={}", reloadAfterStore);
        configureNumberItem(reloadAfterStore ? "_PERSISTED" : "_MEMORY");

        // Store a value
        service.store(numberItem);

        if (reloadAfterStore) {
            service.deactivate();
            service.activate();
        }

        // Wait for background storage to complete
        waitForStorage(numberItem.getName(), STORAGE_TIMEOUT_MS);

        // Query the value back
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(numberItem.getName());
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        criteria.setPageSize(1);
        criteria.setPageNumber(0);

        // HistoricItem will anyway only return last stored value for MapDb, but this should work without errors
        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify the retrieved value
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals(numberItem.getName(), item.getName());
        assertEquals(new DecimalType(42.5), item.getState());
        logger.debug("Ending storeAndRetrieveNumberValue with reloadAfterStore={}", reloadAfterStore);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void storeAndRetrieveStringValue(boolean reloadAfterStore) throws Exception {
        logger.debug("Starting storeAndRetrieveStringValue with reloadAfterStore={}", reloadAfterStore);
        configureStringItem(reloadAfterStore ? "_PERSISTED" : "_MEMORY");

        // Store a value
        service.store(stringItem);

        if (reloadAfterStore) {
            service.deactivate();
            service.activate();
        }

        // Wait for background storage to complete
        waitForStorage(stringItem.getName(), STORAGE_TIMEOUT_MS);

        // Query the value back
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(stringItem.getName());
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        criteria.setPageSize(1);
        criteria.setPageNumber(0);

        Iterable<HistoricItem> results = service.query(criteria);
        assertNotNull(results);

        // Verify the retrieved value
        // HistoricItem will anyway only return last stored value for MapDb, but this should work without errors
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals(stringItem.getName(), item.getName());
        assertEquals(new StringType("TestValue"), item.getState());

        PersistedItem persistedItem = service.persistedItem(stringItem.getName(), null);
        assertNotNull(persistedItem);
        assertEquals(stringItem.getName(), persistedItem.getName());
        assertEquals(new StringType("TestValue"), persistedItem.getState());
        logger.debug("Ending storeAndRetrieveStringValue with reloadAfterStore={}", reloadAfterStore);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void storeAndRetrieveSwitchValue(boolean reloadAfterStore) throws Exception {
        logger.debug("Starting storeAndRetrieveSwitchValue with reloadAfterStore={}", reloadAfterStore);
        configureSwitchItem(reloadAfterStore ? "_PERSISTED" : "_MEMORY");

        // Store a value
        service.store(switchItem);

        if (reloadAfterStore) {
            service.deactivate();
            service.activate();
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
        // HistoricItem will anyway only return last stored value for MapDb, but this should work without errors
        HistoricItem item = results.iterator().next();
        assertNotNull(item);
        assertEquals(switchItem.getName(), item.getName());
        assertEquals(OnOffType.ON, item.getState());

        PersistedItem persistedItem = service.persistedItem(switchItem.getName(), null);
        assertNotNull(persistedItem);
        assertEquals(switchItem.getName(), persistedItem.getName());
        assertEquals(OnOffType.ON, persistedItem.getState());
        logger.debug("Ending storeAndRetrieveSwitchValue with reloadAfterStore={}", reloadAfterStore);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void queryWithTimeRange(boolean reloadAfterStore) throws Exception {
        logger.debug("Starting queryWithTimeRange with reloadAfterStore={}", reloadAfterStore);
        configureNumberItem(reloadAfterStore ? "_PERSISTED" : "_MEMORY");

        // Store a value
        service.store(numberItem);

        if (reloadAfterStore) {
            service.deactivate();
            service.activate();
        }

        // Wait for background storage to complete
        waitForStorage(numberItem.getName(), STORAGE_TIMEOUT_MS);

        // Query with time range
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(numberItem.getName());
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
        logger.debug("Ending queryWithTimeRange with reloadAfterStore={}", reloadAfterStore);
    }

    @Test
    void serviceIdIsCorrect() throws Exception {
        assertEquals("mapdb", service.getId());
    }

    @Test
    void labelIsCorrect() throws Exception {
        assertEquals("MapDB", service.getLabel(null));
    }

    /*
     * Does not make much sense, MapDb stores only last value and creates a new DB on failures
     * void checkMapDbFormatCompatibility() throws Exception {
     * ...
     * }
     */
}
