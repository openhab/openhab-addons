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
package org.openhab.binding.bydbatterybox.internal.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Minimum, maximum, average and spread of a range of values, with the 1-based positions of the extremes. The first
 * occurrence wins if several values are equal.
 * <p>
 * Computed from the value arrays rather than taken from the min/max summary the battery reports, because that summary
 * was observed to come from a different sample than the arrays.
 *
 * @author Michael Weger - Initial contribution
 *
 * @param minPosition 1-based position of the smallest value within the whole list
 * @param maxPosition 1-based position of the largest value within the whole list
 * @param average average, rounded to one decimal
 */
@NonNullByDefault
public record CellStatistics(int min, int minPosition, int max, int maxPosition, long sum, BigDecimal average) {

    public int spread() {
        return max - min;
    }

    /**
     * Computes the statistics of {@code values[from, to)}; positions refer to the whole list.
     *
     * @return {@code null} if the range is empty
     */
    public static @Nullable CellStatistics of(List<Integer> values, int from, int to) {
        int end = Math.min(to, values.size());
        if (from >= end) {
            return null;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int minPosition = 0;
        int maxPosition = 0;
        long sum = 0;
        for (int i = from; i < end; i++) {
            int value = values.get(i);
            sum += value;
            if (value < min) {
                min = value;
                minPosition = i + 1;
            }
            if (value > max) {
                max = value;
                maxPosition = i + 1;
            }
        }
        BigDecimal average = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(end - from), 1, RoundingMode.HALF_UP);
        return new CellStatistics(min, minPosition, max, maxPosition, sum, average);
    }
}
