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
package org.openhab.binding.robonect.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response to an operating statistics request.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class HourInfo extends RobonectAnswer {
    private @Nullable HourGeneral general;
    private @Nullable List<DurationEntry> seek;
    private @Nullable List<DurationEntry> mowing;

    /**
     * Returns the aggregate operating statistics.
     *
     * @return the aggregate operating statistics, or {@code null} if absent
     */
    public @Nullable HourGeneral getGeneral() {
        return general;
    }

    /**
     * Returns the recent searching durations.
     *
     * @return the recent searching durations, or {@code null} if absent
     */
    public @Nullable List<DurationEntry> getSeek() {
        return seek;
    }

    /**
     * Returns the recent mowing durations.
     *
     * @return the recent mowing durations, or {@code null} if absent
     */
    public @Nullable List<DurationEntry> getMowing() {
        return mowing;
    }

    public static class DurationEntry {
        private @Nullable Integer duration;

        /**
         * Returns the duration of the recorded activity.
         *
         * @return the duration of the recorded activity, or {@code null} if absent
         */
        public @Nullable Integer getDuration() {
            return duration;
        }
    }
}
