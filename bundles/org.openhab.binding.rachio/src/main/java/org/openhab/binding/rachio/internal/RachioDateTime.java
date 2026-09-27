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
package org.openhab.binding.rachio.internal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;

/**
 * Converts the timestamp formats returned by the Rachio APIs to openHAB date-time values.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public final class RachioDateTime {
    private static final long EPOCH_SECONDS_THRESHOLD = 10_000_000_000L;

    private RachioDateTime() {
    }

    public static @Nullable DateTimeType parse(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String normalizedValue = value.trim();
        if (normalizedValue.isBlank()) {
            return null;
        }

        try {
            if (isIntegerValue(normalizedValue)) {
                return new DateTimeType(parseEpoch(normalizedValue));
            }
            return new DateTimeType(normalizedValue);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static @Nullable Instant parseInstant(@Nullable String value, ZoneId zoneId) {
        if (value == null) {
            return null;
        }
        String normalizedValue = value.trim();
        if (normalizedValue.isBlank()) {
            return null;
        }

        try {
            if (isIntegerValue(normalizedValue)) {
                return parseEpoch(normalizedValue);
            }
            if (normalizedValue.length() == 10) {
                return LocalDate.parse(normalizedValue).atStartOfDay(zoneId).toInstant();
            }
            return new DateTimeType(normalizedValue).getInstant();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Instant parseEpoch(String value) {
        long epoch = Long.parseLong(value);
        long epochMillis = Math.abs(epoch) < EPOCH_SECONDS_THRESHOLD ? Math.multiplyExact(epoch, 1000L) : epoch;
        return Instant.ofEpochMilli(epochMillis);
    }

    public static boolean isIntegerValue(String value) {
        int length = value.length();
        int start = length > 0 && (value.charAt(0) == '+' || value.charAt(0) == '-') ? 1 : 0;
        if (start == length) {
            return false;
        }
        for (int i = start; i < length; i++) {
            char ch = value.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }
}
