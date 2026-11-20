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

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DateTimeParser}.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
class DateTimeParserTest {

    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    @Test
    void parsesTheFormatsOfTheActions() {
        ZonedDateTime midnight = ZonedDateTime.of(2026, 11, 20, 0, 0, 0, 0, BERLIN);
        assertEquals(midnight, DateTimeParser.parse("2026-11-20", BERLIN));
        assertEquals(midnight.withHour(23).withMinute(59).withSecond(59),
                DateTimeParser.parse("2026-11-20 23:59:59", BERLIN));
        assertEquals(midnight.withHour(6), DateTimeParser.parse("2026-11-20T06:00", BERLIN));
        assertEquals(midnight.withHour(1), DateTimeParser.parse("2026-11-20T00:00:00Z", BERLIN));
        assertEquals(midnight, DateTimeParser.parse("2026-11-20T00:00+01:00[Europe/Berlin]", BERLIN));
        assertThrows(IllegalArgumentException.class, () -> DateTimeParser.parse("yesterday", BERLIN));
    }
}
