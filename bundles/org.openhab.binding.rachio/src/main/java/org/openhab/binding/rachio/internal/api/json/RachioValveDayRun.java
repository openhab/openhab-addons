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

import static org.openhab.binding.rachio.internal.RachioUtils.firstNonBlank;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioDateTime;

/**
 * Smart Hose planned or completed valve run.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioValveDayRun {
    public String id = "";
    public String plannedRunId = "";
    public String programId = "";
    public String valveId = "";
    public @Nullable RachioResourceId resourceId;
    public String startTime = "";
    public String plannedRunStartTime = "";
    public String timestamp = "";
    public String date = "";
    public String status = "";
    public String runStatus = "";
    public String type = "";
    public @Nullable Boolean skipped;
    public int duration = 0;
    public int durationSeconds = 0;

    public String getValveId() {
        RachioResourceId resourceId = this.resourceId;
        return firstNonBlank(valveId, resourceId != null ? resourceId.valveId : "");
    }

    public String getProgramId() {
        RachioResourceId resourceId = this.resourceId;
        return firstNonBlank(programId, resourceId != null ? resourceId.programId : "");
    }

    public String getPlannedRunId() {
        return firstNonBlank(plannedRunId, id);
    }

    public String getStartTime() {
        return firstNonBlank(plannedRunStartTime, startTime, timestamp, date);
    }

    public int getDurationSeconds() {
        return durationSeconds > 0 ? durationSeconds : duration;
    }

    public String getStatus() {
        return firstNonBlank(status, runStatus, type);
    }

    public boolean isSkipped() {
        Boolean skipped = this.skipped;
        return skipped != null ? skipped.booleanValue() : getStatus().toUpperCase(Locale.ROOT).contains("SKIP");
    }

    public String getSkipOverrideDate() {
        String value = getStartTime();
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    public @Nullable Instant getStartInstant(ZoneId zoneId) {
        return RachioDateTime.parseInstant(getStartTime(), zoneId);
    }
}
