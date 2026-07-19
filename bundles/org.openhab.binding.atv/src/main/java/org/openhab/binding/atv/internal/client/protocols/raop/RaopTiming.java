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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.time.Clock;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Methods for working with time and synchronization in RAOP.
 *
 * <p>
 * NTP timestamps are 64-bit unsigned values ({@code seconds << 32 | fraction}) carried
 * in Java {@code long}s with unsigned semantics: values may be "negative" when interpreted
 * as signed but all arithmetic here uses logical shifts to keep the bit pattern correct.
 * The NTP epoch offset relative to the Unix epoch is {@code 0x83AA7E80} (2208988800
 * seconds).
 *
 * <p>
 * Fractional divisions intentionally go through {@code double} to match the required
 * true-division rounding behavior.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopTiming {

    /** Seconds between the NTP epoch (1900) and the Unix epoch (1970). */
    public static final long NTP_EPOCH_OFFSET = 0x83AA7E80L;

    private RaopTiming() {
    }

    /**
     * Returns the current time of the given clock in NTP format.
     *
     * <p>
     * The clock is injectable for testability; pass {@link Clock#systemUTC()} in
     * production code.
     */
    public static long ntpNow(Clock clock) {
        Instant now = clock.instant();
        long nanos = now.getEpochSecond() * 1_000_000_000L + now.getNano();
        return ntpFromNanos(nanos);
    }

    /**
     * Converts a Unix timestamp in nanoseconds to NTP format.
     *
     * <p>
     * The nanosecond value is factored out as a parameter (rather than sampled here)
     * to keep this method deterministic for tests.
     */
    public static long ntpFromNanos(long nanos) {
        double nowUs = nanos / 1000.0;
        long seconds = (long) (nowUs / 1_000_000.0);
        long frac = (long) (nowUs - seconds * 1_000_000.0);
        return (seconds + NTP_EPOCH_OFFSET) << 32 | (long) ((double) (frac << 32) / 1_000_000.0);
    }

    /**
     * Splits NTP time into seconds and fraction, returned as {@code [seconds, fraction]}.
     */
    public static long[] ntp2parts(long ntp) {
        return new long[] { ntp >>> 32, ntp & 0xFFFFFFFFL };
    }

    /**
     * Converts NTP time into an audio timestamp at the given sample rate.
     */
    public static long ntp2ts(long ntp, int rate) {
        return ((ntp >>> 16) * rate) >>> 16;
    }

    /**
     * Converts an audio timestamp at the given sample rate into NTP time.
     */
    public static long ts2ntp(long timestamp, int rate) {
        // timestamp << 16 stays exact as a double for all realistic timestamps
        // (fits in 53 bits after the multiply by 2^16 of values < 2^37).
        return ((long) ((double) timestamp * 65536.0 / rate)) << 16;
    }

    /**
     * Converts NTP time to milliseconds.
     */
    public static long ntp2ms(long ntp) {
        return ((ntp >>> 10) * 1000) >>> 22;
    }

    /**
     * Converts an audio timestamp at the given sample rate to milliseconds.
     */
    public static long ts2ms(long timestamp, int rate) {
        return ntp2ms(ts2ntp(timestamp, rate));
    }
}
