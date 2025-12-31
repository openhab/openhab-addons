/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.util;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exponential backoff helper.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class Backoff {
    private final long jitterMs;
    private final int maxAttempts;

    private Duration nextDelay;
    private int attempts;

    public Backoff(final Duration initialDelay, final Duration jitter, final int maxAttempts) {
        this.nextDelay = initialDelay;
        this.jitterMs = jitter.toMillis();
        this.maxAttempts = maxAttempts;
    }

    public boolean hasMoreAttempts() {
        return attempts < maxAttempts;
    }

    public Duration nextDelay() {
        attempts += 1;
        final long randomJitter = ThreadLocalRandom.current().nextLong(jitterMs);
        nextDelay = nextDelay.multipliedBy(2).plusMillis(randomJitter);
        return nextDelay;
    }
}
