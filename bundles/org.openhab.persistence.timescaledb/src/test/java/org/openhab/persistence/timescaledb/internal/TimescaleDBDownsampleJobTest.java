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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;

/**
 * Unit tests for {@link TimescaleDBDownsampleJob} using mocked DB connections.
 *
 * <p>
 * Verifies:
 * <ul>
 * <li>Correct SQL is sent for all interval/function combinations</li>
 * <li>Both INSERT and DELETE statements are executed within a transaction</li>
 * <li>Retention DELETE is only sent when retentionDays &gt; 0</li>
 * <li>Items not in the item_id cache are skipped gracefully</li>
 * <li>A per-item SQL error does not abort the entire job</li>
 * </ul>
 */
class TimescaleDBDownsampleJobTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private MetadataRegistry registry;
    private TimescaleDBMetadataService metadataService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        registry = mock(MetadataRegistry.class);
        metadataService = new TimescaleDBMetadataService(registry);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
    }

    @Test
    void run_singleItem_executesInsertAndDeleteInTransaction() throws SQLException {
        stubMetadata("SensorA", "AVG", Map.of("downsampleInterval", "1h", "retainRawDays", "5"));
        stubItemNames(List.of("SensorA"));

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService, name -> Optional.of(42));
        job.run();

        // Transaction: setAutoCommit(false), commit, setAutoCommit(true)
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection, never()).rollback();

        // Exactly 2 PreparedStatements: INSERT aggregated + DELETE raw
        verify(connection, times(2)).prepareStatement(anyString());
        verify(preparedStatement, times(2)).setInt(1, 42);
        verify(preparedStatement, times(2)).executeUpdate();
    }

    @Test
    void run_withRetentionDays_executesThreeStatements() throws SQLException {
        stubMetadata("SensorA", "MAX",
                Map.of("downsampleInterval", "15m", "retainRawDays", "3", "retentionDays", "90"));
        stubItemNames(List.of("SensorA"));

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService, name -> Optional.of(7));
        job.run();

        // 3 statements: INSERT, DELETE raw, DELETE retention
        verify(connection, times(3)).prepareStatement(anyString());
        verify(preparedStatement, times(3)).setInt(1, 7);
    }

    @Test
    void run_itemNotInCache_skipsWithoutDatabaseCall() throws SQLException {
        stubMetadata("UnknownItem", "AVG", Map.of("downsampleInterval", "1h"));
        stubItemNames(List.of("UnknownItem"));

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService, name -> Optional.empty()); // not in cache
        job.run();

        verify(dataSource, never()).getConnection();
    }

    @Test
    void run_sqlFailureForOneItem_doesNotAbortOtherItems() throws SQLException {
        stubMetadata("SensorA", "AVG", Map.of("downsampleInterval", "1h"));
        stubMetadata("SensorB", "SUM", Map.of("downsampleInterval", "1d"));
        stubItemNames(List.of("SensorA", "SensorB"));

        // First connection throws, second succeeds
        Connection failConn = mock(Connection.class);
        Connection okConn = mock(Connection.class);
        PreparedStatement okPs = mock(PreparedStatement.class);
        when(okPs.executeUpdate()).thenReturn(1);
        when(okConn.prepareStatement(anyString())).thenReturn(okPs);

        when(dataSource.getConnection()).thenReturn(failConn).thenReturn(okConn);
        when(failConn.prepareStatement(anyString())).thenThrow(new SQLException("simulated error"));

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService, name -> Optional.of(1));
        // Must not throw — errors are logged per-item
        assertNoException(job);

        // Second item was still processed
        verify(okConn, atLeastOnce()).prepareStatement(anyString());
    }

    @Test
    void run_noItemsConfigured_doesNothing() throws SQLException {
        when(registry.getAll()).thenReturn(List.of());

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService, name -> Optional.of(1));
        job.run();

        verify(dataSource, never()).getConnection();
    }

    @Test
    void run_insertSqlContainsCorrectIntervalAndFunction() throws SQLException {
        stubMetadata("Sensor", "MIN", Map.of("downsampleInterval", "6h", "retainRawDays", "3"));
        stubItemNames(List.of("Sensor"));

        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return preparedStatement;
        });

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService, name -> Optional.of(99));
        job.run();

        // First captured SQL is the INSERT
        String insertSql = capturedSql.get(0);
        assertTrue(insertSql.contains("6 hours"), "INSERT should contain interval '6 hours'");
        assertTrue(insertSql.contains("MIN(value)"), "INSERT should contain aggregation 'MIN(value)'");
        assertTrue(insertSql.contains("3 days"), "INSERT should reference retainRawDays=3");
    }

    @Test
    void run_deleteSqlReferencesRetainRawDays() throws SQLException {
        stubMetadata("Sensor", "AVG", Map.of("downsampleInterval", "1h", "retainRawDays", "7"));
        stubItemNames(List.of("Sensor"));

        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return preparedStatement;
        });

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService, name -> Optional.of(5));
        job.run();

        String deleteSql = capturedSql.get(1);
        assertTrue(deleteSql.contains("downsampled = FALSE"), "DELETE should only remove raw rows");
        assertTrue(deleteSql.contains("7 days"), "DELETE should reference retainRawDays=7");
    }

    @Test
    void run_rollbackOnSqlError() throws SQLException {
        stubMetadata("SensorA", "AVG", Map.of("downsampleInterval", "1h"));
        stubItemNames(List.of("SensorA"));

        // Make the INSERT fail
        when(connection.prepareStatement(contains("INSERT"))).thenThrow(new SQLException("insert failed"));

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService, name -> Optional.of(1));
        job.run(); // should not throw

        verify(connection).rollback();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void stubMetadata(String itemName, String value, Map<String, Object> config) {
        MetadataKey key = new MetadataKey("timescaledb", itemName);
        Metadata meta = new Metadata(key, value, config);
        when(registry.get(key)).thenReturn(meta);
    }

    private void stubItemNames(List<String> names) {
        var metaList = names.stream()
                .map(n -> new Metadata(new MetadataKey("timescaledb", n), "AVG", Map.of("downsampleInterval", "1h")))
                .toList();
        // Return our specific metadata items for getAll()
        when(registry.getAll()).thenReturn((java.util.Collection) metaList);
        // Also stub the individual get() calls if not already done
        for (String name : names) {
            MetadataKey key = new MetadataKey("timescaledb", name);
            if (registry.get(key) == null) {
                when(registry.get(key)).thenReturn(new Metadata(key, "AVG", Map.of("downsampleInterval", "1h")));
            }
        }
    }

    private static void assertNoException(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Expected no exception but got: " + e.getMessage());
        }
    }
}
