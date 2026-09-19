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
package org.openhab.binding.solaredge.internal.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;

/**
 * Tests calendar boundaries across daylight saving time changes.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class AggregatePeriodStartTest {
    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    @Test
    public void recalculatesOffsetForMonthlyAndYearlyBoundaries() {
        OffsetDateTime now = OffsetDateTime.parse("2026-10-28T12:00:00+01:00");

        assertEquals(OffsetDateTime.parse("2026-10-01T00:00:00+02:00"),
                AggregatePeriodStart.of(now, BERLIN, AggregatePeriod.MONTH));
        assertEquals(OffsetDateTime.parse("2026-01-01T00:00:00+01:00"),
                AggregatePeriodStart.of(now, BERLIN, AggregatePeriod.YEAR));
    }

    @Test
    public void recalculatesOffsetForWeeklyBoundary() {
        OffsetDateTime now = OffsetDateTime.parse("2026-10-25T12:00:00+01:00");

        assertEquals(OffsetDateTime.parse("2026-10-19T00:00:00+02:00"),
                AggregatePeriodStart.of(now, BERLIN, AggregatePeriod.WEEK));
    }
}
