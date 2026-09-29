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
package org.openhab.binding.rachio.internal.api.json;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Smart Hose day-view response and run selection helpers.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioValveDayViewsResponse {
    public List<RachioValveDayView> dayViews = new ArrayList<>();

    public static RachioValveDayViewsResponse fromJson(String json) {
        RachioValveDayViewsResponse response = new RachioValveDayViewsResponse();
        response.dayViews.addAll(RachioSmartHoseJsonParser.parseArray(json, RachioValveDayView.class, "dayViews",
                "valveDayViews", "days", "items", "data", "results"));
        return response;
    }

    public List<RachioValveDayRun> getRuns() {
        List<RachioValveDayRun> runs = new ArrayList<>();
        for (RachioValveDayView dayView : dayViews) {
            runs.addAll(dayView.getRuns());
        }
        return runs;
    }

    public @Nullable RachioValveDayRun findNextPlannedRun(Instant now, ZoneId zoneId) {
        return findRun(run -> true, now, zoneId, true);
    }

    public @Nullable RachioValveDayRun findNextSkippedRun(Instant now, ZoneId zoneId) {
        return findRun(RachioValveDayRun::isSkipped, now, zoneId, true);
    }

    public @Nullable RachioValveDayRun findLastCompletedRun(Instant now, ZoneId zoneId) {
        return findRun(run -> !run.isSkipped(), now, zoneId, false);
    }

    public @Nullable RachioValveDayRun findNextProgramRun(String programId, boolean skippedOnly, Instant now,
            ZoneId zoneId) {
        return findRun(run -> programId.equalsIgnoreCase(run.getProgramId()) && (!skippedOnly || run.isSkipped()), now,
                zoneId, true);
    }

    private @Nullable RachioValveDayRun findRun(Predicate<RachioValveDayRun> filter, Instant now, ZoneId zoneId,
            boolean next) {
        RachioValveDayRun selectedRun = null;
        Instant selectedStart = null;
        for (RachioValveDayRun run : getRuns()) {
            if (!filter.test(run)) {
                continue;
            }
            Instant start = run.getStartInstant(zoneId);
            if (start == null || (next ? start.isBefore(now) : start.isAfter(now))) {
                continue;
            }
            if (selectedStart == null || (next ? start.isBefore(selectedStart) : start.isAfter(selectedStart))) {
                selectedRun = run;
                selectedStart = start;
            }
        }
        return selectedRun;
    }
}
