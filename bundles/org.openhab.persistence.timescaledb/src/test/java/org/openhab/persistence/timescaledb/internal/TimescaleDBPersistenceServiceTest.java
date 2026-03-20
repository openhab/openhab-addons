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
    void getDefaultStrategiesReturnsemptylist() {
        assertTrue(service.getDefaultStrategies().isEmpty());
    }

    // ------------------------------------------------------------------
    // store
    // ------------------------------------------------------------------

    @Test
    void storeNormalstateSendsinsert() throws Exception {
        // item_id cache is empty → getOrCreateItemId will run SELECT then INSERT
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
    void queryItemnotincacheReturnsemptylist() throws Exception {
        var filter = new FilterCriteria();
        filter.setItemName("UnknownItem");

        var result = service.query(filter);

        assertFalse(result.iterator().hasNext());
        // DataSource should not be queried if item_id is not cached
        verify(dataSource, never()).getConnection();
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
    void removeItemnotincacheReturnsfalse() throws Exception {
        var filter = new FilterCriteria();
        filter.setItemName("UnknownItem");

        assertFalse(service.remove(filter));
        verify(dataSource, never()).getConnection();
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
        var realService = new TimescaleDBPersistenceService(mock(ItemRegistry.class),
                new TimescaleDBMetadataService(mock(MetadataRegistry.class)));
        realService.activate(Map.of()); // no 'url' key

        var dsField = TimescaleDBPersistenceService.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        assertTrue(null == dsField.get(realService), "dataSource must be null when url is missing");
        realService.deactivate();
    }

    @Test
    void activateInvalidurlDatasourceremainsnull() throws Exception {
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
                new TimescaleDBMetadataService(mock(MetadataRegistry.class)));
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
                new TimescaleDBMetadataService(mock(MetadataRegistry.class)));
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
