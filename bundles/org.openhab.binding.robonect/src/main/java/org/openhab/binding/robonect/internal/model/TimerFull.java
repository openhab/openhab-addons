/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
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

    public @Nullable Integer getId() {
        return id;
    }

    public @Nullable Boolean getEnabled() {
        return enabled;
    }

    public @Nullable String getStart() {
        return start;
    }

    public @Nullable String getEnd() {
        return end;
    }

    public @Nullable Weekdays getWeekdays() {
        return weekdays;
    }
}
