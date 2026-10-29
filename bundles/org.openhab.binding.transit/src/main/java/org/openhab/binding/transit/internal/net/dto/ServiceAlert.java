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
package org.openhab.binding.transit.internal.net.dto;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ServiceAlert} is a service alert as returned by the Transit API (schema {@code ServiceAlert}), used by
 * {@code route_details}, {@code stop_departures} and {@code alerts_for_networks}.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class ServiceAlert {

    private @Nullable String effect;
    private @Nullable String cause;
    private @Nullable String severity;
    private @Nullable String title;
    private @Nullable String description;

    @SerializedName("created_at")
    private @Nullable Long createdAt;

    @SerializedName("active_periods")
    private @Nullable List<ActivePeriod> activePeriods;

    public static class ActivePeriod {
        private @Nullable Long start;
        private @Nullable Long end;

        public @Nullable Long getStart() {
            return start;
        }

        public @Nullable Long getEnd() {
            return end;
        }
    }

    public @Nullable String getEffect() {
        return effect;
    }

    public @Nullable String getCause() {
        return cause;
    }

    public @Nullable String getSeverity() {
        return severity;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public @Nullable Long getCreatedAt() {
        return createdAt;
    }

    public List<ActivePeriod> getActivePeriods() {
        List<ActivePeriod> list = activePeriods;
        return list != null ? list : Collections.emptyList();
    }

    /**
     * An alert is active if it has no active periods or if one of its periods contains the given time. A period
     * without start or end is unbounded on that side.
     */
    public boolean isActive(long nowSeconds) {
        List<ActivePeriod> periods = getActivePeriods();
        if (periods.isEmpty()) {
            return true;
        }
        for (ActivePeriod period : periods) {
            Long start = period.getStart();
            Long end = period.getEnd();
            if ((start == null || start <= nowSeconds) && (end == null || end >= nowSeconds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rank of the severity values defined by the API: Unknown, Info, Warning, Severe.
     */
    public int getSeverityRank() {
        String value = severity;
        if (value == null) {
            return 0;
        }
        return switch (value) {
            case "Info" -> 1;
            case "Warning" -> 2;
            case "Severe" -> 3;
            default -> 0;
        };
    }

    /**
     * Returns the active alerts of the given list.
     */
    public static List<ServiceAlert> getActiveAlerts(@Nullable List<ServiceAlert> alerts, long nowSeconds) {
        if (alerts == null) {
            return Collections.emptyList();
        }
        return alerts.stream().filter(alert -> alert.isActive(nowSeconds)).toList();
    }

    /**
     * Returns the most severe of the given alerts; if several alerts have the same severity, the newest one wins.
     */
    public static @Nullable ServiceAlert getMostSevere(List<ServiceAlert> alerts) {
        return alerts.stream().max(Comparator.comparingInt(ServiceAlert::getSeverityRank).thenComparingLong(alert -> {
            Long created = alert.getCreatedAt();
            return created != null ? created : 0L;
        })).orElse(null);
    }
}
