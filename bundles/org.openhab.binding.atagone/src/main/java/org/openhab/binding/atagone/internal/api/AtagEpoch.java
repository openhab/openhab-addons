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
package org.openhab.binding.atagone.internal.api;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Converts between ATAG ONE device time (seconds since 2000-01-01 00:00:00 UTC) and Java time types.
 * <p>
 * The device counts seconds from 2000-01-01 UTC, not the Unix epoch (1970-01-01). Getting this offset
 * wrong produces vacation dates that are off by 30 years — the exact bug in kozmoz/atag-one-api issue #16.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public final class AtagEpoch {

    /** Seconds between Unix epoch (1970-01-01 UTC) and ATAG epoch (2000-01-01 UTC). */
    public static final long OFFSET = 946684800L;

    private AtagEpoch() {
    }

    /**
     * Converts an ATAG epoch value to a {@link ZonedDateTime} in UTC.
     *
     * @param atagSeconds seconds since 2000-01-01 00:00:00 UTC
     * @return corresponding UTC {@link ZonedDateTime}
     */
    public static ZonedDateTime toZonedDateTime(long atagSeconds) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(atagSeconds + OFFSET), ZoneId.of("UTC"));
    }

    /**
     * Converts a {@link ZonedDateTime} to an ATAG epoch value.
     *
     * @param zdt the date/time to convert (any zone; normalised to UTC internally)
     * @return seconds since 2000-01-01 00:00:00 UTC
     */
    public static long fromZonedDateTime(ZonedDateTime zdt) {
        return zdt.toInstant().getEpochSecond() - OFFSET;
    }
}
