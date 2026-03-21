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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Per-item configuration parsed from item metadata (namespace {@code timescaledb}).
 *
 * <p>
 * A config is either a full downsampling config (function + interval + retention) or a
 * retention-only config (function and sqlInterval are {@code null}). Use {@link #hasDownsampling()}
 * to distinguish the two cases.
 *
 * @param function Aggregation function (AVG, MAX, MIN, SUM), or {@code null} for retention-only.
 * @param sqlInterval Validated SQL interval literal, e.g. {@code "1 hour"}, or {@code null} for retention-only.
 * @param retainRawDays Keep raw data for N days before aggregating. Ignored for retention-only configs.
 * @param retentionDays Drop all data older than N days. 0 = disabled.
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
public record DownsampleConfig(@Nullable AggregationFunction function, @Nullable String sqlInterval, int retainRawDays,
        int retentionDays) {

    /** Allowlist mapping from metadata interval strings to SQL interval literals. */
    public static final java.util.Map<String, String> INTERVAL_MAP = java.util.Map.of("1m", "1 minute", "5m",
            "5 minutes", "15m", "15 minutes", "30m", "30 minutes", "1h", "1 hour", "2h", "2 hours", "6h", "6 hours",
            "12h", "12 hours", "1d", "1 day");

    /**
     * Converts a metadata interval string to its SQL literal.
     *
     * @param interval The metadata interval string, e.g. {@code "1h"}.
     * @return The SQL interval literal.
     * @throws IllegalArgumentException if the interval is not in the allowlist.
     */
    public static String toSqlInterval(String interval) {
        String sql = INTERVAL_MAP.get(interval);
        if (sql == null) {
            throw new IllegalArgumentException(
                    "Invalid downsampleInterval '" + interval + "'. Allowed: " + INTERVAL_MAP.keySet());
        }
        return sql;
    }

    /**
     * Returns {@code true} if this config describes a full downsampling run (aggregation + raw-data pruning).
     * Returns {@code false} for retention-only configs where only the retention DELETE is executed.
     */
    public boolean hasDownsampling() {
        return function != null;
    }

    /**
     * Creates a retention-only config: no aggregation, just a periodic DELETE of rows older than
     * {@code retentionDays} days.
     *
     * @param retentionDays Days after which all rows are deleted. Must be &gt; 0.
     */
    public static DownsampleConfig retentionOnly(int retentionDays) {
        if (retentionDays <= 0) {
            throw new IllegalArgumentException("retentionDays must be > 0, got " + retentionDays);
        }
        return new DownsampleConfig(null, null, 0, retentionDays);
    }
}
