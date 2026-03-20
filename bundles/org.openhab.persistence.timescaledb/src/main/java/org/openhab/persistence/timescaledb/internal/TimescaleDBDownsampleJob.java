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
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
public class TimescaleDBDownsampleJob implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(TimescaleDBDownsampleJob.class);

    /**
     * INSERT aggregated rows for one item, skipping buckets that already have a downsampled row.
     * The NOT EXISTS guard makes this statement idempotent at the query level; the trailing
     * ON CONFLICT DO NOTHING (no conflict target) provides a second line of defence for
     * concurrent execution. The target-less form is required because TimescaleDB hypertables
     * do not support column-inference conflict targets. Because the schema uses
     * UNIQUE(time, item_id, downsampled) and we always insert downsampled=TRUE, this never
     * silently drops a bucket that merely collides with a raw row at the bucket boundary.
     * Placeholders: (1) item_id, (2) item_id (NOT EXISTS sub-select).
     * Interval and agg-fn are pre-validated strings from the allowlist/enum.
     */
    private static final String SQL_INSERT_AGGREGATED_TEMPLATE = """
            INSERT INTO items (time, item_id, value, unit, downsampled)
            SELECT bucket, item_id, agg_value, agg_unit, TRUE
            FROM (
                SELECT
                    time_bucket('%s', time) AS bucket,
                    item_id,
                    %s(value)               AS agg_value,
                    MAX(unit)               AS agg_unit
                FROM items
                WHERE item_id = ?
                  AND downsampled = FALSE
                  AND time < NOW() - INTERVAL '%d days'
                GROUP BY time_bucket('%s', time), item_id
            ) AS new_buckets
            WHERE NOT EXISTS (
                SELECT 1
                FROM items existing
                WHERE existing.item_id = new_buckets.item_id
                  AND existing.downsampled = TRUE
                  AND existing.time = new_buckets.bucket
            )
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

    /**
     * @param dataSource The connection pool.
     * @param metadataService The metadata parser.
     */
    public TimescaleDBDownsampleJob(DataSource dataSource, TimescaleDBMetadataService metadataService) {
        this.dataSource = dataSource;
        this.metadataService = metadataService;
    }

    @Override
    public void run() {
        List<String> itemNames = metadataService.getConfiguredItemNames();
        logger.info("Downsampling job started: {} item(s) to process", itemNames.size());

        int success = 0;
        int skipped = 0;
        int failed = 0;

        for (String itemName : itemNames) {
            DownsampleConfig config;
            {
                var configOpt = metadataService.getDownsampleConfig(itemName);
                if (configOpt.isEmpty()) {
                    logger.debug("Item '{}': no valid DownsampleConfig — skipping", itemName);
                    skipped++;
                    continue;
                }
                config = configOpt.get();
            }

            try {
                downsampleItem(itemName, config);
                success++;
            } catch (SQLException e) {
                logger.error("Downsampling failed for item '{}': {}", itemName, e.getMessage(), e);
                failed++;
            }
        }

        logger.info("Downsampling job finished: {} succeeded, {} skipped, {} failed", success, skipped, failed);
    }

    private void downsampleItem(String itemName, DownsampleConfig config) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            var itemIdOpt = TimescaleDBQuery.findItemId(conn, itemName);
            if (itemIdOpt.isEmpty()) {
                logger.debug("Item '{}': not yet known to the database — skipping", itemName);
                return;
            }
            int itemId = itemIdOpt.get();
            conn.setAutoCommit(false);
            try {
                if (config.hasDownsampling()) {
                    AggregationFunction fn = config.function();
                    String interval = config.sqlInterval();
                    if (fn != null && interval != null) {
                        String sqlInsert = SQL_INSERT_AGGREGATED_TEMPLATE.formatted(interval, fn.toSql(),
                                config.retainRawDays(), interval);
                        String sqlDeleteRaw = SQL_DELETE_RAW_TEMPLATE.formatted(config.retainRawDays());
                        int inserted = executeUpdate(conn, sqlInsert, itemId);
                        int deleted = executeUpdate(conn, sqlDeleteRaw, itemId);
                        logger.debug("Item '{}': aggregated {} bucket(s), deleted {} raw row(s)", itemName, inserted,
                                deleted);
                    }
                }

                if (config.retentionDays() > 0) {
                    String sqlRetention = SQL_DELETE_RETENTION_TEMPLATE.formatted(config.retentionDays());
                    int dropped = executeUpdate(conn, sqlRetention, itemId);
                    logger.debug("Item '{}': dropped {} row(s) outside {}d retention window", itemName, dropped,
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
