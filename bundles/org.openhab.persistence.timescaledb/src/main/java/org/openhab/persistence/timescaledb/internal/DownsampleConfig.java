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

/**
 * Per-item downsampling configuration parsed from item metadata (namespace {@code timescaledb}).
 *
 * @param function Aggregation function (AVG, MAX, MIN, SUM).
 * @param sqlInterval Validated SQL interval literal, e.g. {@code "1 hour"}.
 * @param retainRawDays Keep raw data for N days before aggregating. Default: 5.
 * @param retentionDays Drop all data older than N days. 0 = disabled.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public record DownsampleConfig(AggregationFunction function, String sqlInterval, int retainRawDays, int retentionDays) {

    /** Allowlist mapping from metadata interval strings to SQL interval literals. */
    public static final java.util.Map<String, String> INTERVAL_MAP = java.util.Map.of("1m", "1 minute", "5m",
            "5 minutes", "15m", "15 minutes", "30m", "30 minutes", "1h", "1 hour", "6h", "6 hours", "1d", "1 day");

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
}
