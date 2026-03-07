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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TimescaleDBSchema} using mocked JDBC connections.
 */
class TimescaleDBSchemaTest {

    private Connection connection;
    private Statement statement;
    private ResultSet extensionResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        extensionResultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(contains("pg_extension"))).thenReturn(extensionResultSet);
        when(extensionResultSet.next()).thenReturn(true); // extension is present by default
    }

    @Test
    void initialize_executesAllRequiredDdl() throws SQLException {
        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        // Must check TimescaleDB extension
        verify(statement).executeQuery(contains("pg_extension"));

        // Must create item_meta
        verify(statement).execute(contains("CREATE TABLE IF NOT EXISTS item_meta"));

        // Must create items table
        verify(statement).execute(contains("CREATE TABLE IF NOT EXISTS items"));

        // Must create hypertable
        verify(statement).execute(contains("create_hypertable"));

        // Must create index
        verify(statement).execute(contains("CREATE INDEX IF NOT EXISTS items_item_id_time_idx"));
    }

    @Test
    void initialize_hypertableContainsConfiguredChunkInterval() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "14 days", 0, 0);

        String hypertableSql = capturedSql.stream().filter(s -> s.contains("create_hypertable")).findFirst().orElse("");
        assertTrue(hypertableSql.contains("14 days"), "Hypertable should use configured chunk interval");
    }

    @Test
    void initialize_withCompression_sendsCompressionDdl() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 30, 0);

        boolean hasCompress = capturedSql.stream().anyMatch(s -> s.contains("timescaledb.compress"));
        boolean hasPolicy = capturedSql.stream().anyMatch(s -> s.contains("add_compression_policy"));
        assertTrue(hasCompress, "Should enable compression on table");
        assertTrue(hasPolicy, "Should add compression policy");

        // Policy should reference the configured number of days
        String policyCall = capturedSql.stream().filter(s -> s.contains("add_compression_policy")).findFirst()
                .orElse("");
        assertTrue(policyCall.contains("30"), "Compression policy should reference 30 days");
    }

    @Test
    void initialize_withoutCompression_noCompressionDdl() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        boolean hasCompress = capturedSql.stream().anyMatch(s -> s.contains("add_compression_policy"));
        assertFalse(hasCompress, "Should not add compression policy when compressionAfterDays=0");
    }

    @Test
    void initialize_withRetentionPolicy_sendsRetentionDdl() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 365);

        String retentionSql = capturedSql.stream().filter(s -> s.contains("add_retention_policy")).findFirst()
                .orElse("");
        assertFalse(retentionSql.isBlank(), "Should add retention policy");
        assertTrue(retentionSql.contains("365"), "Retention policy should reference 365 days");
    }

    @Test
    void initialize_withoutRetentionPolicy_noRetentionDdl() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        boolean hasRetention = capturedSql.stream().anyMatch(s -> s.contains("add_retention_policy"));
        assertFalse(hasRetention, "Should not add retention policy when retentionDays=0");
    }

    @Test
    void initialize_missingTimescaleDBExtension_throwsSQLException() throws SQLException {
        when(extensionResultSet.next()).thenReturn(false); // extension NOT present

        SQLException ex = assertThrows(SQLException.class,
                () -> TimescaleDBSchema.initialize(connection, "7 days", 0, 0));

        assertTrue(ex.getMessage().contains("TimescaleDB extension"),
                "Error message should mention TimescaleDB extension");
    }
}
