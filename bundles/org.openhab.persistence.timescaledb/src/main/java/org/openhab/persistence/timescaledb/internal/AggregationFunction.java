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
 * Aggregation function used for per-item downsampling.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public enum AggregationFunction {
    AVG,
    MAX,
    MIN,
    SUM;

    /** Returns the SQL aggregate function name, e.g. {@code AVG(value)}. */
    public String toSql() {
        return name();
    }
}
