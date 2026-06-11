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
package org.openhab.binding.jellyfin.internal.util.tick;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Converts between Jellyfin ticks and other playback position units.
 *
 * <p>
 * Jellyfin uses a tick-based time format where {@value #TICKS_PER_SECOND} ticks equal one second,
 * which is compatible with the .NET {@code TimeSpan} tick resolution.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public final class TickConverter {

    /** Number of Jellyfin ticks per second ({@code 10,000,000}). */
    public static final long TICKS_PER_SECOND = 10_000_000L;

    private TickConverter() {
    }

    /**
     * Converts seconds to Jellyfin ticks.
     *
     * @param seconds the number of seconds
     * @return the equivalent number of ticks
     */
    public static long secondsToTicks(long seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    /**
     * Converts Jellyfin ticks to seconds (rounded to nearest).
     *
     * @param ticks the number of ticks
     * @return the equivalent number of seconds
     */
    public static long ticksToSeconds(long ticks) {
        return Math.round(ticks / (double) TICKS_PER_SECOND);
    }

    /**
     * Converts a playback percentage into a tick position within the given runtime.
     *
     * @param runTimeTicks the total runtime in ticks
     * @param percent the percentage value (0–100)
     * @return the tick position at that percentage
     */
    public static long percentToTicks(long runTimeTicks, int percent) {
        return Math.round((runTimeTicks * (double) percent) / 100.0);
    }

    /**
     * Converts a tick position to a playback percentage within the given runtime.
     * The result is clamped to the range [0, 100].
     *
     * @param ticks the current position in ticks
     * @param runTimeTicks the total runtime in ticks
     * @return the position as an integer percentage, clamped to [0, 100]
     */
    public static int ticksToPercent(long ticks, long runTimeTicks) {
        if (runTimeTicks <= 0) {
            return 0;
        }
        int percent = (int) Math.round(ticks * 100.0 / runTimeTicks);
        return Math.max(0, Math.min(100, percent));
    }
}
