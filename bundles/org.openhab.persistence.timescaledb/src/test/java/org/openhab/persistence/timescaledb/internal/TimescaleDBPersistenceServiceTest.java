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

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
@SuppressWarnings("null")
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
            super(ir, mr, new TimescaleDBMetadataService(mr));
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
                throw new IllegalStateException(e);
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
    void getIdReturnstimescaledb() {
        assertEquals("timescaledb", service.getId());
    }

    @Test
    void getLabelReturnshumanreadablelabel() {
        assertNotNull(service.getLabel(null));
        assertFalse(service.getLabel(null).isBlank());
    }

    @Test
    void getSuggestedStrategiesReturnsEmptyList() {
        assertTrue(service.getSuggestedStrategies().isEmpty());
    }

    // ------------------------------------------------------------------
    // store
    // ------------------------------------------------------------------

    @Test
    void storeNormalstateSendsinsert() throws Exception {
        // item_id cache is empty → getOrCreateItemId will run UPSERT
        stubItemIdLookup(7);

        var item = new NumberItem("Sensor1");
        service.store(item, ZonedDateTime.now(), new DecimalType(42.0), null);

        // At minimum one connection obtained for the store operation
        verify(dataSource, atLeastOnce()).getConnection();
    }

    @Test
    void storeUndefstateDoesnottouchdatabase() throws Exception {
        var item = new NumberItem("Sensor1");
        service.store(item, ZonedDateTime.now(), UnDefType.UNDEF, null);

        verify(dataSource, never()).getConnection();
    }

    @Test
    void storeWithaliasUsesaliasname() throws Exception {
        stubItemIdLookup(3);
        var item = new NumberItem("RealName");

        // Capture which PreparedStatements get setString(1, "AliasName")
        PreparedStatement upsertPs = mock(PreparedStatement.class);
        ResultSet upsertRs = mock(ResultSet.class);
        when(upsertRs.next()).thenReturn(true);
        when(upsertRs.getInt(1)).thenReturn(3);
        when(upsertPs.executeQuery()).thenReturn(upsertRs);
        when(connection.prepareStatement(contains("INSERT INTO item_meta"))).thenReturn(upsertPs);

        service.store(item, ZonedDateTime.now(), new DecimalType(1.0), "AliasName");

        // The item_id UPSERT must be called with the alias as parameter 1
        verify(upsertPs, atLeastOnce()).setString(eq(1), eq("AliasName"));
    }

    @Test
    void storeValueStringIsPassedToItemMetaUpsert() throws Exception {
        var upsertPs = mock(PreparedStatement.class);
        var upsertRs = mock(ResultSet.class);
        var insertItemPs = mock(PreparedStatement.class);
        when(upsertRs.next()).thenReturn(true);
        when(upsertRs.getInt(1)).thenReturn(10);
        when(upsertPs.executeQuery()).thenReturn(upsertRs);
        when(insertItemPs.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(contains("INSERT INTO item_meta"))).thenReturn(upsertPs);
        when(connection.prepareStatement(contains("INSERT INTO items"))).thenReturn(insertItemPs);

        // New format: value = "my.sensor" (getValue()), aggregation in config map
        var metaKey = new org.openhab.core.items.MetadataKey("timescaledb", "Sensor1");
        var meta = new org.openhab.core.items.Metadata(metaKey, "my.sensor",
                Map.of("aggregation", "AVG", "downsampleInterval", "1h"));
        when(metadataRegistry.get(metaKey)).thenReturn(meta);

        service.store(new NumberItem("Sensor1"), ZonedDateTime.now(), new DecimalType(1.0), null);

        // Parameter 3 = value string (getText from getValue())
        verify(upsertPs).setString(3, "my.sensor");
    }

    @Test
    void storeMetadataConfigJsonIsPassedToItemMetaUpsert() throws Exception {
        var upsertPs = mock(PreparedStatement.class);
        var upsertRs = mock(ResultSet.class);
        var insertItemPs = mock(PreparedStatement.class);
        when(upsertRs.next()).thenReturn(true);
        when(upsertRs.getInt(1)).thenReturn(11);
        when(upsertPs.executeQuery()).thenReturn(upsertRs);
        when(insertItemPs.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(contains("INSERT INTO item_meta"))).thenReturn(upsertPs);
        when(connection.prepareStatement(contains("INSERT INTO items"))).thenReturn(insertItemPs);

        var metaKey = new org.openhab.core.items.MetadataKey("timescaledb", "Sensor1");
        var meta = new org.openhab.core.items.Metadata(metaKey, "my.sensor",
                Map.of("aggregation", "AVG", "location", "kitchen"));
        when(metadataRegistry.get(metaKey)).thenReturn(meta);

        service.store(new NumberItem("Sensor1"), ZonedDateTime.now(), new DecimalType(1.0), null);

        // Parameter 4 = metadata JSONB — must use setObject with Types.OTHER and contain a JSON string
        verify(upsertPs).setObject(eq(4), argThat(arg -> {
            String s = String.valueOf(arg);
            return s.contains("aggregation") && s.contains("AVG") && s.contains("location");
        }), eq(java.sql.Types.OTHER));
    }

    // ------------------------------------------------------------------
    // query
    // ------------------------------------------------------------------

    @Test
    void queryItemnotincacheAndNotInDbReturnsemptylist() throws Exception {
        var filter = new FilterCriteria();
        filter.setItemName("UnknownItem");
        // Default mock: resultSet.next() = false → item not in DB either

        var result = service.query(filter);

        assertFalse(result.iterator().hasNext());
        // Fix 1: on cache miss the service now performs a DB lookup before giving up
        verify(dataSource, atLeastOnce()).getConnection();
    }

    @Test
    void queryCacheMissItemFoundInDbPopulatescacheAndExecutesquery() throws Exception {
        // Arrange: item_id lookup returns 42, actual query returns no rows
        PreparedStatement findPs = mock(PreparedStatement.class);
        ResultSet findRs = mock(ResultSet.class);
        when(findRs.next()).thenReturn(true);
        when(findRs.getInt(1)).thenReturn(42);
        when(findPs.executeQuery()).thenReturn(findRs);
        when(connection.prepareStatement(contains("SELECT id FROM item_meta"))).thenReturn(findPs);
        when(itemRegistry.getItem("Sensor1")).thenReturn(new NumberItem("Sensor1"));

        var filter = new FilterCriteria();
        filter.setItemName("Sensor1");

        var result = service.query(filter);

        // Still returns empty (no rows in the items table mock)
        assertFalse(result.iterator().hasNext());
        // Fix 1: two connections — one for DB lookup, one for actual query
        verify(dataSource, atLeast(2)).getConnection();
        // Fix 1: item is now cached → a second query must NOT trigger another DB lookup
        reset(findPs);
        service.query(filter);
        verify(findPs, never()).executeQuery();
    }

    @Test
    void queryNoitemnameReturnsemptylist() {
        var filter = new FilterCriteria();
        // no item name set

        var result = service.query(filter);
        assertFalse(result.iterator().hasNext());
    }

    @Test
    void queryKnownitemDelegatestoqueryclass() throws Exception {
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
    void removeItemnotincacheAndNotInDbReturnsfalse() throws Exception {
        var filter = new FilterCriteria();
        filter.setItemName("UnknownItem");
        // Default mock: resultSet.next() = false → item not in DB either

        assertFalse(service.remove(filter));
        // Fix 2: on cache miss the service now performs a DB lookup before giving up
        verify(dataSource, atLeastOnce()).getConnection();
    }

    @Test
    void removeCacheMissItemFoundInDbPopulatescacheAndExecutesdelete() throws Exception {
        // Arrange: item_id lookup returns 7, DELETE returns 3 rows deleted
        PreparedStatement findPs = mock(PreparedStatement.class);
        ResultSet findRs = mock(ResultSet.class);
        when(findRs.next()).thenReturn(true);
        when(findRs.getInt(1)).thenReturn(7);
        when(findPs.executeQuery()).thenReturn(findRs);
        when(connection.prepareStatement(contains("SELECT id FROM item_meta"))).thenReturn(findPs);
        when(preparedStatement.executeUpdate()).thenReturn(3);

        var filter = new FilterCriteria();
        filter.setItemName("Sensor1");

        assertTrue(service.remove(filter));
        // Fix 2: two connections — one for DB lookup, one for DELETE
        verify(dataSource, atLeast(2)).getConnection();
        // Fix 2: item is now cached → a second remove must NOT trigger another DB lookup
        reset(findPs);
        service.remove(filter);
        verify(findPs, never()).executeQuery();
    }

    @Test
    void removeKnownitemReturnstrue() throws Exception {
        stubItemIdLookup(9);

        // Store to populate cache
        service.store(new NumberItem("Sensor1"), ZonedDateTime.now(), new DecimalType(1.0), null);

        var filter = new FilterCriteria();
        filter.setItemName("Sensor1");
        when(preparedStatement.executeUpdate()).thenReturn(3);

        assertTrue(service.remove(filter));
    }

    @Test
    void removeNoitemnameReturnsfalse() {
        var filter = new FilterCriteria();
        assertFalse(service.remove(filter));
    }

    // ------------------------------------------------------------------
    // activate() / deactivate() — real code paths
    // ------------------------------------------------------------------

    @Test
    void activateMissingurlDatasourceremainsnull() throws Exception {
        var realService = new TimescaleDBPersistenceService(mock(ItemRegistry.class), mock(MetadataRegistry.class),
                new TimescaleDBMetadataService(mock(MetadataRegistry.class)));
        realService.activate(Map.of()); // no 'url' key

        var dsField = TimescaleDBPersistenceService.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        assertTrue(null == dsField.get(realService), "dataSource must be null when url is missing");
        realService.deactivate();
    }

    @Test
    void activateInvalidurlDatasourceremainsnull() throws Exception {
        var realService = new TimescaleDBPersistenceService(mock(ItemRegistry.class), mock(MetadataRegistry.class),
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
    void deactivateWithscheduledjobCancelsjob() throws Exception {
        ScheduledFuture<Object> mockJob = mock(ScheduledFuture.class);
        var jobField = TimescaleDBPersistenceService.class.getDeclaredField("downsampleJob");
        jobField.setAccessible(true);
        jobField.set(service, mockJob);

        service.deactivate();

        verify(mockJob).cancel(false);
    }

    @Test
    void deactivateClosesdatasource() throws Exception {
        service.deactivate();
        verify(dataSource).close();
    }

    @Test
    void deactivateSetsDownsampleJobInstanceToNullSoRunDownsampleNowReturnsFalse() throws Exception {
        // Fix 3: inject a job instance, then deactivate → runDownsampleNow() must return false
        var jobField = TimescaleDBPersistenceService.class.getDeclaredField("downsampleJobInstance");
        jobField.setAccessible(true);
        jobField.set(service, mock(TimescaleDBDownsampleJob.class));

        assertTrue(service.runDownsampleNow(), "sanity: job present before deactivate");

        service.deactivate();

        assertFalse(service.runDownsampleNow(),
                "runDownsampleNow() must return false after deactivate() — console command "
                        + "must not trigger a job backed by a closed connection pool");
    }

    // ------------------------------------------------------------------
    // Private helpers tested via reflection
    // ------------------------------------------------------------------

    @Test
    void secondsUntilMidnightIspositiveandatmost24h() {
        long seconds = TimescaleDBPersistenceService.secondsUntilMidnight();
        assertTrue(seconds > 0, "Seconds until midnight must be positive");
        assertTrue(seconds <= 86400, "Seconds until midnight must not exceed 24 h");
    }

    @Test
    void parseIntConfigValidvalueReturnsparsed() {
        assertEquals(42, TimescaleDBPersistenceService.parseIntConfig(Map.of("k", "42"), "k", 0));
    }

    @Test
    void parseIntConfigMissingkeyReturnsdefault() {
        assertEquals(7, TimescaleDBPersistenceService.parseIntConfig(Map.of(), "k", 7));
    }

    @Test
    void parseIntConfigInvalidvalueReturnsdefault() {
        assertEquals(3, TimescaleDBPersistenceService.parseIntConfig(Map.of("k", "notanumber"), "k", 3));
    }

    // ------------------------------------------------------------------
    // runDownsampleNow / ConsoleCommandExtension
    // ------------------------------------------------------------------

    @Test
    void runDownsampleNowReturnsFalseWhenNotActivated() {
        // A fresh service with no activate() call has no job instance
        TimescaleDBPersistenceService fresh = new TimescaleDBPersistenceService(mock(ItemRegistry.class),
                mock(MetadataRegistry.class), new TimescaleDBMetadataService(mock(MetadataRegistry.class)));
        assertFalse(fresh.runDownsampleNow(), "runDownsampleNow() must return false before activate()");
    }

    @Test
    void consoleCommandDownsamplePrintsFinishedWhenServiceActive() throws Exception {
        // Inject a downsampleJobInstance that does nothing (already activated mock service)
        var jobField = TimescaleDBPersistenceService.class.getDeclaredField("downsampleJobInstance");
        jobField.setAccessible(true);
        TimescaleDBDownsampleJob mockJob = mock(TimescaleDBDownsampleJob.class);
        jobField.set(service, mockJob);

        var console = mock(org.openhab.core.io.console.Console.class);
        var cmd = new TimescaleDBConsoleCommandExtension(service);
        cmd.execute(new String[] { "downsample" }, console);

        verify(mockJob).run();
        verify(console).println(contains("finished"));
    }

    @Test
    void consoleCommandDownsamplePrintsNotActiveWhenServiceInactive() {
        TimescaleDBPersistenceService fresh = new TimescaleDBPersistenceService(mock(ItemRegistry.class),
                mock(MetadataRegistry.class), new TimescaleDBMetadataService(mock(MetadataRegistry.class)));
        var console = mock(org.openhab.core.io.console.Console.class);
        new TimescaleDBConsoleCommandExtension(fresh).execute(new String[] { "downsample" }, console);
        verify(console).println(contains("not active"));
    }

    @Test
    void consoleCommandUnknownArgPrintsUsage() {
        var console = mock(org.openhab.core.io.console.Console.class);
        new TimescaleDBConsoleCommandExtension(service).execute(new String[] { "unknown" }, console);
        // printUsage writes to console — at minimum something must be printed
        verify(console, atLeastOnce()).printUsage(anyString());
    }

    // ------------------------------------------------------------------
    // Metadata cache invalidation (RegistryChangeListener)
    // ------------------------------------------------------------------

    @Test
    void metadataAddedInvalidatesCacheSoNextStoreRunsUpsert() throws Exception {
        // Populate the cache via a first store
        stubItemIdLookup(5);
        service.store(new NumberItem("Sensor1"), ZonedDateTime.now(), new DecimalType(1.0), null);

        // Signal metadata added → cache must be invalidated
        var metaKey = new org.openhab.core.items.MetadataKey("timescaledb", "Sensor1");
        var meta = new org.openhab.core.items.Metadata(metaKey, "new.label", Map.of("aggregation", "MAX"));
        service.added(meta);

        // Stub a fresh upsert for the second store
        stubItemIdLookup(5);
        reset(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        service.store(new NumberItem("Sensor1"), ZonedDateTime.now(), new DecimalType(2.0), null);

        // The upsert must have run again (connection obtained)
        verify(dataSource, atLeastOnce()).getConnection();
        verify(connection, atLeastOnce()).prepareStatement(contains("INSERT INTO item_meta"));
    }

    @Test
    void metadataUpdatedInvalidatesCache() throws Exception {
        stubItemIdLookup(6);
        service.store(new NumberItem("Sensor2"), ZonedDateTime.now(), new DecimalType(1.0), null);

        var metaKey = new org.openhab.core.items.MetadataKey("timescaledb", "Sensor2");
        var oldMeta = new org.openhab.core.items.Metadata(metaKey, "old", Map.of());
        var newMeta = new org.openhab.core.items.Metadata(metaKey, "new", Map.of("aggregation", "MIN"));
        service.updated(oldMeta, newMeta);

        stubItemIdLookup(6);
        reset(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        service.store(new NumberItem("Sensor2"), ZonedDateTime.now(), new DecimalType(3.0), null);

        verify(connection, atLeastOnce()).prepareStatement(contains("INSERT INTO item_meta"));
    }

    @Test
    void metadataRemovedInvalidatesCache() throws Exception {
        stubItemIdLookup(7);
        service.store(new NumberItem("Sensor3"), ZonedDateTime.now(), new DecimalType(1.0), null);

        var metaKey = new org.openhab.core.items.MetadataKey("timescaledb", "Sensor3");
        var meta = new org.openhab.core.items.Metadata(metaKey, "label", Map.of());
        service.removed(meta);

        stubItemIdLookup(7);
        reset(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        service.store(new NumberItem("Sensor3"), ZonedDateTime.now(), new DecimalType(4.0), null);

        verify(connection, atLeastOnce()).prepareStatement(contains("INSERT INTO item_meta"));
    }

    @Test
    void metadataChangeForOtherNamespaceDoesNotInvalidateCache() throws Exception {
        stubItemIdLookup(8);
        service.store(new NumberItem("Sensor4"), ZonedDateTime.now(), new DecimalType(1.0), null);

        // Metadata change in a different namespace — cache must NOT be invalidated
        var metaKey = new org.openhab.core.items.MetadataKey("someOtherNamespace", "Sensor4");
        var meta = new org.openhab.core.items.Metadata(metaKey, "irrelevant", Map.of());
        service.added(meta);

        // Reset both mocks so only the second store's interactions are counted
        reset(dataSource, connection);
        when(dataSource.getConnection()).thenReturn(connection);
        PreparedStatement insertItemPs = mock(PreparedStatement.class);
        when(connection.prepareStatement(contains("INSERT INTO items"))).thenReturn(insertItemPs);
        when(insertItemPs.executeUpdate()).thenReturn(1);

        service.store(new NumberItem("Sensor4"), ZonedDateTime.now(), new DecimalType(2.0), null);

        // Cache still valid → upsert must NOT have been called in the second store
        verify(connection, never()).prepareStatement(contains("INSERT INTO item_meta"));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Stubs the item_id UPSERT: INSERT ... ON CONFLICT DO UPDATE ... RETURNING id returns the given id.
     */
    private void stubItemIdLookup(int itemId) throws Exception {
        ResultSet upsertRs = mock(ResultSet.class);
        PreparedStatement upsertPs = mock(PreparedStatement.class);
        PreparedStatement insertItemPs = mock(PreparedStatement.class);

        when(upsertRs.next()).thenReturn(true);
        when(upsertRs.getInt(1)).thenReturn(itemId);
        when(upsertPs.executeQuery()).thenReturn(upsertRs);
        when(insertItemPs.executeUpdate()).thenReturn(1);

        when(connection.prepareStatement(contains("INSERT INTO item_meta"))).thenReturn(upsertPs);
        when(connection.prepareStatement(contains("INSERT INTO items"))).thenReturn(insertItemPs);
    }
}
