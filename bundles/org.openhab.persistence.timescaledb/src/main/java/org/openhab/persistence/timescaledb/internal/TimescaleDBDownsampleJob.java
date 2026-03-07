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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Daily scheduled job that performs in-place downsampling for all items
 * configured with {@code timescaledb} metadata.
 *
 * <p>
 * For each eligible item, the job runs atomically in a single transaction:
 * <ol>
 * <li>INSERT aggregated rows (time_bucket + agg_fn) with {@code downsampled=TRUE}</li>
 * <li>DELETE original raw rows that have been aggregated</li>
 * <li>If {@code retentionDays > 0}: DELETE all rows older than the retention window</li>
 * </ol>
 *
 * <p>
 * <strong>Security note:</strong> The SQL interval and aggregation function are
 * formatted into the query string but are validated against an allowlist
 * ({@link DownsampleConfig#INTERVAL_MAP} and {@link AggregationFunction} enum)
 * before use. The {@code item_id} is always a JDBC bind parameter.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class TimescaleDBDownsampleJob implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimescaleDBDownsampleJob.class);

    /**
     * INSERT aggregated rows for one item.
     * Placeholders: (1) item_id, (2) item_id (GROUP BY).
     * Interval and agg-fn are pre-validated strings from the allowlist/enum.
     */
    private static final String SQL_INSERT_AGGREGATED_TEMPLATE = """
            INSERT INTO items (time, item_id, value, unit, downsampled)
            SELECT
                time_bucket('%s', time) AS time,
                item_id,
                %s(value)              AS value,
                MAX(unit)              AS unit,
                TRUE                   AS downsampled
            FROM items
            WHERE item_id = ?
              AND downsampled = FALSE
              AND time < NOW() - INTERVAL '%d days'
            GROUP BY time_bucket('%s', time), item_id
            ON CONFLICT DO NOTHING
            """;

    /**
     * DELETE raw rows that were just aggregated.
     * Placeholder: (1) item_id.
     */
    private static final String SQL_DELETE_RAW_TEMPLATE = """
            DELETE FROM items
            WHERE item_id = ?
              AND downsampled = FALSE
              AND time < NOW() - INTERVAL '%d days'
            """;

    /**
     * DELETE all rows (raw + downsampled) outside the per-item retention window.
     * Placeholder: (1) item_id.
     */
    private static final String SQL_DELETE_RETENTION_TEMPLATE = """
            DELETE FROM items
            WHERE item_id = ?
              AND time < NOW() - INTERVAL '%d days'
            """;

    private final DataSource dataSource;
    private final TimescaleDBMetadataService metadataService;
    private final java.util.function.Function<String, Optional<Integer>> itemIdLookup;

    /**
     * @param dataSource The connection pool.
     * @param metadataService The metadata parser.
     * @param itemIdLookup Resolves an item name to its cached item_id (empty if not yet known to the DB).
     */
    public TimescaleDBDownsampleJob(DataSource dataSource, TimescaleDBMetadataService metadataService,
            java.util.function.Function<String, Optional<Integer>> itemIdLookup) {
        this.dataSource = dataSource;
        this.metadataService = metadataService;
        this.itemIdLookup = itemIdLookup;
    }

    @Override
    public void run() {
        List<String> itemNames = metadataService.getItemNamesWithDownsampling();
        LOGGER.info("Downsampling job started: {} item(s) to process", itemNames.size());

        int success = 0;
        int skipped = 0;
        int failed = 0;

        for (String itemName : itemNames) {
            Optional<DownsampleConfig> configOpt = metadataService.getDownsampleConfig(itemName);
            if (configOpt.isEmpty()) {
                LOGGER.debug("Item '{}': no valid DownsampleConfig — skipping", itemName);
                skipped++;
                continue;
            }
            Optional<Integer> itemIdOpt = itemIdLookup.apply(itemName);
            if (itemIdOpt.isEmpty()) {
                LOGGER.debug("Item '{}': not yet known to the database — skipping", itemName);
                skipped++;
                continue;
            }

            try {
                downsampleItem(itemName, itemIdOpt.get(), configOpt.get());
                success++;
            } catch (SQLException e) {
                LOGGER.error("Downsampling failed for item '{}': {}", itemName, e.getMessage(), e);
                failed++;
            }
        }

        LOGGER.info("Downsampling job finished: {} succeeded, {} skipped, {} failed", success, skipped, failed);
    }

    private void downsampleItem(String itemName, int itemId, DownsampleConfig config) throws SQLException {
        String sqlInsert = SQL_INSERT_AGGREGATED_TEMPLATE.formatted(config.sqlInterval(), config.function().toSql(),
                config.retainRawDays(), config.sqlInterval());
        String sqlDeleteRaw = SQL_DELETE_RAW_TEMPLATE.formatted(config.retainRawDays());

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int inserted = executeUpdate(conn, sqlInsert, itemId);
                int deleted = executeUpdate(conn, sqlDeleteRaw, itemId);
                LOGGER.debug("Item '{}': aggregated {} bucket(s), deleted {} raw row(s)", itemName, inserted, deleted);

                if (config.retentionDays() > 0) {
                    String sqlRetention = SQL_DELETE_RETENTION_TEMPLATE.formatted(config.retentionDays());
                    int dropped = executeUpdate(conn, sqlRetention, itemId);
                    LOGGER.debug("Item '{}': dropped {} row(s) outside {}d retention window", itemName, dropped,
                            config.retentionDays());
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static int executeUpdate(Connection conn, String sql, int itemId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate();
        }
    }
}
