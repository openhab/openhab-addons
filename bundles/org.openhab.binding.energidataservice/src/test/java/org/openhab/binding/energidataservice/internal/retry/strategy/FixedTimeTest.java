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
package org.openhab.binding.energidataservice.internal.retry.strategy;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.energidataservice.internal.retry.RetryStrategy;

/**
 * Tests for {@link FixedTime}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class FixedTimeTest {

    @Test
    void beforeNoon() {
        RetryStrategy retryPolicy = new FixedTime(LocalTime.of(12, 0),
                Clock.fixed(Instant.parse("2023-01-24T10:00:00Z"), ZoneId.of("UTC")));
        assertThat(retryPolicy.getDuration(), is(Duration.ofHours(2)));
    }

    @Test
    void atNoon() {
        RetryStrategy retryPolicy = new FixedTime(LocalTime.of(12, 0),
                Clock.fixed(Instant.parse("2023-01-24T12:00:00Z"), ZoneId.of("UTC")));
        assertThat(retryPolicy.getDuration(), is(Duration.ZERO));
    }

    @Test
    void afterNoon() {
        RetryStrategy retryPolicy = new FixedTime(LocalTime.of(12, 0),
                Clock.fixed(Instant.parse("2023-01-24T13:00:00Z"), ZoneId.of("UTC")));
        assertThat(retryPolicy.getDuration(), is(Duration.ofHours(23)));
    }
}
