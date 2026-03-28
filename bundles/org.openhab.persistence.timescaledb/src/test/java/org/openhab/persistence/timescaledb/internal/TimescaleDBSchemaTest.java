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
import java.sql.Statement;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TimescaleDBSchema} using mocked JDBC connections.
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
class TimescaleDBSchemaTest {

    private Connection connection;
    private Statement statement;
    private PreparedStatement hypertablePs;
    private ResultSet extensionResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        hypertablePs = mock(PreparedStatement.class);
        extensionResultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(anyString())).thenReturn(hypertablePs);
        when(statement.executeQuery(contains("pg_extension"))).thenReturn(extensionResultSet);
        when(extensionResultSet.next()).thenReturn(true); // extension is present by default
    }

    @Test
    void initializeExecutesallrequiredddl() throws SQLException {
        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        // Must check TimescaleDB extension
        verify(statement).executeQuery(contains("pg_extension"));

        // Must create item_meta with metadata column
        verify(statement).execute(contains("CREATE TABLE IF NOT EXISTS item_meta"));

        // Must run single migration adding both value and metadata columns
        verify(statement).execute(argThat(s -> s.contains("ADD COLUMN IF NOT EXISTS value")
                && s.contains("ADD COLUMN IF NOT EXISTS metadata JSONB")));

        // Must create items table
        verify(statement).execute(contains("CREATE TABLE IF NOT EXISTS items"));

        // Must create hypertable via PreparedStatement (not raw Statement — avoids SQL injection on chunkInterval)
        verify(connection).prepareStatement(contains("create_hypertable"));
        verify(hypertablePs).execute();

        // Must create index
        verify(statement).execute(contains("CREATE INDEX IF NOT EXISTS items_item_id_time_idx"));
    }

    @Test
    void initializeHypertablecontainsconfiguredchunkinterval() throws SQLException {
        TimescaleDBSchema.initialize(connection, "14 days", 0, 0);

        // The chunk interval must be passed as a PreparedStatement parameter, not interpolated into SQL
        verify(hypertablePs).setString(1, "14 days");
        verify(hypertablePs).execute();
    }

    @Test
    void initializeHypertableUsespreparedstatementNotRawSql() throws SQLException {
        // The SQL_CREATE_HYPERTABLE constant must use ?::INTERVAL — verify no string-formatted interval leaks
        // into a raw Statement.execute() call
        var capturedRawSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedRawSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "3 days", 0, 0);

        // No raw Statement should contain "create_hypertable" or the interval value
        assertFalse(capturedRawSql.stream().anyMatch(s -> s.contains("create_hypertable")),
                "create_hypertable must not be executed via raw Statement (SQL injection risk)");
        assertFalse(capturedRawSql.stream().anyMatch(s -> s.contains("3 days")),
                "chunkInterval value must not appear in any raw Statement SQL");
    }

    @Test
    void initializeWithcompressionSendscompressionddl() throws SQLException {
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
        assertTrue(capturedSql.stream().anyMatch(s -> s.contains("add_compression_policy") && s.contains("30")),
                "Compression policy should reference 30 days");
    }

    @Test
    void initializeWithoutcompressionNocompressionddl() throws SQLException {
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
    void initializeWithretentionpolicySendsretentionddl() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 365);

        assertTrue(capturedSql.stream().anyMatch(s -> s.contains("add_retention_policy")),
                "Should add retention policy");
        assertTrue(capturedSql.stream().anyMatch(s -> s.contains("add_retention_policy") && s.contains("365")),
                "Retention policy should reference 365 days");
    }

    @Test
    void initializeWithoutretentionpolicyNoretentionddl() throws SQLException {
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
    void initializeMissingtimescaledbextensionThrowssqlexception() throws SQLException {
        when(extensionResultSet.next()).thenReturn(false); // extension NOT present

        SQLException ex = assertThrows(SQLException.class,
                () -> TimescaleDBSchema.initialize(connection, "7 days", 0, 0));

        String msg = ex.getMessage();
        assertNotNull(msg, "Exception must have a message");
        assertTrue(msg.contains("TimescaleDB extension"), "Error message should mention TimescaleDB extension");
    }

    @Test
    void createTableDdlContainsdownsampledInUniqueconstraint() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        boolean hasConstraint = capturedSql.stream().anyMatch(
                s -> s.contains("UNIQUE") && s.contains("time") && s.contains("item_id") && s.contains("downsampled"));
        assertTrue(hasConstraint,
                "CREATE TABLE must include UNIQUE(time, item_id, downsampled) — without the downsampled "
                        + "column a downsampled row at the exact bucket-boundary timestamp would conflict "
                        + "with the raw row at the same time");
    }

    @Test
    void migrationDdlDropsOldconstraintAndAddsNewone() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        // The migration DO-block is the only DDL statement that contains DROP CONSTRAINT
        java.util.Optional<String> migrationOpt = capturedSql.stream().filter(s -> s.contains("DROP CONSTRAINT"))
                .findFirst();
        assertTrue(migrationOpt.isPresent(), "Migration DO-block not found in executed DDL statements");
        String migrationSql = migrationOpt.get();

        assertTrue(migrationSql.contains("items_time_item_id_ukey"),
                "Migration must drop the legacy items_time_item_id_ukey constraint");
        assertTrue(migrationSql.contains("items_time_item_id_downsampled_ukey"),
                "Migration must add the new items_time_item_id_downsampled_ukey constraint");
        assertTrue(migrationSql.indexOf("DROP") < migrationSql.indexOf("ADD"),
                "Migration must DROP the old constraint before ADD-ing the new one");
    }

    // ------------------------------------------------------------------
    // item_meta schema — DDL and migration
    // ------------------------------------------------------------------

    @Test
    void createTableItemmetaContainsValueTextColumn() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        java.util.Optional<String> createItemMeta = capturedSql.stream()
                .filter(s -> s.contains("CREATE TABLE IF NOT EXISTS item_meta")).findFirst();
        assertTrue(createItemMeta.isPresent(), "CREATE TABLE item_meta statement not found");
        String ddl = createItemMeta.get();
        assertTrue(ddl.contains("value") && ddl.contains("TEXT"),
                "CREATE TABLE item_meta must define a 'value TEXT' column");
    }

    @Test
    void createTableItemmetaContainsMetadataJsonbColumn() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        java.util.Optional<String> createItemMeta = capturedSql.stream()
                .filter(s -> s.contains("CREATE TABLE IF NOT EXISTS item_meta")).findFirst();
        assertTrue(createItemMeta.isPresent(), "CREATE TABLE item_meta statement not found");
        String ddl = createItemMeta.get();
        assertTrue(ddl.contains("metadata") && ddl.contains("JSONB"),
                "CREATE TABLE item_meta must define a 'metadata JSONB' column");
    }

    @Test
    void migrationDdlAddsValueAndMetadataColumnsInSingleStatement() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        // Both columns must be added in a single ALTER TABLE statement (one lock acquisition)
        java.util.Optional<String> migrationOpt = capturedSql.stream()
                .filter(s -> s.contains("ADD COLUMN IF NOT EXISTS value")
                        && s.contains("ADD COLUMN IF NOT EXISTS metadata JSONB"))
                .findFirst();
        assertTrue(migrationOpt.isPresent(),
                "value and metadata columns must be added in a single ALTER TABLE statement");
    }

    @Test
    void migrationDdlUsesLockTimeoutToPreventBlockingServiceStart() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        long migrationStatementsWithoutLockTimeout = capturedSql.stream()
                .filter(s -> s.contains("ALTER TABLE item_meta") && !s.contains("lock_timeout")).count();
        assertEquals(0, migrationStatementsWithoutLockTimeout,
                "All item_meta DDL migrations must use lock_timeout to prevent blocking service startup indefinitely");
    }

    @Test
    void migrationDdlHasExceptionHandlerForLockTimeout() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        long migrationsWithoutExceptionHandler = capturedSql.stream()
                .filter(s -> s.contains("item_meta") && s.contains("ALTER TABLE") && !s.contains("EXCEPTION")).count();
        assertEquals(0, migrationsWithoutExceptionHandler,
                "All item_meta migrations must handle lock_not_available gracefully instead of blocking");
    }

    @Test
    void migrationRunsAfterCreateTableItemMeta() throws SQLException {
        var capturedSql = new java.util.ArrayList<String>();
        doAnswer(inv -> {
            capturedSql.add(inv.getArgument(0));
            return false;
        }).when(statement).execute(anyString());

        TimescaleDBSchema.initialize(connection, "7 days", 0, 0);

        int createTableIdx = -1;
        int migrationIdx = -1;
        for (int i = 0; i < capturedSql.size(); i++) {
            String s = capturedSql.get(i);
            if (s.contains("CREATE TABLE IF NOT EXISTS item_meta") && createTableIdx < 0) {
                createTableIdx = i;
            }
            if (s.contains("ADD COLUMN IF NOT EXISTS value") && s.contains("ADD COLUMN IF NOT EXISTS metadata JSONB")
                    && migrationIdx < 0) {
                migrationIdx = i;
            }
        }

        assertTrue(createTableIdx >= 0, "CREATE TABLE item_meta must be executed");
        assertTrue(migrationIdx >= 0, "Column migration must be executed");
        assertTrue(createTableIdx < migrationIdx,
                "Migration must run after CREATE TABLE item_meta so that the ALTER runs on an existing table");
    }
}
