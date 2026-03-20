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
import static org.junit.jupiter.api.Assumptions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Integration test that verifies {@link TimescaleDBSchema#initialize(Connection, String, int, int)}
 * against the external TimescaleDB instance configured via environment variables:
 * HOST, PORT, DBNAME, USER, PASSWORD.
 *
 * Run with: mvn test -Dgroups=external-integration -pl bundles/org.openhab.persistence.timescaledb
 */
@Tag("external-integration")
@NonNullByDefault
@SuppressWarnings("null")
class TimescaleDBExternalIT {

    private static @Nullable HikariDataSource dataSource;

    @BeforeAll
    static void connect() {
        String host = System.getenv("HOST");
        String port = System.getenv("PORT");
        String db = System.getenv("DBNAME");
        String user = System.getenv("USER");
        String pass = System.getenv("PASSWORD");

        assumeTrue(host != null && !host.isBlank(), "HOST env var not set — skipping external integration tests");
        assumeTrue(db != null && !db.isBlank(), "DBNAME env var not set — skipping external integration tests");

        String url = "jdbc:postgresql://" + host + ":" + (port != null ? port : "5432") + "/" + db;

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setMaximumPoolSize(3);
        cfg.setConnectionTimeout(5000);
        dataSource = new HikariDataSource(cfg);
    }

    @AfterAll
    static void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    void schemaInitializeCreateshypertableandindex() throws SQLException {
        HikariDataSource ds = dataSource;
        assumeTrue(ds != null, "DataSource not initialized — skipping");

        // Drop and recreate for a clean test run
        try (Connection conn = ds.getConnection(); var s = conn.createStatement()) {
            s.execute("DROP TABLE IF EXISTS items CASCADE");
            s.execute("DROP TABLE IF EXISTS item_meta CASCADE");
        }

        // This is the method under test — must succeed without any manual DDL
        try (Connection conn = ds.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
        }

        // Verify item_meta table
        try (Connection conn = ds.getConnection();
                var s = conn.createStatement();
                ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM item_meta")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "item_meta should be empty after fresh init");
        }

        // Verify items is a hypertable
        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT hypertable_name, num_chunks FROM timescaledb_information.hypertables WHERE hypertable_name = 'items'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "Hypertable 'items' must exist after initialize()");
            assertEquals("items", rs.getString("hypertable_name"));
        }

        // Verify composite index
        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT indexname FROM pg_indexes WHERE tablename = 'items' AND indexname = 'items_item_id_time_idx'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "Index items_item_id_time_idx must exist after initialize()");
        }
    }

    @Test
    void schemaInitializeIsidempotent() throws SQLException {
        HikariDataSource ds = dataSource;
        assumeTrue(ds != null, "DataSource not initialized — skipping");

        // Calling initialize() twice on an existing schema must not throw
        try (Connection conn = ds.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
        }
        try (Connection conn = ds.getConnection()) {
            assertDoesNotThrow(() -> TimescaleDBSchema.initialize(conn, "7 days", 0, 0),
                    "initialize() must be idempotent — safe to call on existing schema");
        }
    }
}
