/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.miele.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TimeStabilizer}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class TimeStabilizerTest {

    private @NonNullByDefault({}) TimeStabilizer stabilizer;

    @BeforeEach
    public void initialize() {
        stabilizer = new TimeStabilizer();
    }

    @Test
    public void whenLongestPeriodIsFloorThenWeightedAverageIsLess() {
        assertThat(stabilizer.apply(getInstantOf("02:00:00"), getInstantOf("22:00:00")),
                is(equalTo(getInstantOf("02:00:00"))));
        assertThat(stabilizer.apply(getInstantOf("02:01:00"), getInstantOf("22:00:31")),
                is(equalTo(getInstantOf("02:00:00"))));
        assertThat(stabilizer.apply(getInstantOf("02:00:00"), getInstantOf("22:01:00")),
                is(equalTo(getInstantOf("02:00:29"))));
    }

    @Test
    public void whenLongestPeriodIsCeilThenWeightedAverageIsMore() {
        assertThat(stabilizer.apply(getInstantOf("02:00:00"), getInstantOf("22:00:00")),
                is(equalTo(getInstantOf("02:00:00"))));
        assertThat(stabilizer.apply(getInstantOf("02:01:00"), getInstantOf("22:00:29")),
                is(equalTo(getInstantOf("02:00:00"))));
        assertThat(stabilizer.apply(getInstantOf("02:00:00"), getInstantOf("22:01:00")),
                is(equalTo(getInstantOf("02:00:31"))));
    }

    @Test
    public void whenTooMuchFluctuationThenAverageIsDisregarded() {
        assertThat(stabilizer.apply(getInstantOf("02:00:00"), getInstantOf("22:00:00")),
                is(equalTo(getInstantOf("02:00:00"))));
        assertThat(stabilizer.apply(getInstantOf("02:03:00"), getInstantOf("22:03:00")),
                is(equalTo(getInstantOf("02:00:00"))));
        assertThat(stabilizer.apply(getInstantOf("02:04:00"), getInstantOf("22:03:00")),
                is(equalTo(getInstantOf("02:04:00"))));
    }

    @Test
    public void whenOutsideSlidingWindowThenValueIsDisregarded() {
        assertThat(stabilizer.apply(getInstantOf("02:00:00"), getInstantOf("22:00:00")),
                is(equalTo(getInstantOf("02:00:00"))));
        assertThat(stabilizer.apply(getInstantOf("02:01:00"), getInstantOf("22:10:00")),
                is(equalTo(getInstantOf("02:00:00"))));
        assertThat(stabilizer.apply(getInstantOf("02:02:00"), getInstantOf("22:15:00")),
                is(equalTo(getInstantOf("02:00:30"))));
        assertThat(stabilizer.apply(getInstantOf("02:02:00"), getInstantOf("22:15:01")),
                is(equalTo(getInstantOf("02:01:00"))));
    }

    private Instant getInstantOf(String time) {
        Clock clock = Clock.fixed(Instant.parse("2022-09-13T" + time + "Z"), ZoneId.of("UTC"));
        return Instant.now(clock);
    }
}
