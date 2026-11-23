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
package org.openhab.binding.tibber.internal.pulse.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;
import static org.openhab.binding.tibber.internal.pulse.handler.HandlerSupport.*;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests the staleness rules of the {@code thing-lifecycle} and {@code channels} specs without any scheduler: the
 * state takes the time as an argument.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class MeterStateTest {

    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
    private static final long STALE = 60 * SECOND;
    private static final Instant RECEIVED = Instant.parse("2026-01-01T12:00:00Z");

    @Test
    void whenNothingWasReportedThenNoValueExists() {
        MeterState state = new MeterState();

        assertNull(state.value(CHANNEL_POWER));
        assertNull(state.lastUpdate());
        assertTrue(state.nextExpiry(STALE).isEmpty());
    }

    @Test
    void whenAReadingIsAppliedThenItsValuesAndTimeAreKept() {
        MeterState state = new MeterState();

        state.apply(values(CHANNEL_POWER, watts("1523")), RECEIVED, 0);

        assertEquals(watts("1523"), state.value(CHANNEL_POWER));
        assertEquals(RECEIVED, state.lastUpdate());
        assertNull(state.value(CHANNEL_FREQUENCY));
    }

    @Test
    void whenOneQuantityDisappearsThenOnlyItExpiresAfterTheStaleTimeout() {
        MeterState state = new MeterState();
        state.apply(values(CHANNEL_POWER, watts("100"), CHANNEL_FREQUENCY, hertz("49.9")), RECEIVED, 0);
        for (int second = 3; second <= 60; second += 3) {
            state.apply(values(CHANNEL_POWER, watts("100")), RECEIVED, second * SECOND);
        }

        MeterState.Expiry early = state.expire(59 * SECOND, STALE);
        MeterState.Expiry due = state.expire(60 * SECOND, STALE);

        assertTrue(early.isEmpty());
        assertEquals(java.util.List.of(CHANNEL_FREQUENCY), due.channels());
        assertFalse(due.meter());
        assertNull(state.value(CHANNEL_FREQUENCY));
        assertEquals(watts("100"), state.value(CHANNEL_POWER));
    }

    @Test
    void whenNoReadingArrivesForTheStaleTimeoutThenTheMeterExpiresOnceAndEnergyCountersStay() {
        MeterState state = new MeterState();
        state.apply(values(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("1234.5678")), RECEIVED,
                0);

        MeterState.Expiry first = state.expire(60 * SECOND, STALE);
        MeterState.Expiry second = state.expire(90 * SECOND, STALE);

        assertTrue(first.meter());
        assertEquals(java.util.List.of(CHANNEL_POWER), first.channels());
        assertTrue(second.isEmpty(), "each expiry is reported once");
        assertNull(state.value(CHANNEL_POWER));
        assertEquals(kilowattHours("1234.5678"), state.value(CHANNEL_ENERGY_IMPORT));
        assertEquals(RECEIVED, state.lastUpdate());
    }

    @Test
    void whenTheClockWasRestartedThenTheMeterExpiresWithoutAnyReading() {
        MeterState state = new MeterState();
        state.restartClock(10 * SECOND);

        assertTrue(state.expire(69 * SECOND, STALE).isEmpty());
        assertTrue(state.expire(70 * SECOND, STALE).meter());
    }

    @Test
    void whenTheClockIsStoppedThenNothingExpires() {
        MeterState state = new MeterState();
        state.restartClock(0);
        state.stopClock();

        assertTrue(state.expire(1_000 * SECOND, STALE).isEmpty());
        assertTrue(state.nextExpiry(STALE).isEmpty());
    }

    @Test
    void whenAReadingArrivesAfterExpiryThenTheMeterCanExpireAgain() {
        MeterState state = new MeterState();
        state.restartClock(0);
        assertTrue(state.expire(60 * SECOND, STALE).meter());

        state.apply(values(CHANNEL_POWER, watts("300")), RECEIVED, 70 * SECOND);

        assertTrue(state.expire(129 * SECOND, STALE).isEmpty());
        assertTrue(state.expire(130 * SECOND, STALE).meter());
    }

    @Test
    void whenValuesAreStoredThenTheNextExpiryIsTheEarliestOfChannelsAndMeter() {
        MeterState state = new MeterState();
        state.apply(values(CHANNEL_FREQUENCY, hertz("50")), RECEIVED, 5 * SECOND);
        state.apply(values(CHANNEL_POWER, watts("1")), RECEIVED, 20 * SECOND);

        assertEquals(65 * SECOND, state.nextExpiry(STALE).getAsLong());
        state.expire(65 * SECOND, STALE);
        assertEquals(80 * SECOND, state.nextExpiry(STALE).getAsLong());
    }

    @Test
    void whenOnlyEnergyCountersAreStoredThenTheyDoNotScheduleAnExpiry() {
        MeterState state = new MeterState();
        state.apply(values(CHANNEL_ENERGY_IMPORT, kilowattHours("1")), RECEIVED, 0);
        state.expire(60 * SECOND, STALE);

        assertTrue(state.nextExpiry(STALE).isEmpty());
        assertEquals(kilowattHours("1"), state.value(CHANNEL_ENERGY_IMPORT));
    }

    @Test
    void whenLiveValuesAreClearedThenCountersAndUpdateTimeStay() {
        MeterState state = new MeterState();
        state.apply(values(CHANNEL_POWER, watts("5"), CHANNEL_VOLTAGE_L1, volts("225.5"), CHANNEL_ENERGY_EXPORT,
                kilowattHours("2")), RECEIVED, 0);

        var cleared = state.clearLive();

        assertEquals(2, cleared.size());
        assertNull(state.value(CHANNEL_POWER));
        assertEquals(kilowattHours("2"), state.value(CHANNEL_ENERGY_EXPORT));
        assertEquals(RECEIVED, state.lastUpdate());
    }

    @Test
    void whenTheNanosecondClockWrapsThenTheComparisonsStillHold() {
        MeterState state = new MeterState();
        long nearMax = Long.MAX_VALUE - 10 * SECOND;
        state.apply(values(CHANNEL_POWER, watts("1")), RECEIVED, nearMax);

        assertTrue(state.expire(nearMax + 59 * SECOND, STALE).isEmpty());
        assertEquals(java.util.List.of(CHANNEL_POWER), state.expire(nearMax + 60 * SECOND, STALE).channels());
    }
}
