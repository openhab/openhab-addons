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
package org.openhab.persistence.timescaledb.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.types.UnDefType;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Unit tests for {@link TimescaleDBPersistenceService} using a mocked DataSource.
 *
 * <p>
 * The service is constructed and activated directly (bypassing OSGi) to allow
 * mock injection.
 *
 * @author René Ulbricht - Initial contribution
 */
class TimescaleDBPersistenceServiceTest {

    private HikariDataSource dataSource;
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private ItemRegistry itemRegistry;
    private MetadataRegistry metadataRegistry;

    /**
     * Subclass that exposes the DataSource injection point for testing.
     */
    private static class TestableService extends TimescaleDBPersistenceService {

        private final HikariDataSource injectedDs;

        TestableService(ItemRegistry ir, MetadataRegistry mr, HikariDataSource ds) {
            super(ir, new TimescaleDBMetadataService(mr));
            this.injectedDs = ds;
        }

        @Override
        public void activate(Map<String, Object> config) {
            // Skip the real activate (which would try to connect) and wire the mock directly
            // via reflection to set the dataSource field.
            try {
                var dsField = TimescaleDBPersistenceService.class.getDeclaredField("dataSource");
                dsField.setAccessible(true);
                dsField.set(this, injectedDs);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private TestableService service;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(HikariDataSource.class);
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        itemRegistry = mock(ItemRegistry.class);
        metadataRegistry = mock(MetadataRegistry.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(resultSet.next()).thenReturn(false);
        when(metadataRegistry.getAll()).thenReturn(Collections.emptyList());

        service = new TestableService(itemRegistry, metadataRegistry, dataSource);
        service.activate(Map.of());
    }

    @AfterEach
    void tearDown() {
        service.deactivate();
    }

    // ------------------------------------------------------------------
    // Service metadata
    // ------------------------------------------------------------------

    @Test
    void getId_returnsTimescaledb() {
        assertEquals("timescaledb", service.getId());
    }

    @Test
    void getLabel_returnsHumanReadableLabel() {
        assertNotNull(service.getLabel(null));
        assertFalse(service.getLabel(null).isBlank());
    }

    @Test
    void getDefaultStrategies_returnsEmptyList() {
        assertTrue(service.getDefaultStrategies().isEmpty());
    }

    // ------------------------------------------------------------------
    // store
    // ------------------------------------------------------------------

    @Test
    void store_normalState_sendsInsert() throws Exception {
        // item_id cache is empty → getOrCreateItemId will run SELECT then INSERT
        stubItemIdLookup(7);

        var item = new NumberItem("Sensor1");
        service.store(item, ZonedDateTime.now(), new DecimalType(42.0), null);

        // At minimum one connection obtained for the store operation
        verify(dataSource, atLeastOnce()).getConnection();
    }

    @Test
    void store_undefState_doesNotTouchDatabase() throws Exception {
        var item = new NumberItem("Sensor1");
        service.store(item, ZonedDateTime.now(), UnDefType.UNDEF, null);

        verify(dataSource, never()).getConnection();
    }

    @Test
    void store_withAlias_usesAliasName() throws Exception {
        stubItemIdLookup(3);
        var item = new NumberItem("RealName");

        // Capture which PreparedStatements get setString(1, "AliasName")
        PreparedStatement selectPs = mock(PreparedStatement.class);
        ResultSet selectRs = mock(ResultSet.class);
        when(selectRs.next()).thenReturn(false);
        when(selectPs.executeQuery()).thenReturn(selectRs);
        when(connection.prepareStatement(contains("SELECT id FROM item_meta"))).thenReturn(selectPs);

        service.store(item, ZonedDateTime.now(), new DecimalType(1.0), "AliasName");

        // The item_id lookup SELECT must be called with the alias
        verify(selectPs, atLeastOnce()).setString(eq(1), eq("AliasName"));
    }

    // ------------------------------------------------------------------
    // query
    // ------------------------------------------------------------------

    @Test
    void query_itemNotInCache_returnsEmptyList() throws Exception {
        var filter = new FilterCriteria();
        filter.setItemName("UnknownItem");

        var result = service.query(filter);

        assertFalse(result.iterator().hasNext());
        // DataSource should not be queried if item_id is not cached
        verify(dataSource, never()).getConnection();
    }

    @Test
    void query_noItemName_returnsEmptyList() {
        var filter = new FilterCriteria();
        // no item name set

        var result = service.query(filter);
        assertFalse(result.iterator().hasNext());
    }

    @Test
    void query_knownItem_delegatesToQueryClass() throws Exception {
        // Pre-populate the cache by storing first
        stubItemIdLookup(5);
        when(itemRegistry.getItem("Sensor1")).thenReturn(new NumberItem("Sensor1"));

        var item = new NumberItem("Sensor1");
        service.store(item, ZonedDateTime.now(), new DecimalType(1.0), null);

        var filter = new FilterCriteria();
        filter.setItemName("Sensor1");

        service.query(filter);

        // Connection should now be used for the query as well
        verify(dataSource, atLeast(2)).getConnection();
    }

    // ------------------------------------------------------------------
    // remove
    // ------------------------------------------------------------------

    @Test
    void remove_itemNotInCache_returnsFalse() throws Exception {
        var filter = new FilterCriteria();
        filter.setItemName("UnknownItem");

        assertFalse(service.remove(filter));
        verify(dataSource, never()).getConnection();
    }

    @Test
    void remove_knownItem_returnsTrue() throws Exception {
        stubItemIdLookup(9);

        // Store to populate cache
        service.store(new NumberItem("Sensor1"), ZonedDateTime.now(), new DecimalType(1.0), null);

        var filter = new FilterCriteria();
        filter.setItemName("Sensor1");
        when(preparedStatement.executeUpdate()).thenReturn(3);

        assertTrue(service.remove(filter));
    }

    @Test
    void remove_noItemName_returnsFalse() {
        var filter = new FilterCriteria();
        assertFalse(service.remove(filter));
    }

    // ------------------------------------------------------------------
    // activate() / deactivate() — real code paths
    // ------------------------------------------------------------------

    @Test
    void activate_missingUrl_dataSourceRemainsNull() throws Exception {
        var realService = new TimescaleDBPersistenceService(mock(ItemRegistry.class),
                new TimescaleDBMetadataService(mock(MetadataRegistry.class)));
        realService.activate(Map.of()); // no 'url' key

        var dsField = TimescaleDBPersistenceService.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        assertTrue(null == dsField.get(realService), "dataSource must be null when url is missing");
        realService.deactivate();
    }

    @Test
    void activate_invalidUrl_dataSourceRemainsNull() throws Exception {
        var realService = new TimescaleDBPersistenceService(mock(ItemRegistry.class),
                new TimescaleDBMetadataService(mock(MetadataRegistry.class)));
        // Unreachable host; short timeout so the test does not block long
        realService.activate(
                Map.of("url", "jdbc:postgresql://localhost:19999/invalid", "password", "x", "connectTimeout", "500"));

        var dsField = TimescaleDBPersistenceService.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        assertTrue(null == dsField.get(realService), "dataSource must be null when connection fails");
        realService.deactivate();
    }

    @Test
    @SuppressWarnings("unchecked")
    void deactivate_withScheduledJob_cancelsJob() throws Exception {
        ScheduledFuture<Object> mockJob = mock(ScheduledFuture.class);
        var jobField = TimescaleDBPersistenceService.class.getDeclaredField("downsampleJob");
        jobField.setAccessible(true);
        jobField.set(service, mockJob);

        service.deactivate();

        verify(mockJob).cancel(false);
    }

    @Test
    void deactivate_closesDataSource() throws Exception {
        service.deactivate();
        verify(dataSource).close();
    }

    // ------------------------------------------------------------------
    // Private helpers tested via reflection
    // ------------------------------------------------------------------

    @Test
    void secondsUntilMidnight_isPositiveAndAtMost24h() {
        long seconds = TimescaleDBPersistenceService.secondsUntilMidnight();
        assertTrue(seconds > 0, "Seconds until midnight must be positive");
        assertTrue(seconds <= 86400, "Seconds until midnight must not exceed 24 h");
    }

    @Test
    void parseIntConfig_validValue_returnsParsed() {
        assertEquals(42, TimescaleDBPersistenceService.parseIntConfig(Map.of("k", "42"), "k", 0));
    }

    @Test
    void parseIntConfig_missingKey_returnsDefault() {
        assertEquals(7, TimescaleDBPersistenceService.parseIntConfig(Map.of(), "k", 7));
    }

    @Test
    void parseIntConfig_invalidValue_returnsDefault() {
        assertEquals(3, TimescaleDBPersistenceService.parseIntConfig(Map.of("k", "notanumber"), "k", 3));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Stubs the item_id lookup: SELECT returns nothing, INSERT returns the given id.
     */
    private void stubItemIdLookup(int itemId) throws Exception {
        ResultSet selectRs = mock(ResultSet.class);
        ResultSet insertRs = mock(ResultSet.class);
        PreparedStatement selectPs = mock(PreparedStatement.class);
        PreparedStatement insertPs = mock(PreparedStatement.class);
        PreparedStatement insertItemPs = mock(PreparedStatement.class);

        when(selectRs.next()).thenReturn(false);
        when(insertRs.next()).thenReturn(true);
        when(insertRs.getInt(1)).thenReturn(itemId);
        when(selectPs.executeQuery()).thenReturn(selectRs);
        when(insertPs.executeQuery()).thenReturn(insertRs);
        when(insertItemPs.executeUpdate()).thenReturn(1);

        when(connection.prepareStatement(contains("SELECT id FROM item_meta"))).thenReturn(selectPs);
        when(connection.prepareStatement(contains("INSERT INTO item_meta"))).thenReturn(insertPs);
        when(connection.prepareStatement(contains("INSERT INTO items"))).thenReturn(insertItemPs);
    }
}
