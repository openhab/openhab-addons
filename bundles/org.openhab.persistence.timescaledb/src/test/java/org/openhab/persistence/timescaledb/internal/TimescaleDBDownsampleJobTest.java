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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
 * <li>Retention-only items (blank value + retentionDays) execute only the retention DELETE</li>
 * <li>Items not found in item_meta are skipped gracefully without DML</li>
 * <li>A per-item SQL error does not abort the entire job</li>
 * </ul>
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
@SuppressWarnings("null")
class TimescaleDBDownsampleJobTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private MetadataRegistry registry;
    private TimescaleDBMetadataService metadataService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        registry = mock(MetadataRegistry.class);
        metadataService = new TimescaleDBMetadataService(registry);

        when(dataSource.getConnection()).thenReturn(connection);
        // Default: all prepareStatement calls return the shared mock
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        // Default: SELECT id FROM item_meta returns nothing (item not in DB)
        when(resultSet.next()).thenReturn(false);
        when(preparedStatement.executeUpdate()).thenReturn(1);
    }

    @Test
    void runSingleitemExecutesinsertanddeleteintransaction() throws SQLException {
        stubMetadata("SensorA", "sensor.a",
                Map.of("aggregation", "AVG", "downsampleInterval", "1h", "retainRawDays", "5"));
        stubItemNames(List.of("SensorA"));
        stubItemIdInDb(connection, "SensorA", 42);

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run();

        // Transaction: setAutoCommit(false), commit, setAutoCommit(true)
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection, never()).rollback();

        // DML: setInt(1, 42) called twice (INSERT + DELETE)
        verify(preparedStatement, times(2)).setInt(1, 42);
        verify(preparedStatement, times(2)).executeUpdate();
    }

    @Test
    void runWithretentiondaysExecutesthreestatements() throws SQLException {
        stubMetadata("SensorA", "sensor.a",
                Map.of("aggregation", "MAX", "downsampleInterval", "15m", "retainRawDays", "3", "retentionDays", "90"));
        stubItemNames(List.of("SensorA"));
        stubItemIdInDb(connection, "SensorA", 7);

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run();

        // 3 DML statements: INSERT, DELETE raw, DELETE retention
        verify(preparedStatement, times(3)).setInt(1, 7);
    }

    @Test
    void runItemnotInDbSkipsWithoutDml() throws SQLException {
        stubMetadata("UnknownItem", "sensor.u", Map.of("aggregation", "AVG", "downsampleInterval", "1h"));
        stubItemNames(List.of("UnknownItem"));
        // No stubItemIdInDb → SELECT returns empty (default setUp)

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run();

        // Connection IS opened for the SELECT lookup, but no DML executed
        verify(dataSource, atLeastOnce()).getConnection();
        verify(preparedStatement, never()).executeUpdate();
    }

    @Test
    void runSqlfailureforoneitemDoesnotabortotheritems() throws SQLException {
        stubMetadata("SensorA", "sensor.a", Map.of("aggregation", "AVG", "downsampleInterval", "1h"));
        stubMetadata("SensorB", "sensor.b", Map.of("aggregation", "SUM", "downsampleInterval", "1d"));
        stubItemNames(List.of("SensorA", "SensorB"));

        // First connection: SELECT succeeds (item found), INSERT fails
        Connection failConn = mock(Connection.class);
        PreparedStatement selectPs = mock(PreparedStatement.class);
        ResultSet selectRs = mock(ResultSet.class);
        when(selectRs.next()).thenReturn(true);
        when(selectRs.getInt(1)).thenReturn(1);
        when(selectPs.executeQuery()).thenReturn(selectRs);
        when(failConn.prepareStatement(contains("SELECT id FROM item_meta"))).thenReturn(selectPs);
        when(failConn.prepareStatement(contains("INSERT"))).thenThrow(new SQLException("simulated error"));

        // Second connection: succeeds
        Connection okConn = mock(Connection.class);
        PreparedStatement okSelectPs = mock(PreparedStatement.class);
        ResultSet okSelectRs = mock(ResultSet.class);
        when(okSelectRs.next()).thenReturn(true);
        when(okSelectRs.getInt(1)).thenReturn(2);
        when(okSelectPs.executeQuery()).thenReturn(okSelectRs);
        PreparedStatement okPs = mock(PreparedStatement.class);
        when(okPs.executeUpdate()).thenReturn(1);
        when(okConn.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            return sql.contains("SELECT id FROM item_meta") ? okSelectPs : okPs;
        });

        when(dataSource.getConnection()).thenReturn(failConn).thenReturn(okConn);

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        assertNoException(job); // Must not throw

        // Second item was still processed
        verify(okConn, atLeastOnce()).prepareStatement(contains("INSERT"));
    }

    @Test
    void runNoitemsconfiguredDoesnothing() throws SQLException {
        when(registry.getAll()).thenReturn(List.of());

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run();

        verify(dataSource, never()).getConnection();
    }

    @Test
    void runRetentiononlyitemExecutesonlyretentiondelete() throws SQLException {
        stubRetentionOnlyItem("SensorRO", 30);
        stubItemIdInDb(connection, "SensorRO", 42);

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run();

        // Only 1 DML: the retention DELETE
        verify(preparedStatement, times(1)).setInt(1, 42);
        verify(preparedStatement, times(1)).executeUpdate();
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void runRetentiononlyitemSqlcontainscorrectretentiondays() throws SQLException {
        stubRetentionOnlyItem("SensorRO", 14);

        PreparedStatement selectPs = mock(PreparedStatement.class);
        ResultSet selectRs = mock(ResultSet.class);
        when(selectRs.next()).thenReturn(true);
        when(selectRs.getInt(1)).thenReturn(42);
        when(selectPs.executeQuery()).thenReturn(selectRs);

        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            if (sql.contains("SELECT id FROM item_meta")) {
                return selectPs;
            }
            capturedSql.add(sql);
            return preparedStatement;
        });

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run();

        assertEquals(1, capturedSql.size(), "Retention-only: exactly one DML statement expected");
        assertTrue(capturedSql.get(0).contains("14 days"), "Retention DELETE must reference retentionDays=14");
        assertFalse(capturedSql.get(0).contains("INSERT"), "No INSERT for retention-only item");
        assertFalse(capturedSql.get(0).contains("downsampled = FALSE"), "No raw-delete for retention-only item");
    }

    @Test
    void runRetentiononlyitemWithzeroretentiondaysIsSkipped() throws SQLException {
        // Blank value but retentionDays=0 (or missing) → no DB access at all (invalid config)
        MetadataKey key = new MetadataKey("timescaledb", "SensorRO");
        Metadata meta = new Metadata(key, " ", Map.of());
        when(registry.getAll()).thenReturn(List.of(meta));
        when(registry.get(key)).thenReturn(meta);

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run();

        verify(dataSource, never()).getConnection();
    }

    @Test
    void runRetentiononlyitemNotInDbIsSkipped() throws SQLException {
        stubRetentionOnlyItem("SensorRO", 30);
        // No stubItemIdInDb → SELECT returns empty (default setUp)

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run();

        // Connection opened for SELECT but no DML
        verify(preparedStatement, never()).executeUpdate();
    }

    @Test
    void runInsertsqlcontainscorrectintervalandfunction() throws SQLException {
        stubMetadata("Sensor", "sensor.s",
                Map.of("aggregation", "MIN", "downsampleInterval", "6h", "retainRawDays", "3"));
        stubItemNames(List.of("Sensor"));

        PreparedStatement selectPs = mock(PreparedStatement.class);
        ResultSet selectRs = mock(ResultSet.class);
        when(selectRs.next()).thenReturn(true);
        when(selectRs.getInt(1)).thenReturn(99);
        when(selectPs.executeQuery()).thenReturn(selectRs);

        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            if (sql.contains("SELECT id FROM item_meta")) {
                return selectPs;
            }
            capturedSql.add(sql);
            return preparedStatement;
        });

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run();

        // capturedSql only contains DML (SELECT is handled by selectPs, not the answer)
        String insertSql = capturedSql.get(0);
        assertTrue(insertSql.contains("6 hours"), "INSERT should contain interval '6 hours'");
        assertTrue(insertSql.contains("MIN(value)"), "INSERT should contain aggregation 'MIN(value)'");
        assertTrue(insertSql.contains("3 days"), "INSERT should reference retainRawDays=3");
    }

    @Test
    void runDeletesqlreferencesretainrawdays() throws SQLException {
        stubMetadata("Sensor", "sensor.s",
                Map.of("aggregation", "AVG", "downsampleInterval", "1h", "retainRawDays", "7"));
        stubItemNames(List.of("Sensor"));

        PreparedStatement selectPs = mock(PreparedStatement.class);
        ResultSet selectRs = mock(ResultSet.class);
        when(selectRs.next()).thenReturn(true);
        when(selectRs.getInt(1)).thenReturn(5);
        when(selectPs.executeQuery()).thenReturn(selectRs);

        var capturedSql = new java.util.ArrayList<String>();
        when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            if (sql.contains("SELECT id FROM item_meta")) {
                return selectPs;
            }
            capturedSql.add(sql);
            return preparedStatement;
        });

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run();

        // capturedSql only contains DML (SELECT is handled by selectPs, not the answer)
        String deleteSql = capturedSql.get(1);
        assertTrue(deleteSql.contains("downsampled = FALSE"), "DELETE should only remove raw rows");
        assertTrue(deleteSql.contains("7 days"), "DELETE should reference retainRawDays=7");
    }

    @Test
    void runRollbackonsqlerror() throws SQLException {
        stubMetadata("SensorA", "sensor.a", Map.of("aggregation", "AVG", "downsampleInterval", "1h"));
        stubItemNames(List.of("SensorA"));
        stubItemIdInDb(connection, "SensorA", 1);

        // Make the INSERT fail
        when(connection.prepareStatement(contains("INSERT"))).thenThrow(new SQLException("insert failed"));

        var job = new TimescaleDBDownsampleJob(dataSource, metadataService);
        job.run(); // should not throw

        verify(connection).rollback();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Stubs the SELECT id FROM item_meta query to return the given id for any item name.
     */
    private static void stubItemIdInDb(Connection conn, String itemName, int id) throws SQLException {
        PreparedStatement selectPs = mock(PreparedStatement.class);
        ResultSet selectRs = mock(ResultSet.class);
        when(selectRs.next()).thenReturn(true);
        when(selectRs.getInt(1)).thenReturn(id);
        when(selectPs.executeQuery()).thenReturn(selectRs);
        when(conn.prepareStatement(contains("SELECT id FROM item_meta"))).thenReturn(selectPs);
    }

    private void stubMetadata(String itemName, String value, Map<String, Object> config) {
        MetadataKey key = new MetadataKey("timescaledb", itemName);
        Metadata meta = new Metadata(key, value, config);
        when(registry.get(key)).thenReturn(meta);
    }

    private void stubItemNames(List<String> names) {
        var metaList = names.stream().map(n -> new Metadata(new MetadataKey("timescaledb", n),
                "sensor." + n.toLowerCase(), Map.of("aggregation", "AVG", "downsampleInterval", "1h"))).toList();
        when(registry.getAll()).thenAnswer(inv -> metaList);
        for (String name : names) {
            MetadataKey key = new MetadataKey("timescaledb", name);
            if (registry.get(key) == null) {
                when(registry.get(key)).thenReturn(new Metadata(key, "sensor." + name.toLowerCase(),
                        Map.of("aggregation", "AVG", "downsampleInterval", "1h")));
            }
        }
    }

    /**
     * Stubs registry.getAll() and registry.get() for a retention-only item
     * (blank metadata value + retentionDays configured).
     */
    private void stubRetentionOnlyItem(String itemName, int retentionDays) {
        MetadataKey key = new MetadataKey("timescaledb", itemName);
        Metadata meta = new Metadata(key, " ", Map.of("retentionDays", String.valueOf(retentionDays)));
        when(registry.getAll()).thenReturn(List.of(meta));
        when(registry.get(key)).thenReturn(meta);
    }

    private static void assertNoException(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Expected no exception but got: " + e.getMessage());
        }
    }
}
