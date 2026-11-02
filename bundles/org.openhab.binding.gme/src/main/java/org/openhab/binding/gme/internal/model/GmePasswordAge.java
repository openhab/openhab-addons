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

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Calculates the age and expiry state of a GME API password.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public final class GmePasswordAge {

    public static final Period WARNING_AGE = Period.ofMonths(5);
    public static final Period EXPIRY_AGE = Period.ofMonths(6);

    public enum Status {
        OK,
        CHANGE_SOON,
        EXPIRED
    }

    private final ZoneId zoneId;
    private final Clock clock;

    public GmePasswordAge(ZoneId zoneId, Clock clock) {
        this.zoneId = zoneId;
        this.clock = clock;
    }

    public ZonedDateTime getExpiry(Instant changedAt) {
        return changedAt.atZone(zoneId).plus(EXPIRY_AGE);
    }

    public long getDaysRemaining(Instant changedAt) {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(zoneId);
        ZonedDateTime expiry = getExpiry(changedAt);

        return Math.max(0, ChronoUnit.DAYS.between(now.toLocalDate(), expiry.toLocalDate()));
    }

    public Status getStatus(Instant changedAt) {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(zoneId);
        ZonedDateTime changed = changedAt.atZone(zoneId);

        if (!now.isBefore(changed.plus(EXPIRY_AGE))) {
            return Status.EXPIRED;
        }

        if (!now.isBefore(changed.plus(WARNING_AGE))) {
            return Status.CHANGE_SOON;
        }

        return Status.OK;
    }
}
