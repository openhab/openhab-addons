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
 * Converts between ATAG ONE device time (seconds since 2000-01-01 UTC) and Java time types.
 * The device does not use the Unix epoch; wrong offset produces dates off by 30 years.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public final class AtagEpoch {

    // Seconds between Unix epoch (1970-01-01 UTC) and ATAG epoch (2000-01-01 UTC).
    public static final long OFFSET = 946684800L;

    private AtagEpoch() {
    }

    public static ZonedDateTime toZonedDateTime(long atagSeconds) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(atagSeconds + OFFSET), ZoneId.of("UTC"));
    }

    public static long fromZonedDateTime(ZonedDateTime zdt) {
        return zdt.toInstant().getEpochSecond() - OFFSET;
    }
}
