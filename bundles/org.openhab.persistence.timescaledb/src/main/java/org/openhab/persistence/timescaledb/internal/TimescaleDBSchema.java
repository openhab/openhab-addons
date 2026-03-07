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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates and migrates the TimescaleDB schema on startup.
 *
 * <p>
 * Schema overview:
 * <ul>
 * <li>{@code item_meta} — name-to-ID lookup table for items</li>
 * <li>{@code items} — single hypertable for all item states</li>
 * </ul>
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
public class TimescaleDBSchema {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimescaleDBSchema.class);

    private static final String SQL_CREATE_ITEM_META = """
            CREATE TABLE IF NOT EXISTS item_meta (
                id         SERIAL PRIMARY KEY,
                name       TEXT NOT NULL UNIQUE,
                label      TEXT,
                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
            )
            """;

    private static final String SQL_CREATE_ITEMS = """
            CREATE TABLE IF NOT EXISTS items (
                time        TIMESTAMPTZ      NOT NULL,
                item_id     INTEGER          NOT NULL REFERENCES item_meta(id),
                value       DOUBLE PRECISION,
                string      TEXT,
                unit        TEXT,
                downsampled BOOLEAN          NOT NULL DEFAULT FALSE
            )
            """;

    private static final String SQL_CREATE_HYPERTABLE = "SELECT create_hypertable('items', 'time', if_not_exists => TRUE, chunk_time_interval => INTERVAL '%s')";

    private static final String SQL_CREATE_INDEX = "CREATE INDEX IF NOT EXISTS items_item_id_time_idx ON items (item_id, time DESC)";

    private static final String SQL_CHECK_TIMESCALEDB = "SELECT extname FROM pg_extension WHERE extname = 'timescaledb'";

    private static final String SQL_ENABLE_COMPRESSION = """
            ALTER TABLE items SET (
                timescaledb.compress,
                timescaledb.compress_segmentby = 'item_id',
                timescaledb.compress_orderby   = 'time DESC'
            )
            """;

    private static final String SQL_ADD_COMPRESSION_POLICY = "SELECT add_compression_policy('items', INTERVAL '%d days', if_not_exists => TRUE)";

    private static final String SQL_ADD_RETENTION_POLICY = "SELECT add_retention_policy('items', INTERVAL '%d days', if_not_exists => TRUE)";

    private TimescaleDBSchema() {
        // utility class
    }

    /**
     * Initializes the full schema. Throws {@link SQLException} if the TimescaleDB extension
     * is not installed or if any DDL statement fails.
     *
     * @param connection An open JDBC connection.
     * @param chunkInterval Chunk interval for the hypertable, e.g. {@code "7 days"}.
     * @param compressionAfterDays Compress chunks older than N days. 0 = disabled.
     * @param retentionDays Drop data older than N days via retention policy. 0 = disabled.
     * @throws SQLException on any database error, including missing TimescaleDB extension.
     */
    public static void initialize(Connection connection, String chunkInterval, int compressionAfterDays,
            int retentionDays) throws SQLException {
        checkTimescaleDBExtension(connection);
        createTables(connection, chunkInterval);
        if (compressionAfterDays > 0) {
            setupCompression(connection, compressionAfterDays);
        }
        if (retentionDays > 0) {
            setupRetentionPolicy(connection, retentionDays);
        }
        LOGGER.info("TimescaleDB schema initialized (chunkInterval={}, compression={}d, retention={}d)", chunkInterval,
                compressionAfterDays, retentionDays);
    }

    private static void checkTimescaleDBExtension(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(SQL_CHECK_TIMESCALEDB)) {
            if (!rs.next()) {
                throw new SQLException("TimescaleDB extension is not installed in the target database. "
                        + "Run 'CREATE EXTENSION IF NOT EXISTS timescaledb;' first.");
            }
            LOGGER.debug("TimescaleDB extension found");
        }
    }

    private static void createTables(Connection connection, String chunkInterval) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(SQL_CREATE_ITEM_META);
            LOGGER.debug("Table item_meta ready");

            stmt.execute(SQL_CREATE_ITEMS);
            LOGGER.debug("Table items ready");

            stmt.execute(SQL_CREATE_HYPERTABLE.formatted(chunkInterval));
            LOGGER.debug("Hypertable configured with chunk interval '{}'", chunkInterval);

            stmt.execute(SQL_CREATE_INDEX);
            LOGGER.debug("Index on (item_id, time DESC) ready");
        }
    }

    private static void setupCompression(Connection connection, int compressionAfterDays) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(SQL_ENABLE_COMPRESSION);
            stmt.execute(SQL_ADD_COMPRESSION_POLICY.formatted(compressionAfterDays));
            LOGGER.info("Compression policy set: compress after {} days", compressionAfterDays);
        }
    }

    private static void setupRetentionPolicy(Connection connection, int retentionDays) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(SQL_ADD_RETENTION_POLICY.formatted(retentionDays));
            LOGGER.info("Retention policy set: drop data older than {} days", retentionDays);
        }
    }
}
