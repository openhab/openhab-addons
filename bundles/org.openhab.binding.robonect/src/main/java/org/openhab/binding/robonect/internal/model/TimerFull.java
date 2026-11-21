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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Complete timer configuration returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class TimerFull {
    private @Nullable Integer id;
    private @Nullable Boolean enabled;
    private @Nullable String start, end;
    private @Nullable Weekdays weekdays;

    /**
     * Returns the timer identifier.
     *
     * @return the timer identifier, or {@code null} if not reported
     */
    public @Nullable Integer getId() {
        return id;
    }

    /**
     * Reports whether the timer is enabled.
     *
     * @return whether the timer is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getEnabled() {
        return enabled;
    }

    /**
     * Returns the configured start time in {@code HH:mm} format.
     *
     * @return the configured start time in {@code HH:mm} format, or {@code null} if not reported
     */
    public @Nullable String getStart() {
        return start;
    }

    /**
     * Returns the configured end time in {@code HH:mm} format.
     *
     * @return the configured end time in {@code HH:mm} format, or {@code null} if not reported
     */
    public @Nullable String getEnd() {
        return end;
    }

    /**
     * Returns the configured weekdays.
     *
     * @return the configured weekdays, or {@code null} if not reported
     */
    public @Nullable Weekdays getWeekdays() {
        return weekdays;
    }
}
