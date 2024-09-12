/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.retry.RetryStrategy;

/**
 * This implements a {@link RetryStrategy} for exponential backoff with jitter.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ExponentialBackoff implements RetryStrategy {

    private int attempts = 0;
    private int factor = 2;
    private double jitter = 0.0;
    private Duration minimum = Duration.ofMillis(100);
    private Duration maximum = Duration.ofHours(6);

    public ExponentialBackoff() {
    }

    @Override
    public Duration getDuration() {
        long minimum = this.minimum.toMillis();
        long maximum = this.maximum.toMillis();
        long duration = minimum * (long) Math.pow(this.factor, this.attempts++);
        if (jitter != 0.0) {
            double rand = Math.random();
            if ((((int) Math.floor(rand * 10)) & 1) == 0) {
                duration += (long) (rand * jitter * duration);
            } else {
                duration -= (long) (rand * jitter * duration);
            }
        }
        if (duration < minimum) {
            duration = minimum;
        }
        if (duration > maximum) {
            duration = maximum;
        }
        return Duration.ofMillis(duration);
    }

    public ExponentialBackoff withFactor(int factor) {
        this.factor = factor;
        return this;
    }

    public ExponentialBackoff withJitter(double jitter) {
        this.jitter = jitter;
        return this;
    }

    public ExponentialBackoff withMinimum(Duration minimum) {
        this.minimum = minimum;
        return this;
    }

    public ExponentialBackoff withMaximum(Duration maximum) {
        this.maximum = maximum;
        return this;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ExponentialBackoff)) {
            return false;
        }
        ExponentialBackoff other = (ExponentialBackoff) o;

        return this.factor == other.factor && this.jitter == other.jitter && this.minimum.equals(other.minimum)
                && this.maximum.equals(other.maximum);
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + factor;
        result = prime * result + (int) jitter * 100;
        result = prime * result + (int) minimum.toMillis();
        result = prime * result + (int) maximum.toMillis();

        return result;
    }
}
