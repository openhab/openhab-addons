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
package org.openhab.binding.roborock.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper methods for adaptive polling and status refresh triggers.
 */
@NonNullByDefault
public final class VacuumRefreshPolicy {

    private VacuumRefreshPolicy() {
    }

    public static boolean shouldRequestImmediateStatus(@Nullable Integer previousStateId, int newStateId) {
        return previousStateId != null && previousStateId.intValue() != newStateId;
    }

    public static @Nullable Integer getStatusPollDelaySeconds(RoborockCommunicationMode communicationMode,
            boolean vacuumOn, int fastRefreshIntervalSeconds) {
        return communicationMode == RoborockCommunicationMode.DIRECT && vacuumOn ? fastRefreshIntervalSeconds : null;
    }

    public static int normalizeLegacyRefreshMinutesToSeconds(int configuredMinutes, int defaultMinutes,
            int minimumSeconds) {
        int effectiveMinutes = configuredMinutes > 0 ? configuredMinutes : defaultMinutes;
        int effectiveSeconds = effectiveMinutes * 60;
        return Math.max(effectiveSeconds, minimumSeconds);
    }

    public static int normalizeIntervalSeconds(int configuredSeconds, int defaultSeconds, int minimumSeconds) {
        int effectiveInterval = configuredSeconds > 0 ? configuredSeconds : defaultSeconds;
        return Math.max(effectiveInterval, minimumSeconds);
    }

    public static boolean isRefreshDue(long now, long lastPollTimestamp, int refreshIntervalSeconds) {
        long secondsSinceLastPoll = (now - lastPollTimestamp) / 1000;
        return lastPollTimestamp == 0 || secondsSinceLastPoll >= refreshIntervalSeconds;
    }
}
