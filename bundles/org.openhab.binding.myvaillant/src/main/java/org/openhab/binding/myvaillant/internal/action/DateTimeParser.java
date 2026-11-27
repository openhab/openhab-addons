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
package org.openhab.binding.myvaillant.internal.action;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Parses the date arguments of the actions.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
final class DateTimeParser {

    private DateTimeParser() {
    }

    /**
     * Accepts {@code 2026-11-20}, {@code 2026-11-20 06:00:00} (the format of the Home Assistant export service),
     * {@code 2026-11-20T06:00} and ISO dates with offset or zone. Dates without offset are in the given zone.
     *
     * @throws IllegalArgumentException if the value cannot be parsed
     */
    static ZonedDateTime parse(String value, ZoneId zone) {
        String trimmed = value.trim().replace(' ', 'T');
        try {
            if (trimmed.length() == 10) {
                return LocalDate.parse(trimmed).atStartOfDay(zone);
            }
            try {
                return LocalDateTime.parse(trimmed).atZone(zone);
            } catch (DateTimeParseException e) {
                try {
                    return OffsetDateTime.parse(trimmed).atZoneSameInstant(zone);
                } catch (DateTimeParseException e2) {
                    return ZonedDateTime.parse(trimmed).withZoneSameInstant(zone);
                }
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date '" + value + "', use e.g. 2026-11-20 or 2026-11-20 06:00:00", e);
        }
    }
}
