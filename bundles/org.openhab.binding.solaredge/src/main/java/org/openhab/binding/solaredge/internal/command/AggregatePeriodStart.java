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

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;

/**
 * Calculates local calendar boundaries with the offset valid on the boundary date.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
final class AggregatePeriodStart {
    private AggregatePeriodStart() {
    }

    static OffsetDateTime of(OffsetDateTime now, ZoneId zone, AggregatePeriod period) {
        var date = now.atZoneSameInstant(zone).toLocalDate();
        var startDate = switch (period) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> date.with(TemporalAdjusters.firstDayOfMonth());
            case YEAR -> date.with(TemporalAdjusters.firstDayOfYear());
        };
        return startDate.atStartOfDay(zone).toOffsetDateTime();
    }
}
