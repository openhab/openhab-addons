/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.transform.scale.internal;

import java.math.BigDecimal;

/**
 * Range implementation using BigDecimals.
 *
 * @author Markus Rathgeb - Initial contribution
 */
public class Range {

    public static Range open(final BigDecimal lower, final BigDecimal upper) {
        return new Range(lower, false, upper, false);
    }

    public static Range closed(final BigDecimal lower, final BigDecimal upper) {
        return new Range(lower, true, upper, true);
    }

    public static Range openClosed(final BigDecimal lower, final BigDecimal upper) {
        return new Range(lower, false, upper, true);
    }

    public static Range closedOpen(final BigDecimal lower, final BigDecimal upper) {
        return new Range(lower, true, upper, false);
    }

    public static Range greaterThan(final BigDecimal lower) {
        return new Range(lower, false, null, false);
    }

    public static Range atLeast(final BigDecimal lower) {
        return new Range(lower, true, null, false);
    }

    public static Range lessThan(final BigDecimal upper) {
        return new Range(null, false, upper, false);
    }

    public static Range atMost(final BigDecimal upper) {
        return new Range(null, false, upper, true);
    }

    public static Range all() {
        return new Range(null, false, null, false);
    }

    public static Range range(final BigDecimal lower, final boolean lowerInclusive, final BigDecimal upper,
            final boolean upperInclusive) {
        return new Range(lower, lowerInclusive, upper, upperInclusive);
    }

    final BigDecimal min;
    final boolean minInclusive;
    final BigDecimal max;
    final boolean maxInclusive;

    private Range(final BigDecimal min, final boolean minInclusive, final BigDecimal max, final boolean maxInclusive) {
        this.min = min;
        this.minInclusive = minInclusive;
        this.max = max;
        this.maxInclusive = maxInclusive;
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean contains(final BigDecimal value) {
        final boolean minMatch;
        if (min == null) {
            minMatch = true;
        } else {
            int cmp = value.compareTo(min);
            if (minInclusive) {
                minMatch = cmp == 0 || cmp == 1;
            } else {
                minMatch = cmp == 1;
            }
        }

        if (!minMatch) {
            return false;
        }

        final boolean maxMatch;
        if (max == null) {
            maxMatch = true;
        } else {
            int cmp = value.compareTo(max);
            if (maxInclusive) {
                maxMatch = cmp == 0 || cmp == -1;
            } else {
                maxMatch = cmp == -1;
            }
        }

        if (!maxMatch) {
            return false;
        }

        return true;
    }
}
