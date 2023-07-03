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

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.retry.RetryStrategy;

/**
 * This implements a {@link RetryStrategy} for a fixed time.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class FixedTime implements RetryStrategy {

    private final Clock clock;

    private LocalTime localTime;
    private double jitter = 0.0;

    public FixedTime(LocalTime localTime, Clock clock) {
        this.localTime = localTime;
        this.clock = clock;
    }

    @Override
    public Duration getDuration() {
        LocalTime now = LocalTime.now(clock);
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(clock), localTime);
        if (now.isAfter(localTime)) {
            localDateTime = localDateTime.plusDays(1);
        }

        Duration base = Duration.between(LocalDateTime.now(clock), localDateTime);
        if (jitter == 0.0) {
            return base;
        }

        long duration = base.toMillis();
        double rand = Math.random();
        duration += (long) (rand * jitter * 1000 * 60);

        return Duration.ofMillis(duration);
    }

    public FixedTime withJitter(double jitter) {
        this.jitter = jitter;
        return this;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof FixedTime)) {
            return false;
        }
        FixedTime other = (FixedTime) o;

        return this.jitter == other.jitter && this.localTime.equals(other.localTime);
    }

    @Override
    public final int hashCode() {
        final int result = 1;
        return result;
    }
}
