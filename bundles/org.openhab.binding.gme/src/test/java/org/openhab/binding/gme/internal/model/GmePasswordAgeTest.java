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
package org.openhab.binding.gme.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class GmePasswordAgeTest {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");
    private static final Instant CHANGED_AT = Instant.parse("2026-01-15T10:00:00Z");

    @Test
    void returnsOkBeforeFiveMonths() {
        GmePasswordAge age = at("2026-06-14T10:00:00Z");

        assertEquals(GmePasswordAge.Status.OK, age.getStatus(CHANGED_AT));
    }

    @Test
    void returnsChangeSoonAtFiveMonths() {
        GmePasswordAge age = at("2026-06-15T10:00:00Z");

        assertEquals(GmePasswordAge.Status.CHANGE_SOON, age.getStatus(CHANGED_AT));
    }

    @Test
    void returnsExpiredAtSixMonths() {
        GmePasswordAge age = at("2026-07-15T10:00:00Z");

        assertEquals(GmePasswordAge.Status.EXPIRED, age.getStatus(CHANGED_AT));
    }

    @Test
    void calculatesExpiryAtSixMonths() {
        GmePasswordAge age = at("2026-02-01T00:00:00Z");

        assertEquals(CHANGED_AT.atZone(ROME).plusMonths(6), age.getExpiry(CHANGED_AT));
    }

    @Test
    void calculatesRemainingDays() {
        GmePasswordAge age = at("2026-07-10T10:00:00Z");

        assertEquals(5, age.getDaysRemaining(CHANGED_AT));
    }

    @Test
    void remainingDaysDoesNotBecomeNegativeAfterExpiry() {
        GmePasswordAge age = at("2026-08-15T10:00:00Z");

        assertEquals(0, age.getDaysRemaining(CHANGED_AT));
    }

    private static GmePasswordAge at(String instant) {
        return new GmePasswordAge(ROME, Clock.fixed(Instant.parse(instant), ROME));
    }
}
