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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQL query builder and executor for all TimescaleDB persistence operations.
 *
 * <p>
 * All user-controlled values (item name, timestamps, state values) are passed as
 * JDBC {@link PreparedStatement} parameters to prevent SQL injection.
 * The only dynamically formatted strings are validated enum/allowlist values
 * (SQL operator, ORDER BY direction).
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
public class TimescaleDBQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimescaleDBQuery.class);

    // --- INSERT ---
    // ON CONFLICT DO NOTHING (no conflict target) silently discards duplicate writes.
    // TimescaleDB hypertables do not support column-inference conflict targets, so the target-less
    // form must be used. The UNIQUE(time, item_id, downsampled) constraint on the table ensures
    // that a raw row (downsampled=FALSE) and a downsampled row (downsampled=TRUE) at the same
    // timestamp can coexist and only true duplicates are dropped.
    private static final String SQL_INSERT = "INSERT INTO items (time, item_id, value, string, unit) VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";

    // --- item_meta lookup / insert ---
    private static final String SQL_SELECT_ITEM_ID = "SELECT id FROM item_meta WHERE name = ?";

    // value = user-defined string from metadata.getValue() (stored as TEXT)
    // metadata = full config map serialized as JSON (stored as JSONB via ::jsonb cast)
    private static final String SQL_UPSERT_ITEM_META = "INSERT INTO item_meta (name, label, value, metadata) VALUES (?, ?, ?, ?::jsonb) ON CONFLICT (name) DO UPDATE SET label = EXCLUDED.label, value = EXCLUDED.value, metadata = EXCLUDED.metadata RETURNING id";

    // --- SELECT base ---
    private static final String SQL_SELECT_BASE = "SELECT time, value, string, unit FROM items WHERE item_id = ?";

    // --- DELETE ---
    private static final String SQL_DELETE_BASE = "DELETE FROM items WHERE item_id = ?";

    private TimescaleDBQuery() {
        // utility class
    }

    /**
     * Inserts a single item state row.
     *
     * @param connection The JDBC connection.
     * @param itemId The item_id from {@code item_meta}.
     * @param timestamp The measurement timestamp.
     * @param row The mapped state row.
     * @throws SQLException on any database error.
     */
    public static void insert(Connection connection, int itemId, ZonedDateTime timestamp, TimescaleDBMapper.Row row)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SQL_INSERT)) {
            ps.setTimestamp(1, Timestamp.from(timestamp.toInstant()));
            ps.setInt(2, itemId);
            Double value = row.value();
            if (value != null) {
                ps.setDouble(3, value);
            } else {
                ps.setNull(3, Types.DOUBLE);
            }
            ps.setString(4, row.string());
            ps.setString(5, row.unit());
            ps.executeUpdate();
        }
        LOGGER.debug("Stored item_id={} at {} value={} string={} unit={}", itemId, timestamp, row.value(), row.string(),
                row.unit());
    }

    /**
     * Returns the item_id for the given name, inserting or updating the {@code item_meta} row as needed.
     *
     * @param connection The JDBC connection.
     * @param name The item name.
     * @param label The item label (may be null).
     * @param value The user-defined value string from {@code metadata.getValue()} (may be null), stored in
     *            {@code item_meta.value}.
     * @param metadataJson The full config map serialized as JSON (may be null), stored in
     *            {@code item_meta.metadata} as JSONB.
     * @return The item_id.
     * @throws SQLException on any database error.
     */
    public static int getOrCreateItemId(Connection connection, String name, @Nullable String label,
            @Nullable String value, @Nullable String metadataJson) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SQL_UPSERT_ITEM_META)) {
            ps.setString(1, name);
            ps.setString(2, label);
            ps.setString(3, value);
            // JSONB parameter: use setObject with Types.OTHER so the driver passes it as-is
            // to the ?::jsonb cast in the SQL; setString would bind it as VARCHAR which PostgreSQL
            // accepts with the explicit cast, but setObject is the idiomatic approach for non-standard types.
            ps.setObject(4, metadataJson, Types.OTHER);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    LOGGER.debug("Registered/updated item '{}' with item_id={}", name, id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to get or create item_meta entry for item '" + name + "'");
    }

    /**
     * Convenience overload without value or metadata (both default to {@code null}).
     */
    public static int getOrCreateItemId(Connection connection, String name, @Nullable String label)
            throws SQLException {
        return getOrCreateItemId(connection, name, label, null, null);
    }

    /**
     * Queries historic items according to the given filter criteria.
     *
     * @param connection The JDBC connection.
     * @param item The openHAB item (used for state reconstruction).
     * @param itemId The item_id from {@code item_meta}.
     * @param filter The filter criteria.
     * @return An ordered list of matching {@link HistoricItem}s.
     * @throws SQLException on any database error.
     */
    public static List<HistoricItem> query(Connection connection, Item item, int itemId, FilterCriteria filter)
            throws SQLException {
        StringBuilder sql = new StringBuilder(SQL_SELECT_BASE);
        List<Object> params = new ArrayList<>();
        params.add(itemId);

        // Date range filters
        ZonedDateTime beginDate = filter.getBeginDate();
        if (beginDate != null) {
            sql.append(" AND time >= ?");
            params.add(Timestamp.from(beginDate.toInstant()));
        }
        ZonedDateTime endDate = filter.getEndDate();
        if (endDate != null) {
            sql.append(" AND time <= ?");
            params.add(Timestamp.from(endDate.toInstant()));
        }

        // Optional state filter on the value column (numeric states only)
        State filterState = filter.getState();
        if (filterState != null) {
            TimescaleDBMapper.Row filterRow = TimescaleDBMapper.toRow(filterState);
            String sqlOp = TimescaleDBMapper.toSqlOperator(filter.getOperator());
            Double filterValue = filterRow != null ? filterRow.value() : null;
            if (filterValue != null && sqlOp != null) {
                sql.append(" AND value ").append(sqlOp).append(" ?");
                params.add(filterValue);
            } else {
                LOGGER.debug("State filter on non-numeric or unsupported state/operator — ignoring");
            }
        }

        // ORDER BY
        String direction = filter.getOrdering() == Ordering.ASCENDING ? "ASC" : "DESC";
        sql.append(" ORDER BY time ").append(direction);

        // Pagination
        if (filter.getPageSize() > 0) {
            sql.append(" LIMIT ?");
            params.add(filter.getPageSize());
            if (filter.getPageNumber() > 0) {
                sql.append(" OFFSET ?");
                params.add((long) filter.getPageNumber() * filter.getPageSize());
            }
        }

        LOGGER.debug("Query SQL: {} params={}", sql, params);

        List<HistoricItem> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp time = rs.getTimestamp(1);
                    Double value = (Double) rs.getObject(2);
                    String string = rs.getString(3);
                    String unit = rs.getString(4);

                    State state = TimescaleDBMapper.toState(item, value, string, unit);
                    results.add(new TimescaleDBHistoricItem(item.getName(), state, time.toInstant()));
                }
            }
        }
        LOGGER.debug("Query returned {} items for item_id={}", results.size(), itemId);
        return results;
    }

    /**
     * Deletes rows matching the filter criteria.
     *
     * @param connection The JDBC connection.
     * @param itemId The item_id from {@code item_meta}.
     * @param filter The filter criteria (only date range is evaluated).
     * @return The number of deleted rows.
     * @throws SQLException on any database error.
     */
    public static int remove(Connection connection, int itemId, FilterCriteria filter) throws SQLException {
        StringBuilder sql = new StringBuilder(SQL_DELETE_BASE);
        List<Object> params = new ArrayList<>();
        params.add(itemId);

        ZonedDateTime beginDate = filter.getBeginDate();
        if (beginDate != null) {
            sql.append(" AND time >= ?");
            params.add(Timestamp.from(beginDate.toInstant()));
        }
        ZonedDateTime endDate = filter.getEndDate();
        if (endDate != null) {
            sql.append(" AND time <= ?");
            params.add(Timestamp.from(endDate.toInstant()));
        }

        LOGGER.debug("Remove SQL: {} params={}", sql, params);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            int deleted = ps.executeUpdate();
            LOGGER.debug("Deleted {} rows for item_id={}", deleted, itemId);
            return deleted;
        }
    }

    /**
     * Looks up the item_id for the given name from {@code item_meta}, without creating a new entry.
     *
     * <p>
     * Used by the downsampling job to resolve item names that exist in the DB but are not yet in
     * the in-memory cache (e.g. after a bundle restart where no {@code store()} call has been made).
     *
     * @param connection The JDBC connection.
     * @param name The item name.
     * @return An {@link java.util.Optional} containing the item_id, or empty if not found.
     * @throws SQLException on any database error.
     */
    public static java.util.Optional<Integer> findItemId(Connection connection, String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ITEM_ID)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return java.util.Optional.of(rs.getInt(1));
                }
            }
        }
        return java.util.Optional.empty();
    }
}
