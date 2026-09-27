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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests Rachio timestamp conversion.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class RachioDateTimeTest {
    @Test
    void parsesEpochSecondsWithoutApplyingLocalTimeZone() {
        assertThat(Objects.requireNonNull(RachioDateTime.parse("1710000000")).getInstant(),
                is(Instant.ofEpochSecond(1_710_000_000L)));
    }

    @Test
    void parsesEpochMillisecondsWithoutApplyingLocalTimeZone() {
        assertThat(Objects.requireNonNull(RachioDateTime.parse("1710000000123")).getInstant(),
                is(Instant.ofEpochMilli(1_710_000_000_123L)));
    }

    @Test
    void parsesSignedEpochAndIsoValues() {
        assertThat(Objects.requireNonNull(RachioDateTime.parse("-1")).getInstant(), is(Instant.ofEpochSecond(-1)));
        assertThat(Objects.requireNonNull(RachioDateTime.parse("2026-06-20T10:15:30Z")).getInstant(),
                is(Instant.parse("2026-06-20T10:15:30Z")));
    }

    @Test
    void rejectsBlankAndInvalidValues() {
        assertThat(RachioDateTime.parse("  "), is(nullValue()));
        assertThat(RachioDateTime.parse("not-a-timestamp"), is(nullValue()));
    }

    @Test
    void parsesDateOnlyValuesInTheExplicitTimeZone() {
        assertThat(RachioDateTime.parseInstant("2026-03-29", ZoneId.of("Europe/Budapest")),
                is(Instant.parse("2026-03-28T23:00:00Z")));
        assertThat(RachioDateTime.parseInstant("2026-03-29", ZoneId.of("America/Denver")),
                is(Instant.parse("2026-03-29T06:00:00Z")));
    }

    @Test
    void instantParserRejectsBlankAndInvalidValues() {
        assertThat(RachioDateTime.parseInstant(" ", ZoneOffset.UTC), is(nullValue()));
        assertThat(RachioDateTime.parseInstant("not-a-timestamp", ZoneOffset.UTC), is(nullValue()));
    }
}
